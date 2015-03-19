package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.Objects;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.MappedIntervalSet;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

// TODO document
/**
 * A reference to the {@link Node} representation maintained by the
 * scheduler.
 * 
 * @author Rico Jasper
 * @see Node
 * @see Scheduler
 */
public class NodeReference  {
	
	/**
	 * The referenced node.
	 */
	private final Node node;

	/**
	 * Constructs a new {@code NodeReference} of the given node.
	 * 
	 * @param node
	 */
	public NodeReference(Node node) {
		this.node = Objects.requireNonNull(node, "node");
	}

	/**
	 * @return the node's ID.
	 */
	public String getId() {
		return node.getId();
	}
	
	/**
	 * @return the actual node.
	 */
	Node getActual() {
		return node;
	}

	/**
	 * @return the physical shape of the node.
	 */
	public ImmutablePolygon getShape() {
		return node.getShape();
	}

	/**
	 * @return the radius of this node's shape.
	 */
	public double getRadius() {
		return node.getRadius();
	}

	/**
	 * @return the maximum velocity.
	 */
	public double getMaxSpeed() {
		return node.getMaxSpeed();
	}

	/**
	 * @return the initial location of the node where it begins to 'exist'.
	 */
	public ImmutablePoint getInitialLocation() {
		return node.getInitialLocation();
	}

	/**
	 * @return the initial time of the node when it begins to 'exist'.
	 */
	public LocalDateTime getInitialTime() {
		return node.getInitialTime();
	}

	/**
	 * Returns if the job is assigned to the node.
	 * 
	 * @param job
	 * @return {@code true} if the job is assigned to the node.
	 */
	public boolean hasJob(Job job) {
		return node.hasJob(job);
	}

	/**
	 * @return a unmodifiable view on the node's jobs.
	 */
	public Collection<Job> getJobs() {
		return node.getJobs();
	}

	/**
	 * @return a unmodifiable view on the node's jobs.
	 */
	public NavigableMap<LocalDateTime, Job> getNavigableJobs() {
		return node.getNavigableJobs();
	}

	/**
	 * @return a unmodifiable view on the node's job intervals.
	 */
	public MappedIntervalSet<LocalDateTime, Job> getJobIntervals() {
		return node.getJobIntervals();
	}

	/**
	 * @return a umodifiable view on the node's trajectories.
	 */
	public Collection<Trajectory> getTrajectories() {
		return node.getTrajectories();
	}

	/**
	 * Returns a unmodifiable view on all trajectories overlapping with the
	 * given time interval.
	 * 
	 * @param from
	 * @param to
	 * @return the trajectories.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 */
	public Collection<Trajectory> getTrajectories(LocalDateTime from, LocalDateTime to) {
		return node.getTrajectories(from, to);
	}

	/**
	 * @return {@code true} if there are no jobs assigned to the node.
	 */
	public boolean isIdle() {
		return node.isIdle();
	}

	/**
	 * Returns whether the node is idle within the given time interval.
	 * 
	 * @param from
	 * @param to
	 * @return {@code true} if the node is idle.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 */
	public boolean isIdle(LocalDateTime from, LocalDateTime to) {
		return node.isIdle(from, to);
	}

	/**
	 * <p>
	 * Returns the idle slot within the given time interval.
	 * </p>
	 * 
	 * <p>
	 * Note that the idle slots will only intersect with the interval (no
	 * overlapping)
	 * </p>
	 * 
	 * @param from
	 * @param to
	 * @return the idle slots.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 */
	public Collection<SpaceTimeSlot> NodeSlots(LocalDateTime from, LocalDateTime to) {
		return node.idleSlots(from, to);
	}

	/**
	 * Returns the start time of the idle slot at the given time. If there is no idle slot present
	 * {@code null} is returned.
	 * 
	 * @param time
	 * @return the idle slot's start time or {@code null}.
	 * @throws NullPointerException if {@code time} is {@code null}.
	 */
	public LocalDateTime floorIdleTimeOrNull(LocalDateTime time) {
		return node.floorIdleTimeOrNull(time);
	}

	/**
	 * Returns the finish time of the idle slot at the given time. If there is no idle slot present
	 * {@code null} is returned.
	 * 
	 * @param time
	 * @return the idle slot's finish time or {@code null}.
	 * @throws NullPointerException if {@code time} is {@code null}.
	 */
	public LocalDateTime ceilingIdleTimeOrNull(LocalDateTime time) {
		return node.ceilingIdleTimeOrNull(time);
	}

	/**
	 * Returns whether the node's trajectory is stationary within the given time interval.
	 * @param from
	 * @param to
	 * @return {@code true} if the trajectory is stationary.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 */
	public boolean isStationary(LocalDateTime from, LocalDateTime to) {
		return node.isStationary(from, to);
	}

	/**
	 * Interpolates the location of the node at the given time.
	 * 
	 * @param time
	 * @return the location.
	 * @throws NullPointerException
	 *             if {@code time} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code time} is before the node's initial time.
	 */
	public ImmutablePoint interpolateLocation(LocalDateTime time) {
		return node.interpolateLocation(time);
	}

	/**
	 * Calculates the accumulated duration of any jobs during the given time interval.
	 * 
	 * @param from
	 * @param to
	 * @return the job duration.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 */
	public Duration calcJobDuration(LocalDateTime from, LocalDateTime to) {
		return node.calcJobDuration(from, to);
	}

	/**
	 * Calculates the accumulated duration of motion during the given time
	 * interval. The node is considered to be in motion if it changes its
	 * position over time (||velocity|| &gt; 0).
	 * 
	 * @param from
	 * @param to
	 * @return the motion duration.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 */
	public Duration calcMotionDuration(LocalDateTime from, LocalDateTime to) {
		return node.calcMotionDuration(from, to);
	}

	/**
	 * Calculates the job load during the given interval. More formally, the
	 * ratio of the job duration during the given interval in respect to the
	 * interval itself.
	 * 
	 * @param from
	 * @param to
	 * @return the job load
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 * @see #calcJobDuration(LocalDateTime, LocalDateTime)
	 */
	public double calcJobLoad(LocalDateTime from, LocalDateTime to) {
		return node.calcJobLoad(from, to);
	}

	/**
	 * Calculates the motion load during the given interval. More formally, the
	 * ratio of the motion duration during the given interval in respect to the
	 * interval itself.
	 * 
	 * @param from
	 * @param to
	 * @return the motion load
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 * @see #calcMotionDuration(LocalDateTime, LocalDateTime)
	 */
	public double calcMotionLoad(LocalDateTime from, LocalDateTime to) {
		return node.calcMotionLoad(from, to);
	}

	/**
	 * Calculates the overall load during the given interval. The load is defined as the
	 * sum of the motion load and the job load.
	 * 
	 * @param from
	 * @param to
	 * @return the load
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 */
	public double calcLoad(LocalDateTime from, LocalDateTime to) {
		return node.calcLoad(from, to);
	}

	/**
	 * Calculates the stationary idle load during the given interval. More
	 * formally, the ratio of the stationary idle duration during the given
	 * interval in respect to the interval itself. The stationary idle duration
	 * is the duration while the the node is neither in motion nor executing a
	 * job.
	 * 
	 * @param from
	 * @param to
	 * @return the stationary idle load
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 */
	public double calcStationaryIdleLoad(LocalDateTime from, LocalDateTime to) {
		return node.calcStationaryIdleLoad(from, to);
	}

	/**
	 * Calculates the velocity load during the given interval. The velocity load is defined by
	 * the ratio of the average speed to the maximum speed.
	 * 
	 * @param from
	 * @param to
	 * @return the velocity load.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the interval is invalid.
	 */
	public double calcVelocityLoad(LocalDateTime from, LocalDateTime to) {
		return node.calcVelocityLoad(from, to);
	}

	/**
	 * Calculates a trajectory from all obstacle segments merged together.
	 * 
	 * @return the merged trajectory.
	 */
	public Trajectory calcTrajectory() {
		return node.calcTrajectory();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return node.toString();
	}
	
}
