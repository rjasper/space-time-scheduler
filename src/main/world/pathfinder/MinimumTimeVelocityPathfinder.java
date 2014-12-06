package world.pathfinder;

import java.time.Duration;
import java.time.LocalDateTime;

public abstract class MinimumTimeVelocityPathfinder extends VelocityPathfinder {
	
	private LocalDateTime startTime = null;
	
	private LocalDateTime earliestFinishTime = null;
	
	private LocalDateTime latestFinishTime = null;
	
	private Duration bufferDuration = null;
	
	public boolean isReady() {
		return super.isReady()
			&& startTime != null
			&& earliestFinishTime != null
			&& latestFinishTime != null
			&& earliestFinishTime.compareTo(latestFinishTime) <= 0
			&& bufferDuration != null;
	}
	
	protected LocalDateTime getBaseTime() {
		return getStartTime();
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	protected LocalDateTime getEarliestFinishTime() {
		return earliestFinishTime;
	}

	public void setEarliestFinishTime(LocalDateTime earliestFinishTime) {
		this.earliestFinishTime = earliestFinishTime;
	}

	protected LocalDateTime getLatestFinishTime() {
		return latestFinishTime;
	}

	public void setLatestFinishTime(LocalDateTime latestFinishTime) {
		this.latestFinishTime = latestFinishTime;
	}

	protected Duration getBufferDuration() {
		return bufferDuration;
	}

	public void setBufferDuration(Duration bufferDuration) {
		if (bufferDuration.isNegative())
			throw new IllegalArgumentException("bufferDuration cannot be negative");
		
		this.bufferDuration = bufferDuration;
	}

}
