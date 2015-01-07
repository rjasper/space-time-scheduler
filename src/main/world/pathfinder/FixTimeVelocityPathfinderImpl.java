package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

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
		// The mesh builder uses immutable geometries.
		// It is important that the start point and the finish point
		// are structurally the same to the ones used in the mesh. Therefore,
		// the points are already converted immutable here.
		arcTimeStartPoint = immutable(
			geomBuilder.point(getStartArc(), inSeconds(getStartTime())));
	}

	private Point getArcTimeFinishPoint() {
		return arcTimeFinishPoint;
	}
	
	private void updateArcTimeFinishPoint() {
		// The mesh builder uses immutable geometries.
		// It is important that the start point and the finish point
		// are structurally the same to the ones used in the mesh. Therefore,
		// the points are already converted immutable here.
		arcTimeFinishPoint = immutable(
			geomBuilder.point(getFinishArc(), inSeconds(getFinishTime())));
	}

	@Override
	protected List<Point> calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions) {
		updateArcTimeStartPoint();
		updateArcTimeFinishPoint();
	
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh =
			buildMesh(forbiddenRegions);
		
		List<Point> arcTimePath =
			calculateShortestPath(mesh);
		
		return arcTimePath;
	}

	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> buildMesh(
		Collection<ForbiddenRegion> forbiddenRegions)
	{
		double maxSpeed = getMaxSpeed();
		double maxArc = getFinishArc();
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

	private List<Point> calculateShortestPath(
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
			
			List<Point> points = Stream.concat(sourcePoints, endPoint)
				.collect(Collectors.toList());
			
			return points;
		}
	}

}
