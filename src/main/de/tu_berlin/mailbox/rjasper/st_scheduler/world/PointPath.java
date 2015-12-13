package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static java.lang.Double.max;
import static java.lang.Double.min;

import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableLineString;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;

public interface PointPath<
	V extends PointPath.Vertex,
	S extends PointPath.Segment<? extends V>>
extends Path<V, S>
{

	/**
	 * The vertex of a {@code Path}. Stores additional information about the
	 * vertex in context to the path.
	 */
	public static class Vertex extends Path.Vertex {
		
		/**
		 * The point of the vertex.
		 */
		private final ImmutablePoint point;
		
		/**
		 * Constructs a new {@code Vertex}.
		 * 
		 * @param index
		 * @param point
		 */
		protected Vertex(PointPath<?, ?> path, int index, ImmutablePoint point) {
			super(path, index);
			
			this.point = point;
		}
	
		/**
		 * @return the point
		 */
		public ImmutablePoint getPoint() {
			return point;
		}
	
		/**
		 * @return x-ordinate of the point.
		 */
		public double getX() {
			return point.getX();
		}
	
		/**
		 * @return y-ordinate of the point.
		 */
		public double getY() {
			return point.getY();
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("<%.2f, %.2f>", getX(), getY());
		}
		
	}

	/**
	 * The segment of a {@code Path}. Stores additional information about the
	 * segment in context to the path.
	 * 
	 * @param <V> the vertex type
	 */
	public static class Segment<V extends Vertex> extends Path.Segment<V> {
		
		/**
		 * Constructs a new {@code Segment} connecting the given vertices.
		 * 
		 * @param start
		 *            start vertex
		 * @param finish
		 *            finish vertex
		 */
		protected Segment(V start, V finish) {
			super(start, finish);
		}
		
		/**
		 * @return the start point.
		 */
		public ImmutablePoint getStartPoint() {
			return startVertex.getPoint();
		}
		
		/**
		 * @return the finish point
		 */
		public ImmutablePoint getFinishPoint() {
			return finishVertex.getPoint();
		}
		
		public Envelope getEnvelope() {
			Point p1 = getStartPoint(), p2 = getFinishPoint();
			double x1 = p1.getX(), y1 = p1.getY(), x2 = p2.getX(), y2 = p2.getY();
			
			return new Envelope(min(x1, x2), max(x1, x2), min(y1, y2), max(y1, y2));
		}
		
	}

	/**
	 * @return the points
	 */
	public abstract ImmutableList<ImmutablePoint> getPoints();
	
	/**
	 * Returns the point at the specified index.
	 * 
	 * @param index
	 * @return the point.
	 */
	public abstract ImmutablePoint getPoint(int index);

	/**
	 * @return the first point.
	 * @throws NoSuchElementException
	 *             if the path is empty.
	 */
	public default ImmutablePoint getFirstPoint() {
		if (isEmpty())
			throw new NoSuchElementException("path is empty");
		
		return getPoint(0);
	}

	/**
	 * @return the last point.
	 * @throws NoSuchElementException
	 *             if the path is empty.
	 */
	public default ImmutablePoint getLastPoint() {
		if (isEmpty())
			throw new NoSuchElementException("path is empty");
		
		return getPoint(size()-1);
	}
	
	/**
	 * @return the envelope of this path.
	 */
	public abstract Envelope getEnvelope();

	@Override
	public abstract PointPath<V, S> subPath(double startSubIndex, double finishSubIndex);

	/**
	 * Calculates the trace of this path. The trace is either an
	 * {@link ImmutablePoint} or an {@link ImmutableLineString}. containing all
	 * points visited by the path.
	 * 
	 * @return the trace.
	 */
	public abstract Geometry trace();

}