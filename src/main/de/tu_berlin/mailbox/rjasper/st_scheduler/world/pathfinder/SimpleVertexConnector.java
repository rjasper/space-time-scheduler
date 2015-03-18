package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static com.vividsolutions.jts.geom.IntersectionMatrix.*;
import static com.vividsolutions.jts.geom.Location.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Objects.*;

import java.util.Collection;
import java.util.function.BiFunction;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometryIterable;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometrySplitter;

public class SimpleVertexConnector {

	private DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph;

	private ImmutablePoint startVertex;

	private ImmutablePoint finishVertex;

	private BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator = null;

	private Geometry forbiddenMap;

	private double maxVelocity = Double.NaN;

	public void setGraph(
		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph) {
		this.graph = requireNonNull(graph, "graph");
	}

	public void setStartVertex(ImmutablePoint startVertex) {
		this.startVertex = requireNonNull(startVertex, "startVertex");
	}

	public void setFinishVertex(ImmutablePoint finishVertex) {
		this.finishVertex = requireNonNull(finishVertex, "finishVertex");
	}

	public void setWeightCalculator(BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator) {
		this.weightCalculator = requireNonNull(weightCalculator, "weightCalculator");
	}

	public void setForbiddenMap(Geometry forbiddenMap) {
		this.forbiddenMap = requireNonNull(forbiddenMap, "forbiddenMap");
	}

	public void setMaxVelocity(double maxVelocity) {
		if (!Double.isFinite(maxVelocity) || maxVelocity <= 0)
			throw new IllegalArgumentException("illegal velocity");

		this.maxVelocity = maxVelocity;
	}

	private void checkParameters() {
		if (
			graph == null ||
			startVertex == null ||
			finishVertex == null ||
			weightCalculator == null ||
			forbiddenMap == null ||
			Double.isNaN(maxVelocity))
		{
			throw new IllegalStateException("unset parameters");
		}

		if (startVertex.getX() >= finishVertex.getX() ||
			startVertex.getY() >= finishVertex.getY())
		{
			throw new IllegalStateException("startVertex and finishVertex incompatible");
		}
	}

	public void connect() {
		checkParameters();

		Collection<ImmutablePoint> vertices = graph.vertexSet();

		for (ImmutablePoint source : vertices) {
			for (ImmutablePoint target : vertices) {
				if (checkConnection(source, target)) {
					DefaultWeightedEdge edge = graph.addEdge(source, target);

					if (edge != null)
						graph.setEdgeWeight(edge, weightCalculator.apply(source, target));
				}
			}
		}
	}

//	/**
//	 * A helper method to connect to vertices without checking for validity.
//	 *
//	 * @param graph
//	 * @param from from-point
//	 * @param to to-point
//	 */
//	protected void connectHelper(ImmutablePoint from, ImmutablePoint to) {
//		DefaultWeightedEdge edge = graph.addEdge(from, to);
//
//		if (edge != null)
//			graph.setEdgeWeight(edge, weightCalculator.apply(from, to));
//	}

	/**
	 * <p>
	 * Checks if two nodes can be connects. The following conditions have to be
	 * met:
	 * </p>
	 *
	 * <ul>
	 * <li>Both vertices' arc-ordinates are within [minArc, maxArc].</li>
	 * <li>Both vertices' time-ordinates are within [minTime, maxTime].</li>
	 * <li>The first vertex' time is before the second vertex' time.</li>
	 * <li>The maximum speed is not exceeded.</li>
	 * <li>The "line of sight" is not blocked by forbidden regions.</li>
	 * </ul>
	 * @param from
	 * @param to
	 * @return
	 */
	private boolean checkConnection(Point from, Point to) {
		double startArc = startVertex.getX();
		double finishArc = finishVertex.getX();
		double startTime = startVertex.getY();
		double finishTime = finishVertex.getY();

		double s1 = from.getX(), s2 = to.getX(), t1 = from.getY(), t2 = to.getY();

		// if vertex is not on path
		if (s1 < startArc || s1 > finishArc || s2 < startArc || s2 > finishArc)
			return false;

		// if vertex is not within time window
		if (t1 < startTime || t1 > finishTime || t2 < startTime || t2 > finishTime)
			return false;

		// if 'from' happens after 'to'
		if (t1 > t2)
			return false;

		if (from.equals(to))
			return false;

		// if maximum speed is exceeded
		if (Math.abs((s2 - s1) / (t2 - t1)) > maxVelocity)
			return false;

		return checkVisibility(from, to);
	}

	/**
	 * Checks if two points have a clear line of sight to each other. Forbidden
	 * regions might block the view.
	 *
	 * @param from from-point
	 * @param to to-point
	 * @return {@code true} if no forbidden region blocks the view
	 */
	private boolean checkVisibility(Point from, Point to) {
		LineString line = lineString(from, to);

		return new GeometryIterable(forbiddenMap, true, false, false).stream()
			.allMatch(new GeometrySplitter<Boolean>() {
				// just to be sure, handle all primitives
				// only polygons block the line of sight
				@Override
				protected Boolean take(Point point) {
					return true;
				}
				@Override
				protected Boolean take(LineString lineString) {
					return true;
				}
				@Override
				protected Boolean take(Polygon polygon) {
					IntersectionMatrix matrix = line.relate(polygon);

					return !isTrue(matrix.get(INTERIOR, INTERIOR));
				}
			}::give);
	}

}
