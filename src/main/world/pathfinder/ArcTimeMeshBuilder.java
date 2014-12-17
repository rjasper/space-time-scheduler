package world.pathfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jts.geom.factories.EnhancedGeometryBuilder;
import jts.geom.factories.StaticJtsFactories;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public abstract class ArcTimeMeshBuilder {
	
	protected static final double MIN_ARC = 0.0;
	
	private List<ForbiddenRegion> forbiddenRegions = null;
	
	private transient Geometry regionMap = null;

	private double maxSpeed = Double.NaN;
	
	private double maxArc = Double.NaN;
	
	private Collection<Point> coreVertices;

	private Collection<Point> startVertices;

	private Collection<Point> finishVertices;
	
	private double minTime;
	
	private double maxTime;

	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> resultMesh = null;
	
	public boolean isReady() {
		return forbiddenRegions != null
			&& !Double.isNaN(maxSpeed)
			&& !Double.isNaN(maxArc);
	}

	private List<ForbiddenRegion> getForbiddenRegions() {
		return forbiddenRegions;
	}

	public void setForbiddenRegions(Collection<ForbiddenRegion> forbiddenRegions) {
		this.forbiddenRegions = new ArrayList<>(forbiddenRegions);
		
		setRegionMap(null);
	}

	private Geometry getRegionMap() {
		return regionMap;
	}

	private void setRegionMap(Geometry regionMap) {
		this.regionMap = regionMap;
	}

	protected double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
	
	protected double getMinArc() {
		return MIN_ARC;
	}

	protected double getMaxArc() {
		return maxArc;
	}

	public void setMaxArc(double maxArc) {
		this.maxArc = maxArc;
	}

	private double getMinTime() {
		return minTime;
	}

	private void setMinTime(double minTime) {
		this.minTime = minTime;
	}

	private double getMaxTime() {
		return maxTime;
	}

	private void setMaxTime(double maxTime) {
		this.maxTime = maxTime;
	}

	protected Collection<Point> _getCoreVertices() {
		return coreVertices;
	}

	private void setCoreVertices(Collection<Point> coreVertices) {
		this.coreVertices = coreVertices;
	}
	
	public Collection<Point> getStartVertices() {
		// TODO use a stored unmodifiable view
		return Collections.unmodifiableCollection( _getStartVertices() );
	}

	protected Collection<Point> _getStartVertices() {
		return startVertices;
	}

	private void setStartVertices(Collection<Point> startVertices) {
		this.startVertices = startVertices;
	}
	
	public Collection<Point> getFinishVertices() {
		// TODO use a stored unmodifiable view
		return Collections.unmodifiableCollection( _getFinishVertices() );
	}

	protected Collection<Point> _getFinishVertices() {
		return finishVertices;
	}

	private void setFinishVertices(Collection<Point> finishVertices) {
		this.finishVertices = finishVertices;
	}

	public DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> getResultMesh() {
		return resultMesh;
	}

	private void setResultMesh(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> resultMesh) {
		this.resultMesh = resultMesh;
	}
	
	private void updateVertices() {
		// TODO how to ensure that vertices are immutable?
		
		setCoreVertices( buildCoreVertices() );
		setStartVertices( buildStartVertices() );
		setFinishVertices( buildFinishVertices() );
	}
	
	private void updateMinMaxTime() {
		double min = _getStartVertices().stream()
			.map(Point::getY)
			.reduce(Double.POSITIVE_INFINITY, Math::min);
		double max = _getFinishVertices().stream()
			.map(Point::getY)
			.reduce(Double.NEGATIVE_INFINITY, Math::max);
		
		setMinTime(min);
		setMaxTime(max);
	}
	
	public void build() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		if (getRegionMap() == null)
			updateRegionMap();
		
		updateVertices();
		updateMinMaxTime();
		
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph =
			buildGraph();
		
		setResultMesh(graph);
	}
	
	private void updateRegionMap() {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		Geometry[] regions = getForbiddenRegions().stream()
			.map(ForbiddenRegion::getRegion)
			.toArray(Geometry[]::new);
		
		Geometry regionMap = geomBuilder.geometryCollection(regions).union();
		
		setRegionMap(regionMap);
	}
	
	private Collection<Point> buildCoreVertices() {
		GeometryFactory geomFact = StaticJtsFactories.geomFactory();
		LinkedList<Point> vertices = new LinkedList<>();
		
		getForbiddenRegions().stream()
			.map(ForbiddenRegion::getRegion) // get region geometry
			.map(Geometry::getCoordinates)   // get region coordinates
			.forEach((c) -> Arrays.stream(c) // for each coordinate
				.map(geomFact::createPoint)  // make point
				.forEach(vertices::add));    // put point into vertices list
		
		// using set to ensure uniqueness of points
		return new HashSet<>(vertices);
	}
	
	protected abstract Collection<Point> buildStartVertices();
	
	protected abstract Collection<Point> buildFinishVertices();
	
	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> buildGraph() {
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph =
			new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
	
		Collection<Point> coreVertices = _getCoreVertices();
		Collection<Point> startVertices = _getStartVertices();
		Collection<Point> finishVertices = _getFinishVertices();
		
		Set<Point> vertices = Stream.of(coreVertices, startVertices, finishVertices)
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());
		
		// add vertices to graph
		for (Point v : vertices)
			graph.addVertex(v);
		
		connectCoreVertices(graph);
		connectStartVertices(graph);
		connectFinishVertices(graph);
		
		return graph;
	}
	
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
	
	protected void connectWithoutCheck(
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph,
		Point from, Point to)
	{
		DefaultWeightedEdge edge = graph.addEdge(from, to);
		graph.setEdgeWeight(edge, calculateWeight(from, to));
	}
	
	private void connectCoreVertices(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph) {
		Collection<Point> vertices = _getCoreVertices();

		connect(graph, vertices, vertices);
	}

	protected void connectStartVertices(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph) {
		Collection<Point> startVertices = _getStartVertices();
		Collection<Point> finishVertices = _getFinishVertices();
		Collection<Point> coreVertices = _getCoreVertices();
		
		connect(graph, startVertices, finishVertices);
		connect(graph, startVertices, coreVertices);
	}

	protected void connectFinishVertices(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph) {
		Collection<Point> finishVertices = _getFinishVertices();
		Collection<Point> coreVertices = _getCoreVertices();
		
		connect(graph, coreVertices, finishVertices);
	}
	
	protected boolean checkConnection(Point from, Point to) {
		double maxArc = getMaxArc();
		double minTime = getMinTime();
		double maxTime = getMaxTime();
		double maxSpeed = getMaxSpeed();
		
		double s1 = from.getX(), s2 = to.getX(), t1 = from.getY(), t2 = to.getY();
		
		// if vertex is not on path
		if (s1 < 0 || s1 > maxArc || s2 < 0 || s2 > maxArc)
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
	
	protected boolean checkVisibility(Point from, Point to) {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		Geometry regionMap = getRegionMap();
		LineString line = geomBuilder.lineString(from, to);
		
		// TODO intersection matrix might be calculated twice
		return !line.crosses(regionMap)
			&& !regionMap.contains(line);
	}
	
	protected abstract double calculateWeight(Point from, Point to);
	
}
