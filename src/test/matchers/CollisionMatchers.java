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
	
	public static Matcher<Node> workerCollidesWith(StaticObstacle obstacle) {
		return new NodeCollidesWithStaticObstacle(obstacle);
	}
	
	public static Matcher<Node> workerCollidesWith(DynamicObstacle obstacle) {
		return NodeCollidesWithDynamicObstacles.workerCollidesWith(singleton(obstacle));
	}
	
	public static Matcher<Node> workerCollidesWith(Collection<? extends DynamicObstacle> obstacles) {
		return NodeCollidesWithDynamicObstacles.workerCollidesWith(obstacles);
	}
	
	public static Matcher<Node> workerCollidesWith(Node worker) {
		return NodeCollidesWithNode.workerCollidesWith(worker);
	}
	
	public static Matcher<NodeReference> workerCollidesWith(NodeReference worker) {
		return WorkerRefCollidesWithWorkerRef.workerCollidesWith(worker);
	}

	public static Matcher<DynamicObstacle> obstaclesCollideWith(DynamicObstacle obstacle) {
		return DynamicObstacleCollidesWithDynamicObstacles.obstacleCollidesWith(singleton(obstacle));
	}
	
	public static Matcher<DynamicObstacle> obstaclesCollideWith(Collection<? extends DynamicObstacle> obstacles) {
		return DynamicObstacleCollidesWithDynamicObstacles.obstacleCollidesWith(obstacles);
	}
	
}
