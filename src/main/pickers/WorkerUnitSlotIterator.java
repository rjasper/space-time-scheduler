package pickers;

import static com.vividsolutions.jts.operation.distance.DistanceOp.distance;
import static util.DurationConv.ofSeconds;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import tasks.IdleSlot;
import tasks.WorkerUnit;

import com.vividsolutions.jts.geom.Point;

/**
 * A WorkerUnitSlotIterator iterates over all idle slots of workers which
 * satisfy the given specifications of a task. To satisfy means that a worker is
 * capable to reach the task location while driving at maximum speed without
 * violating any time specification of the new task or other tasks. The
 * avoidance of obstacles are not considered.
 *
 * @author Rico Jasper
 */
public class WorkerUnitSlotIterator implements Iterator<WorkerUnitSlotIterator.WorkerUnitSlot> {

	/**
	 * Helper class to pair a worker and one of its idle slots.
	 */
	public static class WorkerUnitSlot {

		/**
		 * The worker of the idle slot.
		 */
		private final WorkerUnit workerUnit;

		/**
		 * The idle slot.
		 */
		private final IdleSlot idleSlot;

		/**
		 * Pairs an idle slot with its worker.
		 *
		 * @param workerUnit
		 * @param idleSlot
		 */
		public WorkerUnitSlot(WorkerUnit workerUnit, IdleSlot idleSlot) {
			this.workerUnit = workerUnit;
			this.idleSlot = idleSlot;
		}

		/**
		 * @return the worker of the idle slot.
		 */
		public WorkerUnit getWorkerUnit() {
			return workerUnit;
		}

		/**
		 * @return the idle slot.
		 */
		public IdleSlot getIdleSlot() {
			return idleSlot;
		}

	}

	/**
	 * The location of the task specification.
	 */
	private final Point location;

	/**
	 * The earliest time to execute the task.
	 */
	private final LocalDateTime earliestStartTime;

	/**
	 * The latest time to execute the task.
	 */
	private final LocalDateTime latestStartTime;

	/**
	 * The duration of the task execution.
	 */
	private final Duration duration;

	/**
	 * An iterator over the workers to be considered.
	 */
	private Iterator<WorkerUnit> workerIterator;

	/**
	 * An iterator over the idle slots of the current worker.
	 */
	private Iterator<IdleSlot> slotIterator;

	/**
	 * The next worker to be returned as current worker.
	 */
	private WorkerUnit nextWorker = null;

	/**
	 * The next slot to be returned as current slot.
	 */
	private IdleSlot nextSlot = null;

	/**
	 * The current worker of the iteration.
	 */
	private WorkerUnit currentWorker = null;

	/**
	 * The current slot of the iteration.
	 */
	private IdleSlot currentSlot = null;

	/**
	 * Constructs a WorkerUnitSlotIterator which iterates over the given set of
	 * workers to while checking against the given task specifications.
	 *
	 * @param workers the worker pool to check
	 * @param location of the task
	 * @param earliestStartTime the earliest time to begin the task execution
	 * @param latestStartTime the latest time to begin the task execution
	 * @param duration of the task
	 *
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if any of the following is true:
	 * <ul>
	 * <li>The location is empty or invalid.</li>
	 * <li>The earliestStartTime is after the latestStartTime.</li>
	 * <li>The duration is negative.</li>
	 * </ul>
	 */
	public WorkerUnitSlotIterator(
		Collection<WorkerUnit> workers,
		Point location,
		LocalDateTime earliestStartTime,
		LocalDateTime latestStartTime,
		Duration duration)
	{
		Objects.requireNonNull(workers, "workers");
		Objects.requireNonNull(location, "location");
		Objects.requireNonNull(earliestStartTime, "earliestStartTime");
		Objects.requireNonNull(latestStartTime, "latestStartTime");
		Objects.requireNonNull(duration, "duration");

		if (location.isEmpty() || !location.isValid())
			throw new IllegalArgumentException("illegal location");
		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalArgumentException("earliestStartTime is after latestStartTime");
		if (duration.isNegative())
			throw new IllegalArgumentException("duration is negative");

		this.workerIterator = new ArrayList<>(workers).iterator();
		this.location = location;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.duration = duration;

		// The next worker and idle slot pair is calculated before they are
		// requested. This enables an easy check whether or not there is a next
		// pair.
		nextWorker();
		nextSlot();
	}

	@Override
	public boolean hasNext() {
		return getNextWorker() != null;
	}

	/**
	 * @return the location of the task specification.
	 */
	private Point getLocation() {
		return location;
	}

	/**
	 * @return the earliest time to execute the task.
	 */
	private LocalDateTime getEarliestStartTime() {
		return earliestStartTime;
	}

	/**
	 * @return the latest time to execute the task.
	 */
	private LocalDateTime getLatestStartTime() {
		return latestStartTime;
	}

	/**
	 * @return the duration of the task execution.
	 */
	private Duration getDuration() {
		return duration;
	}

	/**
	 * @return the iterator over the workers to be considered.
	 */
	private Iterator<WorkerUnit> getWorkerIterator() {
		return workerIterator;
	}

	/**
	 * @return the next worker to be returned as current worker.
	 */
	private WorkerUnit getNextWorker() {
		return nextWorker;
	}

	/**
	 * Sets the next worker to be returned as current worker.
	 *
	 * @param nextWorker
	 */
	private void setNextWorker(WorkerUnit nextWorker) {
		this.nextWorker = nextWorker;
	}

	/**
	 * @return the next slot to be returned as current slot.
	 */
	private IdleSlot getNextSlot() {
		return nextSlot;
	}

	/**
	 * Sets The next slot to be returned as current slot.
	 *
	 * @param nextSlot
	 */
	private void setNextSlot(IdleSlot nextSlot) {
		this.nextSlot = nextSlot;
	}

	/**
	 * @return the iterator over the idle slots of the current worker.
	 */
	private Iterator<IdleSlot> getSlotIterator() {
		return slotIterator;
	}

	/**
	 * Sets the iterator over the idle slots of the current worker.
	 *
	 * @param slotIterator
	 */
	private void setSlotIterator(Iterator<IdleSlot> slotIterator) {
		this.slotIterator = slotIterator;
	}

	/**
	 * @return the current worker of the iteration.
	 */
	public WorkerUnit getCurrentWorker() {
		return currentWorker;
	}

	/**
	 * Sets the current worker of the iteration.
	 *
	 * @param currentWorker
	 */
	private void setCurrentWorker(WorkerUnit currentWorker) {
		this.currentWorker = currentWorker;
	}

	/**
	 * @return the current slot of the iteration.
	 */
	public IdleSlot getCurrentSlot() {
		return currentSlot;
	}

	/**
	 * Sets the current slot of the iteration.
	 *
	 * @param currentSlot
	 */
	private void setCurrentSlot(IdleSlot currentSlot) {
		this.currentSlot = currentSlot;
	}

	@Override
	public WorkerUnitSlot next() {
		setCurrentWorker(getNextWorker());
		setCurrentSlot(getNextSlot());

		nextSlot();

		return new WorkerUnitSlot(getCurrentWorker(), getCurrentSlot());
	}

	/**
	 * Determines the next worker of the iteration.
	 *
	 * @return the next worker.
	 */
	private WorkerUnit nextWorker() {
		// sets the next worker and initializes an new idle slot iterator

		LocalDateTime earliest = getEarliestStartTime();
		LocalDateTime latest = getLatestStartTime();

		Iterator<WorkerUnit> it = getWorkerIterator();
		WorkerUnit worker = it.next();
		Collection<IdleSlot> slots = worker.idleSlots(earliest, latest);

		setNextWorker(worker);
		setSlotIterator(slots.iterator());

		return worker;
	}

	/**
	 * Determines the next idle slot of the iteration.
	 *
	 * @return the next idle slot.
	 */
	private IdleSlot nextSlot() {
		Iterator<WorkerUnit> wit = getWorkerIterator();
		Iterator<IdleSlot> sit = getSlotIterator();

		WorkerUnit worker = getNextWorker();
		IdleSlot slot = null;

		// iterates over the remaining idle slots of the remaining workers
		// until a valid slot was found
		while (wit.hasNext() || sit.hasNext()) {
			// if there are no more idle slots of the current worker
			// then get the next one
			if (!sit.hasNext()) {
				worker = nextWorker();
				sit = getSlotIterator();
			// otherwise check the next idle slot
			} else {
				IdleSlot candidate = sit.next();

				// break if the current idle slot is accepted
				if (check(worker, candidate)) {
					slot = candidate;
					break;
				}
			}
		}

		// if there are no more valid idle slots
		if (slot == null)
			// #hasNext checks if #nextWorker is null
			setNextWorker(null);

		setNextSlot(slot);

		return slot;
	}

	/**
	 * Checks if a worker is able during a given idle slot to drive to a task
	 * location without violating any time constraints of the new task or
	 * the next task. It does not considers the presence of any obstacles to
	 * avoid.
	 *
	 * @param worker
	 * @param slot
	 * @return {@code true} iff worker can potentially execute the task in time.
	 */
	private boolean check(WorkerUnit worker, IdleSlot slot) {
		Duration d = getDuration();
		double vInv = 1. / worker.getMaxSpeed();
		Point p = getLocation();
		LocalDateTime earliest = getEarliestStartTime();
		LocalDateTime latest = getLatestStartTime();
		LocalDateTime t1 = slot.getStartTime();
		LocalDateTime t2 = slot.getFinishTime();
		Point p1 = slot.getStartLocation();
		Point p2 = slot.getFinishLocation();
		double l1 = distance(p1, p);
		double l2 = p2 == null ? 0. : distance(p, p2);

		// task can be started in time
		// t_max - t1 < l1 / v_max
		if (Duration.between(t1, latest).compareTo(
			ofSeconds(vInv * l1)) < 0)
		{
			return false;
		}
		// task can be finished in time
		// t2 - t_min < l2 / v_max + d
		if (Duration.between(earliest, t2).compareTo(
			ofSeconds(vInv * l2).plus(d)) < 0)
		{
			return false;
		}
		// enough time to complete task
		// t2 - t1 < (l1 + l2) / v_max + d
		if (Duration.between(t1, t2).compareTo(
			ofSeconds(vInv * (l1 + l2)).plus(d)) < 0)
		{
			return false;
		}

		return true;
	}

}
