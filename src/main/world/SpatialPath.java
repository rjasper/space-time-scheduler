package world;

import java.util.Iterator;
import java.util.List;

import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

// TODO document
public class SpatialPath extends Path {

	public SpatialPath(ImmutableList<ImmutablePoint> vertices) {
		super(vertices);
	}

	public SpatialPath(List<Point> vertices) {
		super(vertices);
	}
	
	public Iterator<Vertex> spatialVertexIterator() {
		return new VertexIterator();
	}
	
	public static class Vertex {
		
		private final ImmutablePoint point;
		
		private final double arc;
		
		private final boolean first;
		
		private final boolean last;
		
		private Vertex(ImmutablePoint point, double arc, boolean first, boolean last) {
			this.point = point;
			this.arc = arc;
			this.first = first;
			this.last = last;
		}

		public ImmutablePoint getPoint() {
			return point;
		}

		public double getArc() {
			return arc;
		}

		public boolean isFirst() {
			return first;
		}

		public boolean isLast() {
			return last;
		}
		
	}
	
	private class VertexIterator implements Iterator<Vertex> {
		
		private final Iterator<ImmutablePoint> points = SpatialPath.this.iterator();
		
		private double arc = 0.0;
		
		private ImmutablePoint last = null;
		
		@Override
		public boolean hasNext() {
			return points.hasNext();
		}

		@Override
		public Vertex next() {
			ImmutablePoint point = points.next();
			boolean isFirst = last == null;
			boolean isLast = !points.hasNext();
			
			if (!isFirst)
				arc += DistanceOp.distance(last, point);
			
			Vertex vertex = new Vertex(point, arc, isFirst, isLast);
			
			last = point;
			
			return vertex;
		}
		
	}

}
