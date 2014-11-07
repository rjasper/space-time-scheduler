package world.util;

import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class SpatialPathSegmentIterable implements Iterable<SpatialPathSegmentIterable.SpatialPathSegment> {
	
	private List<Point> spatialPath;
	
	public SpatialPathSegmentIterable(List<Point> spatialPath) {
		this.spatialPath = spatialPath;
	}

	@Override
	public Iterator<SpatialPathSegment> iterator() {
		return new SpatialPathSegmentIterator(spatialPath);
	}

	public static class SpatialPathSegment {
		
		private final Point startPoint;
		private final Point finishPoint;
		private final double startArc;
		private final double length;
		
		public SpatialPathSegment(Point startPoint, Point finishPoint, double startArc, double length) {
			this.startPoint = startPoint;
			this.finishPoint = finishPoint;
			this.startArc = startArc;
			this.length = length;
		}
		
		public Point getStartPoint() {
			return startPoint;
		}
		
		public Point getFinishPoint() {
			return finishPoint;
		}

		public double getStartArc() {
			return startArc;
		}

		public double getLength() {
			return length;
		}
		
	}

	public static class SpatialPathSegmentIterator implements Iterator<SpatialPathSegment> {
	
		private final Iterator<Point> iterator;
	
		private Point lastPosition;
		private double accLength = 0.0;
	
		public SpatialPathSegmentIterator(List<Point> spatialPath) {
			this.iterator = spatialPath.iterator();
			
			if (hasNext())
				init();
		}
		
		private void init() {
			lastPosition = nextPoint();
		}
	
		private Point nextPoint() {
			return lastPosition = iterator.next();
		}
	
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}
	
		@Override
		public SpatialPathSegment next() {
			Point startPoint = lastPosition;
			Point finishPoint = nextPoint();
			
			double startArc = accLength;
			double length = DistanceOp.distance(startPoint, finishPoint);
			
			accLength += length;
			
			return new SpatialPathSegment(startPoint, finishPoint, startArc, length);
		}
		
	}
	
}