package matchers;

import static com.vividsolutions.jts.geom.IntersectionMatrix.*;
import static com.vividsolutions.jts.geom.Location.*;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import tasks.WorkerUnit;
import world.StaticObstacle;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

public class WorkerUnitCollidesWithStaticObstacle extends TypeSafeMatcher<WorkerUnit> {
	
	@Factory
	public static Matcher<WorkerUnit> workerCollideWith(StaticObstacle obstacle) {
		return new WorkerUnitCollidesWithStaticObstacle(obstacle);
	}
	
	private final StaticObstacle obstacle;
	
	public WorkerUnitCollidesWithStaticObstacle(StaticObstacle obstacle) {
		this.obstacle = obstacle;
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker colliding with ")
			.appendValue(obstacle);
	}

	@Override
	protected void describeMismatchSafely(WorkerUnit item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" is not colliding with ")
			.appendValue(obstacle);
	}

	@Override
	protected boolean matchesSafely(WorkerUnit item) {
		StaticObstacle bufferedObstacle = obstacle.buffer(item.getRadius());
		Geometry shape = bufferedObstacle.getShape();
		Geometry trace = item.calcMergedTrajectory().getTrace();
		
		IntersectionMatrix mat = shape.relate(trace);
		
		// true if the trace has any points with the buffered obstacle's
		// interior in common
		return isTrue( mat.get(INTERIOR, INTERIOR) )
			|| isTrue( mat.get(INTERIOR, BOUNDARY) );
	}

}
