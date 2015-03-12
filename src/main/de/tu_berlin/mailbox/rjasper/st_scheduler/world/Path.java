package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static java.util.Spliterator.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// TODO document
public interface Path<V extends Path.Vertex, S extends Path.Segment<? extends V>> {


	/**
	 * A {@code Vertex} represent a corner point of a path. Each vertex,
	 * excluding the first and the last one, have exactly one predecessor vertex
	 * and one successor vertex. Two neighboring vertices are connected by a
	 * linear segment. A path has at least two vertices. All vertices and
	 * segments define the path.
	 */
	public static class Vertex {
		
		private final Path<?, ?> path;
		
		protected final int index;

		public Vertex(Path<?, ?> path, int index) {
			this.path = Objects.requireNonNull(path, "path");
			this.index = index;
			
			if (index < 0 || index >= path.size())
				throw new IndexOutOfBoundsException();
		}

		/**
		 * @return the index
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * @return whether the vertex is the first one.
		 */
		public boolean isFirst() {
			return index == 0;
		}

		/**
		 * @return last whether the vertex is the last one.
		 */
		public boolean isLast() {
			return index == path.size()-1;
		}
		
	}

	/**
	 * A {@code Segment} represents an interconnection of two neighboring
	 * vertices. Each segment, excluding the first and the last one, have
	 * exactly one predecessor segment and one successor segment. A path has at
	 * least one segment. All vertices and segments define the path.
	 */
	public static class Segment<V extends Vertex> {
		
		protected final V startVertex;
		
		protected final V finishVertex;

		public Segment(V startVertex, V finishVertex) {
			this.startVertex = Objects.requireNonNull(startVertex, "startVertex");
			this.finishVertex = Objects.requireNonNull(finishVertex, "finishVertex");
		}

		/**
		 * @return the index
		 */
		public int getIndex() {
			return startVertex.getIndex();
		}

		/**
		 * @return whether the segment is the first one.
		 */
		public boolean isFirst() {
			return startVertex.isFirst();
		}

		/**
		 * @return whether the segment is the last one.
		 */
		public boolean isLast() {
			return finishVertex.isLast();
		}

		/**
		 * @return the start vertex.
		 */
		public V getStartVertex() {
			return startVertex;
		}

		/**
		 * @return the finish vertex.
		 */
		public V getFinishVertex() {
			return finishVertex;
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