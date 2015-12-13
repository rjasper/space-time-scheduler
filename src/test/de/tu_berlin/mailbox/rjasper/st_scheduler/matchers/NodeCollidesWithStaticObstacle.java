package de.tu_berlin.mailbox.rjasper.st_scheduler.matchers;

import static com.vividsolutions.jts.geom.IntersectionMatrix.isTrue;
import static com.vividsolutions.jts.geom.Location.BOUNDARY;
import static com.vividsolutions.jts.geom.Location.INTERIOR;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;

public class NodeCollidesWithStaticObstacle extends TypeSafeMatcher<Node> {
	
	@Factory
	public static Matcher<Node> nodeCollideWith(StaticObstacle obstacle) {
		return new NodeCollidesWithStaticObstacle(obstacle);
	}
	
	private final StaticObstacle obstacle;
	
	public NodeCollidesWithStaticObstacle(StaticObstacle obstacle) {
		this.obstacle = obstacle;
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a node colliding with ")
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
