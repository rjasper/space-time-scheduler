package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.durationToSeconds;
import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.ArcTimePath;

/**
 * Implements a {@link AbstractFixTimePathfinder}. The resulting velocity
 * profile consists of straight line segments along the vertices of forbidden
 * regions. While this is a very simple solution it may result in unnecessary
 * slow movement instead of stopping and waiting.
 *
 * @author Rico Jasper
 */
public class LazyFixTimePathfinder extends AbstractFixTimePathfinder {

	private Duration minStopDuration = null;

	public void setMinStopDuration(Duration minStopDuration) {
		requireNonNull(minStopDuration);

		if (minStopDuration.isNegative())
			throw new IllegalArgumentException("illegal minStopDuration");

		this.minStopDuration = minStopDuration;
	}

	@Override
	protected void checkParameters() {
		super.checkParameters();

		if (minStopDuration == null)
			throw new IllegalStateException("unset parameters");
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.VelocityPathfinder#calculateArcTimePath(java.util.Collection)
	 */
	@Override
	protected ArcTimePath calculateArcTimePath(Collection<ForbiddenRegion> forbiddenRegions) {
		ImmutablePoint startVertex = immutablePoint(getStartArc(), inSeconds(getStartTime()));
		ImmutablePoint finishVertex = immutablePoint(getFinishArc(), inSeconds(getFinishTime()));

		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> mesh =
			buildMesh(forbiddenRegions, startVertex, finishVertex);

		ArcTimePath arcTimePath =
			calculateShortestPath(mesh, startVertex, finishVertex);

		return arcTimePath;
	}

	/**
	 * Builds the mesh avoiding the given forbidden regions.
	 *
	 * @param forbiddenRegions
	 * @return the mesh
	 */
	private DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> buildMesh(
		Collection<ForbiddenRegion> forbiddenRegions,
		ImmutablePoint startVertex,
		ImmutablePoint finishVertex)
	{
		LazyFixTimeMesher mesher = new LazyFixTimeMesher();

		mesher.setStartVertex(startVertex);
		mesher.setFinishVertex(finishVertex);
		mesher.setForbiddenRegions(forbiddenRegions);
		mesher.setMaxVelocity(getMaxSpeed());
		mesher.setLazyVelocity(getMaxSpeed());
		mesher.setMinStopDuration( durationToSeconds(minStopDuration) );
		mesher.setWeightCalculator(WEIGHT_CALCULATOR);

		return mesher.mesh();
	}

	// TODO duplicate code @see LazyMinimumTimePathfinder
	/**
	 * Calculates the fastest path through the mesh.
	 *
	 * @param mesh
	 * @return the fastest path
	 */
	private ArcTimePath calculateShortestPath(
		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> mesh,
		ImmutablePoint startVertex,
		ImmutablePoint finishVertex)
	{
		if (startVertex.equalsTopo(finishVertex))
			return new ArcTimePath(ImmutableList.of(startVertex, finishVertex));

		List<DefaultWeightedEdge> edges =
			ModifiedDijkstraAlgorithm.findPathBetween(mesh, startVertex, finishVertex);

		if (edges.isEmpty())
			return ArcTimePath.empty();

		Iterable<ImmutablePoint> targets = () -> edges.stream()
			.map(mesh::getEdgeTarget)
			.iterator();

		ImmutableList<ImmutablePoint> vertices = ImmutableList.<ImmutablePoint>builder()
			.add(startVertex)
			.addAll(targets)
			.build();

		return new ArcTimePath(vertices);
	}

}
