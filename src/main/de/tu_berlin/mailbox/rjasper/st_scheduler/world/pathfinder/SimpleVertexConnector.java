package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire.*;
import static java.util.Objects.*;

import java.util.Set;
import java.util.function.BiFunction;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Geometry;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;

/**
 * Adds Edges between any pairs of vertices visible to each other.
 *
 * @author Rico Jasper
 */
public class SimpleVertexConnector {

	private DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph;

	private Set<ImmutablePoint> vertices;

	private double minArc = Double.NaN;

	private double maxArc = Double.NaN;

	private double minTime = Double.NaN;

	private double maxTime = Double.NaN;

	private double maxVelocity = Double.NaN;

	private Geometry forbiddenMap;

	private BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator = null;

	private transient BiFunction<ImmutablePoint, ImmutablePoint, Boolean> edgeChecker = null;

	public void setGraph(
		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph) {
		this.graph = requireNonNull(graph, "graph");
	}

	public void setVertices(Set<ImmutablePoint> vertices) {
		this.vertices = requireNonNull(vertices, "vertices");
	}

	public void setMinArc(double minArc) {
		if (!Double.isFinite(minArc))
			throw new IllegalArgumentException("illegal minArc");

		this.minArc = minArc;
	}

	public void setMaxArc(double maxArc) {
		if (!Double.isFinite(maxArc))
			throw new IllegalArgumentException("illegal maxArc");

		this.maxArc = maxArc;
	}

	public void setMinTime(double minTime) {
		if (!Double.isFinite(minTime))
			throw new IllegalArgumentException("illegal minTime");

		this.minTime = minTime;
	}

	public void setMaxTime(double maxTime) {
		if (!Double.isFinite(maxTime))
			throw new IllegalArgumentException("illegal maxTime");

		this.maxTime = maxTime;
	}

	public void setForbiddenMap(Geometry forbiddenMap) {
		this.forbiddenMap = requireNonNull(forbiddenMap, "forbiddenMap");
	}

	public void setWeightCalculator(BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator) {
		this.weightCalculator = requireNonNull(weightCalculator, "weightCalculator");
	}

	public void setMaxVelocity(double maxVelocity) {
		if (!Double.isFinite(maxVelocity) || maxVelocity <= 0)
			throw new IllegalArgumentException("illegal velocity");

		this.maxVelocity = maxVelocity;
	}

	private void checkParameters() {
		if (
			graph == null ||
			vertices == null ||
			Double.isNaN(minArc) ||
			Double.isNaN(maxArc) ||
			Double.isNaN(minTime) ||
			Double.isNaN(maxTime) ||
			weightCalculator == null ||
			forbiddenMap == null ||
			Double.isNaN(maxVelocity))
		{
			throw new IllegalStateException("unset parameters");
		}

		if (minArc > maxArc || minTime > maxTime)
			throw new IllegalStateException("illegal bounds");
	}

	public void connect() {
		checkParameters();
		init();

		for (ImmutablePoint source : vertices) {
			for (ImmutablePoint target : vertices) {
				if (source.equalsTopo(target))
					continue;

				if (edgeChecker.apply(source, target)) {
					DefaultWeightedEdge edge = graph.addEdge(source, target);

					if (edge != null)
						graph.setEdgeWeight(edge, weightCalculator.apply(source, target));
				}
			}
		}

		cleanUp();
	}

	private void init() {
		BoundsEdgeChecker boundsChecker = new BoundsEdgeChecker(
			minArc, maxArc, minTime, maxTime);
		VelocityEdgeChecker velocityEdgeChecker = new VelocityEdgeChecker(maxVelocity);
		VisibilityEdgeChecker visibilityChecker = new VisibilityEdgeChecker(forbiddenMap);

		edgeChecker = (v1, v2) ->
			boundsChecker.check(v1, v2) &&
			velocityEdgeChecker.check(v1, v2) &&
			visibilityChecker.check(v1, v2);
	}

	private void cleanUp() {
		edgeChecker = null;
	}

}
