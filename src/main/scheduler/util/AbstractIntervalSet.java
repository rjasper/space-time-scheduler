package scheduler.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class AbstractIntervalSet<T extends Comparable<? super T>>
implements IntervalSet<T>
{

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
	
	protected IntervalSet<T> makeOverlappingSubSet(IntervalSet<T> other) {
		return other.subSet(minValue(), maxValue());
	}
	
	protected void checkInterval(T fromInclusive, T toExclusive) {
		Objects.requireNonNull(fromInclusive, "fromInclusive");
		Objects.requireNonNull(toExclusive, "toExclusive");
		
		if (fromInclusive.compareTo(toExclusive) >= 0)
			throw new IllegalArgumentException("invalid interval");
	}
	
	protected boolean overlapsNonStrict(T fromInclusive, T toInclusive) {
		return !isEmpty() &&
			fromInclusive.compareTo(maxValue()) <= 0 && // from <= max
			toInclusive  .compareTo(minValue()) >= 0;   // to   >= min
	}
	
	protected boolean overlaps(T fromInclusive, T toExclusive) {
		return !isEmpty() &&
			fromInclusive.compareTo(maxValue()) < 0 && // from < max
			toExclusive  .compareTo(minValue()) > 0;   // to   > min
	}
	
	protected boolean overlaps(IntervalSet<T> other) {
		return !isEmpty() && !other.isEmpty() &&
			other.minValue().compareTo(maxValue()) < 0 && // min2 < max1
			other.maxValue().compareTo(minValue()) > 0;   // max2 > min2
	}
	
	protected boolean includedBy(T fromInclusive, T toInclusive) {
		return isEmpty() || (
			fromInclusive.compareTo(minValue()) <= 0 && // from <= min
			toInclusive  .compareTo(maxValue()) >= 0);  // to   >= max
	}

	@Override
	public int hashCode() {
		if (isEmpty())
			return 1;
		
		final int prime = 31;
		int result = 1;
		
		result = prime * result + minValue().hashCode();
		result = prime * result + maxValue().hashCode();
		
		return result;
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
		
		// equal sets' iterators should be at end of iteration
		if (it1.hasNext() || it2.hasNext())
			return false;
		
		return true;
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
			return iterator.hasNext();
		}

		@Override
		public Interval<T> next() {
			if (peek == null)
				throw new NoSuchElementException("no next element");
			
			Interval<T> first = peek;
			Interval<T> last = first;
			
			while (iterator.hasNext()) {
				peek = iterator.next();
				
				T lastTo = last.getToExclusive();
				T peekFrom = peek.getFromInclusive();
				
				if (!peekFrom.equals(lastTo))
					break;
				
				last = peek;
			}
			
			// !iterator.hasNext() || peek.from != last.to
			
			if (!iterator.hasNext())
				peek = null;
			
			if (last == first)
				return first;
			else
				return new Interval<>(
					first.getFromInclusive(),
					last.getToExclusive());
		}
		
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
