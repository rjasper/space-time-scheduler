package pickers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.vividsolutions.jts.geom.Point;

import tasks.IdleSlot;
import tasks.WorkerUnit;

import static java.lang.Math.ceil;
import static com.vividsolutions.jts.operation.distance.DistanceOp.distance;

public class WorkerUnitSlotPicker {
	
	private final Collection<WorkerUnit> workers;
	
	private final Point location;
	
	private final LocalDateTime earliestStartTime;
	
	private final LocalDateTime latestStartTime;
	
	private final Duration duration;
	
	private Iterator<WorkerUnit> workerIterator;
	
	private Iterator<IdleSlot> slotIterator;
	
	private WorkerUnit nextWorker = null;
	
	private IdleSlot nextSlot = null;
	
	private WorkerUnit currentWorker = null;
	
	private IdleSlot currentSlot = null;

	public WorkerUnitSlotPicker(
		Collection<WorkerUnit> workers,
		Point location,
		LocalDateTime earliestStartTime, LocalDateTime latestStartTime,
		Duration duration)
	{
		this.workers = Collections.unmodifiableCollection(workers);
		this.location = location;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.duration = duration;
		
		this.workerIterator = workers.iterator();
		
		nextWorker();
		nextSlot();
	}
	
	public boolean hasNext() {
		return getNextWorker() != null;
	}

	private Collection<WorkerUnit> getWorkers() {
		return workers;
	}

	private Point getLocation() {
		return location;
	}

	private LocalDateTime getEarliestStartTime() {
		return earliestStartTime;
	}

	private LocalDateTime getLatestStartTime() {
		return latestStartTime;
	}

	private Duration getDuration() {
		return duration;
	}

	private Iterator<WorkerUnit> getWorkerIterator() {
		return workerIterator;
	}

	private WorkerUnit getNextWorker() {
		return nextWorker;
	}

	private void setNextWorker(WorkerUnit nextWorker) {
		this.nextWorker = nextWorker;
	}

	private IdleSlot getNextSlot() {
		return nextSlot;
	}

	private void setNextSlot(IdleSlot nextSlot) {
		this.nextSlot = nextSlot;
	}

	private Iterator<IdleSlot> getSlotIterator() {
		return slotIterator;
	}

	private void setSlotIterator(Iterator<IdleSlot> slotIterator) {
		this.slotIterator = slotIterator;
	}

	public WorkerUnit getCurrentWorker() {
		return currentWorker;
	}

	private void setCurrentWorker(WorkerUnit currentWorker) {
		this.currentWorker = currentWorker;
	}

	public IdleSlot getCurrentSlot() {
		return currentSlot;
	}

	private void setCurrentSlot(IdleSlot currentSlot) {
		this.currentSlot = currentSlot;
	}

	private void setWorkerIterator(Iterator<WorkerUnit> workerIterator) {
		this.workerIterator = workerIterator;
	}

	public void next() {
		setCurrentWorker(getNextWorker());
		setCurrentSlot(getNextSlot());
		
		nextSlot();
	}
	
	private WorkerUnit nextWorker() {
		LocalDateTime earliest = getEarliestStartTime();
		LocalDateTime latest = getLatestStartTime();
		
		Iterator<WorkerUnit> it = getWorkerIterator();
		WorkerUnit worker = it.next();
		Collection<IdleSlot> slots = worker.idleSubSet(earliest, latest);

		setNextWorker(worker);
		setSlotIterator(slots.iterator());
		
		return worker;
	}
	
	private IdleSlot nextSlot() {
		Iterator<WorkerUnit> wit = getWorkerIterator();
		Iterator<IdleSlot> sit = getSlotIterator();
		
		WorkerUnit worker = getNextWorker();
		IdleSlot slot = null;
		
		while (wit.hasNext() || sit.hasNext()) {
			if (!sit.hasNext()) {
				worker = nextWorker();
				sit = getSlotIterator();
			} else {
				IdleSlot candidate = sit.next();
				
				if (check(worker, candidate)) {
					slot = candidate;
					break;
				}
			}
		}
		
		if (slot == null)
			setNextWorker(null);
		setNextSlot(slot);
		
		return slot;
	}
	
	private boolean check(WorkerUnit worker, IdleSlot s) {
		Duration d = getDuration();
		double vInv = 1. / worker.getMaxSpeed(); // TODO: repeating calculation
		Point p = getLocation();
		LocalDateTime earliest = getEarliestStartTime();
		LocalDateTime latest = getLatestStartTime();
		LocalDateTime t1 = s.getStartTime();
		LocalDateTime t2 = s.getFinishTime();
		Point p1 = s.getStartLocation();
		Point p2 = s.getFinishLocation();
		double l1 = distance(p1, p); // TODO: repeating calculation
		double l2 = p2 == null ? 0. : distance(p, p2); // TODO: repeating calculation

		// task can be started in time
		// t_max - t1 < l1 / v_max
		if (Duration.between(t1, latest)
				.compareTo(Duration.ofSeconds((long) ceil(vInv * l1))) < 0)
			return false;
		// task can be finished in time
		// t2 - t_min < l2 / v_max
		if (p2 != null && Duration.between(earliest, t2)
				.compareTo(Duration.ofSeconds((long) ceil(vInv * l2))) < 0)
			return false;
		// enough time to complete task
		// t2 - t1 < d + (l1 + l2) / v_max
		if (p2 != null && Duration.between(t1, t2)
				.compareTo(d.plusSeconds((long) ceil(vInv * (l1 + l2)))) < 0)
			return false;
		
		return true;
	}
	
}
