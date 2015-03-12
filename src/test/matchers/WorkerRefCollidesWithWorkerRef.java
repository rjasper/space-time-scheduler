package matchers;

import static matchers.CollisionMatchers.*;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import scheduler.NodeReference;
import world.DynamicObstacle;

public class WorkerRefCollidesWithWorkerRef
	extends MapMatcher<NodeReference, DynamicObstacle>
{
	@Factory
	public static Matcher<NodeReference> workerCollidesWith(NodeReference worker) {
		return new WorkerRefCollidesWithWorkerRef(worker);
	}
	
	private final NodeReference workerRef;

	public WorkerRefCollidesWithWorkerRef(NodeReference workerRef) {
		super(
			obstaclesCollideWith(makeObstacle(workerRef)),
			WorkerRefCollidesWithWorkerRef::makeObstacle);
		
		this.workerRef = workerRef;
	}
	
	private static DynamicObstacle makeObstacle(NodeReference workerRef) {
		return new DynamicObstacle(workerRef.getShape(), workerRef.calcTrajectory());
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker colliding with ")
			.appendValue(workerRef);
	}
	
}
