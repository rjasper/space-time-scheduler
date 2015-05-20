package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers.LocationIterator;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspectiveCache;

public class PeriodicJobScheduler {

	private World world = null;

	private WorldPerspectiveCache perspectiveCache = null;

	private LocalDateTime frozenHorizonTime = null;

	private Schedule schedule = null;

	private ScheduleAlternative alternative = null;

	private PeriodicJobSpecification periodicSpec = null;

	private int maxLocationPicks = 0;

	public void setWorld(World world) {
		this.world = Objects.requireNonNull(world, "world");
	}

	public void setPerspectiveCache(WorldPerspectiveCache perspectiveCache) {
		this.perspectiveCache = Objects.requireNonNull(perspectiveCache, "perspectiveCache");
	}

	public void setFrozenHorizonTime(LocalDateTime frozenHorizonTime) {
		this.frozenHorizonTime = Objects.requireNonNull(frozenHorizonTime, "frozenHorizonTime");
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = Objects.requireNonNull(schedule, "schedule");
	}

	public void setAlternative(ScheduleAlternative alternative) {
		this.alternative = Objects.requireNonNull(alternative, "alternative");
	}

	public void setSpecification(PeriodicJobSpecification periodicSpec) {
		this.periodicSpec = Objects.requireNonNull(periodicSpec, "periodicSpec");
	}

	public void setMaxLocationPicks(int maxLocationPicks) {
		if (maxLocationPicks <= 0)
			throw new IllegalArgumentException("invalid number of picks");

		this.maxLocationPicks = maxLocationPicks;
	}

	private void checkParameters() {
		Objects.requireNonNull(world, "world");
		Objects.requireNonNull(perspectiveCache, "perspectiveCache");
		Objects.requireNonNull(frozenHorizonTime, "frozenHorizonTime");
		Objects.requireNonNull(schedule, "schedule");
		Objects.requireNonNull(alternative, "alternative");
		Objects.requireNonNull(periodicSpec, "jobSpec");

		if (maxLocationPicks <= 0)
			throw new IllegalStateException("maxLocationPicks undefined");
	}

	public boolean schedule() {
		checkParameters();

		Duration duration = periodicSpec.getDuration();
		LocalDateTime startTime = periodicSpec.getStartTime();
		Duration period = periodicSpec.getPeriod();

		// short cut if first job cannot be scheduled due to frozen horizon
		// startTime + period < frozenHorizonTime + duration
		if (startTime.plus(period) .isBefore( frozenHorizonTime.plus(duration) ))
			return false;

		if (periodicSpec.isSameLocation())
			return scheduleSameLocation();
		else
			return scheduleIndependentLocation();
	}

	private boolean scheduleSameLocation() {
		Collection<UUID> jobIds = periodicSpec.getJobIds();
		Geometry locationSpace = world.space(periodicSpec.getLocationSpace());
		Duration duration = periodicSpec.getDuration();
		LocalDateTime startTime = periodicSpec.getStartTime();
		Duration period = periodicSpec.getPeriod();

		SingularJobScheduler sc = new SingularJobScheduler();

		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setMaxLocationPicks(1); // using external location picker

		Iterable<Point> locations = () -> new LocationIterator(
			locationSpace, maxLocationPicks);

		for (Point location : locations) {
			ScheduleAlternative branch = alternative.branch();
			sc.setAlternative(branch);

			LocalDateTime periodStart = startTime;
			boolean noBreak = true;
			for (UUID jobId : jobIds) {
				LocalDateTime periodFinish = periodStart.plus(period);
				JobSpecification jobSpec = JobSpecification.createSF(
					jobId, immutable(location), periodStart, periodFinish, duration);

				sc.setSpecification(jobSpec);

				boolean status = sc.schedule();

				if (!status) {
					noBreak = false;
					break;
				}

				periodStart = periodFinish;
			}

			// indicates successful scheduling of all jobs
			if (noBreak) {
				branch.merge();
				return true;
			} else {
				branch.delete();
			}
		}

		return false;
	}

	private boolean scheduleIndependentLocation() {
		Collection<UUID> jobIds = periodicSpec.getJobIds();
		Geometry locationSpace = periodicSpec.getLocationSpace();
		Duration duration = periodicSpec.getDuration();
		LocalDateTime startTime = periodicSpec.getStartTime();
		Duration period = periodicSpec.getPeriod();

		SingularJobScheduler sc = new SingularJobScheduler();

		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setMaxLocationPicks(maxLocationPicks);

		LocalDateTime periodStart = startTime;
		boolean noBreak = true;
		for (UUID jobId : jobIds) {
			LocalDateTime periodFinish = periodStart.plus(period);
			JobSpecification jobSpec = JobSpecification.createSF(
				jobId, immutable(locationSpace), periodStart, periodFinish, duration);

			sc.setSpecification(jobSpec);

			boolean status = sc.schedule();

			if (!status) {
				noBreak = false;
				break;
			}

			periodStart = periodFinish;
		}

		// TODO alternative might be polluted

		// indicates successful scheduling of all jobs
		return noBreak;
	}

}
