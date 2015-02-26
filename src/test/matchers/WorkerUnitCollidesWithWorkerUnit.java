package matchers;

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import scheduler.WorkerUnit;
import world.DynamicObstacle;

public class WorkerUnitCollidesWithWorkerUnit extends WorkerUnitCollidesWithDynamicObstacles {
	
	@Factory
	public static Matcher<WorkerUnit> workerCollidesWith(WorkerUnit worker) {
		return new WorkerUnitCollidesWithWorkerUnit(worker);
	}
	
	private final WorkerUnit worker;
	
	public WorkerUnitCollidesWithWorkerUnit(WorkerUnit worker) {
		super(makeObstacles(worker));
		
		this.worker = Objects.requireNonNull(worker, "worker");
	}

	private static Collection<? extends DynamicObstacle> makeObstacles(WorkerUnit worker) {
		return worker.getTrajectories().stream()
			.map(t -> new DynamicObstacle(worker.getShape(), t))
			.collect(toList());
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
