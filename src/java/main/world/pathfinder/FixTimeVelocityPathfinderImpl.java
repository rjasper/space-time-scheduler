package world.pathfinder;

import java.time.Duration;
import java.util.Collection;
import java.util.stream.Stream;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import util.DurationConv;

public class FixTimeVelocityPathfinderImpl extends FixTimeVelocityPathfinder {
	
	private static final double ARC_START = 0.0;
	
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
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		Duration duration = Duration.between(getBaseTime(), getStartTime());
		double timeOffset = DurationConv.inSeconds(duration);
		
		arcTimeStartPoint = geomBuilder.point(ARC_START, timeOffset);
	}

	private Point getArcTimeFinishPoint() {
		return arcTimeFinishPoint;
	}
	
	private void updateArcTimeFinishPoint() {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		double maxArc = getMaxArc();
		Duration duration = Duration.between(getBaseTime(), getFinishTime());
		double timeOffset = DurationConv.inSeconds(duration);
		
		arcTimeFinishPoint = geomBuilder.point(maxArc, timeOffset);
	}
	
//	@Override
//	protected boolean calculateTrajectoryImpl() {
//		updateArcTimeStartPoint();
//		updateArcTimeFinishPoint();
//		
//		Collection<ForbiddenRegion> forbiddenRegions =
//			calculateForbiddenRegions();
//		
//		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh =
//			buildMesh(forbiddenRegions);
//		
//		LineString arcTimePath =
//			calculateArcTimePath(mesh);
//		
//		Trajectory trajectory = arcTimePath == null
//			? null
//			: calculateTrajectory(arcTimePath);
//		
//		setResultTrajectory(trajectory);
//		
//		return trajectory != null;
//	}

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
			
			EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
			
			return geomBuilder.lineString(points);
		}
	}

}
