package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import jts.geom.immutable.ImmutablePoint;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.traverse.ClosestFirstIterator;

import util.TimeConv;
import world.ArcTimePath;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

/**
 * Implements a {@link AbstractMinimumTimePathfinder}. The resulting velocity
 * profile will reach the finish arc as early as possible. Nevertheless, this
 * might still result in slow movement instead of stopping and waiting.
 * 
 * @author Rico
 */
public class SimpleMinimumTimePathfinder extends AbstractMinimumTimePathfinder {
	
	/**
	 * The mesh builder.
	 */
	private SimpleMinimumTimeMeshBuilder meshBuilder = new SimpleMinimumTimeMeshBuilder();
	
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
	private SimpleMinimumTimeMeshBuilder getMeshBuilder() {
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
		arcTimeStartPoint = immutablePoint(
			getStartArc(), inSeconds(getStartTime()));
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
	protected ArcTimePath calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions) {
		updateArcTimeStartPoint();
		
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
		double minArc = getMinArc();
		double maxArc = getMaxArc();
		double finishArc = getFinishArc();
		double bufferDuration = TimeConv.durationToSeconds( getBufferDuration() );
		Point startPoint = getArcTimeStartPoint();
		double earliest = inSeconds( getEarliestFinishTime() );
		double latest = inSeconds( getLatestFinishTime() );
		
		SimpleMinimumTimeMeshBuilder builder = getMeshBuilder();
		
		builder.setForbiddenRegions(forbiddenRegions);
		builder.setMaxSpeed(maxSpeed);
		builder.setMinArc(minArc);
		builder.setMaxArc(maxArc);
		builder.setStartPoint(startPoint);
		builder.setFinishArc(finishArc);
		builder.setEarliestFinishTime(earliest);
		builder.setLatestFinishTime(latest);
		builder.setBufferDuration(bufferDuration);
		
		builder.build();
		
		Collection<Point> finishVertices = builder.getFinishVertices();
		
		setArcTimeFinishPoints(finishVertices);
		
		return builder.getResultMesh();
	}

	/**
	 * Calculates the fastest path through the mesh.
	 * 
	 * @param mesh
	 * @return the fastest path
	 */
	private ArcTimePath calculateShortestPath(
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
		
		return ArcTimePath.empty();
	}

	/**
	 * Builds the fastest path using the provided iterator.
	 * 
	 * @param mesh
	 * @param iterator
	 * @param finishVertex
	 * @return the path
	 */
	private ArcTimePath buildPath(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh,
		ClosestFirstIterator<Point, DefaultWeightedEdge> iterator,
		Point finishVertex)
	{
		LinkedList<ImmutablePoint> vertices = new LinkedList<>();
		
		// collects the vertices along the path from finish to start
		Point cur = finishVertex;
		while (true) {
			vertices.addFirst(immutable(cur));
			DefaultWeightedEdge edge = iterator.getSpanningTreeEdge(cur);
			
			if (edge == null)
				break;
			
			cur = Graphs.getOppositeVertex(mesh, edge, cur);
		}
		
		if (vertices.size() == 1) {
			ImmutablePoint vertex = vertices.getFirst();
			return new ArcTimePath(ImmutableList.of(vertex, vertex));
		} else {
			return new ArcTimePath(ImmutableList.copyOf(vertices));
		}
	}

}
