package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Stream;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

public class ModifiedDijkstraAlgorithm<V, E> {

	public static <V, E> List<E> findPathBetween(DirectedGraph<V, E> graph, V startVertex, V endVertex) {
		return new ModifiedDijkstraAlgorithm<>(graph, startVertex, endVertex).getPath();
	}

	private DirectedGraph<V, E> graph;

	private V startVertex;

	private V endVertex;

	private List<E> path = null;

	private Map<V, TraversalNode> seen = new HashMap<>();

	private PriorityQueue<TraversalNode> queue = new PriorityQueue<>();

	private TraversalNode bestTargetNode;

	private class TraversalNode implements Comparable<TraversalNode> {

		private final V vertex;

		private final TraversalNode predecessor;

		private final E edge;

		private final double weight;

		private final int depth;

		public TraversalNode(V vertex, TraversalNode predecessor, E edge, double weight, int depth) {
			this.vertex = vertex;
			this.predecessor = predecessor;
			this.edge = edge;
			this.weight = weight;
			this.depth = depth;
		}

		@Override
		public int compareTo(TraversalNode o) {
			int weightCmp = Double.compare(weight, o.weight);

			if (weightCmp != 0)
				return weightCmp;
			else
				return Integer.compare(depth, o.depth);
		}

	}

	public ModifiedDijkstraAlgorithm(DirectedGraph<V, E> graph, V startVertex, V endVertex) {
		this.graph = graph;
		this.startVertex = startVertex;
		this.endVertex = endVertex;

		calculate();
	}

	public Graph<V, E> getGraph() {
		return graph;
	}

	public V getStartVertex() {
		return startVertex;
	}

	public V getEndVertex() {
		return endVertex;
	}

	public List<E> getPath() {
		return path;
	}

	private void calculate() {
		bestTargetNode = new TraversalNode(endVertex, null, null, Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
		TraversalNode initial = new TraversalNode(startVertex, null, null, 0.0, 0);

		queue.add(initial);
		seen.put(startVertex, initial);

		while (!queue.isEmpty()) {
			TraversalNode curr = queue.poll();

			if (curr.vertex.equals(endVertex)) {
				buildPath();
				return;
			}

			// discover fills queue
			unseenOf(curr.vertex)
				.forEach(this::discover);
		}

		buildEmptyPath();
	}

	private void discover(E edge) {
		V vertex = graph.getEdgeTarget(edge);
		V predVertex = graph.getEdgeSource(edge);
		TraversalNode predNode = seen.get(predVertex);

		double edgeWeight = graph.getEdgeWeight(edge);
		double weight = predNode.weight + edgeWeight;
		int depth = predNode.depth + 1;

		TraversalNode node = new TraversalNode(vertex, predNode, edge, weight, depth);

		// refuse nodes worse than the best
		if (node.compareTo(bestTargetNode) >= 0)
			return;

		TraversalNode seenNode = seen.get(vertex);

		// keep seen node if better, otherwise replace
		if (seenNode != null) {
			if (node.compareTo(seenNode) < 0) {
				queue.remove(seenNode); // replace seen node
			} else {
				return; // don't add new node
			}
		}

		// must be best target node if endVertex
		if (vertex.equals(endVertex))
			bestTargetNode = node;

		queue.add(node);
		seen.put(vertex, node);
	}

	private boolean seen(V vertex) {
		return seen.containsKey(vertex);
	}

	private Stream<E> unseenOf(V vertex) {
		return graph.outgoingEdgesOf(vertex).stream()
			.filter(e -> !seen( graph.getEdgeTarget(e) ));
	}

	private void buildPath() {
		TraversalNode curr = bestTargetNode;
		int pathLength = curr.depth;

		@SuppressWarnings("unchecked")
		E[] arr = (E[]) Array.newInstance(bestTargetNode.edge.getClass(), curr.depth);

		for (int i = 0; i < pathLength; ++i) {
			arr[curr.depth-1] = curr.edge;
			curr = curr.predecessor;
		}

		path = Arrays.asList(arr);
	}

	private void buildEmptyPath() {
		path = Collections.emptyList();
	}

}
