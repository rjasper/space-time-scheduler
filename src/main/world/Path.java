package world;

import static java.util.Spliterator.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static jts.geom.util.GeometrySequencer.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jts.geom.immutable.ImmutableLineString;
import jts.geom.immutable.ImmutablePoint;
import jts.geom.util.GeometriesRequire;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.UnmodifiableIterator;
import com.vividsolutions.jts.geom.Point;

/**
 * A {@code Path} is an immutable list of immutable {@link Point}s. It ensures
 * validity of the path. All points have to be valid 2-dimensional points.
 * Singular paths of only one vertex are not allowed while empty paths are.
 * 
 * @author Rico
 */
public class Path implements Iterable<ImmutablePoint> {
	
	/**
	 * An empty {@code Path}.
	 */
	private static final Path EMPTY = new Path(ImmutableList.of());
	
	/**
	 * @return an empty {@code SpatialPath}.
	 */
	public static Path empty() {
		return EMPTY;
	}
	
	/**
	 * The vertices of the path.
	 */
	private final ImmutableList<ImmutablePoint> points;
	
	/**
	 * Caches the trace of the path.
	 */
	private transient ImmutableLineString trace = null;
	
	/**
	 * Constructs a path of the given vertices.
	 * 
	 * @param points
	 * @throws NullPointerException
	 *             if {@code points} are {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code points} contain invalid points.
	 * 
	 * @param vertices
	 */
	public Path(ImmutableList<ImmutablePoint> points) {
		checkVertices(points);
		
		this.points = points;
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
	 * @return an empty path.
	 */
	protected Path getEmpty() {
		return empty();
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
	 * @return if the path is empty.
	 */
	public boolean isEmpty() {
		return points.isEmpty();
	}

	/**
	 * @return the amount of vertices.
	 */
	public int size() {
		return points.size();
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
		return points.get(index);
	}
	
	/**
	 * @return the points
	 */
	public ImmutableList<ImmutablePoint> getPoints() {
		return points;
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
		if (points == null) {
			if (other.points != null)
				return false;
		} else if (!points.equals(other.points))
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
			List<ImmutablePoint> points = new LinkedList<>(getPoints());
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
		return points.iterator();
	}

	/**
	 * Concatenates this path with the given one.
	 * 
	 * @param other
	 * @return the concatenated path.
	 */
	public Path concat(Path other) {
		ImmutableList<ImmutablePoint> lhsVertices = this.points;
		ImmutableList<ImmutablePoint> rhsVertices = other.points;
		
		Builder<ImmutablePoint> builder = ImmutableList.builder();
		
		ImmutableList<ImmutablePoint> vertices = builder
			.addAll(lhsVertices)
			.addAll(rhsVertices)
			.build();
		
		return create(vertices);
	}
	
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
	public Path subPath(
		int startIndexInclusive,
		double startAlpha,
		int finishIndexExclusive,
		double finishAlpha)
	{
		int n = size();
		int finishIndexInclusive = finishIndexExclusive - 1;
		if (startIndexInclusive < 0 || startIndexInclusive >= n)
			throw new IllegalArgumentException("startIndex is out of bounds");
		if (finishIndexInclusive < 0 || finishIndexInclusive >= n)
			throw new IllegalArgumentException("finishIndex is out of bounds");
		if (startAlpha < 0.0 || startAlpha >= 1.0)
			throw new IllegalArgumentException("startAlpha is out of bounds");
		if (finishAlpha < 0.0 || finishAlpha >= 1.0 || (finishIndexInclusive == n-1 && finishAlpha != 0.0))
			throw new IllegalArgumentException("finishAlpha is out of bounds");
		
		// if interval is empty
		if (startIndexInclusive > finishIndexInclusive || (startIndexInclusive == finishIndexInclusive && startAlpha > finishAlpha))
			return getEmpty();
		
		// if is identical
		if (startIndexInclusive == 0 && startAlpha == 0.0 && finishIndexInclusive == n-1 && finishAlpha == 0.0)
			return this;
		
		ImmutableList.Builder<ImmutablePoint> builder = ImmutableList.builder();

		builder.add(interpolate(startIndexInclusive, startAlpha));
			
		for (int i = startIndexInclusive+1; i <= finishIndexInclusive-1; ++i)
			builder.add(get(i));
		
		builder.add(interpolate(finishIndexInclusive, finishAlpha));
		
		return null;
	}
	
	/**
	 * <p>
	 * Interpolates a point on segment using this formular:
	 * </p>
	 * 
	 * <i>point(i, alpha) = (x<sub>i</sub> +
	 * alpha*x<sub>i</sub>/(x<sub>i+1</sub> - x<sub>i</sub>), y<sub>i</sub> +
	 * alpha*<sub>i</sub>/(y<sub>i+1</sub> - y<sub>i</sub>))</i>
	 * 
	 * @param index the index of the segment
	 * @param alpha
	 * @return the interpolated point.
	 */
	private ImmutablePoint interpolate(int index, double alpha) {
		if (alpha == 0.0)
			return get(index);
		
		ImmutablePoint p1 = get(index);
		ImmutablePoint p2 = get(index+1);
		
		double x1 = p1.getX(), y1 = p1.getY(), x2 = p2.getX(), y2 = p2.getY();
		double dx = x2 - x1, dy = y2 - y1;
		
		return immutablePoint(x1 + alpha*dx, x2 + alpha*dy);
	}

	/**
	 * @return a {@code VertexIterator}
	 */
	public Iterator<? extends Vertex> vertexIterator() {
		return new VertexIterator();
	}
	
	/**
	 * @return a {@code Spliterator} over all vertices.
	 */
	public Spliterator<? extends Vertex> vertexSpliterator() {
		return Spliterators.spliterator(vertexIterator(), size(), NONNULL | SIZED | IMMUTABLE | ORDERED);
	}

	/**
	 * @return a {@code Stream} over all vertices.
	 */
	public Stream<? extends Vertex> vertexStream() {
		return StreamSupport.stream(vertexSpliterator(), false);
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
	 * @return a {@code Spliterator} over all segments.
	 */
	public Spliterator<? extends Segment<? extends Vertex>> segmentSpliterator() {
		return Spliterators.spliterator(segmentIterator(), size(), NONNULL | SIZED | IMMUTABLE | ORDERED);
	}

	/**
	 * @return a {@code Stream} over all segments.
	 */
	public Stream<? extends Segment<? extends Vertex>> segmentStream() {
		return StreamSupport.stream(segmentSpliterator(), false);
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
		return points.toString();
	}

}
