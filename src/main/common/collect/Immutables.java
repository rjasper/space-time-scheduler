package common.collect;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Provides functions to convert regular collections and maps into the 
 * appropriate immutable variant.
 * 
 * @author Rico
 */
public final class Immutables {
	
	/**
	 * Converts a collection to a immutable list.
	 * 
	 * @param collection
	 * @return the immutable list.
	 */
	public static <E> ImmutableList<E> immutableAsList(Collection<E> collection) {
		if (collection instanceof ImmutableList<?>)
			return (ImmutableList<E>) collection;
		else
			return ImmutableList.copyOf(collection);
	}
	
	/**
	 * Converts a collection to a immutable set.
	 * 
	 * @param collection
	 * @return the immutable set.
	 */
	public static <E> ImmutableSet<E> immutableAsSet(Collection<E> collection) {
		if (collection instanceof ImmutableSet<?>)
			return (ImmutableSet<E>) collection;
		else
			return ImmutableSet.copyOf(collection);
	}

	/**
	 * Converts a list to a immutable list.
	 * 
	 * @param list
	 * @return the immutable list.
	 */
	public static <E> ImmutableList<E> immutable(List<E> list) {
		return immutableAsList(list);
	}

	/**
	 * Converts a set to a immutable set.
	 * 
	 * @param set
	 * @return the immutable set.
	 */
	public static <E> ImmutableSet<E> immutable(Set<E> set) {
		return immutableAsSet(set);
	}

	/**
	 * Converts a map to a immutable map.
	 * 
	 * @param map
	 * @return the immutable map.
	 */
	public static <K, V> ImmutableMap<K, V> immutable(Map<K, V> map) {
		if (map instanceof ImmutableMap<?, ?>)
			return (ImmutableMap<K, V>) map;
		else
			return ImmutableMap.copyOf(map);
	}

	/**
	 * <p>
	 * Converts a collection to a immutable collection.
	 * </p>
	 * 
	 * <p>
	 * Currently only lists and sets are supported.
	 * </p>
	 * 
	 * @param collection
	 * @return the immutable collection.
	 * @throws IllegalArgumentException
	 *             if the given collection is an unsupported type.
	 */
	public static <E> ImmutableCollection<E> immutable(Collection<E> collection) {
		if      (collection instanceof List<?>)
			return immutable((List<E>) collection);
		else if (collection instanceof Set<?>)
			return immutable((Set<E>) collection);
		
		throw new IllegalArgumentException("unsupported collection");
	}

}
