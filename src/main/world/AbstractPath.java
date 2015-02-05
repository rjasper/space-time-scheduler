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
public abstract class AbstractPath<
	V extends Path.Vertex,
	S extends Path.Segment<? extends V>>
implements Path<V, S>, Iterable<ImmutablePoint>
{
	
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
	public AbstractPath(ImmutableList<ImmutablePoint> points) {
		checkVertices(points);
		
		this.points = points;
	}

	/**
	 * Creates a new path containing the given vertices.
	 * 
	 * @param vertices
	 * @return the new path.
	 */
	protected abstract Path<V, S> create(ImmutableList<ImmutablePoint> vertices);
	
	/**
	 * @return an empty path.
	 */
	protected abstract Path<V, S> getEmpty();
	
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

	/* (non-Javadoc)
	 * @see world.Path#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return points.isEmpty();
	}

	/* (non-Javadoc)
	 * @see world.Path#size()
	 */
	@Override
	public int size() {
		return points.size();
	}

	/* (non-Javadoc)
	 * @see world.Path#get(int)
	 */
	@Override
	public ImmutablePoint get(int index) {
		return points.get(index);
	}

	/* (non-Javadoc)
	 * @see world.Path#getFirst()
	 */
	@Override
	public ImmutablePoint getFirst() {
		if (isEmpty())
			return null;
		else
			return points.get(0);
	}

	/* (non-Javadoc)
	 * @see world.Path#getLast()
	 */
	@Override
	public ImmutablePoint getLast() {
		if (isEmpty())
			return null;
		else
			return points.get(size()-1);
	}
	
	/* (non-Javadoc)
	 * @see world.Path#getPoints()
	 */
	@Override
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
		AbstractPath<?, ?> other = (AbstractPath<?, ?>) obj;
		if (points == null) {
			if (other.points != null)
				return false;
		} else if (!points.equals(other.points))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see world.Path#trace()
	 */
	@Override
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
	/* (non-Javadoc)
	 * @see world.Path#iterator()
	 */
	@Override
	public UnmodifiableIterator<ImmutablePoint> iterator() {
		return points.iterator();
	}

	/* (non-Javadoc)
	 * @see world.Path#concat(world.AbstractPath)
	 */
	@Override
	public Path<V, S> concat(Path<?, ?> other) {
		ImmutableList<ImmutablePoint> lhsVertices = this.getPoints();
		ImmutableList<ImmutablePoint> rhsVertices = other.getPoints();
		
		Builder<ImmutablePoint> builder = ImmutableList.builder();
		
		ImmutableList<ImmutablePoint> vertices = builder
			.addAll(lhsVertices)
			.addAll(rhsVertices)
			.build();
		
		return create(vertices);
	}
	
	/* (non-Javadoc)
	 * @see world.Path#subPath(int, double, int, double)
	 */
	@Override
	public Path<V, S> subPath(
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

		builder.add(interpolateImpl(startIndexInclusive, startAlpha));
			
		for (int i = startIndexInclusive+1; i <= finishIndexInclusive; ++i)
			builder.add(get(i));
		
		// point was already added unless finishAlpha > 0.0
		if (finishAlpha > 0.0)
			builder.add(interpolateImpl(finishIndexInclusive, finishAlpha));
		
		return create(builder.build());
	}

	/* (non-Javadoc)
	 * @see world.Path#interpolate(int, double)
	 */
	@Override
	public ImmutablePoint interpolate(int index, double alpha) {
		int n = size();
		if (index < 0 || index >= n-1)
			throw new IllegalArgumentException("index is out of bounds");
		if (alpha < 0.0 || alpha >= 1.0)
			throw new IllegalArgumentException("alpha is out of bounds");
		if (index == n-1 && alpha != 0.0)
			throw new IllegalArgumentException("alpha must be 0.0 if index is size()-1");
		
		return interpolateImpl(index, alpha);
	}
	
	/**
	 * Actual implementation of {@link #interpolate(int, double)}. Doesn't check
	 * arguments.
	 * 
	 * @param index
	 *            the index of the segment
	 * @param alpha
	 * @return the interpolated point.
	 */
	private ImmutablePoint interpolateImpl(int index, double alpha) {
		if (alpha == 0.0)
			return get(index);
		
		ImmutablePoint p1 = get(index);
		ImmutablePoint p2 = get(index+1);
		
		double x1 = p1.getX(), y1 = p1.getY(), x2 = p2.getX(), y2 = p2.getY();
		double dx = x2 - x1, dy = y2 - y1;
		
		return immutablePoint(x1 + alpha*dx, y1 + alpha*dy);
	}

	/* (non-Javadoc)
	 * @see world.Path#vertexIterator()
	 */
	@Override
	public abstract Iterator<V> vertexIterator();
	
	/* (non-Javadoc)
	 * @see world.Path#vertexSpliterator()
	 */
	@Override
	public Spliterator<V> vertexSpliterator() {
		return Spliterators.spliterator(vertexIterator(), size(), NONNULL | SIZED | IMMUTABLE | ORDERED);
	}

	/* (non-Javadoc)
	 * @see world.Path#vertexStream()
	 */
	@Override
	public Stream<V> vertexStream() {
		return StreamSupport.stream(vertexSpliterator(), false);
	}
	
	/**
	 * Base vertex iterator to iterate over path vertices.
	 * 
	 * @param <V> the vertex type.
	 */
	protected abstract class AbstractVertexIterator implements Iterator<V> {
		
		/**
		 * The point iterator.
		 */
		private final Iterator<ImmutablePoint> points = iterator();
		
		/**
		 * The last yielded vertex.
		 */
		private V last = null;
		
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
		protected abstract V createNextVertex(ImmutablePoint point);
		
		/**
		 * @return the current vertex' index.
		 */
		protected int getIndex() {
			if (last == null)
				return 0;
			else
				return last.getIndex() + 1;
		}

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
		protected V getLastVertex() {
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
			V vertex = createNextVertex(nextPoint);
			last = vertex;
			
			return vertex;
		}
	}
	
	/* (non-Javadoc)
	 * @see world.Path#segmentIterator()
	 */
	@Override
	public abstract Iterator<S> segmentIterator();
	
	/* (non-Javadoc)
	 * @see world.Path#segmentSpliterator()
	 */
	@Override
	public Spliterator<S> segmentSpliterator() {
		return Spliterators.spliterator(segmentIterator(), size(), NONNULL | SIZED | IMMUTABLE | ORDERED);
	}

	/* (non-Javadoc)
	 * @see world.Path#segmentStream()
	 */
	@Override
	public Stream<S> segmentStream() {
		return StreamSupport.stream(segmentSpliterator(), false);
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
	protected abstract class AbstractSegmentIterator implements Iterator<S> {
		
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
		protected abstract S createNextSegment(V start, V finish);
		
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
			
			S segment = createNextSegment(start, finish);
			lastVertex = finish;
			
			return segment;
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
