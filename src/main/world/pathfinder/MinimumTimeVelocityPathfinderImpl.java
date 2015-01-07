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

public class MinimumTimeVelocityPathfinderImpl extends MinimumTimeVelocityPathfinder {
	
	private EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
	
	private MinimumTimeMeshBuilder meshBuilder = new MinimumTimeMeshBuilder();
	
	private Point arcTimeStartPoint;
	
	private Collection<Point> arcTimeFinishPoints;

	private MinimumTimeMeshBuilder getMeshBuilder() {
		return meshBuilder;
	}

	private Point getArcTimeStartPoint() {
		return arcTimeStartPoint;
	}
	
	private void updateArcTimeStartPoint() {
		arcTimeStartPoint = immutable(
			geomBuilder.point(getStartArc(), inSeconds(getStartTime())));
	}

	private Collection<Point> getArcTimeFinishPoints() {
		return arcTimeFinishPoints;
	}

	private void setArcTimeFinishPoints(Collection<Point> arcTimeFinishPoints) {
		this.arcTimeFinishPoints = arcTimeFinishPoints;
	}

	@Override
	protected List<Point> calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions) {
		updateArcTimeStartPoint();
		
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
		double bufferDuration = DurationConv.inSeconds( getBufferDuration() );
		Point startPoint = getArcTimeStartPoint();
		double earliest = inSeconds( getEarliestFinishTime() );
		double latest = inSeconds( getLatestFinishTime() );
		
		MinimumTimeMeshBuilder builder = getMeshBuilder();
		
		builder.setForbiddenRegions(forbiddenRegions);
		builder.setMaxSpeed(maxSpeed);
		builder.setMaxArc(maxArc);
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

	private List<Point> buildPath(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> mesh,
		ClosestFirstIterator<Point, DefaultWeightedEdge> iterator,
		Point finishVertex)
	{
		LinkedList<Point> path = new LinkedList<>();
		
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
