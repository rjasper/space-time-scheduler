package scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jts.geom.immutable.ImmutableGeometry;
import jts.geom.util.GeometriesRequire;
import util.CollectionsRequire;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;

public final class PeriodicJobSpecification {
	
	private final ImmutableList<UUID> jobIds;
	
	private final Geometry locationSpace;
	
	private final boolean sameLocation;

	private final Duration duration;

	private final LocalDateTime startTime;
	
	private final Duration period;

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

	public ImmutableList<UUID> getJobIds() {
		return jobIds;
	}

	@SuppressWarnings("unchecked")
	public <G extends Geometry & ImmutableGeometry> G getLocationSpace() {
		return (G) locationSpace;
	}

	public boolean isSameLocation() {
		return sameLocation;
	}

	public Duration getDuration() {
		return duration;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public Duration getPeriod() {
		return period;
	}
	
	@Override
	public String toString() {
		return String.format("(%s, %s, %s, %s, %s, %s)",
			jobIds, locationSpace, sameLocation, duration, startTime, period);
	}

}
