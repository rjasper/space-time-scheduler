package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;

import de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometry;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;

/**
 * <p>
 * An object of this class describes the specification of multiple job
 * repetitions.
 * </p>
 *
 * <p>
 * To schedule periodic jobs it is required to pass a specification to the
 * {@link Scheduler}. It will try to find a realizable configuration for each
 * job repetition which satisfy the specification.
 *
 * <p>
 * A specification describes when and where a job has to be executed. The
 * locationSpace defines the area of valid locations. The start time determines
 * the start of the first period while the period defines the duration of each
 * period. Each job repetition must be scheduled within its own period. The
 * periods are consecutive. The number of job IDs define the number of
 * repetitions. The duration defines the exact duration of the job.
 * </p>
 *
 * @author Rico Jasper
 */
public final class PeriodicJobSpecification {

	/**
	 * The IDs of the job repetitions.
	 */
	private final ImmutableList<UUID> jobIds;

	/**
	 * The spatial space for a valid location.
	 */
	private final Geometry locationSpace;

	/**
	 * Whether to use the exact same location for each job repetition.
	 */
	private final boolean sameLocation;

	/**
	 * The exact duration of each job.
	 */
	private final Duration duration;

	/**
	 * The start time of the first period.
	 */
	private final LocalDateTime startTime;
	
	/**
	 * The duration of each period.
	 */
	private final Duration period;

	/**
	 * Constructs a {@code PeriodicJobSpecification} the location, time interval
	 * and duration of each job repetition. The {@code locationSpace} and the
	 * {@code duration} applies to all job repetition. The job IDs and periodic
	 * time intervals are individually applied to each repetition. The job IDs
	 * are applied as ordered.
	 * 
	 * @param jobIds
	 *            the individual job IDs.
	 * @param locationSpace
	 *            the location space for all repetions.
	 * @param sameLocation
	 *            whether to use the same location for all repetitions.
	 * @param duration
	 *            the duration of each repetition.
	 * @param startTime
	 *            the start time of the first period.
	 * @param period
	 *            the period duration.
	 */
	public <G extends Geometry & ImmutableGeometry> PeriodicJobSpecification(
		ImmutableList<UUID> jobIds,
		G locationSpace,
		boolean sameLocation,
		Duration duration,
		LocalDateTime startTime,
		Duration period)
	{
		this.jobIds       = CollectionsRequire.requireNonNull(jobIds, "jobIds");
		this.locationSpace = GeometriesRequire.requireValidSimple2DGeometry(locationSpace, "locationSpace");
		this.sameLocation  = sameLocation;
		this.duration      = Objects.requireNonNull(duration, "duration");
		this.startTime     = Objects.requireNonNull(startTime, "startTime");
		this.period        = Objects.requireNonNull(period, "period");
		
		if (jobIds.isEmpty())
			throw new IllegalArgumentException("no repetitions");
		if (duration.isNegative() || duration.isZero())
			throw new IllegalArgumentException("illegal duration");
		if (period.compareTo(duration) < 0)
			throw new IllegalArgumentException("illegal period");
	}

	/**
	 * @return the job IDs.
	 */
	public ImmutableList<UUID> getJobIds() {
		return jobIds;
	}

	@SuppressWarnings("unchecked")
	public <G extends Geometry & ImmutableGeometry> G getLocationSpace() {
		return (G) locationSpace;
	}

	/**
	 * @return {@code true} if each repetition shall have the same location.
	 */
	public boolean isSameLocation() {
		return sameLocation;
	}

	/**
	 * @return the duration of each repetition.
	 */
	public Duration getDuration() {
		return duration;
	}

	/**
	 * @return the start time of the first period.
	 */
	public LocalDateTime getStartTime() {
		return startTime;
	}

	/**
	 * @return the duration of each period.
	 */
	public Duration getPeriod() {
		return period;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("(%s, %s, %s, %s, %s, %s)",
			jobIds, locationSpace, sameLocation, duration, startTime, period);
	}

}
