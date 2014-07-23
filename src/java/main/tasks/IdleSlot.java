package tasks;

import java.time.LocalDateTime;

import com.vividsolutions.jts.geom.Point;

public class IdleSlot {
	
	private final Point startLocation;
	
	private final Point finishLocation;
	
	private final LocalDateTime startTime;
	
	private final LocalDateTime finishTime;

	public IdleSlot(
			Point startLocation, Point finishLocation,
			LocalDateTime startTime, LocalDateTime finishTime)
	{
		this.startLocation = startLocation;
		this.finishLocation = finishLocation;
		this.startTime = startTime;
		this.finishTime = finishTime;
	}

	public Point getStartLocation() {
		return startLocation;
	}

	public Point getFinishLocation() {
		return finishLocation;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getFinishTime() {
		return finishTime;
	}

	@Override
	public String toString() {
		return "IdleSlot [startLocation=" + startLocation + ", finishLocation="
				+ finishLocation + ", startTime=" + startTime + ", finishTime="
				+ finishTime + "]";
	}

}
