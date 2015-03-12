package scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;
import scheduler.util.MappedIntervalSet;
import world.Trajectory;

// TODO document
/**
 * A reference to the {@link WorkerUnit} representation maintained by the
 * scheduler.
 * 
 * @author Rico
 * @see WorkerUnit
 * @see Scheduler
 */
public class WorkerUnitReference  {
	
	/**
	 * The referenced worker.
	 */
	private final WorkerUnit worker;

	/**
	 * Constructs a new {@code WorkerUnitReference} of the given worker.
	 * 
	 * @param worker
	 */
	public WorkerUnitReference(WorkerUnit worker) {
		this.worker = Objects.requireNonNull(worker, "worker");
	}

	/**
	 * @return the worker's ID.
	 */
	public String getId() {
		return worker.getId();
	}
	
	/**
	 * @return the actual worker.
	 */
	WorkerUnit getActual() {
		return worker;
	}

	/**
	 * @return the physical shape of the worker.
	 */
	public ImmutablePolygon getShape() {
		return worker.getShape();
	}

	/**
	 * @return the radius of this worker's shape.
	 */
	public double getRadius() {
		return worker.getRadius();
	}

	/**
	 * @return the maximum velocity.
	 */
	public double getMaxSpeed() {
		return worker.getMaxSpeed();
	}

	/**
	 * @return the initial location of the worker where it begins to 'exist'.
	 */
	public ImmutablePoint getInitialLocation() {
		return worker.getInitialLocation();
	}

	/**
	 * @return the initial time of the worker when it begins to 'exist'.
	 */
	public LocalDateTime getInitialTime() {
		return worker.getInitialTime();
	}

	public boolean hasTask(Task task) {
		return worker.hasTask(task);
	}

	public Collection<Task> getTasks() {
		return worker.getTasks();
	}

	public NavigableMap<LocalDateTime, Task> getNavigableTasks() {
		return worker.getNavigableTasks();
	}

	public MappedIntervalSet<LocalDateTime, Task> getTaskIntervals() {
		return worker.getTaskIntervals();
	}

	public boolean isIdle() {
		return worker.isIdle();
	}

	public boolean isIdle(LocalDateTime from, LocalDateTime to) {
		return worker.isIdle(from, to);
	}

	public Collection<Trajectory> getTrajectories() {
		return worker.getTrajectories();
	}

	public Collection<Trajectory> getTrajectories(LocalDateTime from,
		LocalDateTime to) {
		return worker.getTrajectories(from, to);
	}

	public ImmutablePoint interpolateLocation(LocalDateTime time) {
		return worker.interpolateLocation(time);
	}

	public boolean isStationary(LocalDateTime from, LocalDateTime to) {
		return worker.isStationary(from, to);
	}

	public LocalDateTime floorIdleTimeOrNull(LocalDateTime time) {
		return worker.floorIdleTimeOrNull(time);
	}

	public LocalDateTime ceilingIdleTimeOrNull(LocalDateTime time) {
		return worker.ceilingIdleTimeOrNull(time);
	}

	public Collection<IdleSlot> idleSlots(LocalDateTime from, LocalDateTime to) {
		return worker.idleSlots(from, to);
	}

	public Duration calcTaskDuration(LocalDateTime from, LocalDateTime to) {
		return worker.calcTaskDuration(from, to);
	}

	public Duration calcMotionDuration(LocalDateTime from, LocalDateTime to) {
		return worker.calcMotionDuration(from, to);
	}

	public double calcTaskLoad(LocalDateTime from, LocalDateTime to) {
		return worker.calcTaskLoad(from, to);
	}

	public double calcMotionLoad(LocalDateTime from, LocalDateTime to) {
		return worker.calcMotionLoad(from, to);
	}

	public double calcLoad(LocalDateTime from, LocalDateTime to) {
		return worker.calcLoad(from, to);
	}

	public double calcStationaryIdleLoad(LocalDateTime from, LocalDateTime to) {
		return worker.calcStationaryIdleLoad(from, to);
	}

	public double calcVelocityLoad(LocalDateTime from, LocalDateTime to) {
		return worker.calcVelocityLoad(from, to);
	}

	/**
	 * Calculates a trajectory from all obstacle segments merged together.
	 * 
	 * @return the merged trajectory.
	 */
	public Trajectory calcTrajectory() {
		return worker.calcTrajectory();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return worker.toString();
	}
	
}
