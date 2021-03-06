package de.tu_berlin.mailbox.rjasper.st_scheduler.matchers;

import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.CollisionMatchers.obstaclesCollideWith;

import java.util.Collection;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;

public class NodeCollidesWithDynamicObstacles extends MapMatcher<Node, DynamicObstacle> {

	@Factory
	public static Matcher<Node> nodeCollidesWith(Collection<DynamicObstacle> obstacles) {
		return new NodeCollidesWithDynamicObstacles(obstacles);
	}

	private final Collection<? extends DynamicObstacle> obstacles;

	public NodeCollidesWithDynamicObstacles(Collection<DynamicObstacle> obstacles) {
		super(obstaclesCollideWith(obstacles), w ->
			new DynamicObstacle(w.getShape(), w.calcTrajectory()));

		this.obstacles = Objects.requireNonNull(obstacles, "obstacles");
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a node colliding with ")
			.appendValue(obstacles);
	}

}
