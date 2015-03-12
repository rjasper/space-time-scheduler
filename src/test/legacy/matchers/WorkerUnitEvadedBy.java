package legacy.matchers;
//package matchers;
//
//import java.util.Collection;
//
//import org.hamcrest.Description;
//import org.hamcrest.TypeSafeDiagnosingMatcher;
//
//import scheduler.Node;
//import world.NodeObstacle;
//
//public class NodeEvadedBy extends TypeSafeDiagnosingMatcher<Node> {
//	
//	private Node operand;
//	
//	public NodeEvadedBy(Node operand) {
//		this.operand = operand;
//	}
//
//	@Override
//	protected boolean matchesSafely(Node item, Description mismatchDescription) {
//		mismatchDescription
//			.appendValue(item)
//			.appendText(" wasn't evaded by ")
//			.appendValue(operand);
//		
//		// true if item is evaded by the operand
//		return item.getObstacleSections().stream()
//			.map(NodeObstacle::getEvaders)
//			.flatMap(Collection::stream)
//			.map(NodeObstacle::getNode)
//			.anyMatch(operand::equals);
//	}
//
//	@Override
//	public void describeTo(Description description) {
//		description
//			.appendText("a node evaded by ")
//			.appendValue(operand);
//	}
//
//}
