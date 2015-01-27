package matchers;

import static matchers.CollisionMatchers.*;

import java.util.Collection;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import tasks.WorkerUnit;
import world.DynamicObstacle;

public class WorkerUnitCollidesWithDynamicObstacles extends MapMatcher<WorkerUnit, DynamicObstacle> {
	
	@Factory
	public static Matcher<WorkerUnit> workerCollidesWith(Collection<? extends DynamicObstacle> obstacles) {
		return new WorkerUnitCollidesWithDynamicObstacles(obstacles);
	}
	
	private final Collection<? extends DynamicObstacle> obstacles;
	
	public WorkerUnitCollidesWithDynamicObstacles(Collection<? extends DynamicObstacle> obstacles) {
		super(obstaclesCollideWith(obstacles), w ->
			new DynamicObstacle(w.getShape(), w.calcTrajectory()));
		
		this.obstacles = Objects.requireNonNull(obstacles, "obstacles");
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker colliding with ")
			.appendValue(obstacles);
	}

}
