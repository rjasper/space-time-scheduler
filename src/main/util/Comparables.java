package util;

import java.util.Arrays;

/**
 * {@code Comparables} provides static utility functions for {@link Comparable}
 * types.
 *
 * @author Rico Jasper
 */
public final class Comparables {

	private Comparables() {}

	/**
	 * @param lhs
	 *            the left hand side
	 * @param rhs
	 *            the right hand side
	 * @return the <em>lesser</em> of the two.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 */
	public static <T extends Comparable<? super T>> T min(T lhs, T rhs) {
		return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
	}

	/**
	 * Determines the minimum value of the given values.
	 * 
	 * @param values
	 * @return the minimum value.
	 */
	@SafeVarargs
	public static <T extends Comparable<? super T>> T min(T... values) {
		if (values.length == 0)
			throw new IllegalArgumentException("no values");
		
		return Arrays.stream(values)
			.min((u, v) -> u.compareTo(v))
			.get();
	}

	/**
	 * @param lhs
	 *            the left hand side
	 * @param rhs
	 *            the right hand side
	 * @return the <em>larger</em> of the two.
	 * @throws NullPointerException
	 *             if any argmunet is {@code null}.
	 */
	public static <T extends Comparable<? super T>> T max(T lhs, T rhs) {
		return lhs.compareTo(rhs) >= 0 ? lhs : rhs;
	}

	/**
	 * Determines the maximum value of the given values.
	 * 
	 * @param values
	 * @return the maximum value.
	 */
	@SafeVarargs
	public static <T extends Comparable<? super T>> T max(T... values) {
		if (values.length == 0)
			throw new IllegalArgumentException("no values");
		
		return Arrays.stream(values)
			.max((u, v) -> u.compareTo(v))
			.get();
	}

}
