package tasks;

import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import com.vividsolutions.jts.geom.Point;

/**
 * <p>The IdleSlot class represents the idle time of a {@link WorkerUnit} while
 * not being occupied by a task. This also includes the time to drive from
 * one task to the next task.</p>
 *
 * <p>An object of this class stores a quadruple containing the start and end time
 * of this IdleSlot and the locations of the worker at those times. The time
 * interval should always be as large as possible, i.e., the worker was
 * occupied right before the idle slot started and is occupied again immediately
 * after the slot ended. The two exceptions to this are the times before the
 * initialization of the worker and the end of all time after which no task
 * can be scheduled.</p>
 *
 * @author Rico Jasper
 */
public class IdleSlot {

	/**
	 * The location before idling.
	 */
	private final Point startLocation;

	/**
	 * The location after idling.
	 */
	private final Point finishLocation;

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
		Point startLocation,
		Point finishLocation,
		LocalDateTime startTime,
		LocalDateTime finishTime)
	{
		Objects.requireNonNull(startLocation, "startLocation");
		Objects.requireNonNull(finishLocation, "finishLocation");
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(finishTime, "finishTime");

		if (startLocation.isEmpty() || !startLocation.isValid())
			throw new IllegalArgumentException("illegal startLocation");
		if (finishLocation.isEmpty() || !finishLocation.isValid())
			throw new IllegalArgumentException("illegal finishLocation");
		if (startTime.compareTo(finishTime) >= 0)
			throw new IllegalArgumentException("startTime is after finishTime");

		this.startLocation = immutable(startLocation);
		this.finishLocation = immutable(finishLocation);
		this.startTime = startTime;
		this.finishTime = finishTime;
	}

	/**
	 * @return the location before idling.
	 */
	public Point getStartLocation() {
		return startLocation;
	}

	/**
	 * @return the location after idling.
	 */
	public Point getFinishLocation() {
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
		return String.format("((%s, %s), (%s, %s))",
			getStartLocation(), getFinishLocation(), getStartTime(), getFinishTime());
	}

}
