package scheduler;

import static util.Comparables.*;
import static world.util.DynamicCollisionDetector.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import scheduler.util.WorkerUnitObstacleBuilder;
import util.JoinedCollection;
import world.DynamicObstacle;
import world.SimpleTrajectory;
import world.SpatialPath;
import world.Trajectory;
import world.World;
import world.WorldPerspective;
import world.pathfinder.AbstractFixTimePathfinder;
import world.pathfinder.AbstractSpatialPathfinder;
import world.pathfinder.SimpleFixTimePathfinder;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

public class TaskRemovalPlanner {
	
	private World world = null;
	
	private WorldPerspective worldPerspective = null;
	
	private LocalDateTime frozenHorizonTime = null;
	
	private Schedule schedule = null;
	
	private ScheduleAlternative alternative = null;
	
	private Task task = null;
	
	private boolean fixedEnd = true;
	
	private transient WorkerUnit worker;
	
	private transient LocalDateTime slotStartTime;
	
	private transient LocalDateTime slotFinishTime;
	
	private transient Collection<DynamicObstacle> dynamicObstacles;

	public void setWorld(World world) {
		this.world = Objects.requireNonNull(world, "world");
	}

	public void setWorldPerspective(WorldPerspective worldPerspective) {
		this.worldPerspective = Objects.requireNonNull(worldPerspective, "worldPerspective");
	}

	public void setFrozenHorizonTime(LocalDateTime frozenHorizonTime) {
		this.frozenHorizonTime = Objects.requireNonNull(frozenHorizonTime, "frozenHorizonTime");
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = Objects.requireNonNull(schedule, "schedule");
	}

	public void setAlternative(ScheduleAlternative alternative) {
		this.alternative = Objects.requireNonNull(alternative, "alternative");
	}

	public void setTask(Task task) {
		this.task = Objects.requireNonNull(task, "task");
	}

	public void setFixedEnd(boolean fixedEnd) {
		this.fixedEnd = fixedEnd;
	}

	private void checkParameters() {
		Objects.requireNonNull(world, "world");
		Objects.requireNonNull(worldPerspective, "worldPerspective");
		Objects.requireNonNull(frozenHorizonTime, "frozenHorizonTime");
		Objects.requireNonNull(schedule, "schedule");
		Objects.requireNonNull(alternative, "alternative");
		Objects.requireNonNull(task, "task");
	}
	
	public boolean plan() {
		checkParameters();
		
		if (!task.getFinishTime().isBefore(frozenHorizonTime))
			return false;
		
		init();
		boolean status = planImpl();
		cleanUp();
		
		return status;
	}
	
	private void init() {
		worker = task.getWorkerReference().getActual();
		
		LocalDateTime taskStartTime = task.getStartTime();
		LocalDateTime taskFinishTime = task.getFinishTime();
		LocalDateTime idleStartTime = worker.floorIdleTimeOrNull(taskStartTime);
		LocalDateTime idleFinishTime = worker.ceilingIdleTimeOrNull(taskFinishTime);
		
		slotStartTime = max(
			frozenHorizonTime,
			idleStartTime == null ? taskStartTime : idleStartTime);
		slotFinishTime = idleFinishTime == null ? taskFinishTime : idleFinishTime;
		
		WorkerUnitObstacleBuilder builder = new WorkerUnitObstacleBuilder();
		
		builder.setWorker(worker);
		builder.setStartTime(slotStartTime);
		builder.setFinishTime(slotFinishTime);
		builder.setSchedule(schedule);
		builder.setAlternative(alternative);
		
		Collection<DynamicObstacle>
			worldObstacles = worldPerspective.getView().getDynamicObstacles(),
			workerObstacles = builder.build();
		
		dynamicObstacles = JoinedCollection.of(worldObstacles, workerObstacles);
	}

	private void cleanUp() {
		worker = null;
		dynamicObstacles = null;
		slotStartTime = null;
		slotFinishTime = null;
	}
	
	private boolean planImpl() {
		ImmutablePoint startLocation = worker.interpolateLocation(slotStartTime);
		ImmutablePoint finishLocation = fixedEnd
			? worker.interpolateLocation(slotFinishTime)
			: null;
		
		Trajectory trajectory = fixedEnd
			? calculateTrajectory(startLocation, finishLocation, slotStartTime, slotFinishTime)
			: calculateStationaryTrajectory(startLocation, slotStartTime, slotFinishTime);
		
		if (trajectory.isEmpty())
			return false;
		
		alternative.updateTrajectory(worker, trajectory);
		alternative.addTaskRemoval(task);
		
		return true;
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

	private Trajectory calculateTrajectory(
		Point startLocation, Point finishLocation,
		LocalDateTime startTime, LocalDateTime finishTime)
	{
		// calculate spatial path
		
		AbstractSpatialPathfinder spf = worldPerspective.getSpatialPathfinder();
	
		spf.setStartLocation(startLocation);
		spf.setFinishLocation(finishLocation);
		
		boolean status = spf.calculate();
		
		if (!status)
			return SimpleTrajectory.empty(); // not sure if this could ever happen
	
		SpatialPath spatialPath = spf.getResultSpatialPath();
		
		// calculate trajectory
		
		AbstractFixTimePathfinder vpf = new SimpleFixTimePathfinder();
		
		vpf.setDynamicObstacles( dynamicObstacles     );
		vpf.setSpatialPath     ( spatialPath          );
		vpf.setStartArc        ( 0.0                  );
		vpf.setFinishArc       ( spatialPath.length() );
		vpf.setMinArc          ( 0.0                  );
		vpf.setMaxArc          ( spatialPath.length() );
		vpf.setMaxSpeed        ( worker.getMaxSpeed() );
		vpf.setStartTime       ( startTime            );
		vpf.setFinishTime      ( finishTime           );
		
		vpf.calculate();

		return vpf.getResultTrajectory();
	}

}
