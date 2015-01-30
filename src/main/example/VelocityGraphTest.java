package example;

import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import world.DynamicObstacle;
import world.graph.view.GraphViewer;
import world.pathfinder.FixTimeMeshBuilder;
import world.pathfinder.ForbiddenRegion;

import com.vividsolutions.jts.geom.Point;

public class VelocityGraphTest {

	public static void main(String[] args) {
		GraphViewer viewer = new GraphViewer();

		List<ForbiddenRegion> regions = Arrays.asList(
			box(200., 200., 400., 400.),
			box(400., 500., 600., 700.),
			box(100., 600., 300., 800.)
		).stream()
			.map((b) -> new ForbiddenRegion(b, new DynamicObstacle(null, null)))
			.collect(Collectors.toList());

		FixTimeMeshBuilder meshBuilder = new FixTimeMeshBuilder();
//		MinimumTimeMeshBuilder meshBuilder = new MinimumTimeMeshBuilder();
		meshBuilder.setForbiddenRegions(regions);
		meshBuilder.setFinishArc(600.0);
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
