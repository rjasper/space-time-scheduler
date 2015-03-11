package scheduler;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static scheduler.util.DependencyNormalizer.*;

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

import scheduler.pickers.DependentTaskIterator;
import scheduler.util.DependencyNormalizer.DependencyNormalizationException;
import util.CollectionsRequire;
import world.World;
import world.WorldPerspectiveCache;

public class DependentTaskPlanner {
	
	private World world = null;
	
	private WorldPerspectiveCache perspectiveCache = null;
	
	private LocalDateTime frozenHorizonTime = null;
	
	private Schedule schedule = null;
	
	private ScheduleAlternative alternative = null;
	
	private Collection<TaskSpecification> taskSpecs = null;
	
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

	public void setSpecifications(Collection<TaskSpecification> taskSpecs) {
		this.taskSpecs = CollectionsRequire.requireNonNull(taskSpecs, "taskSpecs");
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
		Objects.requireNonNull(taskSpecs, "taskSpecs");
		Objects.requireNonNull(dependencies, "dependencies");
		Objects.requireNonNull(interDependencyMargin, "interDependencyMargin");
		
		if (maxLocationPicks <= 0)
			throw new IllegalStateException("maxLocationPicks undefined");
		
		// check consistency of specifications and dependencies
		
		Set<UUID> specUuids = taskSpecs.stream()
			.map(TaskSpecification::getTaskId)
			.collect(toSet());
		Set<UUID> depUuids = dependencies.vertexSet();
		
		if (!specUuids.equals(depUuids))
			throw new IllegalStateException("specifications and dependencies inconsistent");
	}

	public boolean schedule() {
		checkParameters();
		
		Map<UUID, TaskSpecification> specMap = taskSpecs.stream()
			.collect(toMap(TaskSpecification::getTaskId, identity()));
		
		Map<UUID, TaskSpecification> normalizedSpecMap;
		try {
			normalizedSpecMap = normalizeDependentTaskSpecifications(dependencies, specMap, frozenHorizonTime);
		} catch (DependencyNormalizationException e) {
			return false;
		}
		
		Iterator<TaskSpecification> it = new DependentTaskIterator(dependencies, normalizedSpecMap);
		
		SingularTaskPlanner sc = new SingularTaskPlanner();

		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setMaxLocationPicks(maxLocationPicks);
		
		while (it.hasNext()) {
			TaskSpecification constrained = constrain(it.next());
			
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
	
	private TaskSpecification constrain(TaskSpecification spec) {
		Optional<LocalDateTime> depMaxOpt = dependencies.outgoingEdgesOf(spec.getTaskId())
			.stream()
			.map(dependencies::getEdgeTarget)
			.map(alternative::getTask)
			.map(Task::getFinishTime)
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
		
		return new TaskSpecification(
			spec.getTaskId(),
			spec.getLocationSpace(),
			withMargin,
			spec.getLatestStartTime(),
			spec.getDuration());
	}

}
