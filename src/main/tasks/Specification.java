package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import util.NameProvider;

import com.vividsolutions.jts.geom.Polygon;

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
public class Specification {

	/**
	 * The spatial space for a valid location.
	 */
	private final Polygon locationSpace;

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
	 * start time and the duration of a task.
	 *
	 * @param locationSpace
	 * @param earliestStartTime
	 * @param latestStartTime
	 * @param duration
	 * @throws NullPointerException
	 *             if any argument is null
	 * @throws IllegalArgumentException
	 *             if any of the following is true:
	 *             <ul>
	 *             <li>the locationSpace is empty, non-simple, or invalid</li>
	 *             <li>the earliestStartTime is after the latestStartTime</li>
	 *             <li>the duration is negative</li>
	 *             </ul>
	 */
	public Specification(
		Polygon locationSpace,
		LocalDateTime earliestStartTime,
		LocalDateTime latestStartTime,
		Duration duration)
	{
		if (locationSpace == null)
			throw new NullPointerException("locationSpace is null");
		if (earliestStartTime == null)
			throw new NullPointerException("earliestStartTime is null");
		if (latestStartTime == null)
			throw new NullPointerException("latestStartTime is null");
		if (duration == null)
			throw new NullPointerException("duration is null");

		if (locationSpace.isEmpty() || !locationSpace.isSimple() || !locationSpace.isValid())
			throw new IllegalArgumentException("illegal locationSpace");
		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalArgumentException("earliestStartTime is after latestStartTime");
		if (duration.isNegative())
			throw new IllegalArgumentException("duration is negative");

		this.locationSpace = locationSpace;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.duration = duration;
	}

	/**
	 * @return the spatial space for a valid location.
	 */
	public Polygon getLocationSpace() {
		return locationSpace;
	}

	/**
	 * @return the earliest possible start time.
	 */
	public LocalDateTime getEarliestStartTime() {
		return earliestStartTime;
	}

	/**
	 * @return the latest possible start time.
	 */
	public LocalDateTime getLatestStartTime() {
		return latestStartTime;
	}

	/**
	 * @return the exact duration of a task.
	 */
	public Duration getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		// lazy evaluation
		Supplier<String> defaultString = () -> String.format("(%s, %s, %s)",
			getLocationSpace(), getEarliestStartTime(), getLatestStartTime());

		return NameProvider.nameForOrDefault(this, defaultString);
	}

}
