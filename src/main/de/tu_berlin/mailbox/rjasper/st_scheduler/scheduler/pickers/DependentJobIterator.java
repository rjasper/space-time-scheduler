package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.UUID;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;

public class DependentJobIterator implements Iterator<JobSpecification> {
	
	private final Map<UUID, JobSpecification> specifications;
	
	private final TopologicalOrderIterator<UUID, DefaultEdge> topoIterator;
	
	public DependentJobIterator(
		SimpleDirectedGraph<UUID, DefaultEdge> dependencyGraph,
		Map<UUID, JobSpecification> specifications)
	{
		this.specifications = Objects.requireNonNull(specifications, "specifications");
		
		Comparator<UUID> comparator = (uuid1, uuid2) -> {
			JobSpecification spec1 = specifications.get(uuid1);
			JobSpecification spec2 = specifications.get(uuid2);
			
			return spec1.getLatestStartTime().compareTo( spec2.getLatestStartTime() );
		};
		
		this.topoIterator = new TopologicalOrderIterator<>(
			new EdgeReversedGraph<>(dependencyGraph),
			new PriorityQueue<>(comparator));
	}

	@Override
	public boolean hasNext() {
		return topoIterator.hasNext();
	}

	@Override
	public JobSpecification next() {
		return specifications.get(topoIterator.next());
	}

}
