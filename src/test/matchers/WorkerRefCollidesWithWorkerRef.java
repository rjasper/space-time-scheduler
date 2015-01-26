package matchers;

import static matchers.CollisionMatchers.*;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import tasks.WorkerUnitReference;
import world.DynamicObstacle;

public class WorkerRefCollidesWithWorkerRef
	extends MapMatcher<WorkerUnitReference, DynamicObstacle>
{
	@Factory
	public static Matcher<WorkerUnitReference> workerCollidesWith(WorkerUnitReference worker) {
		return new WorkerRefCollidesWithWorkerRef(worker);
	}
	
	private final WorkerUnitReference workerRef;

	public WorkerRefCollidesWithWorkerRef(WorkerUnitReference workerRef) {
		super(
			obstaclesCollideWith(makeObstacle(workerRef)),
			WorkerRefCollidesWithWorkerRef::makeObstacle);
		
		this.workerRef = workerRef;
	}
	
	private static DynamicObstacle makeObstacle(WorkerUnitReference workerRef) {
		return new DynamicObstacle(workerRef.getShape(), workerRef.calcTrajectory());
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker colliding with ")
			.appendValue(workerRef);
	}
	
}
