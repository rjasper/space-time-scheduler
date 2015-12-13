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
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.LazyMinimumTimeMesher.MeshResult;

/**
 * Implements a {@link AbstractMinimumTimePathfinder}. The resulting velocity
 * profile will reach the finish arc as early as possible. Nevertheless, this
 * might still result in slow movement instead of stopping and waiting.
 *
 * @author Rico Jasper
 */
public class LazyMinimumTimePathfinder extends AbstractMinimumTimePathfinder {

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

		MeshResult res = buildMesh(forbiddenRegions, startVertex);

		if (res.isError())
			return ArcTimePath.empty();

		ArcTimePath arcTimePath =
			calculateShortestPath(res.graph, startVertex, res.finishVertex);

		return arcTimePath;
	}

	/**
	 * Builds the mesh avoiding the given forbidden regions.
	 *
	 * @param forbiddenRegions
	 * @return the mesh
	 */
	private MeshResult buildMesh(
		Collection<ForbiddenRegion> forbiddenRegions,
		ImmutablePoint startVertex)
	{
		LazyMinimumTimeMesher mesher = new LazyMinimumTimeMesher();

		mesher.setStartVertex(startVertex);
		mesher.setFinishArc(getFinishArc());
		mesher.setMinFinishTime(inSeconds(getEarliestFinishTime()));
		mesher.setMaxFinishTime(inSeconds(getLatestFinishTime()));
		mesher.setForbiddenRegions(forbiddenRegions);
		mesher.setMaxVelocity(getMaxSpeed());
		mesher.setLazyVelocity(getMaxSpeed());
		mesher.setMinStopDuration( durationToSeconds(minStopDuration) );
		mesher.setBufferDuration( durationToSeconds(getBufferDuration()) );
		mesher.setWeightCalculator(WEIGHT_CALCULATOR);

		return mesher.mesh();
	}

	// TODO duplicate code @see LazyFixTimePathfinder
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
