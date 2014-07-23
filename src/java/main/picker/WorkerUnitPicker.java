package picker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import com.vividsolutions.jts.geom.Point;

import tasks.WorkerUnit;

public class WorkerUnitPicker {
	
	private final Collection<WorkerUnit> workers;
	
	private final Point location;
	
	private final LocalDateTime earliestStartTime;
	
	private final LocalDateTime latestStartTime;
	
	private final Duration duration;

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
	}
	
	public boolean isDone() {
		// TODO: implement
		
		return true;
	}

	public WorkerUnit next() {
		// TODO: implement
		
		return null;
	}
}
