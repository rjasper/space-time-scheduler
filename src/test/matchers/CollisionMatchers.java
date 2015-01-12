package matchers;

import org.hamcrest.Matcher;

import tasks.WorkerUnit;
import world.DynamicObstacle;

import com.vividsolutions.jts.geom.Polygon;


public final class CollisionMatchers {
	
	private CollisionMatchers() {}
	
	public static Matcher<WorkerUnit> collideWith(Polygon obstacle) {
		return new WorkerUnitCollidesWithStaticObstacle(obstacle);
	}
	
	public static Matcher<WorkerUnit> collideWith(DynamicObstacle obstacle) {
		return new WorkerUnitCollidesWithDynamicObstacle(obstacle);
	}

}
