package scheduler.util;

import static java.util.Spliterator.*;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
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
		return entry == null ? null : intervalMapper.apply(entry.getValue());
	}
	
	private Interval<T> interval(U obj) {
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
	public scheduler.util.IntervalSet.Interval<T> minInterval() {
		return interval( map.firstEntry() );
	}

	@Override
	public scheduler.util.IntervalSet.Interval<T> maxInterval() {
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
	
	@Override
	public Iterator<Interval<T>> iterator() {
		Iterator<Interval<T>> it = map.values().stream()
			.map(this::interval)
			.iterator();
		
		return makeIterator(it);
	}

	@Override
	public Spliterator<Interval<T>> spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), DISTINCT | NONNULL | ORDERED | SORTED);
	}
	
}
