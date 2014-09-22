package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

import com.vividsolutions.jts.geom.Point;

public class Task {
	
	private final Point location;
	
	private final LocalDateTime startTime;
	
	private final LocalDateTime finishTime;
	
	private final Duration duration;
	
	public Task(Point location, LocalDateTime startTime, LocalDateTime finishTime) {
		if (startTime.compareTo(finishTime) > 0)
			throw new IllegalArgumentException("start time cannot be after finish time");
		
		this.location = location;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.duration = Duration.between(startTime, finishTime);
	}
	
	public Task(Point location, LocalDateTime startTime, Duration duration) {
		if (duration.isNegative())
			throw new IllegalArgumentException("duration has to be non-negative");
		
		this.location = location;
		this.startTime = startTime;
		this.finishTime = startTime.plus(duration);
		this.duration = duration;
	}

	public Point getLocation() {
		return location;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getFinishTime() {
		return finishTime;
	}

	public Duration getDuration() {
		return duration;
	}

}
