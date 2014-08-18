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

public class WorkerUnitPicker {
	
	private final Collection<WorkerUnit> workers;
	
	private final Point location;
	
	private final LocalDateTime earliestStartTime;
	
	private final LocalDateTime latestStartTime;
	
	private final Duration duration;
	
	private Iterator<WorkerUnit> workerIterator;
	
	private WorkerUnit nextWorker;

	public WorkerUnitPicker(
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
		
		calcNextWorker();
	}
	
	public boolean isDone() {
		return getNextWorker() == null;
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

	public WorkerUnit next() {
		WorkerUnit current = getNextWorker();
		
		calcNextWorker();
		
		return current;
	}
	
	private void calcNextWorker() {
		WorkerUnit worker = null;
		Iterator<WorkerUnit> it = getWorkerIterator();
		
		while (it.hasNext()) {
			WorkerUnit candidate = it.next();
			
			if (check(candidate)) {
				worker = candidate;
				break;
			}
		}
		
		setNextWorker(worker);
	}
	
	private boolean check(WorkerUnit worker) {
		LocalDateTime earliest = getEarliestStartTime();
		LocalDateTime latest = getLatestStartTime();
		Collection<IdleSlot> slots = worker.idleSubSet(earliest, latest);
		
		Duration d = getDuration();
		double vInv = 1. / worker.getMaxSpeed();
		Point p = getLocation();
		
		for (IdleSlot s : slots) {
			LocalDateTime t1 = s.getStartTime();
			LocalDateTime t2 = s.getFinishTime();
			Point p1 = s.getStartLocation();
			Point p2 = s.getFinishLocation();
			double l1 = distance(p1, p);
			double l2 = p2 == null ? 0. : distance(p, p2);

			// task can be started in time
			// t_max - t1 < l1 / v_max
			if (Duration.between(t1, latest)
					.compareTo(Duration.ofSeconds((long) ceil(vInv * l1))) < 0)
				continue;
			// task can be finished in time
			// t2 - t_min < l2 / v_max
			if (p2 != null && Duration.between(earliest, t2)
					.compareTo(Duration.ofSeconds((long) ceil(vInv * l2))) < 0)
				continue;
			// enough time to do task
			// t2 - t1 < d + (l1 + l2) / v_max
			if (p2 != null && Duration.between(t1, t2)
					.compareTo(d.plusSeconds((long) ceil(vInv * (l1 + l2)))) < 0)
				continue;
			
			return true;
		}
		
		return false;
	}
	
}
