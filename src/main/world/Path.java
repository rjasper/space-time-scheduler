package world;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import jts.geom.factories.EnhancedGeometryBuilder;
import jts.geom.immutable.ImmutableGeometries;
import jts.geom.immutable.ImmutablePoint;
import jts.geom.util.GeometriesRequire;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.UnmodifiableIterator;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

// TODO document
public class Path implements Iterable<ImmutablePoint> {
	
	private final ImmutableList<ImmutablePoint> vertices;
	
	private transient LineString trace = null;
	
	public Path() {
		this.vertices = ImmutableList.of();
	}
	
	public Path(List<? extends Point> vertices) {
		checkVertices(vertices);
		
		this.vertices = makeImmutable(vertices);
	}
	
	public Path(ImmutableList<ImmutablePoint> vertices) {
		checkVertices(vertices);
		
		this.vertices = vertices;
	}

	protected Path create(ImmutableList<ImmutablePoint> vertices) {
		return new Path(vertices);
	}
	
	protected void checkVertices(List<? extends Point> vertices) {
		Objects.requireNonNull(vertices, "vertices");
		
		if (vertices.size() == 1)
			throw new IllegalArgumentException("invalid size");
		
		vertices.forEach(p ->
			GeometriesRequire.requireValid2DPoint((Point) p, "vertices"));
	}
	
	@SuppressWarnings("unchecked")
	private ImmutableList<ImmutablePoint> makeImmutable(List<? extends Point> vertices) {
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

	public boolean isEmpty() {
		return vertices.isEmpty();
	}

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
	
	public ImmutableList<ImmutablePoint> getVertices() {
		return vertices;
	}

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

	public LineString trace() {
		if (trace == null) {
			List<Point> points = new LinkedList<>(getVertices());
			Iterator<Point> it = points.iterator();
	
			// removes points which are identical to their predecessor
			Point last = null;
			while (it.hasNext()) {
				Point p = it.next();
	
				if (last != null && p.equals(last))
					it.remove();
	
				last = p;
			}
	
			// construct LineString
	
			EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
	
			trace = geomBuilder.lineString(points);
		}
		
		return trace;
	}

	@Override
	public UnmodifiableIterator<ImmutablePoint> iterator() {
		return vertices.iterator();
	}

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

	public Iterator<? extends Vertex> vertexIterator() {
		return new VertexIterator();
	}
	
	public static class Vertex {
		
		private final ImmutablePoint point;
		
		private final boolean first;
		
		private final boolean last;
		
		protected Vertex(ImmutablePoint point, boolean first, boolean last) {
			this.point = point;
			this.first = first;
			this.last = last;
		}

		public ImmutablePoint getPoint() {
			return point;
		}

		public double getX() {
			return point.getX();
		}

		public double getY() {
			return point.getY();
		}

		public boolean isFirst() {
			return first;
		}

		public boolean isLast() {
			return last;
		}
		
	}
	
	protected abstract class AbstractVertexIterator<V extends Vertex> implements Iterator<V> {
		
		private final Iterator<ImmutablePoint> points = iterator();
		
		private Vertex last = null;
		
		private ImmutablePoint point = null;
		
		protected abstract V nextVertex(ImmutablePoint point);
		
		protected boolean isFirst() {
			return last() == null;
		}
		
		protected boolean isLast() {
			return !hasNext();
		}
		
		@Override
		public boolean hasNext() {
			return points.hasNext();
		}

		@Override
		public V next() {
			point = points.next();
			V vertex = nextVertex(point);
			last = vertex;
			
			return vertex;
		}
		
		public Vertex last() {
			return last;
		}
	}
	
	protected class VertexIterator extends AbstractVertexIterator<Vertex> {
		
		@Override
		protected Vertex nextVertex(ImmutablePoint point) {
			return new Vertex(point, isFirst(), isLast());
		}
		
	}

	public Iterator<? extends Segment<? extends Vertex>> segmentIterator() {
		return new SegmentIterator();
	}
	
	public static class Segment<V extends Vertex> {
		
		private final V start;
		private final V finish;
		
		protected Segment(V start, V finish) {
			this.start = start;
			this.finish = finish;
		}
		
		public boolean isFirst() {
			return start.isFirst();
		}
		
		public boolean isLast() {
			return finish.isLast();
		}

		public V getStartVertex() {
			return start;
		}

		public V getFinishVertex() {
			return finish;
		}
		
		public ImmutablePoint getStartPoint() {
			return start.getPoint();
		}
		
		public ImmutablePoint getFinishPoint() {
			return finish.getPoint();
		}
		
	}
	
	protected abstract class AbstractSegmentIterator<V extends Vertex, S extends Segment<V>> implements Iterator<S> {
		
		private final Iterator<V> vertexIterator = supplyVertexIterator();
		
		private V lastVertex = null;
		
		protected AbstractSegmentIterator() {
			if (vertexIterator.hasNext())
				lastVertex = vertexIterator.next();
		}
		
		protected abstract Iterator<V> supplyVertexIterator();
		
		protected abstract S nextSegment(V start, V finish);
		
		@Override
		public boolean hasNext() {
			return vertexIterator.hasNext();
		}

		@Override
		public S next() {
			V start = lastVertex;
			V finish = vertexIterator.next();
			
			S segment = nextSegment(start, finish);
			lastVertex = finish;
			
			return segment;
		}
		
	}
	
	protected class SegmentIterator extends AbstractSegmentIterator<Vertex, Segment<Vertex>> {
		@Override
		protected Iterator<Vertex> supplyVertexIterator() {
			return new VertexIterator();
		}

		@Override
		protected Segment<Vertex> nextSegment(Vertex start, Vertex finish) {
			return new Segment<>(start, finish);
		}
	}
	
	@Override
	public String toString() {
		return vertices.toString();
	}

}
