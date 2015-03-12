package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;

/**
 * <p>
 * The {@code MinimumTimeMeshBuilder} implements an {@link AbstractMeshBuilder}.
 * It builds a graph with exactly one start vertex but multiple finish vertices.
 * The start vertex has to be set before building the graph. The finish vertices
 * are determined by the builder. The builder guarantees that if it is possible
 * to reach the maximum arc then the finish vertex of the path with minimum time
 * from start to finish is among the determined finish vertices.
 * </p>
 * 
 * <p>
 * In other words, this builder provides a graph which includes the minimum
 * path in respect to time.
 * </p>
 * 
 * @author Rico Jasper
 */
public class SimpleMinimumTimeMeshBuilder extends AbstractMeshBuilder {
	
	/**
	 * The start vertex.
	 */
	private Point startPoint = null;
	
	/**
	 * The finish arc.
	 */
	private double finishArc = Double.NaN;
	
	/**
	 * The earliest time for the finish vertices.
	 */
	private double earliestFinishTime = Double.NaN;
	
	/**
	 * The latest time for the finish verticies.
	 */
	private double latestFinishTime = Double.NaN;
	
	/**
	 * The duration ahead of the finish vertex without forbidden regions.
	 */
	private double bufferDuration = 0.0;
	
	/**
	 * The pairs of finish vertices and their predecessors.
	 */
	private List<VertexPair> finishVertexPairs;
	
	/**
	 * The earliest finish vertex determined by the earliest finish time.
	 */
	private Point earliestFinishVertex;
	
	/**
	 * @return the startPoint
	 */
	private Point getStartPoint() {
		return startPoint;
	}

	/**
	 * Sets the start vertex.
	 * 
	 * @param startPoint
	 * @throws NullPointerException
	 *             if startPoint is {@code null}.
	 */
	public void setStartPoint(Point startPoint) {
		GeometriesRequire.requireValid2DPoint(startPoint, "startPoint");
		
		this.startPoint = startPoint;
	}

	/**
	 * @return the finishArc
	 */
	private double getFinishArc() {
		return finishArc;
	}

	/**
	 * @param finishArc the finishArc to set
	 */
	public void setFinishArc(double finishArc) {
		this.finishArc = finishArc;
	}

	/**
	 * @return the earliest finish time for finish vertices.
	 */
	private double getEarliestFinishTime() {
		return earliestFinishTime;
	}

	/**
	 * Sets the earliest time for finish vertices.
	 * 
	 * @param earliestFinishTime
	 * @throws IllegalArgumentException
	 *             if earliestFinishTime is not finite.
	 */
	public void setEarliestFinishTime(double earliestFinishTime) {
		if (!Double.isFinite(earliestFinishTime))
			throw new IllegalArgumentException("value is not finite");
		
		this.earliestFinishTime = earliestFinishTime;
	}

	/**
	 * @return the latest tame for finish vertices.
	 */
	private double getLatestFinishTime() {
		return latestFinishTime;
	}

	/**
	 * Sets the latest time for finish vertices.
	 * 
	 * @param latestFinishTime
	 * @throws IllegalArgumentException
	 *             if latestFinishTime is not finite.
	 */
	public void setLatestFinishTime(double latestFinishTime) {
		if (!Double.isFinite(latestFinishTime))
			throw new IllegalArgumentException("value is not finite");
		
		this.latestFinishTime = latestFinishTime;
	}

	/**
	 * @return the buffer duration.
	 */
	private double getBufferDuration() {
		return bufferDuration;
	}

	/**
	 * <p>
	 * Sets the buffer duration.
	 * </p>
	 * 
	 * <p>
	 * The determined finish vertices will not have any forbidden regions above
	 * them for the specified duration. In other words, for at least the buffer
	 * duration there will be no collision with any dynamic obstacles for all
	 * determined finish vertices (i.e. at the finish of the path).
	 * </p>
	 * 
	 * @param bufferDuration
	 * @throws IllegalArgumentException if bufferDuration is not non-positive
	 */
	public void setBufferDuration(double bufferDuration) {
		if (!Double.isFinite(bufferDuration) || bufferDuration < 0.0)
			throw new IllegalArgumentException("value is not finite");
		
		this.bufferDuration = bufferDuration;
	}

	/**
	 * @return the finish vertex pairs
	 */
	private List<VertexPair> getFinishVertexPairs() {
		return finishVertexPairs;
	}

	/**
	 * Sets the finish vertex pairs.
	 * 
	 * @param finishVertexPairs
	 */
	private void setFinishVertexPairs(List<VertexPair> finishVertexPairs) {
		this.finishVertexPairs = finishVertexPairs;
	}

	/**
	 * @return the earliest finish vertex.
	 */
	private Point getEarliestFinishVertex() {
		return earliestFinishVertex;
	}

	/**
	 * Sets the earliest finish vertex.
	 * 
	 * @param earliestFinishVertex
	 */
	private void setEarliestFinishVertex(Point earliestFinishVertex) {
		this.earliestFinishVertex = earliestFinishVertex;
	}

	/**
	 * Checks if all parameters are properly set. Throws an exception otherwise.
	 * 
	 * @throws IllegalStateException
	 *             if any parameter is not set.
	 */
	@Override
	protected void checkParameters() {
		super.checkParameters();
		
		if (startPoint == null               ||
			Double.isNaN(finishArc         ) ||
			Double.isNaN(earliestFinishTime) ||
			Double.isNaN(latestFinishTime  ))
		{
			throw new IllegalStateException("some parameters are not set");
		}
		
		if (startPoint.getX() < getMinArc() || finishArc > getMaxArc())
			throw new IllegalStateException("start and finish vertices outside of arc range");
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.ArcTimeMeshBuilder#buildStartVertices()
	 */
	@Override
	protected Collection<Point> buildStartVertices() {
		return Collections.singleton(getStartPoint());
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.ArcTimeMeshBuilder#buildFinishVertices()
	 */
	@Override
	protected Collection<Point> buildFinishVertices() {
		// note that core and start vertices are build before finish vertices
		Collection<Point> coreVertices = getCoreVertices();
		Collection<Point> startVertices = getStartVertices();
		
		double minArc = getMinArc();
		double maxArc = getMaxArc();
		double earliest = getEarliestFinishTime();
		
		Point earliestFinishVertex = point(maxArc, earliest);
		
		List<VertexPair> finishVertexPairs = Stream.concat(coreVertices.stream(), startVertices.stream())
			.filter((v) -> v.getX() >= minArc && v.getX() <= maxArc) // only within bounds
			.map(this::calculateFinishVertexCandidate) // create candidate
			.filter(this::checkCandidate)              // check candidate (time, visibility and buffer)
			.collect(Collectors.toList());
		
		List<Point> finishVertices = finishVertexPairs.stream()
			.map(VertexPair::getSecond)
			.collect(Collectors.toList());
		
		if (checkBuffer(earliestFinishVertex))
			finishVertices.add(earliestFinishVertex);
		
		setEarliestFinishVertex(earliestFinishVertex);
		setFinishVertexPairs(finishVertexPairs);
		
		return finishVertices;
	}
	
	/**
	 * <p>
	 * Calculates a possible finish vertex by drawing a line at maximum speed
	 * from a original vertex to the maximum arc.
	 * </p>
	 * 
	 * <p>
	 * This method assumes that the origin does not lie beyond the maximum arc.
	 * </p>
	 * 
	 * @param origin
	 * @return the finish vertex candidate.
	 */
	private VertexPair calculateFinishVertexCandidate(Point origin) {
		// method assumes that the origin's arc is not greater than maxArc
		
		double s = origin.getX(), t = origin.getY();
		double maxSpeed = getMaxSpeed();
		double finishArc = getFinishArc();
		
		if (s == finishArc) {
			return new VertexPair(origin, origin);
		} else { // s < maxArc
			Point finishPoint = point(finishArc, (finishArc - s) / maxSpeed + t);
			
			return new VertexPair(origin, finishPoint);
		}
	}
	
	/**
	 * <p>
	 * Checks if the given vertex pair provides a valid connection.
	 * </p>
	 * 
	 * <p>
	 * Both points of the pair need to have a clear line of sight and the
	 * finish point must be collision free at place for at least
	 * {@link #bufferDuration} amount of time.
	 * 
	 * @param candidate origin and finish vertex pair
	 * @return {@code true} if the pair has a valid connection.
	 */
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
	
	/**
	 * Checks if the vertex is collision free at its arc for at least
	 * {@link #bufferDuration} amount of time.
	 * 
	 * @param vertex
	 * @return {@code true} if the vertex is collision free.
	 */
	private boolean checkBuffer(Point vertex) {
		double s = vertex.getX(), t = vertex.getY();
		double buffer = getBufferDuration();
		
		Point p1 = vertex;
		Point p2 = point(s, t + buffer);
		
		return checkVisibility(p1, p2);
	}
	
	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.ArcTimeMeshBuilder#connectVertices(org.jgrapht.graph.DefaultDirectedWeightedGraph)
	 */
	@Override
	protected void connectVertices(DefaultDirectedWeightedGraph<Point, DefaultWeightedEdge> graph) {
		Collection<Point> start = getStartVertices();
		Collection<Point> core = getCoreVertices();
		Collection<Point> earliestFinish = singleton( getEarliestFinishVertex() );
		Collection<VertexPair> finishPairs = getFinishVertexPairs();
		
		connect(graph, start, core);
		connect(graph, start, earliestFinish);
		connect(graph, core, core);
		connect(graph, core, earliestFinish);
		
		for (VertexPair p : finishPairs) {
			Point origin = p.getFirst();
			Point finish = p.getSecond();
			
			if (finish.equals(origin))
				continue;
			
			connectWithoutCheck(graph, origin, finish);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see world.pathfinder.ArcTimeMeshBuilder#calculateWeight(com.vividsolutions.jts.geom.Point, com.vividsolutions.jts.geom.Point)
	 */
	@Override
	protected double calculateWeight(Point from, Point to) {
		double t1 = from.getY(), t2 = to.getY();
		
		return t2 - t1;
	}
	
	/**
	 * Helper class to store a pair of two vertices.
	 */
	private static class VertexPair {
		
		/**
		 * The first vertex.
		 */
		private final Point first;
		
		/**
		 * The second vertex.
		 */
		private final Point second;
		
		/**
		 * Constructs a pair.
		 * 
		 * @param first
		 * @param second
		 */
		public VertexPair(Point first, Point second) {
			this.first = first;
			this.second = second;
		}

		/**
		 * @return the first vertex.
		 */
		public Point getFirst() {
			return first;
		}

		/**
		 * @return the second vertex.
		 */
		public Point getSecond() {
			return second;
		}
		
	}

}
