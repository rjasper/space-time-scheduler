package world.pathfinder;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import world.DynamicObstacle;
import world.Trajectory;

public class FixTimeVelocityPathfinderImpl extends FixTimeVelocityPathfinder {
	
	private ForbiddenRegionBuilder forbiddenRegionBuilder =
		new ForbiddenRegionBuilder();
	
	private ArcTimeMeshBuilder arcTimeMeshBuilder =
		new ArcTimeMeshBuilder();

	// TODO set base time
	private LocalDateTime baseTime;

	private ForbiddenRegionBuilder getForbiddenRegionBuilder() {
		return forbiddenRegionBuilder;
	}

	private ArcTimeMeshBuilder getArcTimeMeshBuilder() {
		return arcTimeMeshBuilder;
	}

	private LocalDateTime getBaseTime() {
		return baseTime;
	}

	public void setBaseTime(LocalDateTime baseTime) {
		this.baseTime = baseTime;
	}

	@Override
	protected boolean calculatePathImpl() {
		Collection<ForbiddenRegion> forbiddenRegions =
			calculateForbiddenRegions();
		
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh =
			buildMesh(forbiddenRegions);
		
		List<Point> arcTimePath =
			calculateArcTimePath(mesh);
		
		Trajectory trajectory =
			calculateTrajectory(arcTimePath);
		
		setResultTrajectory(trajectory);
		
		return trajectory != null;
	}

	private Collection<ForbiddenRegion> calculateForbiddenRegions() {
		LocalDateTime baseTime = getBaseTime();
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
		double maxArc = getSpatialPath().getLength();
		Point startPoint = getStartPoint();
		Point finishPoint = getFinishPoint();
		
		ArcTimeMeshBuilder builder = getArcTimeMeshBuilder();
		
		builder.setForbiddenRegions(forbiddenRegions);
		builder.setMaxSpeed(maxSpeed);
		builder.setMaxArc(maxArc);
		builder.setStartPoint(startPoint);
		builder.setFinishPoint(finishPoint);
		
		builder.build();
		
		return builder.getResultMesh();
	}

	private List<Point> calculateArcTimePath(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh)
	{
		Point startPoint = getStartPoint();
		Point finishPoint = getFinishPoint();
		
		DijkstraShortestPath<Point, DefaultWeightedEdge> dijkstra =
			new DijkstraShortestPath<>(mesh, startPoint, finishPoint);
		
		GraphPath<Point, DefaultWeightedEdge> graphPath =
			dijkstra.getPath();
		
		if (graphPath == null) {
			return null;
		} else {
			Stream<Point> sourcePoints = graphPath.getEdgeList().stream()
				.map(mesh::getEdgeSource);
			Stream<Point> endPoint = Stream.of(graphPath.getEndVertex());
			
			return Stream.concat(sourcePoints, endPoint)
				.collect(Collectors.toList());
		}
	}

	private Trajectory calculateTrajectory(List<Point> arcTimePath) {
		// TODO Auto-generated method stub
		return null;
	}

}
