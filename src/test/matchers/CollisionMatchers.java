package matchers;

import static java.util.Collections.*;

import java.util.Collection;

import org.hamcrest.Matcher;

import tasks.WorkerUnit;
import tasks.WorkerUnitReference;
import world.DynamicObstacle;
import world.StaticObstacle;


public final class CollisionMatchers {
	
	private CollisionMatchers() {}
	
	public static Matcher<WorkerUnit> workerCollidesWith(StaticObstacle obstacle) {
		return new WorkerUnitCollidesWithStaticObstacle(obstacle);
	}
	
	public static Matcher<WorkerUnit> workerCollidesWith(DynamicObstacle obstacle) {
		return WorkerUnitCollidesWithDynamicObstacles.workerCollidesWith(singleton(obstacle));
	}
	
	public static Matcher<WorkerUnit> workerCollidesWith(Collection<? extends DynamicObstacle> obstacles) {
		return WorkerUnitCollidesWithDynamicObstacles.workerCollidesWith(obstacles);
	}
	
	public static Matcher<WorkerUnit> workerCollidesWith(WorkerUnit worker) {
		return WorkerUnitCollidesWithWorkerUnit.workerCollidesWith(worker);
	}
	
	public static Matcher<WorkerUnitReference> workerCollidesWith(WorkerUnitReference worker) {
		return WorkerRefCollidesWithWorkerRef.workerCollidesWith(worker);
	}

	public static Matcher<DynamicObstacle> obstaclesCollideWith(DynamicObstacle obstacle) {
		return DynamicObstacleCollidesWithDynamicObstacles.obstacleCollidesWith(singleton(obstacle));
	}
	
	public static Matcher<DynamicObstacle> obstaclesCollideWith(Collection<? extends DynamicObstacle> obstacles) {
		return DynamicObstacleCollidesWithDynamicObstacles.obstacleCollidesWith(obstacles);
	}
	
}
