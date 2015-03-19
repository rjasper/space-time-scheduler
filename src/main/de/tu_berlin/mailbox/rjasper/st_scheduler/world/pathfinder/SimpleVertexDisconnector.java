package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static java.util.Objects.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;

public class SimpleVertexDisconnector {

	private DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph = null;

	private ImmutablePoint startVertex = null;

	private ImmutablePoint finishVertex = null;

	public void setGraph(DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph) {
		this.graph = requireNonNull(graph, "graph");
	}

	public void setStartVertex(ImmutablePoint startVertex) {
		this.startVertex = requireNonNull(startVertex, "startVertex");
	}

	public void setFinishVertex(ImmutablePoint finishVertex) {
		this.finishVertex = requireNonNull(finishVertex, "finishVertex");
	}

	public void disconnect() {
		checkParameters();

		Graph<ImmutablePoint, DefaultWeightedEdge> reversed = new EdgeReversedGraph<>(graph);

		BreadthFirstIterator<ImmutablePoint, DefaultWeightedEdge> it =
			new BreadthFirstIterator<ImmutablePoint, DefaultWeightedEdge>(reversed, finishVertex);

		// collect reachable vertices
		Set<ImmutablePoint> reachable = new HashSet<>();
		while (it.hasNext())
			reachable.add( it.next() );

		// determine unreachable vertices
		Collection<ImmutablePoint> unreachable = new LinkedList<>();
		for (ImmutablePoint v : graph.vertexSet()) {
			if (!reachable.contains(v))
				unreachable.add(v);
		}

		// remove unreachable vertices
		for (ImmutablePoint v : unreachable)
			graph.removeVertex(v);

		// ensure inclusion of start vertex
		graph.addVertex(startVertex);
	}

	public void checkParameters() {
		if (graph == null ||
			startVertex == null ||
			finishVertex == null)
		{
			throw new IllegalStateException("unset parameters");
		}
	}

}
