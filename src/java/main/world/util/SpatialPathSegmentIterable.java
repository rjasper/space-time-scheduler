package world.util;

import java.util.Iterator;


import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class SpatialPathSegmentIterable implements Iterable<SpatialPathSegmentIterable.SpatialPathSegment> {
	
	private LineString spatialPath;
	
	public SpatialPathSegmentIterable(LineString spatialPath) {
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
	
		private final LineString spatialPath;
	
		private Point lastPosition;
		private double accLength = 0.0;
	
		private int i = 0;
	
		public SpatialPathSegmentIterator(LineString spatialPath) {
			this.spatialPath = spatialPath;
			
			if (hasNext())
				init();
		}
		
		private void init() {
			lastPosition = nextPoint();
		}
	
		private Point nextPoint() {
			return lastPosition = spatialPath.getPointN(i++);
		}
	
		@Override
		public boolean hasNext() {
			return i < spatialPath.getNumPoints();
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