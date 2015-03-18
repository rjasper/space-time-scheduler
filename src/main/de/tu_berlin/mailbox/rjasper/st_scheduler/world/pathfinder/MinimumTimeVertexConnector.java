package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;

public class MinimumTimeVertexConnector {

	private DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph;

	private ImmutablePoint startVertex;

	private double finishArc = Double.NaN;

	private double minFinishTime = Double.NaN;

	private double maxFinishTime = Double.NaN;

	private BiFunction<ImmutablePoint, ImmutablePoint, Boolean> edgeChecker = null;

	private BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator = null;

	private double maxVelocity = Double.NaN;

	public void setGraph(DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph) {
		this.graph = requireNonNull(graph, "graph");
	}

	public void setStartVertex(ImmutablePoint startVertex) {
		this.startVertex = requireNonNull(startVertex, "startVertex");
	}

	public void setFinishArc(double finishArc) {
		if (!Double.isFinite(finishArc))
			throw new IllegalArgumentException("illegal finishArc");

		this.finishArc = finishArc;
	}

	public void setMinFinishTime(double minFinishTime) {
		if (!Double.isFinite(minFinishTime))
			throw new IllegalArgumentException("illegal minFinishTime");

		this.minFinishTime = minFinishTime;
	}

	public void setMaxFinishTime(double maxFinishTime) {
		if (!Double.isFinite(maxFinishTime))
			throw new IllegalArgumentException("illegal maxFinishTime");

		this.maxFinishTime = maxFinishTime;
	}

	public void setEdgeChecker(BiFunction<ImmutablePoint, ImmutablePoint, Boolean> edgeChecker) {
		this.edgeChecker = requireNonNull(edgeChecker, "edgeChecker");
	}

	public void setWeightCalculator(BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator) {
		this.weightCalculator = requireNonNull(weightCalculator, "weightCalculator");
	}

	public void setMaxVelocity(double maxVelocity) {
		if (!Double.isFinite(maxVelocity) || maxVelocity <= 0.0)
			throw new IllegalArgumentException("illegal maxVelocity");

		this.maxVelocity = maxVelocity;
	}

	private void checkParameters() {
		if (graph == null ||
			startVertex == null ||
			Double.isNaN(finishArc) ||
			Double.isNaN(minFinishTime) ||
			Double.isNaN(maxFinishTime) ||
			edgeChecker == null ||
			weightCalculator == null ||
			Double.isNaN(maxVelocity))
		{
			throw new IllegalStateException("unset parameters");
		}

		if (startVertex.getX() >= finishArc ||
			startVertex.getY() >  minFinishTime)
		{
			throw new IllegalStateException("startVertex and finishVertex incompatible");
		}

		if (minFinishTime > maxFinishTime)
			throw new IllegalStateException("minFinishTime > maxFinishTime");
	}

	public Set<ImmutablePoint> connect() {
		checkParameters();

		Set<ImmutablePoint> finishVertices = new HashSet<>();

		ImmutablePoint minFinishVertex = immutablePoint(finishArc, minFinishTime);
		graph.addVertex(minFinishVertex);
		finishVertices.add(minFinishVertex);

		Collection<ImmutablePoint> vertices = new ArrayList<>(graph.vertexSet());

		// connect finish vertices
		for (ImmutablePoint v : vertices) {
			if (!within(v))
				continue;

			ImmutablePoint candidate = calcCandidate(v);
			graph.addVertex(candidate);
			finishVertices.add(candidate);

			connectHelper(v, minFinishVertex);
			connectHelper(v, candidate);
		}

		// remove unconnected finish vertices
		Iterator<ImmutablePoint> it = finishVertices.iterator();
		while (it.hasNext()) {
			ImmutablePoint v = it.next();

			if (graph.inDegreeOf(v) == 0) {
				graph.removeVertex(v);
				it.remove();
			}
		}

		return finishVertices;
	}

	private boolean within(ImmutablePoint vertex) {
		double minArc = startVertex.getX();
		double maxArc = finishArc;
		double minTime = startVertex.getY();
		double maxTime = maxFinishTime;
		double arc = vertex.getX();
		double time = vertex.getY();

		return arc  >= minArc  && arc  < maxArc
			&& time >= minTime && time < maxTime;
	}

	private void connectHelper(ImmutablePoint source, ImmutablePoint target) {
		if (!edgeChecker.apply(source, target))
			return;

		DefaultWeightedEdge edge = graph.addEdge(source, target);

		if (edge != null)
			graph.setEdgeWeight(edge, weightCalculator.apply(source, target));
	}

	private ImmutablePoint calcCandidate(ImmutablePoint origin) {
		// method assumes that the origin's arc is not greater than finishArc

		double s = origin.getX(), t = origin.getY();

		if (s == finishArc)
			return origin;
		else // s < maxArc
			return immutablePoint(finishArc, (finishArc - s) / maxVelocity + t);
	}

}
