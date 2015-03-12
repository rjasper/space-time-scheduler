package experimental;

import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import world.DynamicObstacle;
import world.SimpleTrajectory;
import world.SpatialPath;
import world.graph.view.GraphViewer;
import world.pathfinder.ForbiddenRegion;
import world.pathfinder.SimpleFixTimeMeshBuilder;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

public class VelocityGraphTest {

	public static void main(String[] args) {
		GraphViewer viewer = new GraphViewer();
		
		DynamicObstacle dummy = new DynamicObstacle(
			immutableBox(0, 0, 1, 1),
			new SimpleTrajectory(
				new SpatialPath(
					ImmutableList.of(immutablePoint(0, 0), immutablePoint(0, 0))),
				ImmutableList.of(LocalDateTime.MIN, LocalDateTime.MAX)));

		List<ForbiddenRegion> regions = Arrays.asList(
			box(200., 200., 400., 400.),
			box(400., 500., 600., 700.),
			box(100., 600., 300., 800.)
		).stream()
			.map((b) -> new ForbiddenRegion(b, dummy))
			.collect(Collectors.toList());

		SimpleFixTimeMeshBuilder meshBuilder = new SimpleFixTimeMeshBuilder();
//		MinimumTimeMeshBuilder meshBuilder = new MinimumTimeMeshBuilder();
		meshBuilder.setForbiddenRegions(regions);
		meshBuilder.setMinArc(0.0);
		meshBuilder.setMaxArc(600.0);
		meshBuilder.setMaxSpeed(1.0);
		meshBuilder.setStartPoint(point(0.0, 0.0));
		meshBuilder.setFinishPoint(point(600.0, 1000.0));
//		meshBuilder.setBufferDuration(0.0);

		meshBuilder.build();

		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph =
			meshBuilder.getResultMesh();

		viewer.setGraph(graph);
		viewer.showGraph();
	}

}
