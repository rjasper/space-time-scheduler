package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire.*;
import static java.util.Objects.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.traverse.BreadthFirstIterator;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;

public class MinimumTimeVertexChooser {

	private DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph = null;

	private ImmutablePoint startVertex = null;

	private Set<ImmutablePoint> finishVertices = null;

	public void setGraph(DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph) {
		this.graph = requireNonNull(graph, "graph");
	}

	public void setStartVertex(ImmutablePoint startVertex) {
		this.startVertex = requireNonNull(startVertex, "startVertex");
	}

	public void setFinishVertices(Set<ImmutablePoint> finishVertices) {
		this.finishVertices = requireNonNull(finishVertices, "finishVertices");
	}

	public ImmutablePoint choose() {
		checkParameters();

		if (finishVertices.isEmpty())
			return null;

		Set<ImmutablePoint> reachable = new HashSet<>();

		BreadthFirstIterator<ImmutablePoint, DefaultWeightedEdge> it =
			new BreadthFirstIterator<ImmutablePoint, DefaultWeightedEdge>(graph, startVertex);

		while (it.hasNext()) {
			ImmutablePoint v = it.next();

			if (finishVertices.contains(v))
				reachable.add(v);
		}

		return choose(reachable);
	}

	private void checkParameters() {
		if (graph == null ||
			startVertex == null ||
			finishVertices == null)
		{
			throw new IllegalStateException("unset parameters");
		}
	}

	public ImmutablePoint choose(Collection<ImmutablePoint> vertices) {
		if (vertices.isEmpty())
			return null;

		Iterator<ImmutablePoint> it = vertices.iterator();

		ImmutablePoint chosen = it.next();
		double minTime = chosen.getY();

		while (it.hasNext()) {
			ImmutablePoint v = it.next();
			double time = v.getY();

			if (time < minTime) {
				minTime = time;
				chosen = v;
			}
		}

		return chosen;
	}

}
