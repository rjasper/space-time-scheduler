package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Geometry;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;

public class LazyMinimumTimeMesher {

	private ImmutablePoint startVertex = null;

	private double finishArc = Double.NaN;

	private double minFinishTime = Double.NaN;

	private double maxFinishTime = Double.NaN;

	private Collection<ForbiddenRegion> forbiddenRegions = null;

	private double maxVelocity = Double.NaN;

	private double lazyVelocity = Double.NaN;

	private double minStopDuration = Double.NaN;

	private double bufferDuration = Double.NaN;

	private BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator = null;

	private transient DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph;

	private transient Set<ImmutablePoint> originalVertices;

	private transient Geometry forbiddenMap;

	public void setStartVertex(ImmutablePoint startVertex) {
		this.startVertex = requireNonNull(startVertex, "startVertex");
	}

	public void setFinishArc(double finishArc) {
		if (!Double.isFinite(finishArc))
			throw new IllegalArgumentException("illegal finishArc");

		this.finishArc = finishArc;
	}

	public void setMinFinishTime(double minFinishTime) {
		if (!Double.isFinite(minFinishTime))
			throw new IllegalArgumentException("illegal minFinishTime");

		this.minFinishTime = minFinishTime;
	}

	public void setMaxFinishTime(double maxFinishTime) {
		if (!Double.isFinite(maxFinishTime))
			throw new IllegalArgumentException("illegal maxFinishTime");

		this.maxFinishTime = maxFinishTime;
	}

	public void setForbiddenRegions(Collection<ForbiddenRegion> forbiddenRegions) {
		this.forbiddenRegions = requireNonNull(forbiddenRegions, "forbiddenRegions");
	}

	public void setMaxVelocity(double maxVelocity) {
		if (!Double.isFinite(maxVelocity) || maxVelocity <= 0.0)
			throw new IllegalArgumentException("illegal maxVelocity");

		this.maxVelocity = maxVelocity;
	}

	public void setLazyVelocity(double lazyVelocity) {
		if (!Double.isFinite(lazyVelocity) || lazyVelocity <= 0.0)
			throw new IllegalArgumentException("illegal lazyVelocity");

		this.lazyVelocity = lazyVelocity;
	}

	public void setMinStopDuration(double minStopDuration) {
		if (!Double.isFinite(minStopDuration) || minStopDuration < 0.0)
			throw new IllegalArgumentException("illegal minStopDuration");

		this.minStopDuration = minStopDuration;
	}

	public void setBufferDuration(double bufferDuration) {
		if (!Double.isFinite(bufferDuration) || bufferDuration < 0.0)
			throw new IllegalArgumentException("illegal bufferDuration");

		this.bufferDuration = bufferDuration;
	}

	public void setWeightCalculator(BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator) {
		this.weightCalculator = requireNonNull(weightCalculator, "weightCalculator");
	}

	public static class MeshResult {
		public final DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph;
		public final ImmutablePoint finishVertex;

		private MeshResult(
			DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph,
			ImmutablePoint finishVertex)
		{
			this.graph = graph;
			this.finishVertex = finishVertex;
		}

		public boolean isError() {
			return graph == null;
		}
	}

	public MeshResult mesh() {
		checkParameters();
		init();

		Set<ImmutablePoint> finishCandidates =
			meshFinishVertices();
		if (finishCandidates.isEmpty())
			return error();

		double maxTime = finishCandidates.stream()
			.mapToDouble(ImmutablePoint::getY)
			.max()
			.getAsDouble();
		meshInterconnection(maxTime);

		ImmutablePoint finishVertex =
			determineFinishVertex(finishCandidates);
		if (finishVertex == null)
			return error();

		unmeshUnreachable(finishVertex);

		meshLazy(finishVertex);

		MeshResult result = success(finishVertex);
		cleanUp();
		return result;
	}

	private void checkParameters() {
		if (startVertex == null ||
			Double.isNaN(finishArc) ||
			Double.isNaN(minFinishTime) ||
			Double.isNaN(maxFinishTime) ||
			forbiddenRegions == null ||
			Double.isNaN(maxVelocity) ||
			Double.isNaN(lazyVelocity) ||
			Double.isNaN(minStopDuration) ||
			Double.isNaN(bufferDuration) ||
			weightCalculator == null)
		{
			throw new IllegalStateException("unset parameters");
		}

		if (startVertex.getX()  >= finishArc || startVertex.getY() >  minFinishTime)
			throw new IllegalStateException("illegal bounds");
		if (minFinishTime > maxFinishTime)
			throw new IllegalStateException("minFinishTime > maxFinishTime");
	}

	private void init() {
		graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		originalVertices =
			Stream.concat(
				Stream.of(startVertex),
				forbiddenRegions.stream()
					.map(ForbiddenRegion::getRegion)
					.map(Geometry::getCoordinates)
					.flatMap(Arrays::stream)
					.map(c -> immutablePoint(c.x, c.y)))
			.collect(toSet());

		for (ImmutablePoint v : originalVertices)
			graph.addVertex(v);

		Geometry[] regions = forbiddenRegions.stream()
			.map(ForbiddenRegion::getRegion)
			.toArray(n -> new Geometry[n]);

		forbiddenMap = geometryCollection(regions);
	}

	private void cleanUp() {
		graph = null;
		originalVertices = null;
		forbiddenMap = null;
	}

	private MeshResult error() {
		return new MeshResult(null, null);
	}

	private MeshResult success(ImmutablePoint finishVertex) {
		return new MeshResult(graph, finishVertex);
	}

	private Set<ImmutablePoint> meshFinishVertices() {
		MinimumTimeVertexConnector connector = new MinimumTimeVertexConnector();

		connector.setGraph(graph);
		connector.setMinArc(startVertex.getX());
		connector.setFinishArc(finishArc);
		connector.setMinTime(startVertex.getY());
		connector.setMinFinishTime(minFinishTime);
		connector.setMaxFinishTime(maxFinishTime);
		connector.setBufferDuration(bufferDuration);
		connector.setMaxVelocity(maxVelocity);
		connector.setForbiddenMap(forbiddenMap);
		connector.setWeightCalculator(weightCalculator);

		return connector.connect();
	}

	private void meshInterconnection(double maxTime) {
		SimpleVertexConnector connector = new SimpleVertexConnector();

		connector.setGraph(graph);
		connector.setVertices(originalVertices);
		connector.setMinArc(startVertex.getX());
		connector.setMaxArc(finishArc);
		connector.setMinTime(startVertex.getY());
		connector.setMaxTime(maxTime);
		connector.setMaxVelocity(maxVelocity);
		connector.setForbiddenMap(forbiddenMap);
		connector.setWeightCalculator(weightCalculator);

		connector.connect();
	}

	private ImmutablePoint determineFinishVertex(Set<ImmutablePoint> finishCandidates) {
		MinimumTimeVertexChooser chooser = new MinimumTimeVertexChooser();

		chooser.setGraph(graph);
		chooser.setStartVertex(startVertex);
		chooser.setFinishVertices(finishCandidates);

		return chooser.choose();
	}

	private void unmeshUnreachable(ImmutablePoint finishVertex) {
		SimpleVertexDisconnector disconnector = new SimpleVertexDisconnector();

		disconnector.setGraph(graph);
		disconnector.setStartVertex(startVertex);
		disconnector.setFinishVertex(finishVertex);

		disconnector.disconnect();
	}

	private void meshLazy(ImmutablePoint finishVertex) {
		LazyVertexConnector connector = new LazyVertexConnector();

		connector.setGraph(graph);
		connector.setMinArc(startVertex.getX());
		connector.setMaxArc(finishVertex.getX());
		connector.setMinTime(startVertex.getY());
		connector.setMaxTime(finishVertex.getY());
		connector.setMinStopDuration(minStopDuration);
		connector.setLazyVelocity(lazyVelocity);
		connector.setForbiddenMap(forbiddenMap);
		connector.setWeightCalculator(weightCalculator);

		connector.connect();
	}

}
