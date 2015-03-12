package de.tu_berlin.mailbox.rjasper.st_scheduler.matchers;

import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.CollisionMatchers.*;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;

public class NodeRefCollidesWithNodeRef
	extends MapMatcher<NodeReference, DynamicObstacle>
{
	@Factory
	public static Matcher<NodeReference> nodeCollidesWith(NodeReference node) {
		return new NodeRefCollidesWithNodeRef(node);
	}
	
	private final NodeReference nodeRef;

	public NodeRefCollidesWithNodeRef(NodeReference nodeRef) {
		super(
			obstaclesCollideWith(makeObstacle(nodeRef)),
			NodeRefCollidesWithNodeRef::makeObstacle);
		
		this.nodeRef = nodeRef;
	}
	
	private static DynamicObstacle makeObstacle(NodeReference nodeRef) {
		return new DynamicObstacle(nodeRef.getShape(), nodeRef.calcTrajectory());
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a node colliding with ")
			.appendValue(nodeRef);
	}
	
}
