package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The {@code AbstractMinimumTimePathfinder} extends the {@link AbstractVelocityPathfinder}
 * by configurable parameters specific for determining time minimal paths.
 * 
 * @author Rico
 */
public abstract class AbstractMinimumTimePathfinder extends AbstractVelocityPathfinder {
	
	/**
	 * The start time.
	 */
	private LocalDateTime startTime = null;
	
	/**
	 * The earliest finish time.
	 */
	private LocalDateTime earliestFinishTime = null;
	
	/**
	 * The latest finish time.
	 */
	private LocalDateTime latestFinishTime = null;
	
	/**
	 * The duration ahead of the finish vertex without colliding with dynamic
	 * obstacles.
	 */
	private Duration bufferDuration = null;
	
	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.VelocityPathfinder#getBaseTime()
	 */
	@Override
	protected LocalDateTime getBaseTime() {
		return getStartTime();
	}

	/**
	 * @return the start time.
	 */
	public LocalDateTime getStartTime() {
		return startTime;
	}

	/**
	 * Sets the start time.
	 * 
	 * @param startTime
	 * @throws NullPointerException
	 *             if startTime is {@code null}.
	 */
	public void setStartTime(LocalDateTime startTime) {
		this.startTime = Objects.requireNonNull(startTime, "startTime");
	}

	/**
	 * @return the earliest finish time.
	 */
	protected LocalDateTime getEarliestFinishTime() {
		return earliestFinishTime;
	}

	/**
	 * Sets the earliest finish time.
	 * 
	 * @param earliestFinishTime
	 * @throws NullPointerException
	 *             if earliestFinishTime is {@code null}.
	 */
	public void setEarliestFinishTime(LocalDateTime earliestFinishTime) {
		this.earliestFinishTime =
			Objects.requireNonNull(earliestFinishTime, "earliestFinishTime");
	}

	/**
	 * @return the latest finish time.
	 */
	protected LocalDateTime getLatestFinishTime() {
		return latestFinishTime;
	}

	/**
	 * Sets the latest finish time.
	 * 
	 * @param latestFinishTime
	 * @throws NullPointerException
	 *             if latestFinishTime is {@code null}.
	 */
	public void setLatestFinishTime(LocalDateTime latestFinishTime) {
		this.latestFinishTime =
			Objects.requireNonNull(latestFinishTime, "latestFinishTime");
	}

	/**
	 * @return the buffer duration.
	 */
	protected Duration getBufferDuration() {
		return bufferDuration;
	}

	/**
	 * <p>
	 * Sets the buffer duration.
	 * </p>
	 * 
	 * <p>
	 * For at least the buffer duration there will be no collision with any
	 * dynamic obstacles at the finish of the path.
	 * </p>
	 * 
	 * @param bufferDuration
	 * @throws NullPointerException
	 *             if bufferDuratioin is {@code null}.
	 * @throws IllegalArgumentException
	 *             if bufferDuration is negative.
	 */
	public void setBufferDuration(Duration bufferDuration) {
		Objects.requireNonNull(bufferDuration, "bufferDuration");
		
		if (bufferDuration.isNegative())
			throw new IllegalArgumentException("bufferDuration cannot be negative");
		
		this.bufferDuration = bufferDuration;
	}
	
	/**
	 * Checks if all parameters are properly set. Throws an exception otherwise.
	 * 
	 * @throws IllegalStateException
	 *             if any parameter is not set or {@code earliestFinishTime} is
	 *             after latest {@code latestFinishTime}.
	 */
	@Override
	protected void checkParameters() {
		super.checkParameters();
		
		if (startTime          == null ||
			earliestFinishTime == null ||
			latestFinishTime   == null ||
			bufferDuration     == null)
		{
			throw new IllegalStateException("some parameters are not set");
		}
		
		if (earliestFinishTime.compareTo(latestFinishTime) > 0)
			throw new IllegalStateException("earliestFinishTime is after latestFinishTime");
	}

}
