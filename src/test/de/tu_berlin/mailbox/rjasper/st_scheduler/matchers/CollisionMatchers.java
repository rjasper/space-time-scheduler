package de.tu_berlin.mailbox.rjasper.st_scheduler.matchers;

import static java.util.Collections.*;

import java.util.Collection;

import org.hamcrest.Matcher;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;


public final class CollisionMatchers {

	private CollisionMatchers() {}

	public static Matcher<Trajectory> trajectoryCollidesWith(DynamicObstacle obstacle) {
		return TrajectoryCollidesWithDynamicObstacles.trajectoryCollidesWith(singleton(obstacle));
	}

	public static Matcher<Trajectory> trajectoryCollidesWith(Collection<DynamicObstacle> obstacles) {
		return TrajectoryCollidesWithDynamicObstacles.trajectoryCollidesWith(obstacles);
	}

	public static Matcher<Node> nodeCollidesWith(StaticObstacle obstacle) {
		return new NodeCollidesWithStaticObstacle(obstacle);
	}

	public static Matcher<Node> nodeCollidesWith(DynamicObstacle obstacle) {
		return NodeCollidesWithDynamicObstacles.nodeCollidesWith(singleton(obstacle));
	}

	public static Matcher<Node> nodeCollidesWith(Collection<DynamicObstacle> obstacles) {
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

	public static Matcher<DynamicObstacle> obstaclesCollideWith(Collection<DynamicObstacle> obstacles) {
		return DynamicObstacleCollidesWithDynamicObstacles.obstacleCollidesWith(obstacles);
	}

}
