package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometry;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;
import de.tu_berlin.mailbox.rjasper.util.NameProvider;

/**
 * <p>An object of this class describes the specification of a job.</p>
 *
 * <p>To schedule a new job it is required to pass a specification to the
 * {@link Scheduler}. It will try to find a realizable configuration of a job
 * which satisfies the specification.</p>
 *
 * <p>A specification describes when and where a job has to be executed.
 * The locationSpace defines the area of valid locations. The earliestStartTime
 * and latestStartTime define an interval when it is possible to start a job.
 * The duration defines the exact duration of the job.</p>
 *
 * @author Rico Jasper
 */
public final class JobSpecification {
	
	/**
	 * Creates a JobSpecification defining an interval for the location and
	 * start time and the duration of a {@link Job job}.
	 * 
	 * @param jobId
	 * @param locationSpace
	 * @param earliestStartTime
	 * @param latestFinishTime
	 * @param duration
	 *
	 * @throws NullPointerException
	 *             if any argument is null
	 * @throws IllegalArgumentException
	 *             if any of the following is true:
	 *             <ul>
	 *             <li>the {@code locationSpace} is empty, non-simple, or invalid</li>
	 *             <li>the duration between {@code earliestStartTime} and
	 *             {@code latestStartTime} is smaller than the given
	 *             {@code duration}</li>
	 *             </ul>
	 * @return
	 */
	public static <G extends Geometry & ImmutableGeometry> JobSpecification createSF(
		UUID jobId,
		G locationSpace,
		LocalDateTime earliestStartTime,
		LocalDateTime latestFinishTime,
		Duration duration)
	{
		LocalDateTime latestStartTime = latestFinishTime.minus(duration);
		return new JobSpecification(jobId, locationSpace, earliestStartTime, latestStartTime, duration);
	}

	/**
	 * Creates a JobSpecification defining an interval for the location and
	 * start time and the duration of a {@link Job job}.
	 * 
	 * @param jobId
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
	 *             <li>the {@code locationSpace} is empty, non-simple, or invalid</li>
	 *             <li>the {@code earliestStartTime} is after the {@code latestStartTime}</li>
	 *             <li>the {@code duration} is negative or zero</li>
	 *             </ul>
	 * @return
	 */
	public static <G extends Geometry & ImmutableGeometry> JobSpecification createSS(
		UUID jobId,
		G locationSpace,
		LocalDateTime earliestStartTime,
		LocalDateTime latestStartTime,
		Duration duration)
	{
		return new JobSpecification(jobId, locationSpace, earliestStartTime, latestStartTime, duration);
	}

	/**
	 * Creates a JobSpecification defining an interval for the location and
	 * start time and the duration of a {@link Job job}.
	 * 
	 * @param jobId
	 * @param locationSpace
	 * @param earliestFinishTime
	 * @param latestFinishTime
	 * @param duration
	 *
	 * @throws NullPointerException
	 *             if any argument is null
	 * @throws IllegalArgumentException
	 *             if any of the following is true:
	 *             <ul>
	 *             <li>the {@code locationSpace} is empty, non-simple, or invalid</li>
	 *             <li>the {@code earliestFinishTime} is after the {@code latestFinishTime}</li>
	 *             <li>the {@code duration} is negative or zero</li>
	 *             </ul>
	 * @return
	 */
	public static <G extends Geometry & ImmutableGeometry> JobSpecification createFF(
		UUID jobId,
		G locationSpace,
		LocalDateTime earliestFinishTime,
		LocalDateTime latestFinishTime,
		Duration duration)
	{
		LocalDateTime earliestStartTime = earliestFinishTime.minus(duration);
		LocalDateTime latestStartTime = latestFinishTime.minus(duration);
		return new JobSpecification(jobId, locationSpace, earliestStartTime, latestStartTime, duration);
	}
	
	/**
	 * The ID of the job.
	 */
	private final UUID jobId;

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
	 * Constructs a {@code JobSpecification} defining an interval for the location and
	 * start time and the duration of a {@link Job job}.
	 * 
	 * @param jobId
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
	public <G extends Geometry & ImmutableGeometry> JobSpecification(
		UUID jobId,
		G locationSpace,
		LocalDateTime earliestStartTime,
		LocalDateTime latestStartTime,
		Duration duration)
	{
		Objects.requireNonNull(jobId, "jobId");
		Objects.requireNonNull(earliestStartTime, "earliestStartTime");
		Objects.requireNonNull(latestStartTime, "latestStartTime");
		Objects.requireNonNull(duration, "duration");
		GeometriesRequire.requireValidSimple2DGeometry(locationSpace, "locationSpace");

		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalArgumentException("earliestStartTime is after latestStartTime");
		if (duration.isNegative() || duration.isZero())
			throw new IllegalArgumentException("illegal duration");

		this.jobId = jobId;
		this.locationSpace = locationSpace;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.duration = duration;
	}

	/**
	 * @return the id of the job.
	 */
	public UUID getJobId() {
		return jobId;
	}

	/**
	 * @return the immutable spatial space for a valid {@link Point location}.
	 */
	@SuppressWarnings("unchecked")
	public <G extends Geometry & ImmutableGeometry> G getLocationSpace() {
		return (G) locationSpace;
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
	 * @return the earliest possible finish time.
	 */
	public LocalDateTime getEarliestFinishTime() {
		return earliestStartTime.plus(duration);
	}
	
	/**
	 * @return the latest possible finish time.
	 */
	public LocalDateTime getLatestFinishTime() {
		return latestStartTime.plus(duration);
	}

	/**
	 * @return the exact duration of a job.
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
		Supplier<String> defaultString = () -> String.format("(%s, %s, %s, %s)",
			getLocationSpace(), getEarliestStartTime(), getLatestStartTime(), getDuration());

		return NameProvider.nameForOrDefault(this, defaultString);
	}

}
