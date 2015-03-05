package scheduler.pickers;

import static com.vividsolutions.jts.operation.distance.DistanceOp.*;
import static java.util.Collections.*;
import static util.Comparables.*;
import static util.TimeConv.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import jts.geom.util.GeometriesRequire;
import scheduler.IdleSlot;
import scheduler.WorkerUnit;

import com.vividsolutions.jts.geom.Point;

// TODO document
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
	
	private final LocalDateTime frozenHorizonTime;

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
		Iterable<WorkerUnit> workers,
		LocalDateTime frozenHorizonTime,
		Point location,
		LocalDateTime earliestStartTime,
		LocalDateTime latestStartTime,
		Duration duration)
	{
		Objects.requireNonNull(workers, "workers");
		Objects.requireNonNull(frozenHorizonTime, "frozenHorizon");
		Objects.requireNonNull(location, "location");
		Objects.requireNonNull(earliestStartTime, "earliestStartTime");
		Objects.requireNonNull(latestStartTime, "latestStartTime");
		Objects.requireNonNull(duration, "duration");
		
		GeometriesRequire.requireValid2DPoint(location, "location");

		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalArgumentException("earliestStartTime is after latestStartTime");
		if (duration.isNegative())
			throw new IllegalArgumentException("duration is negative");

		this.workerIterator = workers.iterator();
		this.frozenHorizonTime = frozenHorizonTime;
		this.location = location;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.duration = duration;

		// The next worker and idle slot pair is calculated before they are
		// requested. This enables an easy check whether or not there is a next
		// pair.
		if (!frozenHorizonTime.isAfter(latestStartTime)) {
			nextWorker();
			nextSlot();
		}
	}

	@Override
	public boolean hasNext() {
		return nextWorker != null;
	}

	/**
	 * @return the current worker of the iteration.
	 */
	public WorkerUnit getCurrentWorker() {
		return currentWorker;
	}

	/**
	 * @return the current slot of the iteration.
	 */
	public IdleSlot getCurrentSlot() {
		return currentSlot;
	}
	
	private LocalDateTime earliestStartTime(WorkerUnit worker) {
		return max(earliestStartTime, frozenHorizonTime, worker.getInitialTime());
	}
	
	private LocalDateTime latestStartTime() {
		return latestStartTime;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public WorkerUnitSlot next() {
		if (!hasNext())
			throw new NoSuchElementException();
		
		currentWorker = nextWorker;
		currentSlot = nextSlot;

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

		WorkerUnit worker;
		LocalDateTime from, to;
		do {
			if (!workerIterator.hasNext()) {
				worker = null;
				from = null;
				to = null;
				
				break;
			}
			
			worker = workerIterator.next();
			
			LocalDateTime earliest = earliestStartTime(worker);
			LocalDateTime latest = latestStartTime();
			LocalDateTime floorIdle = worker.floorIdleTimeOrNull(earliest);
			LocalDateTime ceilIdle = worker.ceilingIdleTimeOrNull(latest);
			
			from = max(
				floorIdle != null ? floorIdle : earliest,
				frozenHorizonTime);
			to = ceilIdle  != null ? ceilIdle  : latest;
		} while (from.isAfter(to));
		
		Collection<IdleSlot> slots = worker == null // indicates loop break
			? emptyList()
			: worker.idleSlots(from, to);

		nextWorker = worker;
		slotIterator = slots.iterator();

		return worker;
	}

	/**
	 * Determines the next idle slot of the iteration.
	 *
	 * @return the next idle slot.
	 */
	private IdleSlot nextSlot() {
		WorkerUnit worker = nextWorker;
		IdleSlot slot = null;

		// iterates over the remaining idle slots of the remaining workers
		// until a valid slot was found
		while (workerIterator.hasNext() || slotIterator.hasNext()) {
			// if there are no more idle slots of the current worker
			// then get the next one
			if (!slotIterator.hasNext()) {
				worker = nextWorker();
			// otherwise check the next idle slot
			} else {
				IdleSlot candidate = slotIterator.next();

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
			nextWorker = null;

		nextSlot = slot;

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
		double vInv = 1. / worker.getMaxSpeed();
		LocalDateTime t1 = slot.getStartTime();
		LocalDateTime t2 = slot.getFinishTime();
		Point p1 = slot.getStartLocation();
		Point p2 = slot.getFinishLocation();
		double l1 = distance(p1, location);
		double l2 = p2 == null ? 0. : distance(location, p2);

		// task can be started in time
		// t_max - t1 < l1 / v_max
		if (Duration.between(t1, latestStartTime()).compareTo(
			secondsToDuration(vInv * l1)) < 0)
		{
			return false;
		}
		// task can be finished in time
		// t2 - t_min < l2 / v_max + d
		if (Duration.between(earliestStartTime(worker), t2).compareTo(
			secondsToDuration(vInv * l2).plus(duration)) < 0)
		{
			return false;
		}
		// enough time to complete task
		// t2 - t1 < (l1 + l2) / v_max + d
		if (Duration.between(t1, t2).compareTo(
			secondsToDuration(vInv * (l1 + l2)).plus(duration)) < 0)
		{
			return false;
		}

		return true;
	}

}
