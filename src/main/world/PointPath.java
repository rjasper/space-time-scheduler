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
	 * Returns the point at the specified index.
	 * 
	 * @param index
	 * @return the point.
	 */
	public abstract ImmutablePoint getPoint(int index);

	/**
	 * @return the first point.
	 */
	public default ImmutablePoint getFirstPoint() {
		if (isEmpty())
			return null;
		else
			return getPoint(0);
	}

	/**
	 * @return the last point.
	 */
	public default ImmutablePoint getLastPoint() {
		if (isEmpty())
			return null;
		else
			return getPoint(size()-1);
	}

	/**
	 * Calculates the trace of this path. The trace is a {@code LineString}
	 * containing all points visited by the path.
	 * 
	 * @return the line string.
	 */
	public abstract ImmutableLineString trace();

}