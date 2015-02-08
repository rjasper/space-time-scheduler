package world;

import static java.util.Spliterator.*;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jts.geom.immutable.ImmutablePoint;

import org.apache.commons.collections4.iterators.IteratorIterable;

import util.DurationConv;
import world.util.ArcTimePathInterpolator;
import world.util.BinarySearchVertexSeeker;
import world.util.Interpolator;
import world.util.VertexSeeker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.vividsolutions.jts.geom.Point;

/**
 * Implements an arc-time path. It ensures that the arcs are non-negative and
 * that the time ordinates are in increasing order.
 * 
 * @author Rico
 */
public class ArcTimePath extends AbstractPointPath<ArcTimePath.Vertex, ArcTimePath.Segment> {
	
	/**
	 * An empty {@code ArcTimePath}.
	 */
	private static final ArcTimePath EMPTY = new ArcTimePath(ImmutableList.of());
	
	/**
	 * @return an empty {@code ArcTimePath}.
	 */
	public static ArcTimePath empty() {
		return EMPTY;
	}

	/**
	 * Caches the duration of the path.
	 */
	private transient Duration duration = null;
	
	/**
	 * Caches the length of the path.
	 */
	private transient double length = Double.NaN;
	
	/**
	 * Caches the minimum arc.
	 */
	private transient double minArc = Double.NaN;

	/**
	 * Caches the maximum arc.
	 */
	private transient double maxArc = Double.NaN;

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
	 * @see world.Path#checkVertices(java.util.List)
	 */
	@Override
	protected void checkVertices(List<? extends Point> vertices) {
		super.checkVertices(vertices);
		
		// check arc ordinates
		// arcs have to be equal or greater than 0
		
		boolean nonNegativeArcs = vertices.stream()
			.map(Point::getX) // arc ordinate
			.allMatch(s -> s >= 0.0);
		
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
	 * @see world.Path#create(com.google.common.collect.ImmutableList)
	 */
	@Override
	protected ArcTimePath create(ImmutableList<ImmutablePoint> vertices) {
		return new ArcTimePath(vertices);
	}
	
	/*
	 * (non-Javadoc)
	 * @see world.Path#getEmpty()
	 */
	@Override
	protected ArcTimePath getEmpty() {
		return empty();
	}
	
	/**
	 * The vertex of a {@code ArcTimePath}. Stores additional information about
	 * the vertex in context to the path.
	 */
	public static class Vertex extends PointPath.Vertex {

		/**
		 * Constructs a new {@code Vertex}.
		 * 
		 * @param index
		 * @param point
		 * @param first
		 *            whether the vertex is the first one
		 * @param last
		 *            whether the vertex is the last one
		 */
		private Vertex(int index, ImmutablePoint point, boolean first, boolean last) {
			super(index, point, first, last);
		}
		
	}
	
	/**
	 * The segment of a {@code ArcTimePath}. Stores additional information about
	 * the segment in context to the path.
	 */
	public static class Segment extends PointPath.Segment<Vertex> {
		
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
			return Math.abs(getFinishVertex().getX() - getStartVertex().getX());
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

	/*
	 * (non-Javadoc)
	 * @see world.AbstractPointPath#makeVertex(int, jts.geom.immutable.ImmutablePoint, boolean, boolean)
	 */
	@Override
	protected Vertex makeVertex(int index, ImmutablePoint point, boolean first, boolean last) {
		return new Vertex(index, point, first, last);
	}

	/*
	 * (non-Javadoc)
	 * @see world.AbstractPath#makeSegment(world.Path.Vertex, world.Path.Vertex)
	 */
	@Override
	protected Segment makeSegment(Vertex start, Vertex finish) {
		return new Segment(start, finish);
	}

	/**
	 * Interpolates the arc value of the given time.
	 * 
	 * @param time
	 * @return the arc value.
	 * @throws IllegalArgumentException
	 *             if the time is not within the range [{@link #minArc()},
	 *             {@link #maxArc()}].
	 */
	public double interpolateArc(double time) {
		VertexSeeker<Vertex> seeker =
			new BinarySearchVertexSeeker<Vertex, ArcTimePath>(this, Vertex::getY);
		Interpolator<Double> interpolator =
			new ArcTimePathInterpolator(this, seeker);
		
		return interpolator.interpolate(time);
	}

	/**
	 * @return the minimum arc.
	 */
	public double minArc() {
		if (isEmpty())
			return Double.NaN;
		
		if (Double.isNaN(minArc)) {
			minArc = vertexStream()
				.mapToDouble(Vertex::getX)
				.min().getAsDouble();
		}
		
		return minArc;
	}

	/**
	 * @return the maximum arc.
	 */
	public double maxArc() {
		if (isEmpty())
			return Double.NaN;
		
		if (Double.isNaN(maxArc)) {
			maxArc = vertexStream()
				.mapToDouble(Vertex::getX)
				.max().getAsDouble();
		}
		
		return maxArc;
	}
	
	/**
	 * @return the length of the path.
	 */
	public double length() {
		if (Double.isNaN(length)) {
			if (isEmpty()) {
				length = 0.0;
			} else {
				Stream<Segment> segments = StreamSupport.stream(Spliterators.spliterator(
					segmentIterator(),
					size()-1,
					IMMUTABLE | SIZED | NONNULL), false);
				
				length = segments
					.mapToDouble(Segment::length)
					.sum();
			}
		}
		
		return length;
	}
	
	/**
	 * @return the duration of the path.
	 */
	public Duration duration() {
		if (duration == null) {
			double seconds = getPoint(size()-1).getY();
			
			duration = DurationConv.ofSeconds(seconds);
		}
		
		return duration;
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#concat(world.Path)
	 */
	@Override
	public ArcTimePath concat(Path<? extends Vertex, ? extends Segment> other) {
		if (!(other instanceof ArcTimePath))
			throw new IllegalArgumentException("incompatible path");
		
		return (ArcTimePath) super.concat(other);
	}
	
	/* (non-Javadoc)
	 * @see world.Path#subPath(int, double, int, double)
	 */
	@Override
	public ArcTimePath subPath(
		int startIndexInclusive,
		double startAlpha,
		int finishIndexExclusive,
		double finishAlpha)
	{
		return (ArcTimePath) super.subPath(
			startIndexInclusive,
			startAlpha,
			finishIndexExclusive,
			finishAlpha);
	}

}
