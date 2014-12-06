package tasks;

import java.time.Duration;
import java.time.LocalDateTime;

import com.vividsolutions.jts.geom.Polygon;

public class Specification {
	
	private final Polygon locationSpace;
	
	private final LocalDateTime earliestStartTime;
	
	private final LocalDateTime latestStartTime;
	
	private final Duration duration;

	public Specification(
		Polygon locationSpace,
		LocalDateTime earliestStartTime, LocalDateTime latestStartTime,
		Duration duration)
	{
		if (!locationSpace.isSimple())
			throw new IllegalArgumentException("location space must be simple");
		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalArgumentException("earliest time must be before latest time");
		if (duration.isNegative())
			throw new IllegalArgumentException("duration must be non-negative");
		
		this.locationSpace = locationSpace;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.duration = duration;
	}

	public Polygon getLocationSpace() {
		return locationSpace;
	}

	public LocalDateTime getEarliestStartTime() {
		return earliestStartTime;
	}

	public LocalDateTime getLatestStartTime() {
		return latestStartTime;
	}

	public Duration getDuration() {
		return duration;
	}

}
