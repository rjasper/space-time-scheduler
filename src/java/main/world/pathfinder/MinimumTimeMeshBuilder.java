package world.pathfinder;

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
	
	private Point startPoint = null;
	
	private double earliestFinishTime = Double.NaN;
	
	private double latestFinishTime = Double.NaN;
	
	private double bufferDuration = 0.0;
	
	private List<Point> origins;

//	private Point getStartPoint() {
//		return startPoint;
//	}
	
	public boolean isReady() {
		return super.isReady()
			&& startPoint != null
			&& !Double.isNaN(earliestFinishTime)
			&& !Double.isNaN(latestFinishTime);
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
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

	private List<Point> getOrigins() {
		return origins;
	}

	private void setOrigins(List<Point> origins) {
		this.origins = origins;
	}

	@Override
	protected Collection<Point> buildStartVertices() {
		return Collections.singleton(startPoint);
	}

	@Override
	protected Collection<Point> buildFinishVertices() {
		// note that core and start vertices are build before finish vertices
		Collection<Point> coreVertices = getCoreVertices();
		Collection<Point> startVertices = _getStartVertices();
		
		double minArc = getMinArc();
		double maxArc = getMaxArc();
		
		List<Candidate> candidates = Stream.concat(coreVertices.stream(), startVertices.stream())
			.filter((v) -> v.getX() >= minArc && v.getX() <= maxArc) // only within bounds
			.map(this::calculateFinishVertexCandidate) // create candidate
			.filter(this::checkCandidate)              // check candidate (time, visibility and buffer)
			.collect(Collectors.toList());
		
		List<Point> origins = candidates.stream()
			.map(Candidate::getOrigin)
			.collect(Collectors.toList());
		
		List<Point> finishVertices = candidates.stream()
			.map(Candidate::getFinishVertex)
			.collect(Collectors.toList());
		
		setOrigins(origins);
		
		return finishVertices;
	}
	
	private Candidate calculateFinishVertexCandidate(Point origin) {
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

		double s = origin.getX(), t = origin.getY();
		double maxSpeed = getMaxSpeed();
		double maxArc = getMaxArc();
		
		if (s == maxArc) {
			return new Candidate(origin, origin);
		} else {
			Point finishPoint = geomBuilder.point(maxArc, (maxArc - s) / maxSpeed + t);
			
			return new Candidate(origin, finishPoint);
		}
	}
	
	private boolean checkCandidate(Candidate candidate) {
		Point origin = candidate.getOrigin();
		Point finishVertex = candidate.getFinishVertex();
		
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
	protected void connectFinishVertices(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph) {
		Collection<Point> finishVertices = _getFinishVertices();
		List<Point> origins = getOrigins();
		
		int i = 0;
		for (Point v : finishVertices) {
			Point o = origins.get(i++);
			
			if (v.equals(o))
				continue;
			
			// connect from origin to finishVertex
			connectWithoutCheck(graph, o, v);
		}
	}

	@Override
	protected double calculateWeight(Point from, Point to) {
		double t1 = from.getY(), t2 = to.getY();
		
		return t2 - t1;
	}
	
	private static class Candidate {
		
		private final Point origin;
		
		private final Point finishVertex;
		
		public Candidate(Point origin, Point finishVertex) {
			this.origin = origin;
			this.finishVertex = finishVertex;
		}

		public Point getOrigin() {
			return origin;
		}

		public Point getFinishVertex() {
			return finishVertex;
		}
		
	}

}
