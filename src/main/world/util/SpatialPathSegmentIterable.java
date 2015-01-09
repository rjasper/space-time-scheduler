package world.util;

import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

// TODO remove iterables, iterator only

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
		private final boolean first;
		private final boolean last;

		public SpatialPathSegment(
			Point startPoint,
			Point finishPoint,
			double startArc,
			double length,
			boolean first,
			boolean last)
		{
			this.startPoint = startPoint;
			this.finishPoint = finishPoint;
			this.startArc = startArc;
			this.length = length;
			this.first = first;
			this.last = last;
		}

		public boolean isFirst() {
			return first;
		}

		public boolean isLast() {
			return last;
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

		private boolean first = true;
		private Point lastPosition;
		private double accLength = 0.0;

		public SpatialPathSegmentIterator(List<Point> spatialPath) {
			this.iterator = spatialPath.iterator(); // throws NPE

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
			boolean last = !hasNext();

			accLength += length;

			SpatialPathSegment segment =
				new SpatialPathSegment(startPoint, finishPoint, startArc, length, first, last);

			first = false;

			return segment;
		}

	}

}