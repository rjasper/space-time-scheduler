package world;

import static java.time.Month.JANUARY;
import static jts.geom.LineStringFixtures.threePoints;
import static jts.geom.LineStringFixtures.twoPoints;
import static jts.geom.PolygonFixtures.square;
import static jts.geom.PolygonFixtures.triangle;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public final class DynamicObstacleFixtures {
	
	public static DynamicObstacle movingTriangle() {
		Polygon polygon = triangle();
		List<Point> path = threePoints();
		List<LocalDateTime> times = Arrays.asList(
			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 0),
			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 5),
			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 10)
		);
		
		return new DynamicObstacle(polygon, path, times);
	}
	
	public static DynamicObstacle movingSquare() {
		Polygon polygon = square();
		List<Point> path = twoPoints();
		List<LocalDateTime> times = Arrays.asList(
			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 2),
			LocalDateTime.of(2000, JANUARY, 1, 0, 0, 15)
		);
		
		return new DynamicObstacle(polygon, path, times);
	}

}
