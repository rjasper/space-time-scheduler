package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

import static java.util.Spliterator.*;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;

public class MappedIntervalSet<T extends Comparable<? super T>, U>
extends AbstractIntervalSet<T>
{
	
	private final NavigableMap<T, U> map;
	
	private final Function<U, Interval<T>> intervalMapper;

	public MappedIntervalSet(
		NavigableMap<T, U> map,
		Function<U, Interval<T>> intervalMapper)
	{
		this.map = Objects.requireNonNull(map, "map");
		this.intervalMapper = Objects.requireNonNull(intervalMapper, "intervalMapper");
	}
	
	private Interval<T> interval(Entry<T, U> entry) {
		// TODO cache?
		return entry == null ? null : intervalMapper.apply(entry.getValue());
	}
	
	private Interval<T> interval(U obj) {
		// TODO cache?
		return obj == null ? null : intervalMapper.apply(obj);
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	@Override
	public de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSet.Interval<T> minInterval() {
		return interval( map.firstEntry() );
	}

	@Override
	public de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSet.Interval<T> maxInterval() {
		return interval( map.lastEntry() );
	}

	@Override
	public Interval<T> floorInterval(T obj) {
		return interval( map.floorEntry(obj) );
	}

	@Override
	public Interval<T> ceilingInterval(T obj) {
		return interval( map.ceilingEntry(obj) );
	}

	@Override
	public Interval<T> lowerInterval(T obj) {
		return interval( map.lowerEntry(obj) );
	}

	@Override
	public Interval<T> higherInterval(T obj) {
		return interval( map.higherEntry(obj) );
	}

	@Override
	public IntervalSet<T> subSet(T fromInclusive, T toExclusive) {
		return new SubSet(fromInclusive, toExclusive);
	}
	
	private class SubSet extends MappedIntervalSet<T, U> {
		
		public SubSet(T fromInclusive, T toExclusive) {
			super(
				makeSubMap(MappedIntervalSet.this, fromInclusive, toExclusive),
				MappedIntervalSet.this.intervalMapper);
		}
		
	}
	
	private static <U, T extends Comparable<? super T>>
	NavigableMap<T, U> makeSubMap(
		MappedIntervalSet<T, U> self,
		T fromInclusive, T toExclusive)
	{
		Interval<T> lowerInterval = self.lowerInterval(fromInclusive);
		T from;
		
		if (lowerInterval != null && lowerInterval.getToExclusive().compareTo(fromInclusive) > 0)
			from = lowerInterval.getFromInclusive();
		else
			from = fromInclusive;
		
		return (NavigableMap<T, U>) self.map.subMap(from, toExclusive);
	}

	private static class IntervalIterator<T extends Comparable<? super T>>
	implements Iterator<Interval<T>>
	{
		
		private final Iterator<Interval<T>> iterator;
		
		private final boolean descending;
		
		private Interval<T> peek = null;
		
		public IntervalIterator(Iterator<Interval<T>> iterator, boolean descending) {
			this.iterator = Objects.requireNonNull(iterator, "iterator");
			this.descending = descending;
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
			
			// seek last non-consecutive interval
			boolean noBreak = true;
			if (!descending) {
				while (iterator.hasNext()) {
					peek = iterator.next();
					
					T lastTo = last.getToExclusive();
					T peekFrom = peek.getFromInclusive();
					
					if (!peekFrom.equals(lastTo)) {
						noBreak = false;
						break;
					}
					
					last = peek;
				}
			} else {
				while (iterator.hasNext()) {
					peek = iterator.next();
					
					T lastFrom = last.getFromInclusive();
					T peekTo = peek.getToExclusive();
					
					if (!peekTo.equals(lastFrom)) {
						noBreak = false;
						break;
					}
					
					last = peek;
				}
			}
			
			// !iterator.hasNext() || peek.from != last.to
			
			// if while-loop finished ordinarily
			// indicates that there are no more intervals to iterate over
			if (noBreak)
				peek = null;
			
			if (last == first) {
				return first; // reuse interval
			} else {
				if (!descending) {
					return new Interval<>(
						first.getFromInclusive(),
						last.getToExclusive());
				} else {
					return new Interval<>(
						last.getFromInclusive(),
						first.getToExclusive());
				}
			}
		}
		
	}
	
	@Override
	public Iterator<Interval<T>> iterator() {
		Iterator<Interval<T>> it = map.values().stream()
			.map(this::interval)
			.iterator();
		
		return new IntervalIterator<>(it, false);
	}

	// TODO test
	@Override
	public Iterator<Interval<T>> descendingIterator() {
		Iterator<Interval<T>> it = map.descendingMap().values().stream()
			.map(this::interval)
			.iterator();
		
		return new IntervalIterator<>(it, true);
	}

	@Override
	public Spliterator<Interval<T>> spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), DISTINCT | NONNULL | ORDERED | SORTED);
	}
	
}
