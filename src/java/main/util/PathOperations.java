package util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jts.geom.factories.EnhancedGeometryBuilder;
import world.util.SpatialPathSegmentIterable;
import world.util.SpatialPathSegmentIterable.SpatialPathSegment;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public final class PathOperations {

	public static double length(List<Point> path) {
		double acc = 0;

		SpatialPathSegmentIterable segments = new SpatialPathSegmentIterable(path);

		for (SpatialPathSegment s : segments)
			acc += s.getLength();

		return acc;
	}

	public static Geometry calcTrace(List<Point> spatialPath) {
		if (spatialPath.size() == 1) {
			return (Point) spatialPath.get(0).clone();
		} else {
			List<Point> points = new LinkedList<>(spatialPath);
			Iterator<Point> it = points.iterator();

			Point last = null;
			while (it.hasNext()) {
				Point p = it.next();

				if (last != null && p.equals(last))
					it.remove();

				last = p;
			}

			EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

			return geomBuilder.lineString(points);
		}
	}

}
