package world;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;

import jts.geom.immutable.ImmutablePoint;

// TODO document
public interface Path<V extends Path.Vertex, S extends Path.Segment<? extends V>> {

	public static interface Vertex {
	
		/**
		 * @return the index
		 */
		public abstract int getIndex();
	
		/**
		 * @return whether the vertex is the first one.
		 */
		public abstract boolean isFirst();
	
		/**
		 * @return last whether the vertex is the last one.
		 */
		public abstract boolean isLast();
	
	}

	public static interface Segment<V extends Vertex> {

		/**
		 * @return the index
		 */
		public abstract int getIndex();
	
		/**
		 * @return whether the segment is the first one.
		 */
		public abstract boolean isFirst();
	
		/**
		 * @return whether the segment is the last one.
		 */
		public abstract boolean isLast();
	
		/**
		 * @return the start vertex.
		 */
		public abstract V getStartVertex();
	
		/**
		 * @return the finish vertex.
		 */
		public abstract V getFinishVertex();
	
	}

	/**
	 * @return if the path is empty.
	 */
	public abstract boolean isEmpty();

	/**
	 * @return the amount of vertices.
	 */
	public abstract int size();

	/**
	 * Gets the point at the specified position of this path.
	 * 
	 * @param index
	 * @return the point.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 || index >= size())
	 */
	public abstract ImmutablePoint get(int index);

	/**
	 * Gets the vertex at the first position of this path.
	 * 
	 * @param index
	 * @return the point. {@code null} if the path is empty.
	 */
	public abstract V getFirstVertex();

	/**
	 * Gets the vertex at the last position of this path.
	 * 
	 * @param index
	 * @return the point. {@code null} if the path is empty.
	 */
	public abstract V getLastVertex();

	/**
	 * Concatenates this path with the given one.
	 * 
	 * @param other
	 * @return the concatenated path.
	 */
	public abstract Path<V, S> concat(Path<? extends V, ? extends S> other);

	/**
	 * @return a {@code VertexIterator}
	 */
	public abstract Iterator<V> vertexIterator();

	/**
	 * @return a {@code Spliterator} over all vertices.
	 */
	public abstract Spliterator<V> vertexSpliterator();

	/**
	 * @return a {@code Stream} over all vertices.
	 */
	public abstract Stream<V> vertexStream();

	/**
	 * @return a {@code SegmentIterator}.
	 */
	public abstract Iterator<S> segmentIterator();

	/**
	 * @return a {@code Spliterator} over all segments.
	 */
	public abstract Spliterator<S> segmentSpliterator();

	/**
	 * @return a {@code Stream} over all segments.
	 */
	public abstract Stream<S> segmentStream();

}