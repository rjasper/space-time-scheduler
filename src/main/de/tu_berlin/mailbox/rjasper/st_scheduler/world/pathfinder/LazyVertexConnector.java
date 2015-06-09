package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometryIterator;

/**
 * Adds vertices and edges to a graph to provide stationary path segments.
 *
 * @author Rico Jasper
 */
public class LazyVertexConnector {

	private DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph;

	private double minArc = Double.NaN;

	private double maxArc = Double.NaN;

	private double minTime = Double.NaN;

	private double maxTime = Double.NaN;

	private double minStopDuration = Double.NaN;

	private double lazyVelocity = Double.NaN;

	private Geometry forbiddenMap;

	private BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator = null;

	private transient Collection<Ray> motionRays;

	private transient Collection<Ray> stationaryRays;

	private static class Ray {
		public final ImmutablePoint origin;
		public final LineString line;
		public Ray(ImmutablePoint origin, LineString line) {
			this.origin = origin;
			this.line = line;
		}
	}

	public void setGraph(
		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph) {
		this.graph = requireNonNull(graph, "graph");
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

	public void setMinStopDuration(double minStopDuration) {
		if (!Double.isFinite(minStopDuration) || minStopDuration < 0)
			throw new IllegalArgumentException("illegal minStopDuration");

		this.minStopDuration = minStopDuration;
	}

	public void setLazyVelocity(double lazyVelocity) {
		if (!Double.isFinite(lazyVelocity) || lazyVelocity <= 0)
			throw new IllegalArgumentException("illegal velocity");

		this.lazyVelocity = lazyVelocity;
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
			Double.isNaN(maxArc) ||
			Double.isNaN(minTime) ||
			Double.isNaN(maxTime) ||
			weightCalculator == null ||
			forbiddenMap == null ||
			Double.isNaN(minStopDuration) ||
			Double.isNaN(lazyVelocity))
		{
			throw new IllegalStateException("unset parameters");
		}

		if (minArc > maxArc || minTime > maxTime)
			throw new IllegalStateException("illegal bounds");
	}

	public void connect() {
		checkParameters();

		calcMotionRays();
		calcStationaryRays();

		connectRayIntersections();

		// clean up
		motionRays = null;
		stationaryRays = null;
	}

	private void calcMotionRays() {
		motionRays = graph.vertexSet().stream()
			.filter(this::within)
			.map(this::calcMotionRay)
			.filter(r -> r != null)
			.collect(toList());
	}

	private Ray calcMotionRay(ImmutablePoint origin) {
		double s1 = origin.getX();
		double s2 = minArc;
		double ds = s1 - s2;
		double t1 = origin.getY();
		double t2 = t1 - ds/lazyVelocity;

		LineString fullLine = immutableLineString(s1, t1, s2, t2);

		Geometry intersection = forbiddenMap.intersection(fullLine);

		double s3 = s2;
		double t3 = t2;

		if (!intersection.isEmpty()) {
			// seek first line string intersection
			int n = intersection.getNumGeometries();
			for (int i = 0; i < n; ++i) {
				Geometry g = intersection.getGeometryN(i);

				if (g.isEmpty() || !(g instanceof LineString))
					continue;

				LineString line = (LineString) g;
				Point startPoint = line.getStartPoint();

				if (startPoint.equalsTopo(origin))
					return null;

				// if later collision
				if (startPoint.getY() > t3) {
					s3 = startPoint.getX();
					t3 = startPoint.getY();
				}
			}
		}

		// if no line string intersection
		if (t3 <= t2)
			return new Ray(origin, fullLine);
		else
			return new Ray(origin, immutableLineString(s1, t1, s3, t3));
	}

	private void calcStationaryRays() {
		stationaryRays = graph.vertexSet().stream()
			.filter(this::within)
			.map(this::calcStationaryRay)
			.filter(r -> r != null)
			.collect(toList());
	}

	private Ray calcStationaryRay(ImmutablePoint origin) {
		double s1 = origin.getX();
		double s2 = s1;
		double t1 = origin.getY();
		double t2 = maxTime;

		if (t1 >= t2)
			return null;

		LineString fullLine = immutableLineString(s1, t1, s2, t2);

		MultiLineString fullIntersection = toMultiLineString(
			forbiddenMap.intersection(fullLine));
		MultiLineString boundaryIntersection = toMultiLineString(
			boundary(forbiddenMap).intersection(fullLine));
		Geometry intersection = fullIntersection.difference(boundaryIntersection);

		double s3 = s2;
		double t3 = t2;

		if (!intersection.isEmpty()) {
			// seek first line string intersection
			int n = intersection.getNumGeometries();
			for (int i = 0; i < n; ++i) {
				Geometry g = intersection.getGeometryN(i);

				if (g.isEmpty() || !(g instanceof LineString))
					continue;

				LineString line = (LineString) g;
				Point startPoint = line.getStartPoint();

				if (startPoint.equalsTopo(origin))
					return null;

				// if earlier collision
				if (startPoint.getY() < t3) {
					s3 = startPoint.getX();
					t3 = startPoint.getY();
				}
			}
		}

		// if no line string intersection
		if (t3 >= t2)
			return new Ray(origin, fullLine);
		else
			return new Ray(origin, immutableLineString(s1, t1, s3, t3));
	}

	private boolean within(ImmutablePoint vertex) {
		double arc = vertex.getX();
		double time = vertex.getY();

		return arc  >= minArc  && arc  <= maxArc
			&& time >= minTime && time <= maxTime;
	}

	private MultiLineString boundary(Geometry geometry) {
		if (geometry instanceof MultiLineString)
			return (MultiLineString) geometry;

		GeometryIterator it = new GeometryIterator(geometry, true, true, true);
		List<LineString> lines = new LinkedList<>();

		while (it.hasNext()) {
			Geometry g = it.next();

			if (!g.isEmpty() && g instanceof LineString)
				lines.add((LineString) g);
		}

		return multiLineString(lines.toArray(new LineString[lines.size()]));
	}

	private MultiLineString toMultiLineString(Geometry geometry) {
		if (geometry instanceof MultiLineString)
			return (MultiLineString) geometry;

		GeometryIterator it = new GeometryIterator(geometry, true, false, false);
		List<LineString> lines = new LinkedList<>();

		while (it.hasNext()) {
			Geometry g = it.next();

			if (!g.isEmpty() && g instanceof LineString)
				lines.add((LineString) g);
		}

		return multiLineString(lines.toArray(new LineString[lines.size()]));
	}

	private void connectRayIntersections() {
		for (Ray sr : stationaryRays) {
			for (Ray mr : motionRays) {
				if (sr.origin.equals(mr.origin))
					continue;

				Geometry intersection = sr.line.intersection( mr.line );

				if (intersection.isEmpty())
					continue;

				Point intersectionPoint = (Point) intersection;

				// using origin arc ordinate to ensure stationary path segment
				ImmutablePoint vertex = immutablePoint(sr.origin.getX(), intersectionPoint.getY());

				// don't stop for small durations
				if (vertex.getY() - sr.origin.getY() < minStopDuration)
					continue;

				graph.addVertex(vertex);
				connect(sr.origin, vertex);
				connect(vertex, mr.origin);
			}
		}
	}

	private void connect(ImmutablePoint source, ImmutablePoint target) {
		if (source.equalsTopo(target))
			return;

		DefaultWeightedEdge e = graph.addEdge(source, target);

		// if new edge
		if (e != null)
			graph.setEdgeWeight(e, weightCalculator.apply(source, target));
	}

}
