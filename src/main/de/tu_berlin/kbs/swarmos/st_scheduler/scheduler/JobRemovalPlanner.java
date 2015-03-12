package de.tu_berlin.kbs.swarmos.st_scheduler.scheduler;

import static de.tu_berlin.kbs.swarmos.st_scheduler.util.Comparables.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.world.util.DynamicCollisionDetector.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.util.NodeObstacleBuilder;
import de.tu_berlin.kbs.swarmos.st_scheduler.util.JoinedCollection;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.SpatialPath;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.World;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.WorldPerspective;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.pathfinder.AbstractFixTimePathfinder;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.pathfinder.AbstractSpatialPathfinder;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.pathfinder.SimpleFixTimePathfinder;

public class JobRemovalPlanner {
	
	private World world = null;
	
	private WorldPerspective worldPerspective = null;
	
	private LocalDateTime frozenHorizonTime = null;
	
	private Schedule schedule = null;
	
	private ScheduleAlternative alternative = null;
	
	private Job job = null;
	
	private boolean fixedEnd = true;
	
	private transient Node node;
	
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

	public void setJob(Job job) {
		this.job = Objects.requireNonNull(job, "job");
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
		Objects.requireNonNull(job, "job");
	}
	
	public boolean plan() {
		checkParameters();
		
		if (job.getFinishTime().isBefore(frozenHorizonTime))
			return false;
		
		init();
		boolean status = planImpl();
		cleanUp();
		
		return status;
	}
	
	private void init() {
		node = job.getNodeReference().getActual();
		
		LocalDateTime jobStartTime = job.getStartTime();
		LocalDateTime jobFinishTime = job.getFinishTime();
		LocalDateTime idleStartTime = node.floorIdleTimeOrNull(jobStartTime);
		LocalDateTime idleFinishTime = node.ceilingIdleTimeOrNull(jobFinishTime);
		
		slotStartTime = max(
			frozenHorizonTime,
			idleStartTime == null ? jobStartTime : idleStartTime);
		slotFinishTime = idleFinishTime == null ? jobFinishTime : idleFinishTime;
		
		NodeObstacleBuilder builder = new NodeObstacleBuilder();
		
		builder.setNode(node);
		builder.setStartTime(slotStartTime);
		builder.setFinishTime(slotFinishTime);
		builder.setSchedule(schedule);
		builder.setAlternative(alternative);
		
		Collection<DynamicObstacle>
			worldObstacles = worldPerspective.getView().getDynamicObstacles(),
			nodeObstacles = builder.build();
		
		dynamicObstacles = JoinedCollection.of(worldObstacles, nodeObstacles);
	}

	private void cleanUp() {
		node = null;
		dynamicObstacles = null;
		slotStartTime = null;
		slotFinishTime = null;
	}
	
	private boolean planImpl() {
		ImmutablePoint startLocation = node.interpolateLocation(slotStartTime);
		ImmutablePoint finishLocation = fixedEnd
			? node.interpolateLocation(slotFinishTime)
			: null;
		
		Trajectory trajectory = fixedEnd
			? calculateTrajectory(startLocation, finishLocation, slotStartTime, slotFinishTime)
			: calculateStationaryTrajectory(startLocation, slotStartTime, slotFinishTime);
		
		if (trajectory.isEmpty())
			return false;
		
		alternative.updateTrajectory(node, trajectory);
		alternative.addJobRemoval(job);
		
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
		vpf.setMaxSpeed        ( node.getMaxSpeed() );
		vpf.setStartTime       ( startTime            );
		vpf.setFinishTime      ( finishTime           );
		
		vpf.calculate();

		return vpf.getResultTrajectory();
	}

}
