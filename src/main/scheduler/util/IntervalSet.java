package scheduler.util;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface IntervalSet<T extends Comparable<? super T>> {

	public static class Interval<T> {
		
		private final T fromInclusive;
		
		private final T toExclusive;
	
		protected Interval(
			T fromInclusive,
			T toExclusive)
		{
			this.fromInclusive = fromInclusive;
			this.toExclusive = toExclusive;
		}
	
		public T getFromInclusive() {
			return fromInclusive;
		}
	
		public T getToExclusive() {
			return toExclusive;
		}
		
	}

	public abstract boolean isEmpty();

	public abstract T minValue();

	public abstract T maxValue();

	public abstract boolean contains(T obj);

	public abstract boolean intersects(T fromInclusive, T toExclusive);

	public abstract boolean intersects(IntervalSet<T> other);

	public abstract IntervalSet<T> union(T fromInclusive, T toExclusive);

	public abstract IntervalSet<T> union(IntervalSet<T> other);

	public abstract IntervalSet<T> difference(T fromInclusive, T toExclusive);

	public abstract IntervalSet<T> difference(IntervalSet<T> other);

	public abstract IntervalSet<T> intersection(T fromInclusive, T toExclusive);

	public abstract IntervalSet<T> intersection(IntervalSet<T> other);

	public abstract void forEach(Consumer<Interval<T>> consumer);
	
	// includes the intervals where fromInclusive lies within the given
	// fromInclusive and toExclusive.
	public abstract IntervalSet<T> subSet(T fromInclusive, T toExclusive);
	
	public abstract Interval<T> floorInterval(T obj);
	
	public abstract Interval<T> ceilingInterval(T obj);
	
	public abstract Stream<Interval<T>> stream();

}