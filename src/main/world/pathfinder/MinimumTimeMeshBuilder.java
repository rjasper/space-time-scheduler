package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Point;

public class MinimumTimeMeshBuilder extends ArcTimeMeshBuilder {
	
	private EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
	
	private Point startPoint = null;
	
	private double earliestFinishTime = Double.NaN;
	
	private double latestFinishTime = Double.NaN;
	
	private double bufferDuration = 0.0;
	
	private List<VertexPair> finishVertexPairs;
	
	private Point earliestFinishVertex;
	
	@Override
	public boolean isReady() {
		return super.isReady()
			&& startPoint != null
			&& !Double.isNaN(earliestFinishTime)
			&& !Double.isNaN(latestFinishTime);
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = immutable(startPoint);
	}

	private double getEarliestFinishTime() {
		return earliestFinishTime;
	}

	public void setEarliestFinishTime(double earliestFinishTime) {
		if (!Double.isFinite(earliestFinishTime))
			throw new IllegalArgumentException("value is not finite");
		
		this.earliestFinishTime = earliestFinishTime;
	}

	private double getLatestFinishTime() {
		return latestFinishTime;
	}

	public void setLatestFinishTime(double latestFinishTime) {
		if (!Double.isFinite(latestFinishTime))
			throw new IllegalArgumentException("value is not finite");
		
		this.latestFinishTime = latestFinishTime;
	}

	private double getBufferDuration() {
		return bufferDuration;
	}

	public void setBufferDuration(double bufferDuration) {
		if (!Double.isFinite(bufferDuration))
			throw new IllegalArgumentException("value is not finite");
		
		this.bufferDuration = bufferDuration;
	}

	private List<VertexPair> getFinishVertexPairs() {
		return finishVertexPairs;
	}

	private void setFinishVertexPairs(List<VertexPair> finishVertexPairs) {
		this.finishVertexPairs = finishVertexPairs;
	}

	private Point getEarliestFinishVertex() {
		return earliestFinishVertex;
	}

	private void setEarliestFinishVertex(Point earliestFinishVertex) {
		this.earliestFinishVertex = earliestFinishVertex;
	}

	@Override
	protected Collection<Point> buildStartVertices() {
		return Collections.singleton(startPoint);
	}

	@Override
	protected Collection<Point> buildFinishVertices() {
		// note that core and start vertices are build before finish vertices
		Collection<Point> coreVertices = _getCoreVertices();
		Collection<Point> startVertices = _getStartVertices();
		
		double minArc = getMinArc();
		double maxArc = getMaxArc();
		double earliest = getEarliestFinishTime();
		
		Point earliestFinishVertex = geomBuilder.point(maxArc, earliest);
		
		List<VertexPair> finishVertexPairs = Stream.concat(coreVertices.stream(), startVertices.stream())
			.filter((v) -> v.getX() >= minArc && v.getX() <= maxArc) // only within bounds
			.map(this::calculateFinishVertexCandidate) // create candidate
			.filter(this::checkCandidate)              // check candidate (time, visibility and buffer)
			.collect(Collectors.toList());
		
		List<Point> finishVertices = finishVertexPairs.stream()
			.map(VertexPair::getSecond)
			.collect(Collectors.toList());
		
		finishVertices.add(earliestFinishVertex);
		
		// TODO ugly side-effects
		setEarliestFinishVertex(earliestFinishVertex);
		setFinishVertexPairs(finishVertexPairs);
		
		return finishVertices;
	}
	
	private VertexPair calculateFinishVertexCandidate(Point origin) {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

		double s = origin.getX(), t = origin.getY();
		double maxSpeed = getMaxSpeed();
		double maxArc = getMaxArc();
		
		if (s == maxArc) {
			return new VertexPair(origin, origin);
		} else {
			Point finishPoint = geomBuilder.point(maxArc, (maxArc - s) / maxSpeed + t);
			
			return new VertexPair(origin, finishPoint);
		}
	}
	
	private boolean checkCandidate(VertexPair candidate) {
		Point origin = candidate.getFirst();
		Point finishVertex = candidate.getSecond();
		
		double t = finishVertex.getY();
		double earliest = getEarliestFinishTime();
		double latest = getLatestFinishTime();
		
		return t >= earliest && t <= latest
			&& checkVisibility(origin, finishVertex)
			&& checkBuffer(finishVertex);
	}
	
	private boolean checkBuffer(Point vertex) {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		double s = vertex.getX(), t = vertex.getY();
		double buffer = getBufferDuration();
		
		Point p1 = vertex;
		Point p2 = geomBuilder.point(s, t + buffer);
		
		return checkVisibility(p1, p2);
	}
	
	@Override
	protected void connectStartVertices(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph) {
		Collection<Point> startVertices = _getStartVertices();
		Collection<Point> coreVertices = _getCoreVertices();
		
		connect(graph, startVertices, coreVertices);
	}

	@Override
	protected void connectFinishVertices(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph) {
		Collection<VertexPair> pairs = getFinishVertexPairs();
		Point earliest = getEarliestFinishVertex();

		boolean earliestIncluded = false;
		
		for (VertexPair p : pairs) {
			Point origin = p.getFirst();
			Point finish = p.getSecond();
			
			if (finish.equals(earliest))
				earliestIncluded = true;
			if (finish.equals(origin))
				continue;
			
			// connect from origin to finishVertex
			connectWithoutCheck(graph, origin, finish);
		}
		
		if (!earliestIncluded) {
			Collection<Point> core = _getCoreVertices();
			Collection<Point> start = _getStartVertices();
			Collection<Point> finish = Collections.singleton( getEarliestFinishVertex() );
			
			connect(graph, core, finish);
			connect(graph, start, finish);
		}
	}

	@Override
	protected double calculateWeight(Point from, Point to) {
		double t1 = from.getY(), t2 = to.getY();
		
		return t2 - t1;
	}
	
	private static class VertexPair {
		
		private final Point first;
		
		private final Point second;
		
		public VertexPair(Point first, Point second) {
			this.first = first;
			this.second = second;
		}

		public Point getFirst() {
			return first;
		}

		public Point getSecond() {
			return second;
		}
		
	}

}
