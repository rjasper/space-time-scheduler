package matchers;

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Objects;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import scheduler.Node;
import world.DynamicObstacle;

public class NodeCollidesWithNode extends NodeCollidesWithDynamicObstacles {
	
	@Factory
	public static Matcher<Node> workerCollidesWith(Node worker) {
		return new NodeCollidesWithNode(worker);
	}
	
	private final Node worker;
	
	public NodeCollidesWithNode(Node worker) {
		super(makeObstacles(worker));
		
		this.worker = Objects.requireNonNull(worker, "worker");
	}

	private static Collection<? extends DynamicObstacle> makeObstacles(Node worker) {
		return worker.getTrajectories().stream()
			.map(t -> new DynamicObstacle(worker.getShape(), t))
			.collect(toList());
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("a worker colliding with ")
			.appendValue(worker);
	}

	@Override
	protected void describeMismatchSafely(Node item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" is not colliding with ")
			.appendValue(worker);
	}
	
}
