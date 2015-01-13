package common.collect;

import static java.util.stream.Collector.Characteristics.UNORDERED;

import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class ImmutablesCollectors {
	
	// from https://gist.github.com/JakeWharton/9734167
	public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toImmutableList() {
		return Collector.of(
			ImmutableList.Builder<T>::new,
			ImmutableList.Builder<T>::add,
			(l, r) -> l.addAll(r.build()),
			ImmutableList.Builder<T>::build);
	}

	// https://gist.github.com/JakeWharton/9734167
	public static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toImmutableSet() {
		return Collector.of(
			ImmutableSet.Builder<T>::new,
			ImmutableSet.Builder<T>::add,
			(l, r) -> l.addAll(r.build()),
			ImmutableSet.Builder<T>::build,
			UNORDERED);
	}

}
