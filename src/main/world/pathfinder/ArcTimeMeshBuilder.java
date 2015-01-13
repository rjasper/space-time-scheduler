package world.pathfinder;

import static com.vividsolutions.jts.geom.IntersectionMatrix.isTrue;
import static com.vividsolutions.jts.geom.Location.INTERIOR;
import static java.util.Collections.unmodifiableCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jts.geom.factories.EnhancedGeometryBuilder;
import jts.geom.factories.StaticJtsFactories;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import util.CollectionsRequire;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * An {@link ArcTimeMeshBuilder} builds a directed weighted graph. The edges of
 * the graph represent possible path segments in the arc-time plane and will
 * avoid any forbidden region.
 * 
 * @author Rico
 */
public abstract class ArcTimeMeshBuilder {
	
	/**
	 * The finish arc value.
	 */
	protected static final double START_ARC = 0.0;
	
	/**
	 * The forbidden regions.
	 */
	private Collection<ForbiddenRegion> forbiddenRegions = null;
	
	/**
	 * The forbidden region union.
	 */
	private transient Geometry regionMap = null;

	/**
	 * The maximum speed.
	 */
	private double maxSpeed = Double.NaN;
	
	/**
	 * The finish arc value.
	 */
	private double finishArc = Double.NaN;
	
	/**
	 * The core vertices.
	 */
	private Collection<Point> coreVertices;

	/**
	 * The start vertices.
	 */
	private Collection<Point> startVertices;

	/**
	 * The finish vertices.
	 */
	private Collection<Point> finishVertices;
	
	/**
	 * The minimum time.
	 */
	private double minTime;
	
	/**
	 * The maximum time.
	 */
	private double maxTime;

	/**
	 * The build directed weighted graph.
	 */
	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> resultMesh = null;
	
	// TODO remove
//	/**
//	 * @return {@code true} if all parameters are set.
//	 */
//	public boolean isReady() {
//		return forbiddenRegions != null
//			&& !Double.isNaN(maxSpeed)
//			&& !Double.isNaN(finishArc);
//	}
	
	/**
	 * @return the forbidden regions.
	 */
	private Collection<ForbiddenRegion> getForbiddenRegions() {
		return forbiddenRegions;
	}

	/**
	 * Sets the forbidden regions.
	 * 
	 * @param forbiddenRegions
	 * @throws NullPointerException
	 *             if forbiddenRegions is or contains {@code null}.
	 */
	public void setForbiddenRegions(Collection<ForbiddenRegion> forbiddenRegions) {
		CollectionsRequire.requireContainsNonNull(forbiddenRegions, "forbiddenRegions");
		
		this.forbiddenRegions = unmodifiableCollection( forbiddenRegions );
		
		// reset the region map
		setRegionMap(null);
	}

	/**
	 * @return the region map.
	 */
	private Geometry getRegionMap() {
		return regionMap;
	}

	/**
	 * Sets the region map.
	 * 
	 * @param regionMap
	 */
	private void setRegionMap(Geometry regionMap) {
		this.regionMap = regionMap;
	}

	/**
	 * Rebuilds the {@link #regionMap} by calculating the union of all forbidden
	 * regions.
	 */
	private void updateRegionMap() {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		Geometry[] regions = getForbiddenRegions().stream()
			.map(ForbiddenRegion::getRegion)
			.toArray(Geometry[]::new);
		
		Geometry regionMap = geomBuilder.geometryCollection(regions).union();
		
		setRegionMap(regionMap);
	}

	/**
	 * @return maximum speed.
	 */
	protected double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * Sets the maximum speed.
	 * 
	 * @param maxSpeed
	 * @throws IllegalArgumentException
	 *             if maxSpeed is not positive finite.
	 */
	public void setMaxSpeed(double maxSpeed) {
		if (!Double.isFinite(maxSpeed) || maxSpeed <= 0.0)
			throw new IllegalArgumentException("maxSpeed is not positive finite");
		
		this.maxSpeed = maxSpeed;
	}
	
	/**
	 * @return the start arc value.
	 */
	protected double getStartArc() {
		return START_ARC;
	}

	/**
	 * @return the finish arc value.
	 */
	protected double getFinishArc() {
		return finishArc;
	}

	/**
	 * Sets the finish arc value.
	 * 
	 * @param maxArc
	 * @throws IllegalArgumentException
	 *             if the maxArc is not non-negative finite.
	 */
	public void setFinishArc(double maxArc) {
		if (!Double.isFinite(maxArc) || maxArc < 0.0)
			throw new IllegalArgumentException("maxArc is not non-negative finite");
		
		this.finishArc = maxArc;
	}

	/**
	 * Updates the minimum and maximum time by examining the start and finish
	 * vertices.
	 */
	private void updateMinMaxTime() {
		double min = getStartVertices().stream()
			.map(Point::getY)
			.reduce(Double.POSITIVE_INFINITY, Math::min);
		double max = getFinishVertices().stream()
			.map(Point::getY)
			.reduce(Double.NEGATIVE_INFINITY, Math::max);
		
		setMinTime(min);
		setMaxTime(max);
	}

	/**
	 * @return the minimum time.
	 */
	private double getMinTime() {
		return minTime;
	}

	/**
	 * Sets the minimum time.
	 * 
	 * @param minTime
	 * @throws IllegalArgumentException
	 *             if minTime is not finite.
	 */
	private void setMinTime(double minTime) {
		if (!Double.isFinite(minTime))
			throw new IllegalArgumentException("minTime is not finite");
		
		this.minTime = minTime;
	}

	/**
	 * @return the maximum time.
	 */
	private double getMaxTime() {
		return maxTime;
	}

	/**
	 * Sets the maximum time.
	 * 
	 * @param maxTime
	 * @throws IllegalArgumentException if maxTime is not finite
	 */
	private void setMaxTime(double maxTime) {
		if (!Double.isFinite(maxTime))
			throw new IllegalArgumentException("maxTime is not finite");
		
		this.maxTime = maxTime;
	}

	/**
	 * @return the unmodifiable core vertices.
	 */
	public Collection<Point> getCoreVertices() {
		return coreVertices;
	}

	/**
	 * Sets the core vertices.
	 * 
	 * @param coreVertices
	 */
	private void setCoreVertices(Collection<Point> coreVertices) {
		this.coreVertices = unmodifiableCollection(coreVertices);
	}
	
	/**
	 * Builds the core vertices of the mesh. Core vertices are neither start
	 * nor finish vertices.
	 * 
	 * @return the core vertices.
	 */
	private Collection<Point> buildCoreVertices() {
		GeometryFactory geomFact = StaticJtsFactories.geomFactory();
		Geometry map = getRegionMap();
		int n = map.getNumPoints();
		
		Collection<Point> vertices = new ArrayList<>(n);
		
		map.apply((Coordinate c) -> vertices.add(geomFact.createPoint(c)));
		
		return vertices;
	}

	/**
	 * @return the unmodifiable start vertices.
	 */
	public Collection<Point> getStartVertices() {
		return startVertices;
	}

	/**
	 * Sets the start vertices.
	 * 
	 * @param startVertices
	 */
	private void setStartVertices(Collection<Point> startVertices) {
		this.startVertices = unmodifiableCollection(startVertices);
	}
	
	/**
	 * Builds the start vertices of the mesh. Calculated paths through the mesh
	 * will start at a start vertex.
	 * 
	 * @return the start vertices.
	 */
	protected abstract Collection<Point> buildStartVertices();

	/**
	 * @return the unmodifiable finish vertices.
	 */
	public Collection<Point> getFinishVertices() {
		return finishVertices;
	}

	/**
	 * Sets the finish vertices.
	 * 
	 * @param finishVertices
	 */
	private void setFinishVertices(Collection<Point> finishVertices) {
		this.finishVertices = unmodifiableCollection(finishVertices);
	}

	/**
	 * Builds the finish vertices of the mesh. Calculated paths through the mesh
	 * will finish at a finish vertex.
	 * 
	 * @return the finish vertices.
	 */
	protected abstract Collection<Point> buildFinishVertices();

	/**
	 * <p>
	 * Updates the vertices.
	 * </p>
	 * 
	 * <p>
	 * Rebuilds vertices by calling
	 * {@link #buildCoreVertices()},
	 * {@link #buildStartVertices()}, and {@link #buildFinishVertices()}.
	 * </p> 
	 */
	private void updateVertices() {
		setCoreVertices( buildCoreVertices() );
		setStartVertices( buildStartVertices() );
		setFinishVertices( buildFinishVertices() );
	}

	/**
	 * @return the built mesh.
	 */
	public DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> getResultMesh() {
		return resultMesh;
	}

	/**
	 * Sets the built mesh.
	 * 
	 * @param resultMesh
	 */
	private void setResultMesh(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> resultMesh) {
		this.resultMesh = resultMesh;
	}
	
	/**
	 * Clears all parameters and results.
	 */
	public void clear() {
		forbiddenRegions = null;
		regionMap = null;
		maxSpeed = Double.NaN;
		finishArc = Double.NaN;
		coreVertices = null;
		startVertices = null;
		finishVertices = null;
		resultMesh = null;
	}
	
	/**
	 * Checks if all parameters are properly set. Throws an exception otherwise.
	 * 
	 * @throws IllegalStateException
	 *             if any parameter is not set.
	 */
	protected void checkParameters() {
		if (forbiddenRegions == null ||
			Double.isNaN(maxSpeed)   ||
			Double.isNaN(finishArc))
		{
			throw new IllegalStateException("some parameters are not set");
		}
	}

	/**
	 * Builds the arc-time mesh.
	 * 
	 * @throws IllegalStateException
	 *             if not all parameters are set.
	 */
	public void build() {
		checkParameters();
		
		if (getRegionMap() == null)
			updateRegionMap();
		
		updateVertices();
		updateMinMaxTime();
		
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph =
			buildGraph();
		
		setResultMesh(graph);
	}
	
	/**
	 * The actual graph building implementation. First it adds all vertices
	 * (start, finish, core) to a new graph. Then it connects the vertices by
	 * calling {@link #connectCoreVertices(DefaultDirectedWeightedGraph)},
	 * {@link #connectStartVertices(DefaultDirectedWeightedGraph)}, and
	 * {@link #connectFinishVertices(DefaultDirectedWeightedGraph)}.
	 * 
	 * @return the built graph.
	 */
	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> buildGraph() {
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph =
			new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
	
		Collection<Point> coreVertices = getCoreVertices();
		Collection<Point> startVertices = getStartVertices();
		Collection<Point> finishVertices = getFinishVertices();
		
		Set<Point> vertices = Stream.of(coreVertices, startVertices, finishVertices)
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());
		
		// add vertices to graph
		for (Point v : vertices)
			graph.addVertex(v);
		
		// connect vertices
		connectCoreVertices(graph);
		connectStartVertices(graph);
		connectFinishVertices(graph);
		
		return graph;
	}
	
	/**
	 * A helper method to connect all valid pairs from 'from' to 'to'.
	 * 
	 * @param graph
	 * @param from
	 *            from-vertices
	 * @param to
	 *            to-vertices
	 */
	protected void connect(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph,
		Collection<Point> from, Collection<Point> to)
	{
		// for each from-to pair check for connection
		for (Point f : from) {
			for (Point t : to) {
				if (f.equals(t))
					continue;
				
				if (checkConnection(f, t))
					connectWithoutCheck(graph, f, t);
			}
		}
	}
	
	/**
	 * A helper method to connect to vertices without checking for validity.
	 * 
	 * @param graph
	 * @param from from-point
	 * @param to to-point
	 */
	protected void connectWithoutCheck(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph,
		Point from, Point to)
	{
		DefaultWeightedEdge edge = graph.addEdge(from, to);
		
		if (edge != null)
			graph.setEdgeWeight(edge, calculateWeight(from, to));
	}
	
	// TODO connect*Vertices methods are not symmetric
	
	/**
	 * Connects the core vertices with each other.
	 * @param graph
	 */
	private void connectCoreVertices(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph) {
		Collection<Point> vertices = getCoreVertices();

		connect(graph, vertices, vertices);
	}

	/**
	 * Connects the start vertices with the core and the finish vertices.
	 * 
	 * @param graph
	 */
	protected void connectStartVertices(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph) {
		Collection<Point> startVertices = getStartVertices();
		Collection<Point> finishVertices = getFinishVertices();
		Collection<Point> coreVertices = getCoreVertices();
		
		connect(graph, startVertices, finishVertices);
		connect(graph, startVertices, coreVertices);
	}

	/**
	 * Connects the core vertices with the finish vertices.
	 * 
	 * @param graph
	 */
	protected void connectFinishVertices(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph) {
		Collection<Point> finishVertices = getFinishVertices();
		Collection<Point> coreVertices = getCoreVertices();
		
		connect(graph, coreVertices, finishVertices);
	}
	
	/**
	 * <p>
	 * Checks if two nodes can be connects. The following conditions have to be
	 * met:
	 * </p>
	 * 
	 * <ul>
	 * <li>Both vertices' arc-ordinates are within [minArc, maxArc].</li>
	 * <li>Both vertices' time-ordinates are within [minTime, maxTime].</li>
	 * <li>The first vertex' time is before the second vertex' time.</li>
	 * <li>The maximum speed is not exceeded.</li>
	 * <li>The "line of sight" is not blocked by forbidden regions.</li>
	 * </ul>
	 * @param from
	 * @param to
	 * @return
	 */
	protected boolean checkConnection(Point from, Point to) {
		double minArc = getStartArc();
		double maxArc = getFinishArc();
		double minTime = getMinTime();
		double maxTime = getMaxTime();
		double maxSpeed = getMaxSpeed();
		
		double s1 = from.getX(), s2 = to.getX(), t1 = from.getY(), t2 = to.getY();
		
		// if vertex is not on path
		if (s1 < minArc || s1 > maxArc || s2 < minArc || s2 > maxArc)
			return false;
		
		// if vertex is not within time window
		if (t1 < minTime || t1 > maxTime || t2 < minTime || t2 > maxTime)
			return false;
		
		// if 'from' happens after 'to'
		if (t1 > t2)
			return false;
		
		// if maximum speed is exceeded
		if (Math.abs((s2 - s1) / (t2 - t1)) > maxSpeed)
			return false;
		
		return checkVisibility(from, to);
	}
	
	/**
	 * Checks if two points have a clear line of sight to each other. Forbidden
	 * regions might block the view.
	 * 
	 * @param from from-point
	 * @param to to-point
	 * @return {@code true} if no forbidden region blocks the view
	 */
	protected boolean checkVisibility(Point from, Point to) {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		Geometry regionMap = getRegionMap();
		
		// Relate can't handle GeometryCollections.
		// Since regionMap could be an empty GeometryCollection this needs
		// to be caught here.
		if (regionMap.isEmpty())
			return true;
		
		// TODO is regionMap never a pure (Immutable)GeometryCollection?
		
		LineString line = geomBuilder.lineString(from, to);
		IntersectionMatrix matrix = line.relate(regionMap);
		
		return !isTrue(matrix.get(INTERIOR, INTERIOR));
	}
	
 	/**
	 * Calculates the weight of an edge connecting the given points.
	 * 
	 * @param from from-point
	 * @param to to-point
	 * @return the weight value.
	 */
	protected abstract double calculateWeight(Point from, Point to);
	
}
