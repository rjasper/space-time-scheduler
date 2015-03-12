package legacy.matchers;
//package matchers;
//
//import java.time.LocalDateTime;
//
//import org.hamcrest.Description;
//import org.hamcrest.TypeSafeDiagnosingMatcher;
//
//import scheduler.Node;
//import world.NodeObstacle;
//
//public class NodeEvadedByAt extends TypeSafeDiagnosingMatcher<Node> {
//	
//	private final Node operand;
//	
//	private final LocalDateTime timeOfSegment;
//
//	public NodeEvadedByAt(Node operand, LocalDateTime timeOfSegment) {
//		this.operand = operand;
//		this.timeOfSegment = timeOfSegment;
//	}
//
//	@Override
//	public void describeTo(Description description) {
//		description
//			.appendText("a node evaded by ")
//			.appendValue(operand)
//			.appendText(" moving along a segment present at ")
//			.appendValue(timeOfSegment);
//	}
//
//	@Override
//	protected boolean matchesSafely(Node item, Description mismatchDescription) {
//		NodeObstacle segment = item.getObstacleSection(timeOfSegment);
//		
//		mismatchDescription
//		.appendValue(item)
//		.appendText(" was not evaded by ")
//		.appendValue(operand);
//		
//		if (segment == null) {
//			mismatchDescription
//				.appendText(" moving along a segment present at ")
//				.appendValue(timeOfSegment);
//			
//			return false;
//		} else {
//			mismatchDescription
//				.appendText(" moving along ")
//				.appendValue(segment);
//			
//			return segment.getEvaders().stream()
//				.map(NodeObstacle::getNode)
//				.anyMatch(operand::equals);
//		}
//	}
//
//}
