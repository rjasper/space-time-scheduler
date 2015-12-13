package de.tu_berlin.mailbox.rjasper.st_scheduler.matchers;

import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.DynamicCollisionDetector.collides;

import java.util.Collection;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

public class TrajectoryCollidesWithDynamicObstacles extends TypeSafeMatcher<Trajectory> {

	@Factory
	public static Matcher<Trajectory> trajectoryCollidesWith(Collection<DynamicObstacle> obstacles) {
		return new TrajectoryCollidesWithDynamicObstacles(obstacles);
	}

	protected final Collection<DynamicObstacle> obstacles;

	public TrajectoryCollidesWithDynamicObstacles(Collection<DynamicObstacle> obstacles) {
		this.obstacles = Objects.requireNonNull(obstacles, "obstacles");
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a dynamic obstacle colliding with ")
			.appendValue(obstacles);
	}

	@Override
	protected void describeMismatchSafely(Trajectory item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" is not colliding with ")
			.appendValue(obstacles);
	}

	@Override
	protected boolean matchesSafely(Trajectory item) {
		return collides(item, obstacles);
	}

}
