package world;

import java.util.Iterator;
import java.util.List;

import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

// TODO document
public class SpatialPath extends Path {
	
	private transient double length = Double.NaN;
	
	public SpatialPath() {}

	public SpatialPath(List<Point> vertices) {
		super(vertices);
	}
	
	public SpatialPath(ImmutableList<ImmutablePoint> vertices) {
		super(vertices);
	}

	@Override
	protected Path create(ImmutableList<ImmutablePoint> vertices) {
		return new SpatialPath(vertices);
	}

	@Override
	public SpatialPath concat(Path other) {
		if (!(other instanceof SpatialPath))
			throw new IllegalArgumentException("incompatible path");
		
		return (SpatialPath) super.concat(other);
	}
	
	public double length() {
		if (Double.isNaN(length)) {
			Iterator<? extends Vertex> it = vertexIterator();
			Vertex last = null;
			while (it.hasNext())
				last = it.next();
			
			length = last == null ? 0.0 : last.getArc();
		}
		
		return length;
	}
	
	@Override
	public Iterator<Vertex> vertexIterator() {
		return new VertexIterator();
	}
	
	public static class Vertex extends Path.Vertex {
		
		private final double arc;
		
		private Vertex(ImmutablePoint point, double arc, boolean first, boolean last) {
			super(point, first, last);
			
			this.arc = arc;
		}

		public double getArc() {
			return arc;
		}
	}
	
	private class VertexIterator extends AbstractVertexIterator<Vertex> {
		
		private double arc = 0.0;
		
		@Override
		protected Vertex nextVertex(ImmutablePoint point) {
			if (!isFirst())
				arc += DistanceOp.distance(last().getPoint(), point);
			
			return new Vertex(point, arc, isFirst(), isLast());
		}
		
	}

	@Override
	public Iterator<Segment> segmentIterator() {
		return new SegmentIterator();
	}
	
	public static class Segment extends Path.Segment<Vertex> {
		
		private Segment(Vertex start, Vertex finish) {
			super(start, finish);
		}
		
		public double length() {
			return getFinishVertex().getArc() - getStartVertex().getArc();
		}
	}
	
	private class SegmentIterator extends AbstractSegmentIterator<Vertex, Segment> {
		@Override
		protected Iterator<Vertex> supplyVertexIterator() {
			return new VertexIterator();
		}

		@Override
		protected Segment nextSegment(Vertex start, Vertex finish) {
			return new Segment(start, finish);
		}
	}

}
