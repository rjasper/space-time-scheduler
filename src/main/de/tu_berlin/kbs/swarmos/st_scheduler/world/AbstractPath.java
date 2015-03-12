package de.tu_berlin.kbs.swarmos.st_scheduler.world;

import java.util.Iterator;

public abstract class AbstractPath<
	V extends Path.Vertex,
	S extends Path.Segment<? extends V>>
implements Path<V, S>
{
	
	/**
	 * Base vertex iterator to iterate over path vertices.
	 * 
	 * @param <V> the vertex type.
	 */
	protected class VertexIterator implements Iterator<V> {
		
		/**
		 * The current position of the iterator.
		 */
		private int index = 0;

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return index < size();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public V next() {
			return getVertex(index++);
		}
	}

	/**
	 * Base segment iterator to iterate over path segments.
	 *
	 * @param <V>
	 *            the vertex type
	 * @param <S>
	 *            the segment type
	 */
	protected class SegmentIterator implements Iterator<S> {
		
		/**
		 * The current position of the iterator.
		 */
		private int index = 0;
		
		private V lastVertex = isEmpty() ? null : getFirstVertex();
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return index < size()-1;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public S next() {
			V start = lastVertex;
			V finish = getVertex(++index);
			
			S segment = makeSegment(start, finish);
			lastVertex = finish;
			
			return segment;
		}
		
	}

	/**
	 * Makes a vertex for the given index.
	 * 
	 * @param index
	 * @param first whether it's the first vertex
	 * @param last whether it's the last vertex
	 * @return the vertex.
	 */
	protected abstract V makeVertex(int index, boolean first, boolean last);

	/**
	 * Makes a segment from the start vertex to the finish vertex.
	 * 
	 * @param start
	 * @param finish
	 * @return the segment.
	 */
	protected abstract S makeSegment(V start, V finish);

	/*
	 * (non-Javadoc)
	 * @see world.Path#getVertex(int)
	 */
	@Override
	public V getVertex(int index) {
		if (index < 0 || index >= size())
			throw new IllegalArgumentException("invalid index");
		
		boolean first = index == 0;
		boolean last = index == size() - 1;
		
		return makeVertex(index, first, last);
	}
	
	/*
	 * (non-Javadoc)
	 * @see world.Path#getSegment(int)
	 */
	@Override
	public S getSegment(int index) {
		if (index < 0 || index >= size()-1)
			throw new IllegalArgumentException("invalid index");
		
		return makeSegment(getVertex(index), getVertex(index+1));
	}
	
	/* (non-Javadoc)
	 * @see world.Path#vertexIterator()
	 */
	@Override
	public Iterator<V> vertexIterator() {
		return new VertexIterator();
	}

	/* (non-Javadoc)
	 * @see world.Path#segmentIterator()
	 */
	@Override
	public Iterator<S> segmentIterator() {
		return new SegmentIterator();
	}
	
}
