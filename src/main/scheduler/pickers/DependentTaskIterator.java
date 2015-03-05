package scheduler.pickers;

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

import scheduler.TaskSpecification;

public class DependentTaskIterator implements Iterator<TaskSpecification> {
	
	private final Map<UUID, TaskSpecification> specifications;
	
	private final TopologicalOrderIterator<UUID, DefaultEdge> topoIterator;
	
	private final Comparator<UUID> comparator = (uuid1, uuid2) -> {
		TaskSpecification spec1 = specifications.get(uuid1);
		TaskSpecification spec2 = specifications.get(uuid2);
		
		return spec1.getLatestStartTime().compareTo( spec2.getLatestStartTime() );
	};
	
	public DependentTaskIterator(
		SimpleDirectedGraph<UUID, DefaultEdge> dependencyGraph,
		Map<UUID, TaskSpecification> specifications)
	{
		this.specifications = Objects.requireNonNull(specifications, "specifications");
		
		this.topoIterator = new TopologicalOrderIterator<>(
			new EdgeReversedGraph<>(dependencyGraph),
			new PriorityQueue<>(comparator));
	}

	@Override
	public boolean hasNext() {
		return topoIterator.hasNext();
	}

	@Override
	public TaskSpecification next() {
		return specifications.get(topoIterator.next());
	}

}
