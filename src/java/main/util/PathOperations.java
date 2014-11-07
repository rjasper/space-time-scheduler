package util;

import java.util.List;

import world.util.SpatialPathSegmentIterable;
import world.util.SpatialPathSegmentIterable.SpatialPathSegment;

import com.vividsolutions.jts.geom.Point;

public final class PathOperations {
	
	public static double length(List<Point> path) {
		double acc = 0;
		
		SpatialPathSegmentIterable segments = new SpatialPathSegmentIterable(path);
		
		for (SpatialPathSegment s : segments)
			acc += s.getLength();
		
		return acc;
	}

}
