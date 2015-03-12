package de.tu_berlin.mailbox.rjasper.st_scheduler.matchers;

import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.DynamicCollisionDetector.*;
import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;

public class DynamicObstacleCollidesWithDynamicObstacles extends TypeSafeMatcher<DynamicObstacle> {
	
	@Factory
	public static Matcher<DynamicObstacle> obstacleCollidesWith(Collection<? extends DynamicObstacle> obstacles) {
		return new DynamicObstacleCollidesWithDynamicObstacles(obstacles);
	}
	
	protected final Collection<? extends DynamicObstacle> obstacles;

	public DynamicObstacleCollidesWithDynamicObstacles(Collection<? extends DynamicObstacle> obstacles) {
		this.obstacles = Objects.requireNonNull(obstacles, "obstacles");
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a dynamic obstacle colliding with ")
			.appendValue(obstacles);
	}

	@Override
	protected void describeMismatchSafely(DynamicObstacle item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" is not colliding with ")
			.appendValue(obstacles);
	}

	@Override
	protected boolean matchesSafely(DynamicObstacle item) {
		double radius = item.calcRadius();
		
		Collection<DynamicObstacle> bufferedObstacles = obstacles.stream()
			.map(o -> o.buffer(radius))
			.collect(toList());
		
		return collides(item.getTrajectory(), bufferedObstacles);
	}

}
