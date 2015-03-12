package matchers;

import static com.vividsolutions.jts.geom.IntersectionMatrix.*;
import static com.vividsolutions.jts.geom.Location.*;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import scheduler.Node;
import world.StaticObstacle;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

public class NodeCollidesWithStaticObstacle extends TypeSafeMatcher<Node> {
	
	@Factory
	public static Matcher<Node> workerCollideWith(StaticObstacle obstacle) {
		return new NodeCollidesWithStaticObstacle(obstacle);
	}
	
	private final StaticObstacle obstacle;
	
	public NodeCollidesWithStaticObstacle(StaticObstacle obstacle) {
		this.obstacle = obstacle;
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker colliding with ")
			.appendValue(obstacle);
	}

	@Override
	protected void describeMismatchSafely(Node item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" is not colliding with ")
			.appendValue(obstacle);
	}

	@Override
	protected boolean matchesSafely(Node item) {
		StaticObstacle bufferedObstacle = obstacle.buffer(item.getRadius());
		Geometry shape = bufferedObstacle.getShape();
		Geometry trace = item.calcTrajectory().trace();
		
		IntersectionMatrix mat = shape.relate(trace);
		
		// true if the trace has any points with the buffered obstacle's
		// interior in common
		return isTrue( mat.get(INTERIOR, INTERIOR) )
			|| isTrue( mat.get(INTERIOR, BOUNDARY) );
	}

}
