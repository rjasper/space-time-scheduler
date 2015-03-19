package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;
import static de.tu_berlin.mailbox.rjasper.lang.Comparables.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.DynamicCollisionDetector.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.collect.JoinedCollection;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.NodeObstacleBuilder;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspective;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.AbstractSpatialPathfinder;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.LazyFixTimePathfinder;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.LazyMinimumTimePathfinder;

// TODO document
/**
 * <p>The JobPlanner plans a new {@link Job} into an established set of jobs.
 * It requires multiple parameters which determine the {@link Node node}
 * to execute the new job, and the location, duration, and time interval of the
 * execution. It is also responsible for ensuring that the designated node is
 * able to reach the job's location with colliding with any other object; be it
 * stationary or another node.</p>
 *
 * <p>Should it be impossible to plan the new job then the JobPlanner will
 * not change the current job set. This might be the case when the designated
 * node is unable to reach the location without violating any time
 * constraints.</p>
 *
 * <p>The planning involves the calculation of a spatial path from the previous
 * location of the node to the job's location and the successive path to
 * the next location the node is required to be. The next step is to calculate
 * a velocity profile to evade dynamic obstacles.</p>
 *
 * @author Rico Jasper
 */
public class JobPlanner {

	/**
	 * The id of the {@link Job job} to be planned.
	 */
	private UUID jobId = null;

	/**
	 * The current node.
	 */
	private Node node = null;

	/**
	 * The location of the {@link Job job} to be planned.
	 */
	private ImmutablePoint location = null;

	/**
	 * The earliest start time of the {@link Job job} to be planned.
	 */
	private LocalDateTime earliestStartTime = null;

	/**
	 * The latest start time of the {@link Job job} to be planned.
	 */
	private LocalDateTime latestStartTime = null;

	/**
	 * The duration of the {@link Job job} to be planned.
	 */
	private Duration duration = null;

	/**
	 * The used idle slot to schedule the job.
	 */
	private SpaceTimeSlot slot = null;

	/**
	 * The world perspective of obstacles as perceived by the {@link #node}.
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
	 * The nodes as dynamic obstacles.
	 */
	private transient Collection<DynamicObstacle> dynamicObstacles;

	/**
	 * Indicates if the final position of time slot is mandatory.
	 */
	private boolean fixedEnd = true;

	public void setJobId(UUID jobId) {
		this.jobId = Objects.requireNonNull(jobId, "jobId");
	}

	public void setNode(Node node) {
		this.node = Objects.requireNonNull(node, "node");
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

	public void setSlot(SpaceTimeSlot slot) {
		this.slot = Objects.requireNonNull(slot, "NodeSlot");
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
			node.getInitialTime(),
			slot.getStartTime());
	}

	private LocalDateTime latestStartTime() {
		return min(
			latestStartTime,
			slot.getFinishTime().minus(duration));
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
	 * <li>jobId</li>
	 * <li>node</li>
	 * <li>location</li>
	 * <li>earliestStartTime</li>
	 * <li>latestStartTime</li>
	 * <li>duration</li>
	 * <li>NodeSlot</li>
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
		if (jobId              == null ||
			node              == null ||
			location            == null ||
			earliestStartTime   == null ||
			latestStartTime     == null ||
			duration            == null ||
			slot            == null ||
			worldPerspective    == null ||
			schedule            == null ||
			alternative == null)
		{
			throw new IllegalStateException("some parameters are not set");
		}

		// assert earliest <= latest
		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalStateException("earliestStartTime is after latestStartTime");

		// cannot plan with node which is not initialized yet
		if (latestStartTime.compareTo(node.getInitialTime()) < 0)
			throw new IllegalStateException("node not initialized yet");

		if (duration.compareTo(Duration.ZERO) <= 0)
			throw new IllegalStateException("duration is not positive");
	}


	/**
	 * <p>Plans new path sections of the current node to the new job and
	 * the following one. The old section is replaced by the new ones.</p>
	 *
	 * @return {@code true} if the job has been successfully planned.
	 */
	public boolean plan() {
		checkParameters();

		// check timing constraints
		// ensures possibility to start and finish job within slot
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
		NodeObstacleBuilder builder = new NodeObstacleBuilder();

		builder.setNode(node);
		builder.setStartTime(slot.getStartTime());
		builder.setFinishTime(slot.getFinishTime());
		builder.setSchedule(schedule);
		builder.setAlternative(alternative);

		Collection<DynamicObstacle>
			worldObstacles = worldPerspective.getView().getDynamicObstacles(),
			nodeObstacles = builder.build();

		dynamicObstacles = JoinedCollection.of(worldObstacles, nodeObstacles);
	}

	private void cleanUp() {
		dynamicObstacles = null;
	}

	private boolean planImpl() {
		// calculate trajectory to job

		Trajectory trajToJob = calculateTrajectoryToJob();
		if (trajToJob.isEmpty())
			return false;

		// make job

		LocalDateTime jobStartTime = trajToJob.getFinishTime();
		Job job = new Job(jobId, node.getReference(), location, jobStartTime, duration);
		LocalDateTime jobFinishTime = job.getFinishTime();

		// calculate trajectory from job

		Trajectory trajFromJob = fixedEnd
			? calculateTrajectoryFromJob(jobFinishTime)
			: calculateStationaryTrajectory(location, jobFinishTime, slot.getFinishTime());

		if (trajFromJob.isEmpty())
			return false;

		Trajectory trajAtJob = makeTrajectoryAtJob(job);

		// apply changes to scheduleAlternative

		if (!trajToJob.duration().isZero())
			alternative.updateTrajectory(node, trajToJob);
		alternative.updateTrajectory(node, trajAtJob);
		if (!trajFromJob.duration().isZero())
			alternative.updateTrajectory(node, trajFromJob);

		alternative.addJob(job);

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

	private Trajectory calculateTrajectoryToJob() {
		SpatialPath path = calculateSpatialPath(slot.getStartLocation(), location);

		LazyMinimumTimePathfinder pf = new LazyMinimumTimePathfinder();

		pf.setDynamicObstacles  ( dynamicObstacles        );
		pf.setSpatialPath       ( path                    );
		pf.setStartArc          ( 0.0                     );
		pf.setFinishArc         ( path.length()           );
		pf.setMinArc            ( 0.0                     );
		pf.setMaxArc            ( path.length()           );
		pf.setMaxSpeed          ( node.getMaxSpeed()    );
		pf.setStartTime         ( slot.getStartTime() );
		pf.setEarliestFinishTime( earliestStartTime()     );
		pf.setLatestFinishTime  ( latestStartTime()       );
		pf.setBufferDuration    ( duration                ); // TODO expand buffer duration if !fixedEnd
		pf.setMinStopDuration( Duration.ZERO ); // TODO use appropriate value

		pf.calculate();

		return pf.getResultTrajectory();
	}

	private Trajectory calculateTrajectoryFromJob(LocalDateTime startTime) {
		SpatialPath path = calculateSpatialPath(location, slot.getFinishLocation());

		LazyFixTimePathfinder pf = new LazyFixTimePathfinder();

		pf.setDynamicObstacles( dynamicObstacles         );
		pf.setSpatialPath     ( path                     );
		pf.setStartArc        ( 0.0                      );
		pf.setFinishArc       ( path.length()            );
		pf.setMinArc          ( 0.0                      );
		pf.setMaxArc          ( path.length()            );
		pf.setMinStopDuration( Duration.ZERO ); // TODO use appropriate value
		pf.setMaxSpeed        ( node.getMaxSpeed()     );
		pf.setStartTime       ( startTime                );
		pf.setFinishTime      ( slot.getFinishTime() );

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

	private Trajectory makeTrajectoryAtJob(Job job) {
		return new SimpleTrajectory(
			new SpatialPath(ImmutableList.of(location, location)),
			ImmutableList.of(job.getStartTime(), job.getFinishTime()));
	}

//	private Trajectory makeFinalTrajectory(ImmutablePoint location, LocalDateTime startTime) {
//		return new SimpleTrajectory(
//			new SpatialPath(ImmutableList.of(location, location)),
//			ImmutableList.of(startTime, NodeSlot.getFinishTime()));
//	}

}
