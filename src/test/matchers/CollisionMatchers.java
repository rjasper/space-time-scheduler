package matchers;

import static java.util.Collections.*;

import java.util.Collection;

import org.hamcrest.Matcher;

import scheduler.Node;
import scheduler.NodeReference;
import world.DynamicObstacle;
import world.StaticObstacle;


public final class CollisionMatchers {
	
	private CollisionMatchers() {}
	
	public static Matcher<Node> nodeCollidesWith(StaticObstacle obstacle) {
		return new NodeCollidesWithStaticObstacle(obstacle);
	}
	
	public static Matcher<Node> nodeCollidesWith(DynamicObstacle obstacle) {
		return NodeCollidesWithDynamicObstacles.nodeCollidesWith(singleton(obstacle));
	}
	
	public static Matcher<Node> nodeCollidesWith(Collection<? extends DynamicObstacle> obstacles) {
		return NodeCollidesWithDynamicObstacles.nodeCollidesWith(obstacles);
	}
	
	public static Matcher<Node> nodeCollidesWith(Node node) {
		return NodeCollidesWithNode.nodeCollidesWith(node);
	}
	
	public static Matcher<NodeReference> nodeCollidesWith(NodeReference node) {
		return NodeRefCollidesWithNodeRef.nodeCollidesWith(node);
	}

	public static Matcher<DynamicObstacle> obstaclesCollideWith(DynamicObstacle obstacle) {
		return DynamicObstacleCollidesWithDynamicObstacles.obstacleCollidesWith(singleton(obstacle));
	}
	
	public static Matcher<DynamicObstacle> obstaclesCollideWith(Collection<? extends DynamicObstacle> obstacles) {
		return DynamicObstacleCollidesWithDynamicObstacles.obstacleCollidesWith(obstacles);
	}
	
}
