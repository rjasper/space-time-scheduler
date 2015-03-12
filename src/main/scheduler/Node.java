package scheduler;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static util.Comparables.*;
import static util.Maps.*;
import static util.TimeConv.*;
import static world.util.ArcTimePathMotionIntervalCalculation.*;
import static world.util.TrajectoryLengthDurationCalculation.*;
import static world.util.TrajectoryMotionIntervalCalculation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;
import scheduler.util.IntervalSet;
import scheduler.util.IntervalSet.Interval;
import scheduler.util.MappedIntervalSet;
import scheduler.util.SimpleIntervalSet;
import world.DecomposedTrajectory;
import world.SimpleTrajectory;
import world.SpatialPath;
import world.Trajectory;
import world.TrajectoryContainer;
import world.util.TrajectoryLengthDurationCalculation.LengthDuration;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

// TODO document
/**
 * <p>The representation of a physical node unit in the real world which is
 * managed by a scheduler. This class abstracts the physical abilities of
 * the real node, such as its shape and maximum velocity. It stores a
 * list of takes it is assigned to and its routes which it travels in the real
 * world.</p>
 *
 * <p>An object of this class is not meant to be used by any scheduler unrelated
 * class since it would expose critical methods, such as
 * {@link #addObstacleSection(NodeObstacle)} or
 * {@link #removeObstacleSection(NodeObstacle)} which don't check for
 * overall consistency.</p>
 *
 * @author Rico Jasper
 */
public class Node {
	
	/**
	 * The node's ID.
	 */
	private final String id;
	
	/**
	 * The reference to this node.
	 */
	private final NodeReference reference;

	/**
	 * The physical shape of this node.
	 */
	private final ImmutablePolygon shape;

	/**
	 * The radius of the shape.
	 */
	private final double radius;

	/**
	 * The maximum velocity of this node.
	 */
	private final double maxSpeed;

	/**
	 * The initial location of the node where it begins to 'exist'.
	 */
	private final ImmutablePoint initialLocation;

	/**
	 * The initial time of the node when it begins to 'exist'.
	 */
	private final LocalDateTime initialTime;

	/**
	 * All jobs which were assigned to this node.
	 */
	private TreeMap<LocalDateTime, Job> jobs = new TreeMap<>();
	
	/**
	 * Contains all consecutive trajectories of this node
	 */
	private TrajectoryContainer trajectoryContainer = new TrajectoryContainer();

	/**
	 * Constructs a node defining its shape, maximum velocity, initial
	 * location and initial time.
	 * 
	 * @param spec
	 *            the specification used to define configure the node.
	 */
	public Node(NodeSpecification spec) {
		this.id = spec.getNodeId();
		this.reference = new NodeReference(this);
		this.shape = spec.getShape();
		this.maxSpeed = spec.getMaxSpeed();
		this.initialLocation = spec.getInitialLocation();
		this.initialTime = spec.getInitialTime();
		this.radius = calcRadius(shape);

		initTrajectoryContainer();
	}

	/**
	 * Initializes the {@link #trajectoryContainer} with an
	 * stationary Trajectory at the node's initial location and initial
	 * time until {@link LocalDateTime#MAX}.
	 */
	private void initTrajectoryContainer() {
		SpatialPath spatialPath = new SpatialPath(
			ImmutableList.of(initialLocation, initialLocation));
		ImmutableList<LocalDateTime> times = ImmutableList.of(
			initialTime, Scheduler.END_OF_TIME);
		Trajectory initialTrajectory = new SimpleTrajectory(spatialPath, times);
		
		trajectoryContainer.update(initialTrajectory);
	}

	/**
	 * @return the ID.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the reference to this node.
	 */
	public NodeReference getReference() {
		return reference;
	}

	/**
	 * @return the physical shape of this node.
	 */
	public ImmutablePolygon getShape() {
		return shape;
	}

	/**
	 * @return the radius of this node's shape.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Calculates the radius.
	 *
	 * @param shape of the node
	 * @return the radius.
	 */
	private static double calcRadius(Polygon shape) {
		Coordinate[] coords = shape.getCoordinates();

		// determine the maximum square-distance to the origin
		double sqRadius = Arrays.stream(coords)
			.mapToDouble(c -> c.x*c.x + c.y*c.y)
			.max()
			.getAsDouble();

		return Math.sqrt(sqRadius);
	}

	/**
	 * @return the maximum velocity.
	 */
	public double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * @return the initial location of the node where it begins to 'exist'.
	 */
	public ImmutablePoint getInitialLocation() {
		return initialLocation;
	}

	/**
	 * @return the initial time of the node when it begins to 'exist'.
	 */
	public LocalDateTime getInitialTime() {
		return initialTime;
	}
	
	/**
	 * Determines whether the given job is currently assigned to this node.
	 * 
	 * @param job
	 * @return {@code true} if {@code job} is assigned.
	 */
	public boolean hasJob(Job job) {
		Objects.requireNonNull(job, "job");
		
		Job retrieval = jobs.get(job.getStartTime());
		
		return retrieval != null && retrieval.equals(job);
	}

	/**
	 * @return all jobs this unit is assigned to.
	 */
	public Collection<Job> getJobs() {
		return unmodifiableCollection(jobs.values());
	}
	
	public NavigableMap<LocalDateTime, Job> getNavigableJobs() {
		return unmodifiableNavigableMap(jobs);
	}

	/**
	 * Assigns a new job to this node.
	 *
	 * @param job
	 * @throws NullPointerException
	 *             if {@code job} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code job} is not assigned to this node.
	 */
	public void addJob(Job job) {
		Objects.requireNonNull(job, "job");
		
		if (job.getNodeReference().getActual() != this)
			throw new IllegalArgumentException("job not assigned to this node");
	
		jobs.put(job.getStartTime(), job);
	}

	/**
	 * Removes a job from this node.
	 * 
	 * @param job
	 * @throws NullPointerException
	 *             if {@code job} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code job} is not assigned to this node.
	 */
	public void removeJob(Job job) {
		Objects.requireNonNull(job, "job");
		
		boolean status = jobs.remove(job.getStartTime(), job);
		
		if (!status)
			throw new IllegalArgumentException("unknown job");
	}

	/**
	 * @return a view on the jobs as a time interval set.
	 */
	public MappedIntervalSet<LocalDateTime, Job> getJobIntervals() {
		return new MappedIntervalSet<LocalDateTime, Job>(jobs,
			t -> new Interval<LocalDateTime>(t.getStartTime(), t.getFinishTime()));
	}
	
	public boolean isIdle() {
		return jobs.isEmpty();
	}

	/**
	 * Determines whether the node is idle for the given entire interval.
	 * 
	 * @param from
	 * @param to
	 * @return {@code true} if the node is idle.
	 */
	public boolean isIdle(LocalDateTime from, LocalDateTime to) {
		return !getJobIntervals().intersects(from, to);
	}

	public Collection<Trajectory> getTrajectories() {
		return trajectoryContainer.getTrajectories();
	}

	public Collection<Trajectory> getTrajectories(LocalDateTime from, LocalDateTime to) {
		return trajectoryContainer.getTrajectories(from, to);
	}

	/**
	 * Updates the given trajectory.
	 * 
	 * @param trajectory
	 * @throws NullPointerException if {@code trajectory} is {@code null}.
	 */
	public void updateTrajectory(Trajectory trajectory) {
		trajectoryContainer.update(trajectory);
	}
	
	/**
	 * Calculates a trajectory from all obstacle sections concatenated together.
	 *
	 * @return the merged trajectory.
	 */
	public Trajectory calcTrajectory() {
		return trajectoryContainer.calcTrajectory();
	}
	
	public void cleanUp(LocalDateTime presentTime) {
		// remove past trajectories
		trajectoryContainer.deleteBefore(presentTime);
		
		// remove past jobs
		
		Entry<LocalDateTime, Job> lowerJobEntry = jobs.lowerEntry(presentTime);
		
		// determine lowest key not to be removed
		if (lowerJobEntry != null) {
			Job lowerJob = lowerJobEntry.getValue();
			
			LocalDateTime lowestKey = lowerJob.getFinishTime().isAfter(presentTime)
				? lowerJob.getStartTime()
				: presentTime;
				
			jobs.headMap(lowestKey).clear();
		}
	}

	/**
	 * Interpolates the location of the node at the given time.
	 * 
	 * @param time
	 * @return the interpolated location.
	 */
	public ImmutablePoint interpolateLocation(LocalDateTime time) {
		return trajectoryContainer.interpolateLocation(time);
	}

	/**
	 * Determines whether the node unit is following a stationary trajectory
	 * during the given time interval.
	 * 
	 * @param from
	 * @param to
	 * @return {@code true} if the node is stationary.
	 */
	public boolean isStationary(LocalDateTime from, LocalDateTime to) {
		return trajectoryContainer.isStationary(from, to);
	}

	public LocalDateTime floorIdleTimeOrNull(LocalDateTime time) {
		if (time.isBefore(initialTime))
			return null;
		
		Job lowerJob = value(jobs.lowerEntry(time));
		LocalDateTime lowerFinish = lowerJob == null
			? initialTime
			: lowerJob.getFinishTime();
		
		// lower.start < time
		
		int lowerFinishCmpTime = lowerFinish.compareTo(time);
		
		if (lowerFinishCmpTime > 0)
			// time < lower.finish
			return null;
		else if (lowerFinishCmpTime == 0) {
			// time == lower.finish

			// if floorJob and ceilJob touch
			if (jobs.containsKey(time))
				// lower.finish == time == ceil.start
				return null;
		}
		
		// lower.finish <= time <= ceil.start
		// lower.finish < ceil.start
		
		return lowerFinish;
	}
	
	public LocalDateTime ceilingIdleTimeOrNull(LocalDateTime time) {
		if (time.isBefore(initialTime))
			return null;
		
		Job lowerJob = value(jobs.lowerEntry(time));
		LocalDateTime lowerFinish = lowerJob == null
			? initialTime
			: lowerJob.getFinishTime();
		
		// lower.start < time
		
		// if lowerJob intersects with time
		if (lowerFinish.compareTo(time) > 0)
			// lower.start < time < lower.finish
			return null;
		
		// lower.finish <= time
		
		Job ceilJob = value(jobs.ceilingEntry(time));
		LocalDateTime ceilStart = ceilJob == null
			? Scheduler.END_OF_TIME
			: ceilJob.getStartTime();
		
		// time <= ceil.start
		
		// if floorJob and ceilJob touch
		if (lowerFinish.isEqual(ceilStart))
			// lower.finish == time == ceil.start
			return null;
		
		// lower.finish <= time <= ceil.start
		// lower.finish < ceil.start
		
		return ceilStart;
	}
	
	/**
	 * Creates a set of idle slots which represent sections of the node
	 * while idling during a given time period.
	 *
	 * @param from the beginning of the time period
	 * @param to the ending of the time period
	 * @return the idle slots
	 * @throws NullPointerException if any argument is null
	 * @throws IllegalArgumentException if from is after to
	 */
	public Collection<IdleSlot> idleSlots(LocalDateTime from, LocalDateTime to) {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");

		if (from.isAfter(to))
			throw new IllegalArgumentException("from is after to");
		
		// short cut empty interval
		if (from.isEqual(to) || !to.isAfter(initialTime))
			return emptyList();
		
		IntervalSet<LocalDateTime> jobIntervals = getJobIntervals();
		IntervalSet<LocalDateTime> idleIntervals = new SimpleIntervalSet<LocalDateTime>()
			.add(max(from, initialTime), to)
			.remove(jobIntervals);
		
		return idleIntervals.stream()
			.map(i -> {
				LocalDateTime startTime  = i.getFromInclusive();
				LocalDateTime finishTime = i.getToExclusive();
				
				Trajectory left = trajectoryContainer.getTrajectory(startTime);
				Trajectory right = left.getFinishTime().compareTo(finishTime) >= 0
					? left
					: trajectoryContainer.getTrajectory(finishTime);
				
				return new IdleSlot(
					left .interpolateLocation(startTime ),
					right.interpolateLocation(finishTime),
					startTime,
					finishTime);
			})
			.collect(toList());
	}
	
	public Duration calcJobDuration(LocalDateTime from, LocalDateTime to) {
		return calcDuration( getJobIntervals().intersection(from, to) );
	}
	
	public Duration calcMotionDuration(LocalDateTime from, LocalDateTime to) {
			return trajectoryContainer.getTrajectories(from, to).stream()
				.map(t -> {
					LocalDateTime tFrom = max(from, t.getStartTime());
					LocalDateTime tTo = min(to, t.getFinishTime());
					
					if (t instanceof DecomposedTrajectory) {
						DecomposedTrajectory dt = (DecomposedTrajectory) t;
						
						LocalDateTime baseTime = dt.getBaseTime();
						double tFromD = timeToSeconds(tFrom, baseTime);
						double tToD = timeToSeconds(tTo, baseTime);
						
						IntervalSet<Double> intervals = calcMotionIntervals(
							dt.getArcTimePathComponent(), tFromD, tToD);
						
						return secondsToDuration( calcDouble(intervals) );
					} else {
						return calcDuration( calcMotionIntervals(t, tFrom, tTo));
					}
				})
				.reduce(Duration::plus)
				.orElse(Duration.ZERO);
		}

	public double calcJobLoad(LocalDateTime from, LocalDateTime to) {
		Duration scopeDuration = Duration.between(from, to);
		Duration jobsDuration = calcJobDuration(from, to);
		
		return durationToSeconds(jobsDuration) / durationToSeconds(scopeDuration);
	}

	public double calcMotionLoad(LocalDateTime from, LocalDateTime to) {
		Duration scopeDuration = Duration.between(from, to);
		Duration motionDuration = calcMotionDuration(from, to);
		
		return durationToSeconds(motionDuration) / durationToSeconds(scopeDuration);
	}

	public double calcLoad(LocalDateTime from, LocalDateTime to) {
		Duration scopeDuration = Duration.between(from, to);
		// assumes that jobs and motions are disjoint
		Duration loadDuration =
			calcJobDuration(from, to).plus(
			calcMotionDuration(from, to));

		return durationToSeconds(loadDuration) / durationToSeconds(scopeDuration);
	}

	public double calcStationaryIdleLoad(LocalDateTime from, LocalDateTime to) {
		Duration scopeDuration = Duration.between(from, to);
		Duration loadDuration =
			calcJobDuration(from, to).plus(
			calcMotionDuration(from, to));
		
		return durationToSeconds(scopeDuration.minus(loadDuration)) /
			durationToSeconds(scopeDuration);
	}

	public double calcVelocityLoad(LocalDateTime from, LocalDateTime to) {
		if (from.isBefore(initialTime))
			throw new IllegalArgumentException("from is before initial time");
		
		LengthDuration lengthDuration = trajectoryContainer
			.getTrajectories(from, to).stream()
			.map(t -> {
				LocalDateTime tFrom = max(from, t.getStartTime());
				LocalDateTime tTo = min(to, t.getFinishTime());
				
				return calcLengthDuration(t, tFrom, tTo);
			})
			.reduce((ld1, ld2) ->
				new LengthDuration(
					ld1.getLength() + ld2.getLength(),
					ld1.getDuration().plus(ld2.getDuration()))
			).get();
		
		return lengthDuration.getLength() /
			(getMaxSpeed() * durationToSeconds( lengthDuration.getDuration() ));
	}
	
	private static Duration calcDuration(IntervalSet<LocalDateTime> timeIntervals) {
		return timeIntervals.stream()
			.map(ti -> Duration.between(ti.getFromInclusive(), ti.getToExclusive()))
			.reduce(Duration::plus)
			.orElse(Duration.ZERO);
	}
	
	private static double calcDouble(IntervalSet<Double> doubleIntervals) {
		return doubleIntervals.stream()
			.map(di -> di.getToExclusive() - di.getFromInclusive())
			.reduce((d1, d2) -> d1 + d2)
			.orElse(0.0);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return id;
	}

}
