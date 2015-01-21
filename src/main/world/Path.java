package world;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static jts.geom.util.GeometrySequencer.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import jts.geom.immutable.ImmutableGeometries;
import jts.geom.immutable.ImmutableLineString;
import jts.geom.immutable.ImmutablePoint;
import jts.geom.util.GeometriesRequire;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.UnmodifiableIterator;
import com.vividsolutions.jts.geom.Point;

/**
 * A {@code Path} is an immutable list of immutable {@link Point}s. It ensures
 * validity of the path. All vertices have to be valid 2-dimensional points.
 * Singular paths of only one vertex are not allowed while empty paths are.
 * 
 * @author Rico
 */
public class Path implements Iterable<ImmutablePoint> {
	
	/**
	 * The vertices of the path.
	 */
	private final ImmutableList<ImmutablePoint> vertices;
	
	/**
	 * Caches the trace of the path.
	 */
	private transient ImmutableLineString trace = null;
	
	/**
	 * Constructs an empty path.
	 */
	public Path() {
		this.vertices = ImmutableList.of();
	}
	
	/**
	 * Constructs a path of the given vertices. If the given list is immutable
	 * and contains immutable points, the argument will be stored directly.
	 * 
	 * @param vertices
	 * @throws NullPointerException
	 *             if {@code vertices} are {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code vertices} contain invalid points.
	 */
	public Path(List<? extends Point> vertices) {
		checkVertices(vertices);
		
		this.vertices = makeImmutable(vertices);
	}
	
	/**
	 * Constructs a path of the given vertices. The argument will be stored
	 * directly.
	 * 
	 * @param vertices
	 * @throws NullPointerException
	 *             if {@code vertices} are {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code vertices} contain invalid points.
	 * 
	 * @param vertices
	 */
	public Path(ImmutableList<ImmutablePoint> vertices) {
		checkVertices(vertices);
		
		this.vertices = vertices;
	}

	/**
	 * Creates a new path containing the given vertices.
	 * 
	 * @param vertices
	 * @return the new path.
	 */
	protected Path create(ImmutableList<ImmutablePoint> vertices) {
		return new Path(vertices);
	}
	
	/**
	 * Checks for validity of the given vertices.
	 * 
	 * @param vertices
	 * @throws NullPointerException
	 *             if the {@code vertices} are {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code vertices} contain invalid points.
	 */
	protected void checkVertices(List<? extends Point> vertices) {
		Objects.requireNonNull(vertices, "vertices");
		
		if (vertices.size() == 1)
			throw new IllegalArgumentException("invalid size");
		
		vertices.forEach(p ->
			GeometriesRequire.requireValid2DPoint((Point) p, "vertices"));
	}
	
	/**
	 * Makes a immutable list of immutable points from the given list of
	 * vertices.
	 * 
	 * @param vertices
	 * @return the list.
	 */
	@SuppressWarnings("unchecked")
	private ImmutableList<ImmutablePoint> makeImmutable(List<? extends Point> vertices) {
		// if already immutable
		if (vertices instanceof ImmutableList<?> &&
			vertices.stream().allMatch(ImmutableGeometries::isImmutable))
		{
			return (ImmutableList<ImmutablePoint>) vertices;
		} else {
			Builder<ImmutablePoint> builder = ImmutableList.builder();
			
			vertices.stream()
				.map(ImmutableGeometries::immutable)
				.forEach(builder::add);
			
			return builder.build();
		}
	}

	/**
	 * @return if the path is empty.
	 */
	public boolean isEmpty() {
		return vertices.isEmpty();
	}

	/**
	 * @return the amount of vertices.
	 */
	public int size() {
		return vertices.size();
	}

	/**
	 * Gets the vertex at the specified position of this path.
	 * 
	 * @param index
	 * @return the vertex.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 || index >= size())
	 */
	public ImmutablePoint get(int index) {
		return vertices.get(index);
	}
	
	/**
	 * @return the vertices
	 */
	public ImmutableList<ImmutablePoint> getVertices() {
		return vertices;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Path other = (Path) obj;
		if (vertices == null) {
			if (other.vertices != null)
				return false;
		} else if (!vertices.equals(other.vertices))
			return false;
		return true;
	}

	/**
	 * Calculates the trace of this path. The trace is a {@code LineString}
	 * containing all points visited by the path.
	 * 
	 * @return the line string.
	 */
	public ImmutableLineString trace() {
		if (trace == null) {
			List<ImmutablePoint> points = new LinkedList<>(getVertices());
			Iterator<ImmutablePoint> it = points.iterator();
	
			// removes points which are identical to their predecessor
			Point last = null;
			while (it.hasNext()) {
				Point p = it.next();
	
				if (last != null && p.equals(last))
					it.remove();
	
				last = p;
			}
	
			// construct LineString
			
			if (points.size() == 1) {
				ImmutablePoint point = points.get(0);
				
				trace = immutableLineString(sequence(point, point));
			} else {
				trace = immutableLineString(sequence(points));
			}
	
		}
		
		return trace;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<ImmutablePoint> iterator() {
		return vertices.iterator();
	}

	/**
	 * Concatenates this path with the given one.
	 * 
	 * @param other
	 * @return the concatenated path.
	 */
	public Path concat(Path other) {
		ImmutableList<ImmutablePoint> lhsVertices = this.vertices;
		ImmutableList<ImmutablePoint> rhsVertices = other.vertices;
		
		Builder<ImmutablePoint> builder = ImmutableList.builder();
		
		ImmutableList<ImmutablePoint> vertices = builder
			.addAll(lhsVertices)
			.addAll(rhsVertices)
			.build();
		
		return create(vertices);
	}

	/**
	 * @return a {@code VertexIterator}
	 */
	public Iterator<? extends Vertex> vertexIterator() {
		return new VertexIterator();
	}
	
	/**
	 * The vertex of a {@code Path}. Stores additional information about the
	 * vertex in context to the path.
	 */
	public static class Vertex {
		
		/**
		 * The point of the vertex.
		 */
		private final ImmutablePoint point;
		
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
		 * @param point
		 * @param first
		 *            whether the vertex is the first one
		 * @param last
		 *            whether the vertex is the last one
		 */
		protected Vertex(ImmutablePoint point, boolean first, boolean last) {
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

		/**
		 * @return whether the vertex is the first one.
		 */
		public boolean isFirst() {
			return first;
		}

		/**
		 * @return last whether the vertex is the last one.
		 */
		public boolean isLast() {
			return last;
		}
		
	}
	
	/**
	 * Base vertex iterator to iterate over path vertices.
	 * 
	 * @param <V> the vertex type.
	 */
	protected abstract class AbstractVertexIterator<V extends Vertex> implements Iterator<V> {
		
		/**
		 * The point iterator.
		 */
		private final Iterator<ImmutablePoint> points = iterator();
		
		/**
		 * The last yielded vertex.
		 */
		private Vertex last = null;
		
		/**
		 * The point of the next vertex.
		 */
		private ImmutablePoint nextPoint = null;
		
		/**
		 * Return the next vertex represented by the given point.
		 * 
		 * @param point
		 * @return the next vertex.
		 */
		protected abstract V nextVertex(ImmutablePoint point);

		/**
		 * @return whether the current vertex is the first one.
		 */
		protected boolean isFirst() {
			return getLastVertex() == null;
		}
		
		/**
		 * @return whether the current vertex is the last one.
		 */
		protected boolean isLast() {
			return !hasNext();
		}
		
		/**
		 * @return the last yielded vertex.
		 */
		protected Vertex getLastVertex() {
			return last;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return points.hasNext();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public V next() {
			nextPoint = points.next();
			V vertex = nextVertex(nextPoint);
			last = vertex;
			
			return vertex;
		}
	}
	
	/**
	 * The {@code VertexIterator} of a {@code Path}.
	 */
	protected class VertexIterator extends AbstractVertexIterator<Vertex> {
		
		/*
		 * (non-Javadoc)
		 * @see world.Path.AbstractVertexIterator#nextVertex(jts.geom.immutable.ImmutablePoint)
		 */
		@Override
		protected Vertex nextVertex(ImmutablePoint point) {
			return new Vertex(point, isFirst(), isLast());
		}
		
	}

	/**
	 * @return a {@code SegmentIterator}.
	 */
	public Iterator<? extends Segment<? extends Vertex>> segmentIterator() {
		return new SegmentIterator();
	}
	
	/**
	 * The segment of a {@code Path}. Stores additional information about the
	 * segment in context to the path.
	 * 
	 * @param <V> the vertex type
	 */
	public static class Segment<V extends Vertex> {

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
		
		/**
		 * @return whether the segment is the first one.
		 */
		public boolean isFirst() {
			return start.isFirst();
		}

		/**
		 * @return whether the segment is the last one.
		 */
		public boolean isLast() {
			return finish.isLast();
		}

		/**
		 * @return the start vertex.
		 */
		public V getStartVertex() {
			return start;
		}

		/**
		 * @return the finish vertex.
		 */
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
	 * 
	 * Base segment iterator to iterate over path segments.
	 *
	 * @param <V>
	 *            the vertex type
	 * @param <S>
	 *            the segment type
	 */
	protected abstract class AbstractSegmentIterator<V extends Vertex, S extends Segment<V>> implements Iterator<S> {
		
		/**
		 * The vertex iterator.
		 */
		private final Iterator<V> vertexIterator = supplyVertexIterator();
		
		/**
		 * The last yielded vertex.
		 */
		private V lastVertex = null;
		
		/**
		 * Constructs a new {@code SegmentIterator}.
		 */
		protected AbstractSegmentIterator() {
			if (vertexIterator.hasNext())
				lastVertex = vertexIterator.next();
		}
		
		/**
		 * @return a vertex iterator.
		 */
		protected abstract Iterator<V> supplyVertexIterator();
		
		/**
		 * Provides the next segment.
		 * 
		 * @param start
		 *            start vertex
		 * @param finish
		 *            finish vertex
		 * @return the next segment.
		 */
		protected abstract S nextSegment(V start, V finish);
		
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return vertexIterator.hasNext();
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public S next() {
			V start = lastVertex;
			V finish = vertexIterator.next();
			
			S segment = nextSegment(start, finish);
			lastVertex = finish;
			
			return segment;
		}
		
	}
	
	/**
	 * The {@code SegmentIterator} of a {@code Path}.
	 */
	protected class SegmentIterator extends AbstractSegmentIterator<Vertex, Segment<Vertex>> {
		
		/*
		 * (non-Javadoc)
		 * @see world.Path.AbstractSegmentIterator#supplyVertexIterator()
		 */
		@Override
		protected Iterator<Vertex> supplyVertexIterator() {
			return new VertexIterator();
		}

		/*
		 * (non-Javadoc)
		 * @see world.Path.AbstractSegmentIterator#nextSegment(world.Path.Vertex, world.Path.Vertex)
		 */
		@Override
		protected Segment<Vertex> nextSegment(Vertex start, Vertex finish) {
			return new Segment<>(start, finish);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return vertices.toString();
	}

}
