package scheduler.util;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static jts.geom.immutable.ImmutableGeometries.*;
import static util.Comparables.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import scheduler.TaskSpecification;

// XXX last edition
// TODO test
public class DependencyNormalizer {
	
	public static Map<UUID, TaskSpecification> normalizeDependentTaskSpecification(
		SimpleDirectedGraph<UUID, DefaultEdge> dependencyGraph,
		Map<UUID, TaskSpecification> specifications)
	throws DependencyNormalizationException
	{
		return new DependencyNormalizer(dependencyGraph, specifications)
			.normalize();
	}

	private final SimpleDirectedGraph<UUID, DefaultEdge> dependencyGraph;
	
	private final Map<UUID, TaskSpecification> origin;
	
	private Map<UUID, Intermediate> intermediate;
	
	private static class Intermediate {

		private final TaskSpecification origin;
		private LocalDateTime earliestStartTime;
		private LocalDateTime latestStartTime;
		
		public Intermediate(TaskSpecification origin) {
			this.earliestStartTime = origin.getEarliestStartTime();
			this.latestStartTime = origin.getLatestStartTime();
			this.origin = origin;
		}

		public TaskSpecification getOrigin() {
			return origin;
		}

		public LocalDateTime getEarliestStartTime() {
			return earliestStartTime;
		}

		public void setEarliestStartTime(LocalDateTime earliestStartTime) {
			this.earliestStartTime = earliestStartTime;
		}

		public LocalDateTime getLatestStartTime() {
			return latestStartTime;
		}

		public LocalDateTime getEarliestFinishTime() {
			return earliestStartTime.plus(origin.getDuration());
		}

		public LocalDateTime getLatestFinishTime() {
			return latestStartTime.plus( origin.getDuration() );
		}

		public void setLatestFinishTime(LocalDateTime latestFinishTime) {
			this.latestStartTime = latestFinishTime.minus( origin.getDuration() );
		}
		
	}
	
	public DependencyNormalizer(
		SimpleDirectedGraph<UUID, DefaultEdge> dependencyGraph,
		Map<UUID, TaskSpecification> specifications)
	{
		this.dependencyGraph = Objects.requireNonNull(dependencyGraph, "dependencyGraph");
		this.origin = Objects.requireNonNull(specifications, "specifications");
	}
	
	public static class DependencyNormalizationException extends Exception {
	
		private static final long serialVersionUID = -950917191397962404L;
	
		private DependencyNormalizationException(String message) {
			super(message);
		}
		
	}

	public Map<UUID, TaskSpecification> normalize() throws DependencyNormalizationException {
		initIntermediate();
		
		TopologicalOrderIterator<UUID, DefaultEdge> forwardIterator =
			new TopologicalOrderIterator<>(new EdgeReversedGraph<>(dependencyGraph));
		TopologicalOrderIterator<UUID, DefaultEdge> backwardIterator =
			new TopologicalOrderIterator<>(dependencyGraph);
		
		while (forwardIterator.hasNext())
			normalizeLatest(forwardIterator.next());
		while (backwardIterator.hasNext())
			normalizeEarliest(forwardIterator.next());
		
		if (!verify()) {
			resetIntermediate();
			throw new DependencyNormalizationException("unable to normalize");
		}
		
		Map<UUID, TaskSpecification> normalized = build();
		
		resetIntermediate();
		
		return normalized;
	}
	
	private void initIntermediate() {
		intermediate = origin.values().stream()
			.collect(toMap(TaskSpecification::getTaskId, Intermediate::new));
	}

	private void resetIntermediate() {
		intermediate = null;
	}

	private void normalizeLatest(UUID taskId) {
		Intermediate inter = intermediate.get(taskId);
		
		// collect all dependent intermediates and
		// determine the minimum latest start time
		LocalDateTime depMin = dependencyGraph.incomingEdgesOf(taskId).stream()
			.map(dependencyGraph::getEdgeSource)
			.map(intermediate::get)
			.map(Intermediate::getLatestStartTime)
			.min(TIME_COMPARATOR)
			.orElse(LocalDateTime.MAX);
		
		inter.setLatestFinishTime(min(depMin, inter.getLatestFinishTime()));
	}

	private void normalizeEarliest(UUID taskId) {
		Intermediate inter = intermediate.get(taskId);
		
		// collect all required intermediates and
		// determine the maximum earliest finish time
		LocalDateTime reqMax = dependencyGraph.outgoingEdgesOf(taskId).stream()
			.map(dependencyGraph::getEdgeTarget)
			.map(intermediate::get)
			.map(Intermediate::getEarliestFinishTime)
			.max(TIME_COMPARATOR)
			.orElse(LocalDateTime.MIN);
		
		inter.setEarliestStartTime(max(reqMax, inter.getEarliestStartTime()));
	}

	private boolean verify() {
		return intermediate.values().stream()
			.allMatch(inter -> inter.getEarliestStartTime().isBefore( inter.getLatestStartTime() ));
	}

	private Map<UUID, TaskSpecification> build() {
		return intermediate.values().stream()
			.map(DependencyNormalizer::makeSpec)
			.collect(toMap(TaskSpecification::getTaskId, identity()));
	}
	
	private static TaskSpecification makeSpec(Intermediate inter) {
		TaskSpecification origin = inter.getOrigin();
		
		return new TaskSpecification(
			origin.getTaskId(),
			immutable(origin.getLocationSpace()),
			inter.getEarliestStartTime(),
			inter.getLatestStartTime(),
			origin.getDuration());
	}

	private static final Comparator<LocalDateTime> TIME_COMPARATOR =
		(lhs, rhs) -> lhs.compareTo(rhs);

}
