package de.tu_berlin.mailbox.rjasper.st_scheduler.matchers;

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;

public class NodeCollidesWithNode extends NodeCollidesWithDynamicObstacles {
	
	@Factory
	public static Matcher<Node> nodeCollidesWith(Node node) {
		return new NodeCollidesWithNode(node);
	}
	
	private final Node node;
	
	public NodeCollidesWithNode(Node node) {
		super(makeObstacles(node));
		
		this.node = Objects.requireNonNull(node, "node");
	}

	private static Collection<? extends DynamicObstacle> makeObstacles(Node node) {
		return node.getTrajectories().stream()
			.map(t -> new DynamicObstacle(node.getShape(), t))
			.collect(toList());
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a node colliding with ")
			.appendValue(node);
	}

	@Override
	protected void describeMismatchSafely(Node item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" is not colliding with ")
			.appendValue(node);
	}
	
}
