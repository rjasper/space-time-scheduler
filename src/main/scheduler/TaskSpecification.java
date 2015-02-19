package scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import jts.geom.immutable.ImmutableGeometry;
import jts.geom.util.GeometriesRequire;
import util.NameProvider;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>An object of this class describes the specifications of a task.</p>
 *
 * <p>To schedule a new task it is required to pass a specification to the
 * {@link Scheduler}. It will try to find a realizable configuration of a task
 * which satisfies the specification.</p>
 *
 * <p>A specification describes when and where a task has to be executed.
 * The locationSpace defines the area of valid locations. The earliestStartTime
 * and latestStartTime define an interval when it is possible to start a task.
 * The duration defines the exact duration of the task.</p>
 *
 * @author Rico Jasper
 */
public class TaskSpecification {
	
	/**
	 * The ID of the task.
	 */
	private final UUID taskId;

	/**
	 * The spatial space for a valid location.
	 */
	private final Geometry locationSpace;

	/**
	 * The earliest possible start time.
	 */
	private final LocalDateTime earliestStartTime;

	/**
	 * The latest possible start time.
	 */
	private final LocalDateTime latestStartTime;

	/**
	 * The exact duration.
	 */
	private final Duration duration;

	/**
	 * Constructs a new Specification defining an interval for the location and
	 * start time and the duration of a {@link Task task}.
	 * 
	 * @param taskId
	 * @param locationSpace
	 * @param earliestStartTime
	 * @param latestStartTime
	 * @param duration
	 *
	 * @throws NullPointerException
	 *             if any argument is null
	 * @throws IllegalArgumentException
	 *             if any of the following is true:
	 *             <ul>
	 *             <li>the locationSpace is empty, non-simple, or invalid</li>
	 *             <li>the earliestStartTime is after the latestStartTime</li>
	 *             <li>the duration is negative or zero</li>
	 *             </ul>
	 */
	public <G extends Geometry & ImmutableGeometry> TaskSpecification(
		UUID taskId,
		G locationSpace,
		LocalDateTime earliestStartTime,
		LocalDateTime latestStartTime,
		Duration duration)
	{
		Objects.requireNonNull(taskId, "taskId");
		Objects.requireNonNull(earliestStartTime, "earliestStartTime");
		Objects.requireNonNull(latestStartTime, "latestStartTime");
		Objects.requireNonNull(duration, "duration");
		GeometriesRequire.requireValidSimple2DGeometry(locationSpace, "locationSpace");

		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalArgumentException("earliestStartTime is after latestStartTime");
		if (duration.isNegative() || duration.isZero())
			throw new IllegalArgumentException("illegal duration");

		this.taskId = taskId;
		this.locationSpace = locationSpace;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.duration = duration;
	}

	/**
	 * @return the id of the task.
	 */
	public final UUID getTaskId() {
		return taskId;
	}

	/**
	 * @return the immutable spatial space for a valid {@link Point location}.
	 */
	public final Geometry getLocationSpace() {
		return locationSpace;
	}

	/**
	 * @return the earliest possible start time.
	 */
	public final LocalDateTime getEarliestStartTime() {
		return earliestStartTime;
	}

	/**
	 * @return the latest possible start time.
	 */
	public final LocalDateTime getLatestStartTime() {
		return latestStartTime;
	}

	/**
	 * @return the exact duration of a task.
	 */
	public final Duration getDuration() {
		return duration;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// lazy evaluation
		Supplier<String> defaultString = () -> String.format("(%s, %s, %s, %s)",
			getLocationSpace(), getEarliestStartTime(), getLatestStartTime(), getDuration());

		return NameProvider.nameForOrDefault(this, defaultString);
	}

}
