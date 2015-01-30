package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.util.GeometriesRequire;
import util.NameProvider;

/**
 * <p>A Task represents the smallest assignment of a {@link WorkerUnit} that the
 * {@link Scheduler} can manage. Each task is assigned to exactly one
 * worker.</p>
 *
 * <p>An object of this class stores the location, start time, finish time, and
 * duration of the execution of the task.</p>
 *
 * @author Rico Jasper
 */
public class Task {
	
	/**
	 * The ID of this task.
	 */
	private final UUID id;

	/**
	 * The location where the task is executed.
	 */
	private final ImmutablePoint location;

	/**
	 * The time when the execution starts.
	 */
	private final LocalDateTime startTime;

	/**
	 * The time when the execution ends.
	 */
	private final LocalDateTime finishTime;

	/**
	 * The duration of the execution.
	 */
	private final Duration duration;

	/**
	 * Helper constructor checking all arguments and initializing all fields.
	 * Does not check consistency of duration.
	 * @param id
	 * @param location
	 * @param startTime
	 * @param finishTime
	 * @param duration
	 *
	 * @throws NullPointerException
	 *             if any argument is null
	 * @throws IllegalArgumentException
	 *             if any of the following is true:
	 *             <ul>
	 *             <li>The location is empty or invalid.</li>
	 *             <li>The startTime is after the finishTime.</li>
	 *             <li>The duration is negative.</li>
	 *             </ul>
	 */
	private Task(UUID id, ImmutablePoint location, LocalDateTime startTime, LocalDateTime finishTime, Duration duration) {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(finishTime, "finishTime");
		Objects.requireNonNull(duration, "duration");
		GeometriesRequire.requireValid2DPoint(location, "location");

		if (startTime.compareTo(finishTime) > 0)
			throw new IllegalArgumentException("startTime is after finishTime");
		if (duration.isNegative())
			throw new IllegalArgumentException("negative duration");

		this.id = id;
		this.location = location;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.duration = duration;
	}

	/**
	 * Constructs a Task where the duration is derived from the start and
	 * finish time.
	 * 
	 * @param id
	 * @param location
	 * @param startTime
	 * @param finishTime
	 */
	public Task(UUID id, ImmutablePoint location, LocalDateTime startTime, LocalDateTime finishTime) {
		this(id, location, startTime, finishTime, Duration.between(startTime, finishTime));
	}

	/**
	 * Constructs a Task where the finish time is derived from the start time
	 * and duration.
	 * 
	 * @param id
	 * @param location
	 * @param startTime
	 * @param duration
	 */
	public Task(UUID id, ImmutablePoint location, LocalDateTime startTime, Duration duration) {
		this(id, location, startTime, startTime.plus(duration), duration);
	}

	/**
	 * @return the id of this task.
	 */
	public UUID getId() {
		return id;
	}

	/**
	 *
	 * @return the location where the task is executed.
	 */
	public ImmutablePoint getLocation() {
		return location;
	}

	/**
	 * @return the time when the execution starts.
	 */
	public LocalDateTime getStartTime() {
		return startTime;
	}

	/**
	 * @return the time when the execution ends.
	 */
	public LocalDateTime getFinishTime() {
		return finishTime;
	}

	/**
	 * @return the duration of the execution.
	 */
	public Duration getDuration() {
		return duration;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// lazy evaluation
		Supplier<String> defaultString = () -> String.format("(%s, %s, %s)",
			getLocation(), getStartTime(), getFinishTime());

		return NameProvider.nameForOrDefault(this, defaultString);
	}

}
