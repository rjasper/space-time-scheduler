package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

import java.util.Iterator;
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
	
		public Interval(
			T fromInclusive,
			T toExclusive)
		{
			Objects.requireNonNull(fromInclusive, "fromInclusive");
			Objects.requireNonNull(toExclusive, "toExclusive");
			
			if (fromInclusive.compareTo(toExclusive) >= 0)
				throw new IllegalArgumentException("invalid interval");
			
			this.fromInclusive = fromInclusive;
			this.toExclusive = toExclusive;
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

	/**
	 * @return the lowest value belonging to the set.
	 */
	public abstract T minValue();

	/**
	 * @return the highest value, where there is no lowest epsilon greater than
	 *         zero with a value equivalent to the value minus epsilon not
	 *         belonging to this set.
	 */
	public abstract T maxValue();
	
	/**
	 * Returns the value lower than or equal to given object, where there is no
	 * lowest epsilon greater than zero with a value equivalent to the value
	 * minus epsilon not belonging to this set.
	 * 
	 * @param obj
	 * @return the floor value.
	 */
	public abstract T floorValue(T obj);
	
	/**
	 * Returns the value greater or equal to given object, which belongs to this set.
	 * 
	 * @param obj
	 * @return the ceiling value.
	 */
	public abstract T ceilingValue(T obj);

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
	
	// includes all original intervals which overlap with [fromInclusive, toExclusive]
	public abstract IntervalSet<T> subSet(T fromInclusive, T toExclusive);
	
	public abstract Iterator<Interval<T>> descendingIterator();
	
	public abstract Stream<Interval<T>> stream();
	
	public List<Interval<T>> toList();

}