package matchers;

import org.hamcrest.Matcher;

import tasks.WorkerUnit;
import world.DynamicObstacle;
import world.StaticObstacle;


public final class CollisionMatchers {
	
	private CollisionMatchers() {}
	
	public static Matcher<WorkerUnit> collideWith(StaticObstacle obstacle) {
		return new WorkerUnitCollidesWithStaticObstacle(obstacle);
	}
	
	public static Matcher<WorkerUnit> collideWith(DynamicObstacle obstacle) {
		return new WorkerUnitCollidesWithDynamicObstacle(obstacle);
	}

}
