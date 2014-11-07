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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((finishLocation == null) ? 0 : finishLocation.hashCode());
		result = prime * result
			+ ((finishTime == null) ? 0 : finishTime.hashCode());
		result = prime * result
			+ ((startLocation == null) ? 0 : startLocation.hashCode());
		result = prime * result
			+ ((startTime == null) ? 0 : startTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdleSlot other = (IdleSlot) obj;
		if (finishLocation == null) {
			if (other.finishLocation != null)
				return false;
		} else if (!finishLocation.equals(other.finishLocation))
			return false;
		if (finishTime == null) {
			if (other.finishTime != null)
				return false;
		} else if (!finishTime.equals(other.finishTime))
			return false;
		if (startLocation == null) {
			if (other.startLocation != null)
				return false;
		} else if (!startLocation.equals(other.startLocation))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "IdleSlot [startLocation=" + startLocation + ", finishLocation="
				+ finishLocation + ", startTime=" + startTime + ", finishTime="
				+ finishTime + "]";
	}

}
