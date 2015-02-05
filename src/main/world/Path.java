package world;

import static java.util.Spliterator.*;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
	public default Spliterator<V> vertexSpliterator() {
		return Spliterators.spliterator(vertexIterator(), size(), NONNULL | SIZED | IMMUTABLE | ORDERED);
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
		return Spliterators.spliterator(segmentIterator(), size(), NONNULL | SIZED | IMMUTABLE | ORDERED);
	}

	/**
	 * @return a {@code Stream} over all segments.
	 */
	public default Stream<S> segmentStream() {
		return StreamSupport.stream(segmentSpliterator(), false);
	}

}