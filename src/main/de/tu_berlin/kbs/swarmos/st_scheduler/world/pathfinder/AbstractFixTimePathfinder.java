package de.tu_berlin.kbs.swarmos.st_scheduler.world.pathfinder;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The {@code AbstractFixTimePathfinder} extends the {@link AbstractVelocityPathfinder}
 * by configurable parameters specific for fixated start and finish times.
 * 
 * @author Rico
 */
public abstract class AbstractFixTimePathfinder extends AbstractVelocityPathfinder {
	
	/**
	 * The start time.
	 */
	private LocalDateTime startTime = null;
	
	/**
	 * The finish time.
	 */
	private LocalDateTime finishTime = null;

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
	protected LocalDateTime getStartTime() {
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
		this.startTime = Objects.requireNonNull(startTime);
	}

	/**
	 * @return the finish time.
	 */
	protected LocalDateTime getFinishTime() {
		return finishTime;
	}

	/**
	 * Sets the finish time.
	 * 
	 * @param finishTime
	 * @throws NullPointerException
	 *             if finishTime is {@code null}.
	 */
	public void setFinishTime(LocalDateTime finishTime) {
		this.finishTime = Objects.requireNonNull(finishTime);
	}
	
	/**
	 * Checks if all parameters are properly set. Throws an exception otherwise.
	 * 
	 * @throws IllegalStateException
	 *             if any parameter is not set.
	 */
	@Override
	protected void checkParameters() {
		super.checkParameters();
		
		if (startTime  == null || finishTime == null)
			throw new IllegalStateException("some parameters are not set");
	}

}
