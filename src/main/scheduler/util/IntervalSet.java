package scheduler.util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface IntervalSet<T extends Comparable<? super T>>
extends Iterable<IntervalSet.Interval<T>>
{

	public static class Interval<T extends Comparable<? super T>>
	implements Comparable<Interval<T>>
	{
		
		private final T fromInclusive;
		
		private final T toExclusive;
	
		protected Interval(
			T fromInclusive,
			T toExclusive)
		{
			this.fromInclusive = Objects.requireNonNull(fromInclusive, "fromInclusive");
			this.toExclusive = Objects.requireNonNull(toExclusive, "toExclusive");
		}
	
		public T getFromInclusive() {
			return fromInclusive;
		}
	
		public T getToExclusive() {
			return toExclusive;
		}

		@Override
		public String toString() {
			return String.format("[%s, %s]", fromInclusive, toExclusive);
		}

		@Override
		public int compareTo(Interval<T> o) {
			int fromCmp = fromInclusive.compareTo(o.fromInclusive);
			
			if (fromCmp == 0)
				return toExclusive.compareTo(o.toExclusive);
			else
				return fromCmp;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			
			result = prime * result + fromInclusive.hashCode();
			result = prime * result + toExclusive.hashCode();
			
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			Interval<?> other = (Interval<?>) obj;
			
			if (!fromInclusive.equals(other.fromInclusive))
				return false;
			if (!toExclusive.equals(other.toExclusive))
				return false;
			
			return true;
		}
		
	}

	public abstract boolean isEmpty();
	
	public abstract Interval<T> minInterval();
	
	public abstract Interval<T> maxInterval();
	
	public abstract Interval<T> floorInterval(T obj);
	
	public abstract Interval<T> ceilingInterval(T obj);
	
	public abstract Interval<T> lowerInterval(T obj);
	
	public abstract Interval<T> higherInterval(T obj);

	public abstract T minValue();

	public abstract T maxValue();

	public abstract boolean contains(T obj);

	public abstract boolean intersects(T fromInclusive, T toExclusive);

	public abstract boolean includedBy(T fromInclusive, T toInclusive);

	public abstract boolean overlaps(T fromInclusive, T toExclusive);

	public abstract boolean overlapsNonStrict(T fromInclusive, T toInclusive);

	public abstract boolean overlaps(IntervalSet<T> other);

	public abstract boolean intersects(IntervalSet<T> other);

	public abstract IntervalSet<T> union(T fromInclusive, T toExclusive);

	public abstract IntervalSet<T> union(IntervalSet<T> other);

	public abstract IntervalSet<T> difference(T fromInclusive, T toExclusive);

	public abstract IntervalSet<T> difference(IntervalSet<T> other);

	public abstract IntervalSet<T> intersection(T fromInclusive, T toExclusive);

	public abstract IntervalSet<T> intersection(IntervalSet<T> other);
	
	// includes the intervals where fromInclusive lies within the given
	// fromInclusive and toExclusive.
	public abstract IntervalSet<T> subSet(T fromInclusive, T toExclusive);
	
	public abstract Stream<Interval<T>> stream();
	
	public List<Interval<T>> toList();

}