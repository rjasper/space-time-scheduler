package util;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Implements require methods for collections. If requiring condition is not
 * met then an exception is thrown.
 * 
 * @author Rico
 */
public final class CollectionsRequire {
	
	private CollectionsRequire() {}
	
	/**
	 * Checks if the collection is non-null and does not contain {@code null}.
	 * 
	 * @param collection
	 * @throws NullPointerException
	 *             if collection is {@code null} or contains a {@code null}.
	 */
	public static void requireContainsNunNull(Collection<?> collection) {
		if (!checkContainsNonNull(collection))
			throw new NullPointerException();
	}
	
	/**
	 * Checks if the collection is non-null and does not contain {@code null}.
	 * Uses a message for Exceptions.
	 * 
	 * @param collection
	 * @param message
	 * @throws NullPointerException
	 *             if collection is {@code null} or contains a {@code null}.
	 */
	public static void requireContainsNonNull(Collection<?> collection, String message) {
		if (!checkContainsNonNull(collection))
			throw new NullPointerException(message);
	}

	/**
	 * Checks if the collection is non-null and does not contain {@code null}.
	 * Uses a message supplier for Exceptions.
	 * 
	 * @param collection
	 * @param message
	 * @throws NullPointerException
	 *             if collection is {@code null} or contains a {@code null}.
	 */
	public static void requireContainsNonNull(Collection<?> collection, Supplier<String> messageSupplier) {
		if (!checkContainsNonNull(collection))
			throw new NullPointerException(messageSupplier.get());
	}

	/**
	 * Checks if the collection is non-null and does not contain {@code null}.
	 * 
	 * @param collection
	 * @return {@code true} if the collection is valid
	 */
	private static boolean checkContainsNonNull(Collection<?> collection) {
		if (collection == null)
			return false;
	
		boolean valid; // whether or not collection contains null (negated)
		try {
			// The collection implementation might not allow any null elements
			// to begin with. Such a collection might throws a
			// NullPointerException if null is given.
			
			valid = !collection.contains(null);
		} catch (NullPointerException e) {
			// Implementation does not allow null elements. Therefore, it should
			// also not contain any.
			
			valid = true;
		}
		
		return valid;
	}

}
