package world.pathfinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jts.geom.factories.EnhancedGeometryBuilder;
import jts.geom.factories.StaticJtsFactories;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class ArcTimeMeshBuilder {
	
	private List<ForbiddenRegion> forbiddenRegions = null;
	
	private transient Geometry regionMap = null;

	private Point startPoint = null;
	
	private Point finishPoint = null;
	
	private double maxSpeed = 0.0;
	
	private double maxArc = 0.0;
	
	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> resultMesh = null;
	
	public boolean isReady() {
		return forbiddenRegions != null
			&& startPoint != null
			&& finishPoint != null
			&& maxSpeed > 0.0;
	}

	private List<ForbiddenRegion> getForbiddenRegions() {
		return forbiddenRegions;
	}

	public void setForbiddenRegions(List<ForbiddenRegion> forbiddenRegions) {
		this.forbiddenRegions = new ArrayList<>(forbiddenRegions);
		
		setRegionMap(null);
	}

	private Geometry getRegionMap() {
		return regionMap;
	}

	private void setRegionMap(Geometry regionMap) {
		this.regionMap = regionMap;
	}

	private Point getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	private Point getFinishPoint() {
		return finishPoint;
	}

	public void setFinishPoint(Point finishPoint) {
		this.finishPoint = finishPoint;
	}

	private double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	private double getMaxArc() {
		return maxArc;
	}

	public void setMaxArc(double maxArc) {
		this.maxArc = maxArc;
	}
	
	public double getMinTime() {
		return getStartPoint().getY();
	}
	
	public double getMaxTime() {
		return getFinishPoint().getY();
	}

	public DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> getResultMesh() {
		return resultMesh;
	}

	private void setResultMesh(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> resultMesh) {
		this.resultMesh = resultMesh;
	}
	
	public void build() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		if (getRegionMap() == null)
			updateRegionMap();
		
		Collection<Point> vertices =
			buildVertices();
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph =
			connectVertices(vertices);
		
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
	
	private Collection<Point> buildVertices() {
		GeometryFactory geomFact = StaticJtsFactories.geomFactory();
		LinkedList<Point> vertices = new LinkedList<>();
		
		vertices.add(getStartPoint());
		vertices.add(getFinishPoint());
		
		getForbiddenRegions().stream()
			.map(ForbiddenRegion::getRegion) // get region geometry
			.map(Geometry::getCoordinates)   // get region coordinates
			.forEach((c) -> Arrays.stream(c) // for each coordinate
				.map(geomFact::createPoint)  // make point
				.forEach(vertices::add));    // put point into vertices list
		
		return vertices;
	}
	
	private DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> connectVertices(Collection<Point> vertices) {
		DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph =
			new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		// add vertices to graph
		vertices.stream()
			.forEach(graph::addVertex);
		
		// add edges to graph
		for (Point from : vertices) {
			for (Point to : vertices) {
				if (from == to)
					continue;
				
				if (checkConnection(from, to)) {
					DefaultWeightedEdge edge = graph.addEdge(from, to);
					graph.setEdgeWeight(edge, calculateWeight(from, to));
				}
			}
		}
		
		return graph;
	}
	
	private boolean checkConnection(Point from, Point to) {
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
	
	private boolean checkVisibility(Point from, Point to) {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		Geometry regionMap = getRegionMap();
		LineString line = geomBuilder.lineString(from, to);
		
		return !line.crosses(regionMap);
	}
	
	private double calculateWeight(Point from, Point to) {
		double t1 = from.getY(), t2 = to.getY();
		
		// TODO reconsider weight cost function
		// for fix time pretty dumb
		// maybe: square error to average speed
		// (maxArc/duration - (s2-s1)/(t2-t1))^2
		
		return t2 - t1;
	}
	
}
