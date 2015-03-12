package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static java.util.Spliterator.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// TODO document
public interface Path<V extends Path.Vertex, S extends Path.Segment<? extends V>> {

	// TODO should belong to the path (non-static)
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

	// TODO should belong to the path (non-static)
	public static interface Segment<V extends Vertex> {

		/**
		 * @return the index
		 */
		public default int getIndex() {
			return getStartVertex().getIndex();
		}
	
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
	public default boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * @return the amount of vertices.
	 */
	public abstract int size();

	/**
	 * @return the first vertex.
	 * @throws NoSuchElementException
	 *             if the path is empty.
	 */
	public default V getFirstVertex() {
		if (isEmpty())
			throw new NoSuchElementException("path is empty");
		
		return getVertex(0);
	}

	/**
	 * @return the last vertex.
	 * @throws NoSuchElementException
	 *             if the path is empty.
	 */
	public default V getLastVertex() {
		if (isEmpty())
			throw new NoSuchElementException("path is empty");
		
		return getVertex(size()-1);
	}
	
	/**
	 * Returns the vertex at position {@code index}.
	 * 
	 * @param index
	 * @return the vertex.
	 * @throws IllegalArgumentException
	 *             if the {@code index} is invalid.
	 */
	public abstract V getVertex(int index);

	/**
	 * @return the first segment.
	 * @throws NoSuchElementException
	 *             if the path is empty.
	 */
	public default S getFirstSegment() {
		if (isEmpty())
			throw new NoSuchElementException("path is empty");
		
		return getSegment(0);
	}

	/**
	 * @return the last segment.
	 * @throws NoSuchElementException
	 *             if the path is empty.
	 */
	public default S getLastSegment() {
		if (isEmpty())
			throw new NoSuchElementException("path is empty");
		
		return getSegment(size()-2);
	}

	/**
	 * Returns the segment at position {@code index}.
	 * 
	 * @param index
	 * @return the segment.
	 * @throws IllegalArgumentException
	 *             if the {@code index} is invalid.
	 */
	public abstract S getSegment(int index);
	
	/**
	 * Concatenates this path with the given one.
	 * 
	 * @param other
	 * @return the concatenated path.
	 */
	public abstract Path<V, S> concat(Path<? extends V, ? extends S> other);
	
	/**
	 * Returns a sub path from the given start sub-index to the finish sub-index.
	 * 
	 * @param fromSubIndex
	 * @param toSubIndex
	 * @return the sub path.
	 */
	public abstract Path<V, S> subPath(double fromSubIndex, double toSubIndex);

	/**
	 * @return a {@code VertexIterator}
	 */
	public abstract Iterator<V> vertexIterator();

	/**
	 * @return a {@code Spliterator} over all vertices.
	 */
	public default Spliterator<V> vertexSpliterator() {
		return Spliterators.spliterator(
			vertexIterator(), size(),
			NONNULL | SIZED | IMMUTABLE | ORDERED);
	}

	/**
	 * @return a {@code Stream} over all vertices.
	 */
	public default Stream<V> vertexStream() {
		return StreamSupport.stream(vertexSpliterator(), false);
	}
	
	/**
	 * @return a {@code SegmentIterator}.
	 */
	public abstract Iterator<S> segmentIterator();

	/**
	 * @return a {@code Spliterator} over all segments.
	 */
	public default Spliterator<S> segmentSpliterator() {
		return Spliterators.spliterator(
			segmentIterator(), size(),
			NONNULL | SIZED | IMMUTABLE | ORDERED);
	}

	/**
	 * @return a {@code Stream} over all segments.
	 */
	public default Stream<S> segmentStream() {
		return StreamSupport.stream(segmentSpliterator(), false);
	}

}