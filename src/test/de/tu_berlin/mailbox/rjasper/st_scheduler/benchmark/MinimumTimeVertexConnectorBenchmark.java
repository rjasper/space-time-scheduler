package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.Duration;
import java.util.function.BiFunction;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.MinimumTimeVertexConnector;

public class MinimumTimeVertexConnectorBenchmark implements Benchmarkable {

	private StopWatch sw = new StopWatch();

	@Override
	public int minProblemSize() {
		return 1000;
	}

	@Override
	public int maxProblemSize() {
		return 10000;
	}

	@Override
	public int stepProblemSize() {
		return 1000;
	}

	private static final BiFunction<ImmutablePoint, ImmutablePoint, Double> WEIGHT_CALCULATOR =
		(v1, v2) -> 1.0;

	@Override
	public Duration benchmark(int n) {
		sw.reset();

		MinimumTimeVertexConnector connector = new MinimumTimeVertexConnector();

		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph =
			new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		Geometry forbiddenMap = makeForbiddenMap(n);

		forbiddenMap.apply((Coordinate coord) -> {
			graph.addVertex( immutablePoint(coord.x, coord.y) );
		});

		connector.setGraph(graph);
		connector.setForbiddenMap(forbiddenMap);
		connector.setMaxVelocity(5);
		connector.setBufferDuration(0);
		connector.setMinArc(0);
		connector.setMinTime(0);
		connector.setMinFinishTime(0);
		connector.setMaxFinishTime(13);
		connector.setFinishArc(13);
		connector.setWeightCalculator(WEIGHT_CALCULATOR);

		sw.start();
		connector.connect();
		sw.stop();

//		System.out.println(forbiddenMap);
//
//		LineString[] edges = graph.edgeSet().stream()
//			.map(e -> lineString(graph.getEdgeSource(e), graph.getEdgeTarget(e)))
//			.toArray(m -> new LineString[m]);
//		System.out.println(multiLineString(edges));

		return sw.duration();
	}

	private Geometry makeForbiddenMap(int n) {
		return multiPolygon(
			circle(4, 4, 1, n),  // bottom left
			circle(9, 4, 1, n),  // bottom right
			circle(4, 9, 1, n),  // top left
			circle(9, 9, 1, n)); // top right
	}

}
