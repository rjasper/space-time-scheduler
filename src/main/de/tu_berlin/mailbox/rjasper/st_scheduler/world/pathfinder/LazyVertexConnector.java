package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.time.Duration;
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

public class LazyVertexConnector {

	private DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph;

	private ImmutablePoint startVertex;

	private ImmutablePoint finishVertex;

	private BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator = null;

	private Geometry forbiddenMap;

	private double lazyVelocity = Double.NaN;

	private Duration minStopDuration = Duration.ZERO;

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

	public void setLazyVelocity(double lazyVelocity) {
		if (!Double.isFinite(lazyVelocity) || lazyVelocity <= 0)
			throw new IllegalArgumentException("illegal velocity");

		this.lazyVelocity = lazyVelocity;
	}

	public void setMinStopDuration(Duration minStopDuration) {
		requireNonNull(minStopDuration, "minStopDuration");

		if (minStopDuration.isNegative())
			throw new IllegalArgumentException("illegal duration");

		this.minStopDuration = minStopDuration;
	}

	private void checkParameters() {
		if (graph == null ||
			startVertex == null ||
			finishVertex == null ||
			weightCalculator == null ||
			forbiddenMap == null ||
			Double.isNaN(lazyVelocity))
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
			.filter(v -> !v.equals(startVertex))
			.map(this::calcMotionRay)
			.filter(r -> r != null)
			.collect(toList());
	}

	private boolean within(ImmutablePoint vertex) {
		double minArc = startVertex.getX();
		double maxArc = finishVertex.getX();
		double minTime = startVertex.getY();
		double maxTime = finishVertex.getY();
		double arc = vertex.getX();
		double time = vertex.getY();

		return arc  >= minArc  && arc  <= maxArc
			&& time >= minTime && time <= maxTime;
	}

	private Ray calcMotionRay(ImmutablePoint origin) {
		double s1 = origin.getX();
		double s2 = startVertex.getX();
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
			.filter(v -> !v.equals(finishVertex))
			.map(this::calcStationaryRay)
			.filter(r -> r != null)
			.collect(toList());
	}

	private Ray calcStationaryRay(ImmutablePoint origin) {
		double s1 = origin.getX();
		double s2 = s1;
		double t1 = origin.getY();
		double t2 = finishVertex.getY();

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
		double minStop = durationToSeconds(minStopDuration);

		for (Ray sr : stationaryRays) {
			for (Ray mr : motionRays) {
				Geometry intersection = sr.line .intersection( mr.line );

				if (intersection.isEmpty())
					continue;

				ImmutablePoint vertex = immutable((Point) intersection);

				// don't stop for small durations
				if (vertex.getY() - sr.origin.getY() < minStop)
					continue;

				graph.addVertex(vertex);
				connect(sr.origin, vertex);
				connect(vertex, mr.origin);
			}
		}
	}

	private void connect(ImmutablePoint source, ImmutablePoint target) {
		DefaultWeightedEdge e = graph.addEdge(source, target);

		// if new edge
		if (e != null)
			graph.setEdgeWeight(e, weightCalculator.apply(source, target));
	}

}
