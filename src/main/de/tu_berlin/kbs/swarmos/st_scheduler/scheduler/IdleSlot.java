package de.tu_berlin.kbs.swarmos.st_scheduler.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.util.GeometriesRequire;

/**
 * <p>The IdleSlot class represents the idle time of a {@link Node} while
 * not being occupied by a job. This also includes the time to drive from
 * one job to the next job.</p>
 *
 * <p>An object of this class stores a quadruple containing the start and end time
 * of this IdleSlot and the locations of the node at those times. The time
 * interval should always be as large as possible, i.e., the node was
 * occupied right before the idle slot started and is occupied again immediately
 * after the slot ended. The two exceptions to this are the times before the
 * initialization of the node and the end of all time after which no job
 * can be scheduled.</p>
 *
 * @author Rico Jasper
 */
public class IdleSlot {

	/**
	 * The location before idling.
	 */
	private final ImmutablePoint startLocation;

	/**
	 * The location after idling.
	 */
	private final ImmutablePoint finishLocation;

	/**
	 * The time before idling.
	 */
	private final LocalDateTime startTime;

	/**
	 * The time after idling.
	 */
	private final LocalDateTime finishTime;

	/**
	 * The cached duration of the idling.
	 */
	private transient Duration duration = null;

	/**
	 * Constructs a new IdleSlot specified by the time interval and locations
	 * of the start and end.
	 *
	 * @param startLocation
	 * @param finishLocation
	 * @param startTime
	 * @param finishTime
	 * @throws NullPointerException
	 *             if any of the arguments is {@code null}
	 * @throws IllegalArgumentException
	 *             if locations are empty or invalid or
	 *             the startTime is equal to or before the finishTime
	 */
	public IdleSlot(
		ImmutablePoint startLocation,
		ImmutablePoint finishLocation,
		LocalDateTime startTime,
		LocalDateTime finishTime)
	{
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(finishTime, "finishTime");
		GeometriesRequire.requireValid2DPoint(startLocation, "startLocation");
		GeometriesRequire.requireValid2DPoint(finishLocation, "finishLocation");
		
		if (startTime.compareTo(finishTime) >= 0)
			throw new IllegalArgumentException("startTime is after finishTime");

		this.startLocation = startLocation;
		this.finishLocation = finishLocation;
		this.startTime = startTime;
		this.finishTime = finishTime;
	}

	/**
	 * @return the location before idling.
	 */
	public ImmutablePoint getStartLocation() {
		return startLocation;
	}

	/**
	 * @return the location after idling.
	 */
	public ImmutablePoint getFinishLocation() {
		return finishLocation;
	}

	/**
	 * @return the time before idling.
	 */
	public LocalDateTime getStartTime() {
		return startTime;
	}

	/**
	 * @return the time after idling.
	 */
	public LocalDateTime getFinishTime() {
		return finishTime;
	}

	/**
	 * Calculates the duration between {@link #getStartTime()} and
	 * {@link #getFinishTime()}. Caches the result so further recalculations
	 * are avoided.
	 *
	 * @return the duration of the idling.
	 */
	public Duration getDuration() {
		if (duration == null)
			duration = Duration.between(getStartTime(), getFinishTime());

		return duration;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("((%s, %s), (%s, %s))",
			getStartLocation(), getFinishLocation(), getStartTime(), getFinishTime());
	}

}
