package pickers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import pickers.WorkerUnitSlotIterator.WorkerUnitSlot;

import com.vividsolutions.jts.geom.Point;

import tasks.WorkerUnit;

public class WorkerUnitSlotIteratorFactory implements Iterable<WorkerUnitSlot> {
	
	private Collection<WorkerUnit> workers;
	
	private Point location;
	
	private LocalDateTime earliestStartTime;
	
	private LocalDateTime latestStartTime;
	
	private Duration duration;

	public WorkerUnitSlotIteratorFactory(
		Collection<WorkerUnit> workers,
		Point location,
		LocalDateTime earliestStartTime, LocalDateTime latestStartTime,
		Duration duration)
	{
		this.workers = new ArrayList<>(workers);
		this.location = location;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.duration = duration;
	}

	@Override
	public Iterator<WorkerUnitSlot> iterator() {
		return new WorkerUnitSlotIterator(workers, location, earliestStartTime, latestStartTime, duration);
	}
	
}
