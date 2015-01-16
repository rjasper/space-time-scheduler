package common.collect;

import static java.util.stream.Collector.Characteristics.*;

import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Provides collectors for immutable collections of the Guava Library.
 * 
 * @author Rico
 */
public final class ImmutablesCollectors {
	
	private ImmutablesCollectors() {}
	
	/**
	 * Returns a {@link Collector} that accumulates the input elements into a
	 * new {@link ImmutableList}.
	 * 
	 * @param <T> the type elements
	 * @return the collector.
	 */
	public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toImmutableList() {
		// from https://gist.github.com/JakeWharton/9734167
		return Collector.of(
			ImmutableList.Builder<T>::new,
			ImmutableList.Builder<T>::add,
			(l, r) -> l.addAll(r.build()),
			ImmutableList.Builder<T>::build);
	}

	/**
	 * Returns a {@link Collector} that accumulates the input elements into a
	 * new {@link ImmutableSet}.
	 * 
	 * @param <T> the type elements
	 * @return the collector.
	 */
	public static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toImmutableSet() {
		// https://gist.github.com/JakeWharton/9734167
		return Collector.of(
			ImmutableSet.Builder<T>::new,
			ImmutableSet.Builder<T>::add,
			(l, r) -> l.addAll(r.build()),
			ImmutableSet.Builder<T>::build,
			UNORDERED);
	}

}
