package util;

/**
 * {@code Comparables} provides static utility functions for {@link Comparable}
 * types.
 *
 * @author Rico Jasper
 */
public final class Comparables {

	private Comparables() {}

	/**
	 * @param lhs the left hand side
	 * @param rhs the right hand side
	 * @return the <em>lesser</em> of the two.
	 */
	public static <T extends Comparable<? super T>> T min(T lhs, T rhs) {
		return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
	}

	/**
	 * @param lhs the left hand side
	 * @param rhs the right hand side
	 * @return the <em>larger</em> of the two.
	 */
	public static <T extends Comparable<? super T>> T max(T lhs, T rhs) {
		return lhs.compareTo(rhs) >= 0 ? lhs : rhs;
	}

}
