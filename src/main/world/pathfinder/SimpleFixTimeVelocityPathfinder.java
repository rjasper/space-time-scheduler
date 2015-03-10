package world.pathfinder;

import static common.collect.ImmutablesCollectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.util.Collection;
import java.util.stream.Stream;

import jts.geom.immutable.ImmutableGeometries;
import jts.geom.immutable.ImmutablePoint;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import world.ArcTimePath;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

/**
 * Implements a {@link AbstractFixTimePathfinder}. The resulting velocity
 * profile consists of straight line segments along the vertices of forbidden
 * regions. While this is a very simple solution it may result in unnecessary
 * slow movement instead of stopping and waiting.
 * 
 * @author Rico
 */
public class SimpleFixTimeVelocityPathfinder extends AbstractFixTimePathfinder {

	/**
	 * The mesh builder.
	 */
	private SimpleFixTimeMeshBuilder meshBuilder = new SimpleFixTimeMeshBuilder();
	
	/**
	 * The arc-time start point.
	 */
	private Point arcTimeStartPoint;
	
	/**
	 * The arc-time finish point.
	 */
	private Point arcTimeFinishPoint;
	
	/**
	 * @return the mesh builder.
	 */
	private SimpleFixTimeMeshBuilder getMeshBuilder() {
		return meshBuilder;
	}

	/**
	 * @return the arc-time start point.
	 */
	private Point getArcTimeStartPoint() {
		return arcTimeStartPoint;
	}
	
	/**
	 * Updates the arc-time start point using the start arc and time.
	 */
	private void updateArcTimeStartPoint() {
		// The mesh builder uses immutable geometries.
		// It is important that the start point and the finish point
		// are structurally the same to the ones used in the mesh.
		arcTimeStartPoint = immutablePoint(
			getStartArc(), inSeconds(getStartTime()));
	}

	/**
	 * @return the arc-time finish point.
	 */
	private Point getArcTimeFinishPoint() {
		return arcTimeFinishPoint;
	}
	
	/**
	 * Updates the arc-time finish point using the finish arc and time.
	 */
	private void updateArcTimeFinishPoint() {
		// The mesh builder uses immutable geometries.
		// It is important that the start point and the finish point
		// are structurally the same to the ones used in the mesh.
		arcTimeFinishPoint =  immutablePoint(
			getFinishArc(), inSeconds(getFinishTime()));
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.VelocityPathfinder#calculateArcTimePath(java.util.Collection)
	 */
	@Override
	protected ArcTimePath calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions) {
		updateArcTimeStartPoint();
		updateArcTimeFinishPoint();
	
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh =
			buildMesh(forbiddenRegions);
		
		ArcTimePath arcTimePath =
			calculateShortestPath(mesh);
		
		return arcTimePath;
	}

	/**
	 * Builds the mesh avoiding the given forbidden regions.
	 * 
	 * @param forbiddenRegions
	 * @return the mesh
	 */
	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> buildMesh(
		Collection<ForbiddenRegion> forbiddenRegions)
	{
		double maxSpeed = getMaxSpeed();
		double startArc = getMinArc();
		double finishArc = getMaxArc();
		Point startPoint = getArcTimeStartPoint();
		Point finishPoint = getArcTimeFinishPoint();
		
		SimpleFixTimeMeshBuilder builder = getMeshBuilder();
		
		builder.setForbiddenRegions(forbiddenRegions);
		builder.setMaxSpeed(maxSpeed);
		builder.setMinArc(startArc);
		builder.setMaxArc(finishArc);
		builder.setStartPoint(startPoint);
		builder.setFinishPoint(finishPoint);
		
		builder.build();
		
		return builder.getResultMesh();
	}

	/**
	 * Calculates a path through the mesh.
	 * 
	 * @param mesh
	 * @return the path.
	 */
	private ArcTimePath calculateShortestPath(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh)
	{
		Point startVertex = getArcTimeStartPoint();
		Point finishVertex = getArcTimeFinishPoint();
		
		DijkstraShortestPath<Point, DefaultWeightedEdge> dijkstra =
			new DijkstraShortestPath<>(mesh, startVertex, finishVertex);
		
		GraphPath<Point, DefaultWeightedEdge> graphPath =
			dijkstra.getPath();
		
		// if the finish point is unreachable
		if (graphPath == null) {
			return ArcTimePath.empty();
		} else {
			Stream<Point> sourceVertices = graphPath.getEdgeList().stream()
				.map(mesh::getEdgeSource);
			Stream<Point> endVertex = Stream.of(graphPath.getEndVertex());
			
			ImmutableList<ImmutablePoint> vertices = Stream.concat(sourceVertices, endVertex)
				.map(ImmutableGeometries::immutable)
				.collect(toImmutableList());
			
			if (vertices.size() == 1) {
				ImmutablePoint vertex = vertices.get(0);
				return new ArcTimePath(ImmutableList.of(vertex, vertex));
			} else {
				return new ArcTimePath(vertices);
			}
		}
	}

}
