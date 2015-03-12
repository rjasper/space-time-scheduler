package scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.util.GeometriesRequire;
import util.NameProvider;

/**
 * <p>A Job represents the smallest assignment of a {@link Node} that the
 * {@link Scheduler} can manage. Each job is assigned to exactly one
 * node.</p>
 *
 * <p>An object of this class stores the location, start time, finish time, and
 * duration of the execution of the job.</p>
 *
 * @author Rico Jasper
 */
public class Job {
	
	/**
	 * The ID of this job.
	 */
	private final UUID id;
	
	/**
	 * The node assigned to this job.
	 */
	private final NodeReference assignedNode;

	/**
	 * The location where the job is executed.
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
	 * Constructs a Job where the finish time is derived from the start time
	 * and duration.
	 * 
	 * @param id
	 * @param assignedNode
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
	public Job(
		UUID id,
		NodeReference assignedNode,
		ImmutablePoint location,
		LocalDateTime startTime,
		Duration duration)
	{
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(assignedNode, "assignedNode");
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(duration, "duration");
		GeometriesRequire.requireValid2DPoint(location, "location");

		if (duration.isZero() || duration.isNegative())
			throw new IllegalArgumentException("invalid duration");

		this.id = id;
		this.assignedNode = assignedNode;
		this.location = location;
		this.startTime = startTime;
		this.finishTime = startTime.plus(duration);
		this.duration = duration;
	}

	/**
	 * @return the id of this job.
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @return the assigned node.
	 */
	public NodeReference getNodeReference() {
		return assignedNode;
	}

	/**
	 *
	 * @return the location where the job is executed.
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
			getNodeReference(), getLocation(), getStartTime(), getFinishTime());

		return NameProvider.nameForOrDefault(this, defaultString);
	}

}
