package world;

import jts.geom.immutable.ImmutableLineString;
import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;

public interface PointPath<V extends PointPath.Vertex, S extends PointPath.Segment<? extends V>> extends Path<V, S> {

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
	
		/*
		 * (non-Javadoc)
		 * @see world.Path.Segment#getIndex()
		 */
		@Override
		public int getIndex() {
			return start.getIndex();
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
		
	}

	/**
	 * @return the points
	 */
	public abstract ImmutableList<ImmutablePoint> getPoints();

	/**
	 * Calculates the trace of this path. The trace is a {@code LineString}
	 * containing all points visited by the path.
	 * 
	 * @return the line string.
	 */
	public abstract ImmutableLineString trace();

	/**
	 * <p>
	 * Calculates a sub path from this path. The index parameters specify the
	 * the relevant segments. The alpha values specify the start and finish
	 * points of the first and last segment to be included. Such a point is
	 * calculated according to this formular:
	 * </p>
	 * 
	 * <i>point(i, alpha) = (x<sub>i</sub> +
	 * alpha*x<sub>i</sub>/(x<sub>i+1</sub> - x<sub>i</sub>), y<sub>i</sub> +
	 * alpha*<sub>i</sub>/(y<sub>i+1</sub> - y<sub>i</sub>))</i>
	 * 
	 * <p>
	 * Where <i>i</i> is the index of the segment and <i>(x<sub>i</sub>,
	 * y<sub>i</sub>)</i> is the <i>i</i>-th vertex.
	 * </p>
	 * 
	 * @param startIndexInclusive
	 *            the position of the first segment
	 * @param startAlpha
	 *            specifies the start point on the first segment
	 * @param finishIndexExclusive
	 *            the position after the last segment
	 * @param finishAlpha
	 *            specifies the finish point on the last segment
	 * @return the sub path
	 * @throw IllegalArgumentException if any of the following is true:
	 *        <ul>
	 *        <li>The indices are out of bounds.</li>
	 *        <li>The alpha values are not within [0, 1)</li>
	 *        <li>The finish index is equal to size()-1 and finish alpha is not
	 *        zero</li>
	 *        </ul>
	 */
	public abstract PointPath<V, S> subPath(int startIndexInclusive,
		double startAlpha, int finishIndexExclusive, double finishAlpha);

	/**
	 * <p>
	 * Interpolates a point on segment using this formula:
	 * </p>
	 * 
	 * <i>point(i, alpha) = (x<sub>i</sub> +
	 * alpha*x<sub>i</sub>/(x<sub>i+1</sub> - x<sub>i</sub>), y<sub>i</sub> +
	 * alpha*<sub>i</sub>/(y<sub>i+1</sub> - y<sub>i</sub>))</i>
	 * 
	 * @param index
	 *            the index of the segment
	 * @param alpha
	 * @return the interpolated point.
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>If {@code index} does not represent a valid point
	 *             position.</li>
	 *             <li>If {@code alpha} is not within [0, 1).</li>
	 *             <li>If {@code index} is equal to {@link #size()}{@code -1} and
	 *             alpha is unequal to {@code 0.0}.</li>
	 *             </ul>
	 */
	public abstract ImmutablePoint interpolate(int index, double alpha);

}