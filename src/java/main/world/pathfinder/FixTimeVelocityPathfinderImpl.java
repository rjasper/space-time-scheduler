package world.pathfinder;

import java.time.Duration;
import java.time.LocalDateTime;
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
import world.DynamicObstacle;
import world.Trajectory;
import world.TrajectoryBuilder;

public class FixTimeVelocityPathfinderImpl extends FixTimeVelocityPathfinder {
	
	private ForbiddenRegionBuilder forbiddenRegionBuilder =
		new ForbiddenRegionBuilder();
	
	private FixTimeMeshBuilder meshBuilder =
		new FixTimeMeshBuilder();
	
	private TrajectoryBuilder trajBuilder =
		new TrajectoryBuilder();
	
	private Point arcTimeStartPoint;
	
	private Point arcTimeFinishPoint;
	
	private double maxArc;

	private ForbiddenRegionBuilder getForbiddenRegionBuilder() {
		return forbiddenRegionBuilder;
	}
	
	private FixTimeMeshBuilder getMeshBuilder() {
		return meshBuilder;
	}

	private TrajectoryBuilder getTrajectoryBuilder() {
		return trajBuilder;
	}

	private Point getArcTimeStartPoint() {
		return arcTimeStartPoint;
	}
	
	private void updateArcTimeStartPoint() {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		this.arcTimeStartPoint = geomBuilder.point(0., 0.);
	}

	private Point getArcTimeFinishPoint() {
		return arcTimeFinishPoint;
	}
	
	private void updateArcTimeFinishPoint() {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		double maxArc = getMaxArc();
		Duration duration = Duration.between(getStartTime(), getFinishTime());
		double maxTime = DurationConv.inSeconds(duration);
		
		arcTimeFinishPoint = geomBuilder.point(maxArc, maxTime);
	}
	
	private double getMaxArc() {
		return maxArc;
	}
	
	private void updateMaxArc() {
		maxArc = getSpatialPath().getLength();
	}

	@Override
	protected boolean calculatePathImpl() {
		updateMaxArc();
		updateArcTimeStartPoint();
		updateArcTimeFinishPoint();
		
		Collection<ForbiddenRegion> forbiddenRegions =
			calculateForbiddenRegions();
		
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh =
			buildMesh(forbiddenRegions);
		
		LineString arcTimePath =
			calculateArcTimePath(mesh);
		
		Trajectory trajectory = arcTimePath == null
			? null
			: calculateTrajectory(arcTimePath);
		
		setResultTrajectory(trajectory);
		
		return trajectory != null;
	}

	private Collection<ForbiddenRegion> calculateForbiddenRegions() {
		LocalDateTime baseTime = getStartTime();
		Collection<DynamicObstacle> dynamicObstacles = getDynamicObstacles();
		LineString spatialPath = getSpatialPath();
		
		ForbiddenRegionBuilder builder = getForbiddenRegionBuilder();
		
		builder.setBaseTime(baseTime);
		builder.setDynamicObstacles(dynamicObstacles);
		builder.setSpatialPath(spatialPath);
		
		builder.calculate();
		
		return builder.getResultForbiddenRegions();
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

	private LineString calculateArcTimePath(
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
	
	private Trajectory calculateTrajectory(LineString arcTimePath) {
		LocalDateTime baseTime = getStartTime();
		LineString spatialPath = getSpatialPath();
		TrajectoryBuilder trajBuilder = getTrajectoryBuilder();

		trajBuilder.setBaseTime(baseTime);
		trajBuilder.setSpatialPath(spatialPath);
		trajBuilder.setArcTimePath(arcTimePath);
		
		trajBuilder.calculate();
		
		return trajBuilder.getResultTrajectory();
	}

}
