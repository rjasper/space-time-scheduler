package world;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import jts.geom.immutable.ImmutablePoint;

import org.apache.commons.collections4.iterators.IteratorIterable;

import util.DurationConv;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.vividsolutions.jts.geom.Point;

/**
 * Implements an arc-time path. It ensures that the arcs are non-negative and
 * that the time ordinates are in increasing order.
 * 
 * @author Rico
 */
public class ArcTimePath extends Path {
	
	/**
	 * Caches the duration of the path.
	 */
	private transient Duration duration = null;

	/**
	 * Creates an empty {@code ArcTimePath}.
	 */
	public ArcTimePath() {
		super();
	}

	/**
	 * Constructs a arc-time path of the given vertices. The argument will be
	 * stored directly.
	 * 
	 * @param vertices
	 * @throws NullPointerException
	 *             if {@code vertices} are {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code vertices} contain invalid points.
	 */
	public ArcTimePath(ImmutableList<ImmutablePoint> vertices) {
		super(vertices);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#create(com.google.common.collect.ImmutableList)
	 */
	@Override
	protected ArcTimePath create(ImmutableList<ImmutablePoint> vertices) {
		return new ArcTimePath(vertices);
	}
	
	/**
	 * @return the length of the path.
	 */
	public double length() {
		if (isEmpty())
			return 0.0;
		else
			return get(size()-1).getX();
	}
	
	/**
	 * @return the duration of the path.
	 */
	public Duration duration() {
		if (duration == null) {
			double seconds = get(size()-1).getY();
			
			duration = DurationConv.ofSeconds(seconds);
		}
		
		return duration;
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#concat(world.Path)
	 */
	@Override
	public ArcTimePath concat(Path other) {
		if (!(other instanceof ArcTimePath))
			throw new IllegalArgumentException("incompatible path");
		
		return (ArcTimePath) super.concat(other);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#checkVertices(java.util.List)
	 */
	@Override
	protected void checkVertices(List<? extends Point> vertices) {
		super.checkVertices(vertices);
		
		// check arc ordinates
		// arcs have to be equal or greater than 0
		
		boolean nonNegativeArcs = vertices.stream()
			.map(Point::getX) // arc ordinate
			.allMatch(s -> s >= 0);
		
		if (!nonNegativeArcs) // e.g. negative arcs
			throw new IllegalArgumentException("path has negative arc values");
		
		// check time ordinates
		// times have to be strictly increasing
		
		Iterator<Double> it = vertices.stream()
			.map(Point::getY) // time ordinate
			.iterator();
		
		boolean isOrdered = Ordering.natural()
			.isOrdered(new IteratorIterable<>(it));
		
		if (!isOrdered)
			throw new IllegalArgumentException("path is not causal");
	}
	
	/*
	 * (non-Javadoc)
	 * @see world.Path#vertexIterator()
	 */
	@Override
	public Iterator<Vertex> vertexIterator() {
		return new VertexIterator();
	}

	/**
	 * The vertex of a {@code ArcTimePath}. Stores additional information about
	 * the vertex in context to the path.
	 */
	public static class Vertex extends Path.Vertex {

		/**
		 * Constructs a new {@code Vertex}.
		 * 
		 * @param point
		 * @param first
		 *            whether the vertex is the first one
		 * @param last
		 *            whether the vertex is the last one
		 */
		private Vertex(ImmutablePoint point, boolean first, boolean last) {
			super(point, first, last);
		}
		
	}
	
	/**
	 * The {@code VertexIterator} of a {@code ArcTimePath}.
	 */
	private class VertexIterator extends AbstractVertexIterator<Vertex> {

		/*
		 * (non-Javadoc)
		 * @see world.Path.AbstractVertexIterator#nextVertex(jts.geom.immutable.ImmutablePoint)
		 */
		@Override
		protected Vertex nextVertex(ImmutablePoint point) {
			return new Vertex(point, isFirst(), isLast());
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#segmentIterator()
	 */
	@Override
	public Iterator<Segment> segmentIterator() {
		return new SegmentIterator();
	}
	
	/**
	 * The segment of a {@code ArcTimePath}. Stores additional information about
	 * the segment in context to the path.
	 */
	public static class Segment extends Path.Segment<Vertex> {
		
		/**
		 * Constructs a new {@code Segment} connecting the given vertices.
		 * 
		 * @param start
		 *            start vertex
		 * @param finish
		 *            finish vertex
		 */
		private Segment(Vertex start, Vertex finish) {
			super(start, finish);
		}
		
		/**
		 * @return the length of the segment.
		 */
		public double length() {
			return getFinishVertex().getX() - getStartVertex().getX();
		}
		
		/**
		 * @return the duration.
		 */
		public Duration duration() {
			return DurationConv.ofSeconds(durationInSeconds());
		}

		/**
		 * @return the duration in seconds.
		 */
		public double durationInSeconds() {
			return getFinishVertex().getY() - getStartVertex().getY();
		}
		
	}
	
	/**
	 * The {@code SegmentIterator} of a {@code ArcTimePath}.
	 */
	private class SegmentIterator extends AbstractSegmentIterator<Vertex, Segment> {
		
		/*
		 * (non-Javadoc)
		 * @see world.Path.AbstractSegmentIterator#supplyVertexIterator()
		 */
		@Override
		protected Iterator<Vertex> supplyVertexIterator() {
			return new VertexIterator();
		}

		/*
		 * (non-Javadoc)
		 * @see world.Path.AbstractSegmentIterator#nextSegment(world.Path.Vertex, world.Path.Vertex)
		 */
		@Override
		protected Segment nextSegment(Vertex start, Vertex finish) {
			return new Segment(start, finish);
		}
		
	}

}
