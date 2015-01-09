package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.traverse.ClosestFirstIterator;

import util.DurationConv;

import com.vividsolutions.jts.geom.Point;

/**
 * Implements a {@link MinimumTimeVelocityPathfinder}. The resulting velocity
 * profile will reach the finish arc as early as possible. Nevertheless, this
 * might still result in slow movement instead of stopping and waiting.
 * 
 * @author Rico
 */
public class MinimumTimeVelocityPathfinderImpl extends MinimumTimeVelocityPathfinder {
	
	private EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
	
	/**
	 * The mesh builder.
	 */
	private MinimumTimeMeshBuilder meshBuilder = new MinimumTimeMeshBuilder();
	
	/**
	 * The arc-time start point.
	 */
	private Point arcTimeStartPoint;
	
	/**
	 * The possible arc-time finish points.
	 */
	private Collection<Point> arcTimeFinishPoints;

	/**
	 * @return the mesh builder.
	 */
	private MinimumTimeMeshBuilder getMeshBuilder() {
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
		// are structurally the same to the ones used in the mesh. Therefore,
		// the points are already converted immutable here.
		arcTimeStartPoint = immutable(
			geomBuilder.point(getStartArc(), inSeconds(getStartTime())));
	}

	/**
	 * @return the arc-time finish points.
	 */
	private Collection<Point> getArcTimeFinishPoints() {
		return arcTimeFinishPoints;
	}

	/**
	 * Sets the arc-time finish points.
	 * 
	 * @param arcTimeFinishPoints
	 */
	private void setArcTimeFinishPoints(Collection<Point> arcTimeFinishPoints) {
		this.arcTimeFinishPoints = arcTimeFinishPoints;
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.VelocityPathfinder#calculateArcTimePath(java.util.Collection)
	 */
	@Override
	protected List<Point> calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions) {
		updateArcTimeStartPoint();
		
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh =
			buildMesh(forbiddenRegions);
		
		List<Point> arcTimePath =
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
		double maxArc = getFinishArc();
		double bufferDuration = DurationConv.inSeconds( getBufferDuration() );
		Point startPoint = getArcTimeStartPoint();
		double earliest = inSeconds( getEarliestFinishTime() );
		double latest = inSeconds( getLatestFinishTime() );
		
		MinimumTimeMeshBuilder builder = getMeshBuilder();
		
		builder.setForbiddenRegions(forbiddenRegions);
		builder.setMaxSpeed(maxSpeed);
		builder.setFinishArc(maxArc);
		builder.setStartPoint(startPoint);
		builder.setEarliestFinishTime(earliest);
		builder.setLatestFinishTime(latest);
		builder.setBufferDuration(bufferDuration);
		
		builder.build();
		
		Collection<Point> finishVertices = builder.getFinishVertices();
		
		// TODO ugly side-effect
		setArcTimeFinishPoints(finishVertices);
		
		return builder.getResultMesh();
	}

	/**
	 * Calculates the fastest path through the mesh.
	 * 
	 * @param mesh
	 * @return the fastest path
	 */
	private List<Point> calculateShortestPath(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh)
	{
		Point startVertex = getArcTimeStartPoint();

		// convert to hash set for quick lookup
		HashSet<Point> finishVertices = new HashSet<>(getArcTimeFinishPoints());
		
		ClosestFirstIterator<Point, DefaultWeightedEdge> it =
			new ClosestFirstIterator<>(mesh, startVertex);
		
		Point cur = null;
		while (it.hasNext()) {
			cur = it.next();
			
			if (finishVertices.contains(cur))
				return buildPath(mesh, it, cur);
		}
		
		return null;
	}

	/**
	 * Builds the fastest path using the provided iterator.
	 * 
	 * @param mesh
	 * @param iterator
	 * @param finishVertex
	 * @return the path
	 */
	private List<Point> buildPath(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh,
		ClosestFirstIterator<Point, DefaultWeightedEdge> iterator,
		Point finishVertex)
	{
		LinkedList<Point> path = new LinkedList<>();
		
		// collects the vertices along the path from finish to start
		Point cur = finishVertex;
		while (true) {
			path.addFirst(cur);
			DefaultWeightedEdge edge = iterator.getSpanningTreeEdge(cur);
			
			if (edge == null)
				break;
			
			cur = Graphs.getOppositeVertex(mesh, edge, cur);
		}
		
		return path;
	}

}
