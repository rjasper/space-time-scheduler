package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.DependencyNormalizer.normalizeDependentJobSpecifications;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers.DependentJobIterator;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.DependencyNormalizer.DependencyNormalizationException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspectiveCache;

/**
 * Specialized scheduler to schedule a set of dependent jobs.
 *
 * @author Rico Jasper
 */
public class DependentJobScheduler {

	private World world = null;

	private WorldPerspectiveCache perspectiveCache = null;

	private LocalDateTime frozenHorizonTime = null;

	private Schedule schedule = null;

	private ScheduleAlternative alternative = null;

	private Collection<JobSpecification> jobSpecs = null;

	private SimpleDirectedGraph<UUID, DefaultEdge> dependencies = null;

	private Duration interDependencyMargin = null;

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

	public void setSpecifications(Collection<JobSpecification> jobSpecs) {
		this.jobSpecs = CollectionsRequire.requireNonNull(jobSpecs, "jobSpecs");
	}

	public void setDependencies(SimpleDirectedGraph<UUID, DefaultEdge> dependencies) {
		this.dependencies = Objects.requireNonNull(dependencies, "dependencies");
	}

	public void setInterDependencyMargin(Duration interDependencyMargin) {
		Objects.requireNonNull(interDependencyMargin, "interDependencyMargin");

		if (interDependencyMargin.isNegative())
			throw new IllegalArgumentException("negative margin");

		this.interDependencyMargin = interDependencyMargin;
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
		Objects.requireNonNull(jobSpecs, "jobSpecs");
		Objects.requireNonNull(dependencies, "dependencies");
		Objects.requireNonNull(interDependencyMargin, "interDependencyMargin");

		if (maxLocationPicks <= 0)
			throw new IllegalStateException("maxLocationPicks undefined");

		// check consistency of specifications and dependencies

		Set<UUID> specUuids = jobSpecs.stream()
			.map(JobSpecification::getJobId)
			.collect(toSet());
		Set<UUID> depUuids = dependencies.vertexSet();

		if (!specUuids.equals(depUuids))
			throw new IllegalStateException("specifications and dependencies inconsistent");
	}

	public boolean schedule() {
		checkParameters();

		Map<UUID, JobSpecification> specMap = jobSpecs.stream()
			.collect(toMap(JobSpecification::getJobId, identity()));

		Map<UUID, JobSpecification> normalizedSpecMap;
		try {
			normalizedSpecMap = normalizeDependentJobSpecifications(dependencies, specMap, frozenHorizonTime);
		} catch (DependencyNormalizationException e) {
			return false;
		}

		Iterator<JobSpecification> it = new DependentJobIterator(dependencies, normalizedSpecMap);

		SingularJobScheduler sc = new SingularJobScheduler();

		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setMaxLocationPicks(maxLocationPicks);

		while (it.hasNext()) {
			JobSpecification constrained = constrain(it.next());

			if (constrained == null)
				return false;

			sc.setSpecification(constrained);

			boolean status = sc.schedule();

			// no back-tracking
			if (!status)
				return false;
		}

		return true;
	}

	private JobSpecification constrain(JobSpecification spec) {
		Optional<LocalDateTime> depMaxOpt = dependencies.outgoingEdgesOf(spec.getJobId())
			.stream()
			.map(dependencies::getEdgeTarget)
			.map(alternative::getJob)
			.map(Job::getFinishTime)
			.max((t1, t2) -> t1.compareTo(t2));

		// if no dependencies
		if (!depMaxOpt.isPresent())
			return spec;

		LocalDateTime withMargin = depMaxOpt.get()
			.plus(interDependencyMargin);

		// if margin is irrelevant
		if (withMargin.isBefore(spec.getEarliestStartTime()))
			return spec;
		// if margin is impossible
		if (withMargin.isAfter(spec.getLatestStartTime()))
			return null;

		return new JobSpecification(
			spec.getJobId(),
			spec.getLocationSpace(),
			withMargin,
			spec.getLatestStartTime(),
			spec.getDuration());
	}

}
