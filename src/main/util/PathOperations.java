package util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jts.geom.factories.EnhancedGeometryBuilder;
import world.util.SpatialPathSegmentIterable;
import world.util.SpatialPathSegmentIterable.SpatialPathSegment;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * {@code PathOperations} provides static utility methods for paths. A path is
 * a {@link List} of {@link Point}s.
 *
 * @author Rico Jasper
 */
public final class PathOperations {

	private PathOperations() {}

	/**
	 * Calculates the euclidean length of the path.
	 *
	 * @param path
	 * @return the length.
	 */
	public static double length(List<Point> path) {
		double acc = 0;

		SpatialPathSegmentIterable segments = new SpatialPathSegmentIterable(path);

		for (SpatialPathSegment s : segments)
			acc += s.getLength();

		return acc;
	}

	/**
	 * Calculates the trace of a path. The trace is a geometry which only
	 * includes all points of the path.
	 *
	 * @param path
	 * @return the trace.
	 */
	public static Geometry calcTrace(List<Point> path) {
		// if the size is 1 then the trace is only a point
		if (path.size() == 1) {
			return (Point) path.get(0).clone();
		// otherwise it is a LineString (empty if the size was zero)
		} else {
			List<Point> points = new LinkedList<>(path);
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

			return geomBuilder.lineString(points);
		}
	}

}
