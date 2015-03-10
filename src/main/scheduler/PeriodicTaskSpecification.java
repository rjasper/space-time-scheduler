package scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jts.geom.immutable.ImmutableGeometry;
import jts.geom.util.GeometriesRequire;
import util.CollectionsRequire;

import com.google.common.collect.ImmutableCollection;
import com.vividsolutions.jts.geom.Geometry;

public final class PeriodicTaskSpecification {
	
	private final ImmutableCollection<UUID> taskIds;
	
	private final Geometry locationSpace;

	private final Duration duration;

	private final LocalDateTime startTime;
	
	private final Duration period;

	public <G extends Geometry & ImmutableGeometry> PeriodicTaskSpecification(
		ImmutableCollection<UUID> taskIds,
		G locationSpace,
		Duration duration,
		LocalDateTime startTime,
		Duration period)
	{
		this.taskIds       = CollectionsRequire.requireNonNull(taskIds, "taskIds");
		this.locationSpace = GeometriesRequire.requireValidSimple2DGeometry(locationSpace, "locationSpace");
		this.duration      = Objects.requireNonNull(duration, "duration");
		this.startTime     = Objects.requireNonNull(startTime, "startTime");
		this.period        = Objects.requireNonNull(period, "period");
		
		if (duration.isNegative() || duration.isZero())
			throw new IllegalArgumentException("illegal duration");
		if (period.compareTo(duration) < 0)
			throw new IllegalArgumentException("illegal period");
	}

	public ImmutableCollection<UUID> getTaskIds() {
		return taskIds;
	}

	@SuppressWarnings("unchecked")
	public <G extends Geometry & ImmutableGeometry> G getLocationSpace() {
		return (G) locationSpace;
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

}
