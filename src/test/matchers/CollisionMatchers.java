package matchers;

import org.hamcrest.Matcher;

import com.vividsolutions.jts.geom.Polygon;

import tasks.WorkerUnit;
import world.DynamicObstacle;


public final class CollisionMatchers {
	
	private CollisionMatchers() {}
	
	public static Matcher<WorkerUnit> collideWith(Polygon obstacle) {
		return new WorkerUnitCollidesWithStaticObstacle(obstacle);
	}
	
	public static Matcher<WorkerUnit> collideWith(DynamicObstacle obstacle) {
		return new WorkerUnitCollidesWithDynamicObstacle(obstacle);
	}

}
