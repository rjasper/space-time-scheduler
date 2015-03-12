package legacy.matchers;
//package matchers;
//
//import java.util.Collection;
//import java.util.stream.StreamSupport;
//
//import org.hamcrest.Description;
//import org.hamcrest.TypeSafeDiagnosingMatcher;
//
//import scheduler.Node;
//import world.NodeObstacle;
//
//public class NodeEvadedByNumTimes extends TypeSafeDiagnosingMatcher<Iterable<Node>> {
//
//	private final Node operand;
//	
//	private final int expectedTimes;
//
//	public NodeEvadedByNumTimes(Node operand, int expectedTimes) {
//		if (operand == null)
//			throw new NullPointerException("operand is null");
//		
//		this.operand = operand;
//		this.expectedTimes = expectedTimes;
//	}
//
//	@Override
//	public void describeTo(Description description) {
//		description
//			.appendText("a worker evaded by ")
//			.appendValue(operand)
//			.appendText(" ")
//			.appendValue(expectedTimes)
//			.appendText(" times");
//	}
//
//	@Override
//	protected boolean matchesSafely(Iterable<Node> item, Description mismatchDescription) {
//		long actualTimes = StreamSupport.stream(item.spliterator(), false)
//			.map(Node::getObstacleSections)
//			.flatMap(Collection::stream)
//			.map(NodeObstacle::getEvaders)
//			.flatMap(Collection::stream)
//			.map(NodeObstacle::getNode)
//			.filter(operand::equals)
//			.count();
//		
//		boolean status = actualTimes == expectedTimes;
//		
//		mismatchDescription
//			.appendText("number of times being evaded by ")
//			.appendValue(operand)
//			.appendText(" was ")
//			.appendValue(actualTimes)
//			.appendText(" instead of ")
//			.appendValue(expectedTimes);
//		
//		return status;
//	}
//
//}
