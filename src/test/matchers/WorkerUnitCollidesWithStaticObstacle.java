package matchers;

import static com.vividsolutions.jts.geom.IntersectionMatrix.isTrue;
import static com.vividsolutions.jts.geom.Location.BOUNDARY;
import static com.vividsolutions.jts.geom.Location.INTERIOR;
import static jts.geom.immutable.ImmutableGeometries.immutable;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import tasks.WorkerUnit;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.Polygon;

public class WorkerUnitCollidesWithStaticObstacle extends TypeSafeDiagnosingMatcher<WorkerUnit> {
	
	private final Polygon obstacle;
	
	public WorkerUnitCollidesWithStaticObstacle(Polygon obstacle) {
		this.obstacle = immutable(obstacle);
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker colliding with ")
			.appendValue(obstacle);
	}

	@Override
	protected boolean matchesSafely(WorkerUnit item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" is colliding with ")
			.appendValue(obstacle);
		
		Geometry bufferedObstacle = obstacle.buffer(item.getRadius());
		Geometry trace = item.calcMergedTrajectory().getTrace();
		
		IntersectionMatrix mat = bufferedObstacle.relate(trace);
		
		// true if the trace has any points with the buffered obstacle's
		// interior in common
		return isTrue( mat.get(INTERIOR, INTERIOR) )
			|| isTrue( mat.get(INTERIOR, BOUNDARY) );
	}

}
