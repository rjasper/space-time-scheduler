package scheduler;

import static jts.geom.immutable.ImmutableGeometries.*;
import static util.Comparables.*;
import static world.util.DynamicCollisionDetector.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import jts.geom.immutable.ImmutablePoint;
import scheduler.util.WorkerUnitObstacleBuilder;
import util.JoinedCollection;
import world.DynamicObstacle;
import world.SimpleTrajectory;
import world.SpatialPath;
import world.Trajectory;
import world.WorldPerspective;
import world.pathfinder.AbstractFixTimePathfinder;
import world.pathfinder.AbstractMinimumTimePathfinder;
import world.pathfinder.AbstractSpatialPathfinder;
import world.pathfinder.SimpleFixTimePathfinder;
import world.pathfinder.SimpleMinimumTimePathfinder;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

// TODO document
/**
 * <p>The TaskPlanner plans a new {@link Task} into an established set of tasks.
 * It requires multiple parameters which determine the {@link WorkerUnit worker}
 * to execute the new task, and the location, duration, and time interval of the
 * execution. It is also responsible for ensuring that the designated worker is
 * able to reach the task's location with colliding with any other object; be it
 * stationary or another worker.</p>
 *
 * <p>Should it be impossible to plan the new task then the TaskPlanner will
 * not change the current task set. This might be the case when the designated
 * worker is unable to reach the location without violating any time
 * constraints.</p>
 *
 * <p>The planning involves the calculation of a spatial path from the previous
 * location of the worker to the task's location and the successive path to
 * the next location the worker is required to be. The next step is to calculate
 * a velocity profile to evade dynamic obstacles.</p>
 *
 * @author Rico Jasper
 */
public class TaskPlanner {

	/**
	 * The id of the {@link Task task} to be planned.
	 */
	private UUID taskId = null;

	/**
	 * The current worker.
	 */
	private WorkerUnit worker = null;
	
	/**
	 * The location of the {@link Task task} to be planned.
	 */
	private ImmutablePoint location = null;

	/**
	 * The earliest start time of the {@link Task task} to be planned.
	 */
	private LocalDateTime earliestStartTime = null;

	/**
	 * The latest start time of the {@link Task task} to be planned.
	 */
	private LocalDateTime latestStartTime = null;

	/**
	 * The duration of the {@link Task task} to be planned.
	 */
	private Duration duration = null;
	
	/**
	 * The used idle slot to schedule the task.
	 */
	private IdleSlot idleSlot = null;
	
	/**
	 * The world perspective of obstacles as perceived by the {@link #worker}.
	 */
	private WorldPerspective worldPerspective = null;
	
	/**
	 * The current schedule of the scheduler.
	 */
	private Schedule schedule = null;
	
	/**
	 * The schedule alternative used to store schedule changes.
	 */
	private ScheduleAlternative alternative = null;
	
	/**
	 * The workers as dynamic obstacles.
	 */
	private transient Collection<DynamicObstacle> dynamicObstacles;
	
	/**
	 * Indicates if the final position of time slot is mandatory.
	 */
	private boolean fixedEnd = true;

	public void setTaskId(UUID taskId) {
		this.taskId = Objects.requireNonNull(taskId, "taskId");
	}

	public void setWorker(WorkerUnit workerUnit) {
		this.worker = Objects.requireNonNull(workerUnit, "workerUnit");
	}

	public void setLocation(Point location) {
		this.location = Objects.requireNonNull(immutable(location), "location");
	}

	public void setEarliestStartTime(LocalDateTime earliestStartTime) {
		this.earliestStartTime = Objects.requireNonNull(earliestStartTime, "earliestStartTime");
	}

	public void setLatestStartTime(LocalDateTime latestStartTime) {
		this.latestStartTime = Objects.requireNonNull(latestStartTime, "latestStartTime");
	}

	public void setDuration(Duration duration) {
		this.duration = Objects.requireNonNull(duration, "duration");
	}

	public void setIdleSlot(IdleSlot idleSlot) {
		this.idleSlot = Objects.requireNonNull(idleSlot, "idleSlot");
	}

	public void setWorldPerspective(WorldPerspective worldPerspective) {
		this.worldPerspective = Objects.requireNonNull(worldPerspective, "worldPerspective");
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = Objects.requireNonNull(schedule, "schedule");
	}

	public void setScheduleAlternative(ScheduleAlternative alternative) {
		this.alternative = Objects.requireNonNull(alternative, "alternative");
	}

	public void setFixedEnd(boolean fixedEnd) {
		this.fixedEnd = fixedEnd;
	}
	
	private LocalDateTime earliestStartTime() {
		return max(
			earliestStartTime,
			worker.getInitialTime(),
			idleSlot.getStartTime());
	}
	
	private LocalDateTime latestStartTime() {
		return min(
			latestStartTime,
			idleSlot.getFinishTime().minus(duration));
	}

	/**
	 * <p>
	 * Checks if all parameters are properly set. Throws an exception otherwise.
	 * </p>
	 * 
	 * <p>
	 * The following parameters are to be set by their respective setters:
	 * </p>
	 * 
	 * <ul>
	 * <li>taskId</li>
	 * <li>workerUnit</li>
	 * <li>location</li>
	 * <li>earliestStartTime</li>
	 * <li>latestStartTime</li>
	 * <li>duration</li>
	 * <li>idleSlot</li>
	 * <li>worldPerspective</li>
	 * <li>schedule</li>
	 * </ul>
	 * 
	 * @throws IllegalStateException
	 *             if any parameter is not set or if {@code earliestStartTime}
	 *             is after {@code latestStartTime}.
	 */
	private void checkParameters() {
		// assert all parameters set
		if (taskId              == null ||
			worker              == null ||
			location            == null ||
			earliestStartTime   == null ||
			latestStartTime     == null ||
			duration            == null ||
			idleSlot            == null ||
			worldPerspective    == null ||
			schedule            == null ||
			alternative == null)
		{
			throw new IllegalStateException("some parameters are not set");
		}
		
		// assert earliest <= latest 
		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalStateException("earliestStartTime is after latestStartTime");

		// cannot plan with worker which is not initialized yet
		if (latestStartTime.compareTo(worker.getInitialTime()) < 0)
			throw new IllegalStateException("worker not initialized yet");
		
		if (duration.compareTo(Duration.ZERO) <= 0)
			throw new IllegalStateException("duration is not positive");
	}

	
	/**
	 * <p>Plans new path sections of the current worker to the new task and
	 * the following one. The old section is replaced by the new ones.</p>
	 *
	 * @return {@code true} if the task has been successfully planned.
	 */
	public boolean plan() {
		checkParameters();
		
		// check timing constraints
		// ensures possibility to start and finish task within slot
		LocalDateTime earliestStartTime = earliestStartTime();
		LocalDateTime latestStartTime   = latestStartTime();
		LocalDateTime latestFinishTime  = latestStartTime().plus(duration);
		Duration potentialDuration = Duration.between(earliestStartTime, latestFinishTime);
		
		if (earliestStartTime.isAfter(latestStartTime) ||
			duration.compareTo(potentialDuration) > 0)
		{
			return false;
		}

		init();
		boolean status = planImpl();
		cleanUp();
		
		return status;
	}
	
	private void init() {
		WorkerUnitObstacleBuilder builder = new WorkerUnitObstacleBuilder();
		
		builder.setWorker(worker);
		builder.setStartTime(idleSlot.getStartTime());
		builder.setFinishTime(idleSlot.getFinishTime());
		builder.setSchedule(schedule);
		builder.setAlternative(alternative);
		
		Collection<DynamicObstacle>
			worldObstacles = worldPerspective.getView().getDynamicObstacles(),
			workerObstacles = builder.build();
		
		dynamicObstacles = JoinedCollection.of(worldObstacles, workerObstacles);
	}

	private void cleanUp() {
		dynamicObstacles = null;
	}

	private boolean planImpl() {
		// calculate trajectory to task
			
		Trajectory trajToTask = calculateTrajectoryToTask();
		if (trajToTask.isEmpty())
			return false;
		
		// make task
		
		LocalDateTime taskStartTime = trajToTask.getFinishTime();
		Task task = new Task(taskId, worker.getReference(), location, taskStartTime, duration);
		LocalDateTime taskFinishTime = task.getFinishTime();
		
		// calculate trajectory from task
		
		Trajectory trajFromTask = fixedEnd
			? calculateTrajectoryFromTask(taskFinishTime)
			: calculateStationaryTrajectory(location, taskFinishTime, idleSlot.getFinishTime());

		if (trajFromTask.isEmpty())
			return false;
		
		Trajectory trajAtTask = makeTrajectoryAtTask(task);
		
		// apply changes to scheduleAlternative
		
		if (!trajToTask.duration().isZero())
			alternative.updateTrajectory(worker, trajToTask);
		alternative.updateTrajectory(worker, trajAtTask);
		if (!trajFromTask.duration().isZero())
			alternative.updateTrajectory(worker, trajFromTask);
		
		alternative.addTask(task);
		
		return true;
	}

	/**
	 * Calculates the path between to locations.
	 *
	 * @param startLocation
	 * @param finishLocation
	 * @return {@code true} if a path connecting both locations was found.
	 */
	private SpatialPath calculateSpatialPath(Point startLocation, Point finishLocation) {
		AbstractSpatialPathfinder pf = worldPerspective.getSpatialPathfinder();
	
		pf.setStartLocation(startLocation);
		pf.setFinishLocation(finishLocation);
		
		pf.calculate();
	
		return pf.getResultSpatialPath();
	}

	private Trajectory calculateTrajectoryToTask() {
		SpatialPath path = calculateSpatialPath(idleSlot.getStartLocation(), location);
		
		AbstractMinimumTimePathfinder pf = new SimpleMinimumTimePathfinder();
		
		pf.setDynamicObstacles  ( dynamicObstacles        );
		pf.setSpatialPath       ( path                    );
		pf.setStartArc          ( 0.0                     );
		pf.setFinishArc         ( path.length()           );
		pf.setMinArc            ( 0.0                     );
		pf.setMaxArc            ( path.length()           );
		pf.setMaxSpeed          ( worker.getMaxSpeed()    );
		pf.setStartTime         ( idleSlot.getStartTime() );
		pf.setEarliestFinishTime( earliestStartTime()     );
		pf.setLatestFinishTime  ( latestStartTime()       );
		pf.setBufferDuration    ( duration                ); // TODO expand buffer duration if !fixedEnd
		
		pf.calculate();
		
		return pf.getResultTrajectory();
	}

	private Trajectory calculateTrajectoryFromTask(LocalDateTime startTime) {
		SpatialPath path = calculateSpatialPath(location, idleSlot.getFinishLocation());
		
		AbstractFixTimePathfinder pf = new SimpleFixTimePathfinder();
		
		pf.setDynamicObstacles( dynamicObstacles         );
		pf.setSpatialPath     ( path                     );
		pf.setStartArc        ( 0.0                      );
		pf.setFinishArc       ( path.length()            );
		pf.setMinArc          ( 0.0                      );
		pf.setMaxArc          ( path.length()            );
		pf.setMaxSpeed        ( worker.getMaxSpeed()     );
		pf.setStartTime       ( startTime                );
		pf.setFinishTime      ( idleSlot.getFinishTime() );
		
		pf.calculate();

		return pf.getResultTrajectory();
	}

	private Trajectory calculateStationaryTrajectory(
		ImmutablePoint location,
		LocalDateTime startTime,
		LocalDateTime finishTime)
	{
		// make stationary obstacle
		
		SpatialPath spatialPath = new SpatialPath( ImmutableList.of(location, location) );
		ImmutableList<LocalDateTime> times = ImmutableList.of(startTime, finishTime);
		
		Trajectory trajectory = new SimpleTrajectory(spatialPath, times);
		
		// check for dynamic collisions
		
		if (collides(trajectory, dynamicObstacles))
			return SimpleTrajectory.empty();
		else
			return trajectory;
	}

	private Trajectory makeTrajectoryAtTask(Task task) {
		return new SimpleTrajectory(
			new SpatialPath(ImmutableList.of(location, location)),
			ImmutableList.of(task.getStartTime(), task.getFinishTime()));
	}
	
//	private Trajectory makeFinalTrajectory(ImmutablePoint location, LocalDateTime startTime) {
//		return new SimpleTrajectory(
//			new SpatialPath(ImmutableList.of(location, location)),
//			ImmutableList.of(startTime, idleSlot.getFinishTime()));
//	}
	
}
