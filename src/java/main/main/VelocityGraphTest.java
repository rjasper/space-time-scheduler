package main;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Point;

import world.DynamicObstacle;
import world.graph.view.GraphViewer;
import world.pathfinder.ArcTimeMeshBuilder;
import world.pathfinder.ForbiddenRegion;

public class VelocityGraphTest {

	public static void main(String[] args) {
		GraphViewer viewer = new GraphViewer();
		
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		List<ForbiddenRegion> regions = Arrays.asList(
			geomBuilder.box(200., 200., 400., 400.),
			geomBuilder.box(400., 500., 600., 700.),
			geomBuilder.box(100., 600., 300., 800.)
		).stream()
			.map((b) -> new ForbiddenRegion(b, new DynamicObstacle(null, null)))
			.collect(Collectors.toList());
		
		ArcTimeMeshBuilder meshBuilder = new ArcTimeMeshBuilder();
		meshBuilder.setForbiddenRegions(regions);
		meshBuilder.setMaxArc(600.0);
		meshBuilder.setMaxSpeed(1.0);
		meshBuilder.setStartPoint(geomBuilder.point(0.0, 0.0));
		meshBuilder.setFinishPoint(geomBuilder.point(600.0, 1000.0));
		
		meshBuilder.build();
		
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph =
			meshBuilder.getResultMesh();
		
		viewer.setGraph(graph);
		viewer.showGraph();
	}

}
