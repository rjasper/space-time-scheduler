package world;

import static java.lang.Math.*;
import java.util.NoSuchElementException;

import jts.geom.immutable.ImmutableLineString;
import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public interface PointPath<
	V extends PointPath.Vertex,
	S extends PointPath.Segment<? extends V>>
extends Path<V, S>
{

	/**
	 * The vertex of a {@code Path}. Stores additional information about the
	 * vertex in context to the path.
	 */
	public static class Vertex implements Path.Vertex {
		
		/**
		 * The point of the vertex.
		 */
		private final ImmutablePoint point;
		
		/**
		 * The position of the vertex.
		 */
		private final int index;
		
		/**
		 * Whether the vertex is the first one.
		 */
		private final boolean first;
		
		/**
		 * Whether the vertex is the last one.
		 */
		private final boolean last;
		
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
		protected Vertex(int index, ImmutablePoint point, boolean first, boolean last) {
			this.index = index;
			this.point = point;
			this.first = first;
			this.last = last;
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
	
		/* (non-Javadoc)
		 * @see world.PathVertex#getIndex()
		 */
		@Override
		public int getIndex() {
			return index;
		}
	
		/* (non-Javadoc)
		 * @see world.PathVertex#isFirst()
		 */
		@Override
		public boolean isFirst() {
			return first;
		}
	
		/* (non-Javadoc)
		 * @see world.PathVertex#isLast()
		 */
		@Override
		public boolean isLast() {
			return last;
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
	public static class Segment<V extends PointPath.Vertex> implements Path.Segment<V> {
	
		/**
		 * The start vertex.
		 */
		private final V start;
		
		/**
		 * The finish vertex.
		 */
		private final V finish;
		
		/**
		 * Constructs a new {@code Segment} connecting the given vertices.
		 * 
		 * @param start
		 *            start vertex
		 * @param finish
		 *            finish vertex
		 */
		protected Segment(V start, V finish) {
			this.start = start;
			this.finish = finish;
		}
		
		/* (non-Javadoc)
		 * @see world.PathSegment#isFirst()
		 */
		@Override
		public boolean isFirst() {
			return start.isFirst();
		}
	
		/* (non-Javadoc)
		 * @see world.PathSegment#isLast()
		 */
		@Override
		public boolean isLast() {
			return finish.isLast();
		}
	
		/* (non-Javadoc)
		 * @see world.PathSegment#getStartVertex()
		 */
		@Override
		public V getStartVertex() {
			return start;
		}
	
		/* (non-Javadoc)
		 * @see world.PathSegment#getFinishVertex()
		 */
		@Override
		public V getFinishVertex() {
			return finish;
		}
		
		/**
		 * @return the start point.
		 */
		public ImmutablePoint getStartPoint() {
			return start.getPoint();
		}
		
		/**
		 * @return the finish point
		 */
		public ImmutablePoint getFinishPoint() {
			return finish.getPoint();
		}
		
		public Envelope getEnvelope() {
			Point p1 = getStartPoint(), p2 = getFinishPoint();
			double x1 = p1.getX(), y1 = p1.getY(), x2 = p2.getX(), y2 = p2.getY();
			
			return new Envelope(min(x1, x2), max(x1, x2), min(y1, y2), max(y1, y2));
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("(%s, %s)", getStartVertex(), getFinishVertex());
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