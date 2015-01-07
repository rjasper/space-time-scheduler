package world.pathfinder;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The {@code FixTimeVelocityPathfinder} extends the {@link VelocityPathfinder}
 * by configurable parameters specific for fixated start and finish times.
 * 
 * @author Rico
 */
public abstract class FixTimeVelocityPathfinder extends VelocityPathfinder {
	
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
	 * @see world.pathfinder.VelocityPathfinder#isReady()
	 */
	@Override
	public boolean isReady() {
		// TODO check start < finish
		
		return super.isReady()
			&& startTime != null
			&& finishTime != null;
	}
	
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

}
