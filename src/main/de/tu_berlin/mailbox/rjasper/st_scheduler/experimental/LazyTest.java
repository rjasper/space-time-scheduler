package de.tu_berlin.mailbox.rjasper.st_scheduler.experimental;

import static com.vividsolutions.jts.geom.IntersectionMatrix.*;
import static com.vividsolutions.jts.geom.Location.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.stream.Collectors.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometryIterable;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometrySplitter;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.ForbiddenRegion;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.LazyVertexConnector;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.MinimumTimeVertexConnector;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.SimpleVertexConnector;
import de.tu_berlin.mailbox.rjasper.time.TimeConv;

public class LazyTest {

	public static final LocalDateTime BASE_TIME =
		LocalDateTime.of(2000, 1, 1, 0, 0);

	public static LocalDateTime atSecond(double second) {
		Duration offset = TimeConv.secondsToDurationSafe(second);

		return BASE_TIME.plus(offset);
	}

	private static final DynamicObstacle DUMMY_OBSTACLE;

	static {
		ImmutablePolygon dummyShape = immutableBox(0, 0, 1, 1);
		Trajectory dummyTrajectory = new SimpleTrajectory(
			new SpatialPath(ImmutableList.of(
				immutablePoint(0, 0),
				immutablePoint(1, 1))),
			ImmutableList.of(
				atSecond(0),
				atSecond(1)));
		DUMMY_OBSTACLE = new DynamicObstacle(dummyShape, dummyTrajectory);
	}

	private static ForbiddenRegion forbiddenRegion(Geometry geometry) {
		return new ForbiddenRegion(geometry, DUMMY_OBSTACLE);
	}

	public static void main(String[] args) {
		Collection<ForbiddenRegion> forbiddenRegions = Stream.<Geometry>builder()
			.add( immutableBox(9.5, 9.5, 10.5, 10.5) )
			.add( immutableBox(5, 6, 6, 7) )
			.add( immutableBox(1.5, 5, 2.5, 6) )
			.add( immutableBox(1, 3, 2, 4) )
			.add( immutableBox(3.5, 3, 4.5, 4) )
			.add( immutableBox(1, 1, 2, 2) )
			.build()
			.map(LazyTest::forbiddenRegion)
			.collect(toList());

		ImmutablePoint startVertex = immutablePoint(0, 0);
		ImmutablePoint finishVertex = immutablePoint(10, 10);
		BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator =
			(s, t) -> {
				if (s.getX() == t.getX())
					return 0.0;
				else
					return t.getY() - t.getY();
			};
		double maxVelocity = 2.0;
		double lazyVelocity = 1.25;
		double buffer = 1;

		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph =
			new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		graph.addVertex(startVertex);
		graph.addVertex(finishVertex);
		forbiddenRegions.stream()
			.map(ForbiddenRegion::getRegion)
			.map(Geometry::getCoordinates)
			.flatMap(Arrays::stream)
			.map(c -> immutablePoint(c.x, c.y))
			.forEach(graph::addVertex);


		Geometry forbiddenMap = makeForbiddenMap(forbiddenRegions);

		MinimumTimeVertexConnector mtConnector = new MinimumTimeVertexConnector();

		ConnectionChecker cc = new ConnectionChecker(
			startVertex.getX(), finishVertex.getX(),
			startVertex.getY(), finishVertex.getY(),
			forbiddenMap,
			maxVelocity,
			buffer);

		mtConnector.setGraph(graph);
		mtConnector.setStartVertex(startVertex);
		mtConnector.setFinishArc(10);
		mtConnector.setMinFinishTime(0);
		mtConnector.setMaxFinishTime(20);
		mtConnector.setEdgeChecker(cc::checkConnection);
		mtConnector.setWeightCalculator(weightCalculator);
		mtConnector.setMaxVelocity(maxVelocity);

		mtConnector.connect();

		SimpleVertexConnector simpleConnector = new SimpleVertexConnector();

		simpleConnector.setGraph(graph);
		simpleConnector.setStartVertex(startVertex);
		simpleConnector.setFinishVertex(finishVertex);
		simpleConnector.setWeightCalculator(weightCalculator);
		simpleConnector.setForbiddenMap(forbiddenMap);
		simpleConnector.setMaxVelocity(maxVelocity);

		simpleConnector.connect();

		LazyVertexConnector lazyConnector = new LazyVertexConnector();

		lazyConnector.setGraph(graph);
		lazyConnector.setStartVertex(startVertex);
		lazyConnector.setFinishVertex(finishVertex);
		lazyConnector.setWeightCalculator(weightCalculator);
		lazyConnector.setForbiddenMap(forbiddenMap);
		lazyConnector.setLazyVelocity(lazyVelocity);

		lazyConnector.connect();

		System.out.println(graph.edgeSet().size());

		System.out.println(toGeometry(graph));

		Geometry[] regions = forbiddenRegions.stream()
			.map(ForbiddenRegion::getRegion)
			.toArray(n -> new Geometry[n]);

		System.out.println(geometryCollection(regions));
	}

	private static Geometry toGeometry(Graph<ImmutablePoint, DefaultWeightedEdge> graph) {
		LineString[] lines = graph.edgeSet().stream()
			.map(e -> lineString(graph.getEdgeSource(e), graph.getEdgeTarget(e)))
			.toArray(n -> new LineString[n]);

		return multiLineString(lines);
	}

	private static Geometry makeForbiddenMap(Collection<ForbiddenRegion> forbiddenRegions) {
		Geometry[] regions = forbiddenRegions.stream()
			.map(ForbiddenRegion::getRegion)
			.toArray(n -> new Geometry[n]);

		return geometryCollection(regions);
	}

	private static class ConnectionChecker {

		private final double startArc;
		private final double finishArc;
		private final double startTime;
		private final double finishTime;
		private final Geometry forbiddenMap;
		private final double maxVelocity;
		private final double buffer;

		public ConnectionChecker(double startArc, double finishArc,
			double startTime, double finishTime, Geometry forbiddenMap,
			double maxVelocity, double buffer) {
			this.startArc = startArc;
			this.finishArc = finishArc;
			this.startTime = startTime;
			this.finishTime = finishTime;
			this.forbiddenMap = forbiddenMap;
			this.maxVelocity = maxVelocity;
			this.buffer = buffer;
		}

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

			return checkVisibility(from, to) && checkBuffer(to);
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


		/**
		 * Checks if the vertex is collision free at its arc for at least
		 * {@link #bufferDuration} amount of time.
		 *
		 * @param vertex
		 * @return {@code true} if the vertex is collision free.
		 */
		private boolean checkBuffer(Point vertex) {
			double s = vertex.getX(), t = vertex.getY();

			Point p1 = vertex;
			Point p2 = point(s, t + buffer);

			return checkVisibility(p1, p2);
		}
	}

}
