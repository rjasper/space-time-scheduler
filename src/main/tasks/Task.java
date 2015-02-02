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
	 * The worker assigned to this task.
	 */
	private final WorkerUnitReference assignedWorker;

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
	 * Constructs a Task where the finish time is derived from the start time
	 * and duration.
	 * 
	 * @param id
	 * @param assignedWorker
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
	public Task(
		UUID id,
		WorkerUnitReference assignedWorker,
		ImmutablePoint location,
		LocalDateTime startTime,
		Duration duration)
	{
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(assignedWorker, "assignedWorker");
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(duration, "duration");
		GeometriesRequire.requireValid2DPoint(location, "location");

		if (duration.isZero() || duration.isNegative())
			throw new IllegalArgumentException("invalid duration");

		this.id = id;
		this.assignedWorker = assignedWorker;
		this.location = location;
		this.startTime = startTime;
		this.finishTime = startTime.plus(duration);
		this.duration = duration;
	}

	/**
	 * @return the id of this task.
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @return the assigned worker.
	 */
	public WorkerUnitReference getAssignedWorker() {
		return assignedWorker;
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
		Supplier<String> defaultString = () -> String.format("%s:(%s, %s, %s)",
			getAssignedWorker(), getLocation(), getStartTime(), getFinishTime());

		return NameProvider.nameForOrDefault(this, defaultString);
	}

}
