package world;

import static jts.geom.PolygonFixtures.*;
import static jts.geom.LineStringFixtures.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.time.Month.*;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public final class DynamicObstacleFixtures {
	
	public static DynamicObstacle movingTriangle() {
		Polygon polygon = triangle();
		LineString path = threePoints();
		List<LocalDateTime> times = Arrays.asList(
			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 0),
			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 5),
			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 10)
		);
		
		return new DynamicObstacle(polygon, path, times);
	}
	
	public static DynamicObstacle movingSquare() {
		Polygon polygon = square();
		LineString path = twoPoints();
		List<LocalDateTime> times = Arrays.asList(
			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 2),
			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 15)
		);
		
		System.out.println(path.getCoordinateSequence().getDimension());
		
		return new DynamicObstacle(polygon, path, times);
	}

}
