package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Geometry;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;

public class MinimumTimeVertexConnector {

	private DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph;

	private double minArc = Double.NaN;

	private double finishArc = Double.NaN;

	private double minTime = Double.NaN;

	private double minFinishTime = Double.NaN;

	private double maxFinishTime = Double.NaN;

	private double bufferDuration = Double.NaN;

	private double maxVelocity = Double.NaN;

	private Geometry forbiddenMap = null;

	private BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator = null;

	private transient BiFunction<ImmutablePoint, ImmutablePoint, Boolean> fullEdgeChecker = null;

	private transient BiFunction<ImmutablePoint, ImmutablePoint, Boolean> nonVelocityEdgeChecker = null;

	public void setGraph(DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph) {
		this.graph = requireNonNull(graph, "graph");
	}

	public void setMinArc(double minArc) {
		if (!Double.isFinite(minArc))
			throw new IllegalArgumentException("illegal minArc");

		this.minArc = minArc;
	}

	public void setFinishArc(double finishArc) {
		if (!Double.isFinite(finishArc))
			throw new IllegalArgumentException("illegal finishArc");

		this.finishArc = finishArc;
	}

	public void setMinTime(double minTime) {
		if (!Double.isFinite(minTime))
			throw new IllegalArgumentException("illegal minTime");

		this.minTime = minTime;
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

	public void setBufferDuration(double bufferDuration) {
		if (!Double.isFinite(bufferDuration) || bufferDuration < 0.0)
			throw new IllegalArgumentException("illegal bufferDuration");

		this.bufferDuration = bufferDuration;
	}

	public void setMaxVelocity(double maxVelocity) {
		if (!Double.isFinite(maxVelocity) || maxVelocity <= 0.0)
			throw new IllegalArgumentException("illegal maxVelocity");

		this.maxVelocity = maxVelocity;
	}

	public void setForbiddenMap(Geometry forbiddenMap) {
		this.forbiddenMap = requireNonNull(forbiddenMap, "forbiddenMap");
	}

	public void setWeightCalculator(BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator) {
		this.weightCalculator = requireNonNull(weightCalculator, "weightCalculator");
	}

	private void checkParameters() {
		if (graph == null ||
			Double.isNaN(minArc) ||
			Double.isNaN(finishArc) ||
			Double.isNaN(minTime) ||
			Double.isNaN(minFinishTime) ||
			Double.isNaN(maxFinishTime) ||
			Double.isNaN(bufferDuration) ||
			Double.isNaN(maxVelocity) ||
			forbiddenMap == null ||
			weightCalculator == null)
		{
			throw new IllegalStateException("unset parameters");
		}

		if (minArc > finishArc || minTime > minFinishTime)
			throw new IllegalStateException("illegal bounds");
		if (minFinishTime > maxFinishTime)
			throw new IllegalStateException("minFinishTime > maxFinishTime");
	}

	public Set<ImmutablePoint> connect() {
		checkParameters();
		init();

		Set<ImmutablePoint> finishVertices = new HashSet<>();

		ImmutablePoint minFinishVertex = immutablePoint(finishArc, minFinishTime);
		graph.addVertex(minFinishVertex);

		Collection<ImmutablePoint> vertices = new ArrayList<>(graph.vertexSet());

		// connect finish vertices
		for (ImmutablePoint v : vertices) {
			ImmutablePoint candidate = calcCandidate(v);
			graph.addVertex(candidate);

			boolean status;
			status = connectHelper(v, minFinishVertex, fullEdgeChecker);
			if (status)
				finishVertices.add(minFinishVertex);
			status = connectHelper(v, candidate, nonVelocityEdgeChecker);
			if (status)
				finishVertices.add(candidate);
		}

		cleanUp();

		return finishVertices;
	}

	private void init() {
		BoundsEdgeChecker boundsChecker = new BoundsEdgeChecker(
			minArc, finishArc, minTime, maxFinishTime);
		VelocityEdgeChecker velocityChecker = new VelocityEdgeChecker(maxVelocity);
		VisibilityEdgeChecker visibilityChecker = new VisibilityEdgeChecker(forbiddenMap);
		BufferTimeEdgeChecker bufferChecker = new BufferTimeEdgeChecker(
			bufferDuration, visibilityChecker::check);

		fullEdgeChecker = (v1, v2) ->
			checkMinFinishBound(v2) &&
			boundsChecker.check(v1, v2) &&
			velocityChecker.check(v1, v2) &&
			visibilityChecker.check(v1, v2) &&
			bufferChecker.check(v2);

		nonVelocityEdgeChecker = (v1, v2) ->
			checkMinFinishBound(v2) &&
			boundsChecker.check(v1, v2) &&
			visibilityChecker.check(v1, v2) &&
			bufferChecker.check(v2);
	}

	private boolean checkMinFinishBound(ImmutablePoint vertex) {
		return vertex.getY() >= minFinishTime;
	}

	private void cleanUp() {
		nonVelocityEdgeChecker = null;
	}

	private boolean connectHelper(
		ImmutablePoint source,
		ImmutablePoint target,
		BiFunction<ImmutablePoint, ImmutablePoint, Boolean> edgeChecker)
	{
		if (!edgeChecker.apply(source, target))
			return false;

		if (!source.equalsTopo(target)) {
			DefaultWeightedEdge edge = graph.addEdge(source, target);

			if (edge != null)
				graph.setEdgeWeight(edge, weightCalculator.apply(source, target));
		}

		return true;
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
