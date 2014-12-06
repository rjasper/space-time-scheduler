package util;

public class Comparables {

	public static <T extends Comparable<? super T>> T min(T lhs, T rhs) {
		return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
	}
	
	public static <T extends Comparable<? super T>> T max(T lhs, T rhs) {
		return lhs.compareTo(rhs) >= 0 ? lhs : rhs;
	}

}
