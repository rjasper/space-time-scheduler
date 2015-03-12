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

	public boolean hasJob(Job job) {
		return node.hasJob(job);
	}

	public Collection<Job> getJobs() {
		return node.getJobs();
	}

	public NavigableMap<LocalDateTime, Job> getNavigableJobs() {
		return node.getNavigableJobs();
	}

	public MappedIntervalSet<LocalDateTime, Job> getJobIntervals() {
		return node.getJobIntervals();
	}

	public Collection<Trajectory> getTrajectories() {
		return node.getTrajectories();
	}

	public Collection<Trajectory> getTrajectories(LocalDateTime from,
		LocalDateTime to) {
		return node.getTrajectories(from, to);
	}

	public boolean isIdle() {
		return node.isIdle();
	}

	public boolean isIdle(LocalDateTime from, LocalDateTime to) {
		return node.isIdle(from, to);
	}

	public Collection<IdleSlot> idleSlots(LocalDateTime from, LocalDateTime to) {
		return node.idleSlots(from, to);
	}

	public LocalDateTime floorIdleTimeOrNull(LocalDateTime time) {
		return node.floorIdleTimeOrNull(time);
	}

	public LocalDateTime ceilingIdleTimeOrNull(LocalDateTime time) {
		return node.ceilingIdleTimeOrNull(time);
	}

	public boolean isStationary(LocalDateTime from, LocalDateTime to) {
		return node.isStationary(from, to);
	}

	public ImmutablePoint interpolateLocation(LocalDateTime time) {
		return node.interpolateLocation(time);
	}

	public Duration calcJobDuration(LocalDateTime from, LocalDateTime to) {
		return node.calcJobDuration(from, to);
	}

	public Duration calcMotionDuration(LocalDateTime from, LocalDateTime to) {
		return node.calcMotionDuration(from, to);
	}

	public double calcJobLoad(LocalDateTime from, LocalDateTime to) {
		return node.calcJobLoad(from, to);
	}

	public double calcMotionLoad(LocalDateTime from, LocalDateTime to) {
		return node.calcMotionLoad(from, to);
	}

	public double calcLoad(LocalDateTime from, LocalDateTime to) {
		return node.calcLoad(from, to);
	}

	public double calcStationaryIdleLoad(LocalDateTime from, LocalDateTime to) {
		return node.calcStationaryIdleLoad(from, to);
	}

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
