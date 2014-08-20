package world;

import static jts.geom.PolygonFixtures.*;
import static jts.geom.LineStringFixtures.*;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public final class DynamicObstacleFixtures {
	
	public static DynamicObstacle movingTriangle() {
		Polygon polygon = triangle();
		LineString path = threePoints();
		
		return new DynamicObstacle(polygon, path);
	}
	
	public static DynamicObstacle movingSquare() {
		Polygon polygon = square();
		LineString path = twoPoints();
		
		System.out.println(path.getCoordinateSequence().getDimension());
		
		return new DynamicObstacle(polygon, path);
	}

}
