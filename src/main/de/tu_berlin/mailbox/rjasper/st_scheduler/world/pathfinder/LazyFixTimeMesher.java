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
import org.jgrapht.traverse.DepthFirstIterator;

import com.vividsolutions.jts.geom.Geometry;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;

public class LazyFixTimeMesher {

	private ImmutablePoint startVertex = null;

	private ImmutablePoint finishVertex = null;

	private Collection<ForbiddenRegion> forbiddenRegions = null;

	private double maxVelocity = Double.NaN;

	private double lazyVelocity = Double.NaN;

	private double minStopDuration = Double.NaN;

	private BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator = null;

	private transient DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph;

	private transient Set<ImmutablePoint> originalVertices;

	private transient Geometry forbiddenMap;

	public void setStartVertex(ImmutablePoint startVertex) {
		this.startVertex = requireNonNull(startVertex, "startVertex");
	}

	public void setFinishVertex(ImmutablePoint finishVertex) {
		this.finishVertex = requireNonNull(finishVertex, "finishVertex");
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

	public void setWeightCalculator(BiFunction<ImmutablePoint, ImmutablePoint, Double> weightCalculator) {
		this.weightCalculator = requireNonNull(weightCalculator, "weightCalculator");
	}

	public static class MeshResult {
		public final DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph;

		private MeshResult(DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> graph) {
			this.graph = graph;
		}

		public boolean isError() {
			return graph == null;
		}
	}

	public DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> mesh() {
		checkParameters();
		init();

		meshLazy();

		if (!checkReachability())
			meshInterconnection();

		DefaultDirectedWeightedGraph<ImmutablePoint, DefaultWeightedEdge> result = graph;
		cleanUp();
		return result;
	}

	private boolean checkReachability() {
		DepthFirstIterator<ImmutablePoint, DefaultWeightedEdge> it =
			new DepthFirstIterator<>(graph, startVertex);

		while (it.hasNext()) {
			if (it.next().equals(finishVertex))
				return true;
		}

		return false;
	}

	private void checkParameters() {
		if (startVertex == null ||
			finishVertex == null ||
			forbiddenRegions == null ||
			Double.isNaN(maxVelocity) ||
			Double.isNaN(lazyVelocity) ||
			Double.isNaN(minStopDuration) ||
			weightCalculator == null)
		{
			throw new IllegalStateException("unset parameters");
		}

		if (startVertex.getX() >= finishVertex.getX() || startVertex.getY() >  finishVertex.getY())
			throw new IllegalStateException("illegal bounds");
	}

	private void init() {
		graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

		originalVertices =
			Stream.concat(
				Stream.of(startVertex, finishVertex),
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

	private void meshInterconnection() {
		SimpleVertexConnector connector = new SimpleVertexConnector();

		connector.setGraph(graph);
		connector.setVertices(originalVertices);
		connector.setMinArc(startVertex.getX());
		connector.setMaxArc(finishVertex.getX());
		connector.setMinTime(startVertex.getY());
		connector.setMaxTime(finishVertex.getY());
		connector.setMaxVelocity(maxVelocity);
		connector.setForbiddenMap(forbiddenMap);
		connector.setWeightCalculator(weightCalculator);

		connector.connect();
	}

	private void meshLazy() {
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
