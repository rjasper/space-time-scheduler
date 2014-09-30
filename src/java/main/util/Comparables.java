package util;

public class Comparables {
	
	public static <T extends Comparable<T>, U extends T> U min(U lhs, U rhs) {
		return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
	}
	
	public static <T extends Comparable<T>, U extends T> U max(U lhs, U rhs) {
		return lhs.compareTo(rhs) >= 0 ? lhs : rhs;
	}

}
