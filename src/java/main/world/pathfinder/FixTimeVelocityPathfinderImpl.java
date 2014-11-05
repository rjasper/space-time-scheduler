package world.pathfinder;

import java.util.Collection;
import java.util.stream.Stream;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class FixTimeVelocityPathfinderImpl extends FixTimeVelocityPathfinder {

	private EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
	
	private FixTimeMeshBuilder meshBuilder =
		new FixTimeMeshBuilder();
	
	private Point arcTimeStartPoint;
	
	private Point arcTimeFinishPoint;
	
	private FixTimeMeshBuilder getMeshBuilder() {
		return meshBuilder;
	}

	private Point getArcTimeStartPoint() {
		return arcTimeStartPoint;
	}
	
	private void updateArcTimeStartPoint() {
		arcTimeStartPoint = geomBuilder.point(getMinArc(), inSeconds(getStartTime()));
	}

	private Point getArcTimeFinishPoint() {
		return arcTimeFinishPoint;
	}
	
	private void updateArcTimeFinishPoint() {
		arcTimeFinishPoint = geomBuilder.point(getMaxArc(), inSeconds(getFinishTime()));
	}

	@Override
	protected LineString calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions) {
		updateArcTimeStartPoint();
		updateArcTimeFinishPoint();
	
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh =
			buildMesh(forbiddenRegions);
		
		LineString arcTimePath =
			calculateShortestPath(mesh);
		
		return arcTimePath;
	}

	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> buildMesh(
		Collection<ForbiddenRegion> forbiddenRegions)
	{
		double maxSpeed = getMaxSpeed();
		double maxArc = getMaxArc();
		Point startPoint = getArcTimeStartPoint();
		Point finishPoint = getArcTimeFinishPoint();
		
		FixTimeMeshBuilder builder = getMeshBuilder();
		
		builder.setForbiddenRegions(forbiddenRegions);
		builder.setMaxSpeed(maxSpeed);
		builder.setMaxArc(maxArc);
		builder.setStartPoint(startPoint);
		builder.setFinishPoint(finishPoint);
		
		builder.build();
		
		return builder.getResultMesh();
	}

	private LineString calculateShortestPath(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh)
	{
		Point startPoint = getArcTimeStartPoint();
		Point finishPoint = getArcTimeFinishPoint();
		
		DijkstraShortestPath<Point, DefaultWeightedEdge> dijkstra =
			new DijkstraShortestPath<>(mesh, startPoint, finishPoint);
		
		GraphPath<Point, DefaultWeightedEdge> graphPath =
			dijkstra.getPath();
		
		// if the finish point is unreachable
		if (graphPath == null) {
			return null;
		} else {
			Stream<Point> sourcePoints = graphPath.getEdgeList().stream()
				.map(mesh::getEdgeSource);
			Stream<Point> endPoint = Stream.of(graphPath.getEndVertex());
			
			Point[] points = Stream.concat(sourcePoints, endPoint)
				.toArray(Point[]::new);
			
			return geomBuilder.lineString(points);
		}
	}

}
