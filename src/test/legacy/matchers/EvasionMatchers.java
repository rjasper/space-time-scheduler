package legacy.matchers;
//package matchers;
//
//import static org.hamcrest.CoreMatchers.*;
//
//import java.time.LocalDateTime;
//
//import org.hamcrest.Matcher;
//
//import scheduler.Node;
//
//public final class EvasionMatchers {
//	
//	private EvasionMatchers() {}
//	
//	public static Matcher<Node> isEvadedBy(Node operand) {
//		return new NodeEvadedBy(operand);
//	}
//	
//	public static Matcher<Iterable<? super Node>> areEvadedBy(Node operand) {
//		return hasItem(isEvadedBy(operand));
//	}
//	
//	public static Matcher<Node> isEvadedBy(Node operand, LocalDateTime timeOfSegment) {
//		return new NodeEvadedByAt(operand, timeOfSegment);
//	}
//	
//	public static Matcher<Iterable<Node>> evadedByNumTimes(Node operand, int times) {
//		return new NodeEvadedByNumTimes(operand, times);
//	}
//
//}
//
