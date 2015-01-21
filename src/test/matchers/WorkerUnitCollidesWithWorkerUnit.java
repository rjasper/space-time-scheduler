package matchers;

import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import tasks.WorkerUnit;

public class WorkerUnitCollidesWithWorkerUnit extends WorkerUnitCollidesWithDynamicObstacles {
	
	@Factory
	public static Matcher<WorkerUnit> workerCollidesWith(WorkerUnit worker) {
		return new WorkerUnitCollidesWithWorkerUnit(worker);
	}
	
	private final WorkerUnit worker;
	
	public WorkerUnitCollidesWithWorkerUnit(WorkerUnit worker) {
		super(worker.getObstacleSegments());
		
		this.worker = Objects.requireNonNull(worker, "worker");
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker colliding with ")
			.appendValue(worker);
	}

	@Override
	protected void describeMismatchSafely(WorkerUnit item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" is not colliding with ")
			.appendValue(worker);
	}
	
}
