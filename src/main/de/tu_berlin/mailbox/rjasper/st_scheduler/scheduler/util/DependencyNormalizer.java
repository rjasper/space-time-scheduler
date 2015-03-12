package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

import static de.tu_berlin.mailbox.rjasper.lang.Comparables.*;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;

public class DependencyNormalizer {
	
	public static Map<UUID, JobSpecification> normalizeDependentJobSpecifications(
		SimpleDirectedGraph<UUID, DefaultEdge> dependencyGraph,
		Map<UUID, JobSpecification> specifications,
		LocalDateTime frozenHorizonTime)
	throws DependencyNormalizationException
	{
		return new DependencyNormalizer(dependencyGraph, specifications, frozenHorizonTime)
			.normalize();
	}

	private final SimpleDirectedGraph<UUID, DefaultEdge> dependencyGraph;
	
	private final Map<UUID, JobSpecification> origin;
	
	private final LocalDateTime frozenHorizonTime;
	
	private transient Map<UUID, Intermediate> intermediate;
	
	private static class Intermediate {

		private final JobSpecification origin;
		private LocalDateTime earliestStartTime;
		private LocalDateTime latestStartTime;
		
		public Intermediate(JobSpecification origin) {
			this.earliestStartTime = origin.getEarliestStartTime();
			this.latestStartTime = origin.getLatestStartTime();
			this.origin = origin;
		}

		public JobSpecification getOrigin() {
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
		Map<UUID, JobSpecification> specifications,
		LocalDateTime frozenHorizonTime)
	{
		this.dependencyGraph = Objects.requireNonNull(dependencyGraph, "dependencyGraph");
		this.origin = Objects.requireNonNull(specifications, "specifications");
		this.frozenHorizonTime = Objects.requireNonNull(frozenHorizonTime, "frozenHorizonTime");
	}
	
	public static class DependencyNormalizationException extends Exception {
	
		private static final long serialVersionUID = -950917191397962404L;
	
		private DependencyNormalizationException(String message) {
			super(message);
		}

		private DependencyNormalizationException(String message, Throwable cause) {
			super(message, cause);
		}
		
	}

	public Map<UUID, JobSpecification> normalize() throws DependencyNormalizationException {
		initIntermediate();
		
		TopologicalOrderIterator<UUID, DefaultEdge> forwardIterator =
			new TopologicalOrderIterator<>(new EdgeReversedGraph<>(dependencyGraph));
		TopologicalOrderIterator<UUID, DefaultEdge> backwardIterator =
			new TopologicalOrderIterator<>(dependencyGraph);

		while (forwardIterator.hasNext())
			normalizeEarliest(forwardIterator.next());
		while (backwardIterator.hasNext())
			normalizeLatest(backwardIterator.next());
		
		Map<UUID, JobSpecification> normalized;
		try {
			// intermediate might include invalid start time intervals
			normalized = build();
		} catch (IllegalArgumentException e) {
			resetIntermediate();
			throw new DependencyNormalizationException("unable to normalize", e);
		}
		
		resetIntermediate();
		
		return normalized;
	}
	
	private void initIntermediate() {
		intermediate = origin.values().stream()
			.collect(toMap(JobSpecification::getJobId, Intermediate::new));
	}

	private void resetIntermediate() {
		intermediate = null;
	}

	private void normalizeLatest(UUID jobId) {
		Intermediate inter = intermediate.get(jobId);
		
		// collect all dependent intermediates and
		// determine the minimum latest start time
		LocalDateTime depMin = dependencyGraph.incomingEdgesOf(jobId).stream()
			.map(dependencyGraph::getEdgeSource)
			.map(intermediate::get)
			.map(Intermediate::getLatestStartTime)
			.min(TIME_COMPARATOR)
			.orElse(LocalDateTime.MAX);
		
		inter.setLatestFinishTime(min(depMin, inter.getLatestFinishTime()));
	}

	private void normalizeEarliest(UUID jobId) {
		Intermediate inter = intermediate.get(jobId);
		
		// collect all required intermediates and
		// determine the maximum earliest finish time
		LocalDateTime reqMax = dependencyGraph.outgoingEdgesOf(jobId).stream()
			.map(dependencyGraph::getEdgeTarget)
			.map(intermediate::get)
			.map(Intermediate::getEarliestFinishTime)
			.max(TIME_COMPARATOR)
			.orElse(LocalDateTime.MIN);
		
		inter.setEarliestStartTime(max(reqMax, inter.getEarliestStartTime(), frozenHorizonTime));
	}

	private Map<UUID, JobSpecification> build() {
		return intermediate.values().stream()
			.map(DependencyNormalizer::makeSpec)
			.collect(toMap(JobSpecification::getJobId, identity()));
	}
	
	private static JobSpecification makeSpec(Intermediate inter) {
		JobSpecification origin = inter.getOrigin();
		
		// reuse origin if possible
		if (inter.getEarliestStartTime().isEqual(origin.getEarliestStartTime()) &&
			inter.getLatestStartTime().isEqual(origin.getLatestStartTime()))
		{
			return origin;
		}
		
		return new JobSpecification(
			origin.getJobId(),
			origin.getLocationSpace(),
			inter.getEarliestStartTime(),
			inter.getLatestStartTime(),
			origin.getDuration());
	}

	private static final Comparator<LocalDateTime> TIME_COMPARATOR =
		(lhs, rhs) -> lhs.compareTo(rhs);

}
