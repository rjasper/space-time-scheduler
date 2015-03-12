package matchers;

import static matchers.CollisionMatchers.*;

import java.util.Collection;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import scheduler.Node;
import world.DynamicObstacle;

public class NodeCollidesWithDynamicObstacles extends MapMatcher<Node, DynamicObstacle> {
	
	@Factory
	public static Matcher<Node> nodeCollidesWith(Collection<? extends DynamicObstacle> obstacles) {
		return new NodeCollidesWithDynamicObstacles(obstacles);
	}
	
	private final Collection<? extends DynamicObstacle> obstacles;
	
	public NodeCollidesWithDynamicObstacles(Collection<? extends DynamicObstacle> obstacles) {
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
