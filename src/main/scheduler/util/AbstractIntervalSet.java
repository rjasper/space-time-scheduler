package scheduler.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractIntervalSet<T extends Comparable<? super T>>
implements IntervalSet<T>
{

	@Override
	public T minValue() {
		if (isEmpty())
			throw new IllegalStateException("set is empty");
		
		return minInterval().getFromInclusive();
	}

	@Override
	public T maxValue() {
		if (isEmpty())
			throw new IllegalStateException("set is empty");
		
		return maxInterval().getToExclusive();
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#contains(T)
	 */
	@Override
	public boolean contains(T obj) {
		Objects.requireNonNull(obj, "obj");
		
		Interval<T> interval = floorInterval(obj);
		
		return interval != null && interval.getToExclusive().compareTo(obj) > 0;
	}

	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#intersects(C, C)
	 */
	@Override
	public boolean intersects(T fromInclusive, T toExclusive) {
		checkInterval(fromInclusive, toExclusive);
		
		// short cut if map don't overlap
		if (!overlaps(fromInclusive, toExclusive))
			return false;
		
		Interval<T> lower = lowerInterval(toExclusive);
		
		return lower != null &&
			lower.getToExclusive().compareTo(fromInclusive) > 0;
	}

	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#intersects(scheduler.util.SimpleIntervalSet)
	 */
	@Override
	public boolean intersects(IntervalSet<T> other) {
		Objects.requireNonNull(other, "other");
		
		// short cut if map don't overlap
		if (!overlaps(other))
			return false;
		
		// checks only relevant segments
		
		return makeOverlappingSubSet(other).stream()
			.anyMatch(i -> intersects(i.getFromInclusive(), i.getToExclusive()));
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#union(T, T)
	 */
	@Override
	public SimpleIntervalSet<T> union(T fromInclusive, T toExclusive) {
		checkInterval(fromInclusive, toExclusive);
		
		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();

		// short cut
		if (!includedBy(fromInclusive, toExclusive))
			set.add(this);
		
		set.addImpl(fromInclusive, toExclusive);
		
		return set;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#union(scheduler.util.SimpleIntervalSet)
	 */
	@Override
	public SimpleIntervalSet<T> union(IntervalSet<T> other) {
		Objects.requireNonNull(other, "other");

		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();
		set.add(this);
		
		// short cut
		if (other != this)
			set.add(other);
		
		return set;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#difference(T, T)
	 */
	@Override
	public SimpleIntervalSet<T> difference(T fromInclusive, T toExclusive) {
		checkInterval(fromInclusive, toExclusive);

		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();
		
		// short cut
		if (!includedBy(fromInclusive, toExclusive)) {
			// TODO possible enhancement
			// only add head and tail intervals
			set.add(this);
			
			set.removeImpl(fromInclusive, toExclusive);
		}
		
		return set;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#difference(scheduler.util.SimpleIntervalSet)
	 */
	@Override
	public SimpleIntervalSet<T> difference(IntervalSet<T> other) {
		Objects.requireNonNull(other, "other");

		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();
		
		if (other != this) {
			// TODO possible enhancement
			// only add head and tail intervals
			set.add(this);
			
			set.remove(other);
		}
		
		return set;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#intersection(T, T)
	 */
	@Override
	public SimpleIntervalSet<T> intersection(T fromInclusive, T toExclusive) {
		checkInterval(fromInclusive, toExclusive);

		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();
		
		// short cut
		if (!overlaps(fromInclusive, toExclusive))
			return set;

		// short cut
		if (includedBy(fromInclusive, toExclusive)) {
			set.add(this);
		// regular case
		} else {
			set.add(subSet(fromInclusive, toExclusive));
			set.intersectImpl(fromInclusive, toExclusive);
		}
		
		return set;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#intersection(scheduler.util.SimpleIntervalSet)
	 */
	@Override
	public SimpleIntervalSet<T> intersection(IntervalSet<T> other) {
		Objects.requireNonNull(other, "other");

		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();
		
		// short cut
		if (!overlaps(other))
			return set;
		
		set.add(makeOverlappingSubSet(other));
		set.intersect(this);
		
		return set;
	}
	
	@Override
	public boolean overlapsNonStrict(T fromInclusive, T toInclusive) {
		return !isEmpty() &&
			fromInclusive.compareTo(maxValue()) <= 0 && // from <= max
			toInclusive  .compareTo(minValue()) >= 0;   // to   >= min
	}
	
	@Override
	public boolean overlaps(T fromInclusive, T toExclusive) {
		return !isEmpty() &&
			fromInclusive.compareTo(maxValue()) < 0 && // from < max
			toExclusive  .compareTo(minValue()) > 0;   // to   > min
	}
	
	@Override
	public boolean overlaps(IntervalSet<T> other) {
		return !isEmpty() && !other.isEmpty() &&
			other.minValue().compareTo(maxValue()) < 0 && // min2 < max1
			other.maxValue().compareTo(minValue()) > 0;   // max2 > min2
	}
	
	@Override
	public boolean includedBy(T fromInclusive, T toInclusive) {
		return isEmpty() || (
			fromInclusive.compareTo(minValue()) <= 0 && // from <= min
			toInclusive  .compareTo(maxValue()) >= 0);  // to   >= max
	}

	protected static class IntervalIterator<T extends Comparable<? super T>>
	implements Iterator<Interval<T>>
	{
		
		private final Iterator<Interval<T>> iterator;
		
		private Interval<T> peek = null;
		
		public IntervalIterator(Iterator<Interval<T>> iterator) {
			this.iterator = Objects.requireNonNull(iterator, "iterator");
			this.peek = iterator.hasNext() ? iterator.next() : null;
		}
	
		@Override
		public boolean hasNext() {
			return peek != null;
		}
	
		@Override
		public Interval<T> next() {
			if (peek == null)
				throw new NoSuchElementException("no next element");
			
			Interval<T> first = peek;
			Interval<T> last = first;
			
			// seek last interval which is not consecutive or overlapping
			boolean noBreak = true;
			while (iterator.hasNext()) {
				peek = iterator.next();
				
				T lastTo = last.getToExclusive();
				T peekFrom = peek.getFromInclusive();
				
				if (peekFrom.compareTo(lastTo) > 0) {
					noBreak = false;
					break;
				}
				
				last = peek;
			}
			
			// !iterator.hasNext() || peek.from != last.to
			
			// if while-loop finished ordinarily
			// indicates that there are no more intervals to iterate over
			if (noBreak)
				peek = null;
			
			if (last == first)
				return first; // reuse interval
			else
				return new Interval<>(
					first.getFromInclusive(),
					last.getToExclusive());
		}
		
	}

	@Override
	public Stream<Interval<T>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	protected void checkInterval(T fromInclusive, T toExclusive) {
		Objects.requireNonNull(fromInclusive, "fromInclusive");
		Objects.requireNonNull(toExclusive, "toExclusive");
		
		if (fromInclusive.compareTo(toExclusive) >= 0)
			throw new IllegalArgumentException("invalid interval");
	}
	
	protected Iterator<Interval<T>> makeIterator(Iterator<Interval<T>> it) {
		return new IntervalIterator<>(it);
	}

	protected IntervalSet<T> makeOverlappingSubSet(IntervalSet<T> other) {
		return other.subSet(minValue(), maxValue());
	}

	@Override
	public int hashCode() {
		return isEmpty() ? 1 : minValue().hashCode() ^ maxValue().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof IntervalSet<?>))
			return false;
		
		IntervalSet<?> other = (IntervalSet<?>) obj;
		
		Iterator<?> it1 = iterator();
		Iterator<?> it2 = other.iterator();
		
		while (it1.hasNext() && it2.hasNext()) {
			if (!it1.next() .equals( it2.next() ))
				return false;
		}
		
		// !it1.hasNext() || !it2.hasNext()
		
		// equal sets' iterators should be at the end of iteration
		if (it1.hasNext() || it2.hasNext())
			return false;
		
		return true;
	}
	
	@Override
	public List<Interval<T>> toList() {
		return stream().collect(Collectors.toList());
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		buf.append('[');
		
		Iterator<Interval<T>> it = iterator();
		
		while (it.hasNext()) {
			buf.append(it.next());
			
			if (it.hasNext())
				buf.append(", ");
		}
		
		buf.append(']');
		
		return buf.toString();
	}

}
