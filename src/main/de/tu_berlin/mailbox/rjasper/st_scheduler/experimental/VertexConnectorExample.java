package de.tu_berlin.mailbox.rjasper.st_scheduler.experimental;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.stream.Collectors.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.ForbiddenRegion;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.LazyMinimumTimeMesher;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.LazyMinimumTimeMesher.MeshResult;
import de.tu_berlin.mailbox.rjasper.time.TimeConv;

public class VertexConnectorExample {

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
			.add( immutableBox(9.5, 8, 10.5, 9) )
			.add( immutableBox(5, 6, 6, 7) )
			.add( immutableBox(1.5, 5, 2.5, 6) )
			.add( immutableBox(1, 3, 2, 4) )
			.add( immutableBox(3.5, 3, 4.5, 4) )
			.add( immutableBox(1, 1, 2, 2) )
			.build()
			.map(VertexConnectorExample::forbiddenRegion)
			.collect(toList());

		ImmutablePoint startVertex = immutablePoint(0, 0);
		double finishArc = 10.0;
		double minFinishTime = 5.0;
		double maxFinishTime = 20.0;
		double minStopDuration = 0.0;
		double maxVelocity = 1.9;
		double lazyVelocity = 1.25;
		double bufferDuration = 0.0;

		BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator =
			(s, t) -> s.getX() == t.getX() ? 0.0 : t.getY() - s.getY();

		LazyMinimumTimeMesher mesher = new LazyMinimumTimeMesher();

		mesher.setStartVertex(startVertex);
		mesher.setFinishArc(finishArc);
		mesher.setMinFinishTime(minFinishTime);
		mesher.setMaxFinishTime(maxFinishTime);
		mesher.setForbiddenRegions(forbiddenRegions);
		mesher.setMaxVelocity(maxVelocity);
		mesher.setLazyVelocity(lazyVelocity);
		mesher.setMinStopDuration(minStopDuration);
		mesher.setBufferDuration(bufferDuration);
		mesher.setWeightCalculator(weightCalculator);

		MeshResult res = mesher.mesh();
		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph = res.graph;
		ImmutablePoint finishVertex = res.finishVertex;

//		ImmutablePoint finishVertex = immutablePoint(10, 10);
//
//		LazyFixTimeMesher mesher = new LazyFixTimeMesher();
//
//		mesher.setStartVertex(startVertex);
//		mesher.setFinishVertex(finishVertex);
//		mesher.setForbiddenRegions(forbiddenRegions);
//		mesher.setMaxVelocity(maxVelocity);
//		mesher.setLazyVelocity(lazyVelocity);
//		mesher.setMinStopDuration(minStopDuration);
//		mesher.setWeightCalculator(weightCalculator);
//
//		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph =
//			mesher.mesh();

		System.out.println(graph.edgeSet().size());

		System.out.println(toGeometry(graph));

		Geometry[] regions = forbiddenRegions.stream()
			.map(ForbiddenRegion::getRegion)
			.toArray(n -> new Geometry[n]);

		System.out.println(geometryCollection(regions));

		List<ImmutablePoint> path = shortestPath(graph, startVertex, finishVertex);

		Geometry pathGeometry = lineString(path.toArray(new Point[path.size()]));

		System.out.println(pathGeometry);
		System.out.println(graph);
	}

	private static Geometry toGeometry(Graph<ImmutablePoint, DefaultWeightedEdge> graph) {
		LineString[] lines = graph.edgeSet().stream()
			.map(e -> lineString(graph.getEdgeSource(e), graph.getEdgeTarget(e)))
			.toArray(n -> new LineString[n]);

		return multiLineString(lines);
	}

	private static List<ImmutablePoint> shortestPath(
		Graph<ImmutablePoint, DefaultWeightedEdge> graph,
		ImmutablePoint startVertex,
		ImmutablePoint finishVertex)
	{
		List<DefaultWeightedEdge> edges =
			DijkstraShortestPath.findPathBetween(graph, startVertex, finishVertex);

		Stream<ImmutablePoint> targets = edges.stream()
			.map(graph::getEdgeTarget);

		return Stream.concat(Stream.of(startVertex), targets)
			.collect(toList());
	}

}
