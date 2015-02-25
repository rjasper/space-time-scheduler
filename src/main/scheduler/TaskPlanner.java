package scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

import world.DynamicObstacle;
import world.Trajectory;
import world.WorldPerspective;
import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;

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
	private ScheduleAlternative scheduleAlternative = null;
	
	/**
	 * The workers as dynamic obstacles.
	 */
	private Collection<DynamicObstacle> workerObstacles = new LinkedList<>();
	
	/**
	 * Indicates if the final position of time slot is mandatory.
	 */
	private boolean fixedEnd = true;

	public void setTaskId(UUID taskId) {
		this.taskId = Objects.requireNonNull(taskId, "taskId");
	}

	public void setWorkerUnit(WorkerUnit workerUnit) {
		this.worker = Objects.requireNonNull(workerUnit, "workerUnit");
	}

	public void setLocation(ImmutablePoint location) {
		this.location = Objects.requireNonNull(location, "location");
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

	public void setScheduleAlternative(ScheduleAlternative alternative) {
		this.scheduleAlternative = Objects.requireNonNull(alternative, "alternative");
	}

	public void setFixedEnd(boolean fixedEnd) {
		this.fixedEnd = fixedEnd;
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
			scheduleAlternative == null)
		{
			throw new IllegalStateException("some parameters are not set");
		}
		
		// assert earliest <= latest 
		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalStateException("earliestStartTime is after latestStartTime");

		// cannot plan with worker which is not initialized yet
		if (latestStartTime.compareTo(worker.getInitialTime()) < 0)
			throw new IllegalStateException("worker not initialized yet");
	}

	
	/**
	 * <p>Plans new path sections of the current worker to the new task and
	 * the following one. The old section is replaced by the new ones.</p>
	 *
	 * @return {@code true} if the task has been successfully planned.
	 */
	public boolean plan() {
		checkParameters();

		initWorkerObstacles();

		// TODO implement
		
		shapeLookUp.clear();
		
		return false;
	}
	
	private Map<WorkerUnit, ImmutablePolygon> shapeLookUp = new IdentityHashMap<>();
	
	private DynamicObstacle makeWorkerObstacle(WorkerUnit worker, Trajectory trajectory) {
		double radius = this.worker.getRadius();
		
		ImmutablePolygon shape = shapeLookUp.computeIfAbsent(worker, w ->
			(ImmutablePolygon) w.getShape().buffer(radius));
		
		return new DynamicObstacle(shape, trajectory);
	}
	
	private void initWorkerObstacles() {
		workerObstacles.clear();
		
		LocalDateTime from = idleSlot.getStartTime();
		LocalDateTime to = idleSlot.getFinishTime();
		
		// TODO make code fancier (a little repetitive right now)

		// original trajectories
		for (WorkerUnit w : schedule.getWorkers()) {
			if (w == worker)
				continue;
			
			for (Trajectory t : w.getTrajectories(from, to))
				workerObstacles.add(makeWorkerObstacle(w, t));
		}
		
		// alternative trajectories added to schedule
		for (ScheduleAlternative a : schedule.getAlternatives()) {
			for (WorkerUnit w : a.getWorkers()) {
				if (w == worker)
					continue;
				
				for (Trajectory t : a.getTrajectoryUpdates(w))
					workerObstacles.add(makeWorkerObstacle(w, t));
			}
		}

		// alternative trajectories of current alternative
		for (WorkerUnit w : scheduleAlternative.getWorkers()) {
			if (w == worker)
				continue;
			
			for (Trajectory t : scheduleAlternative.getTrajectoryUpdates(w))
				workerObstacles.add(makeWorkerObstacle(w, t));
		}
	}
	
}
