package scheduler.util;

import static java.util.Spliterator.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MappedIntervalSet<U, T extends Comparable<? super T>>
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
	
	protected T to(Entry<T, U> entry) {
		return to(entry.getValue());
	}
	
	protected T from(Entry<T, U> entry) {
		return from(entry.getValue());
	}
	
	protected T from(U obj) {
		return interval(obj).getFromInclusive();
	}
	
	protected T to(U obj) {
		return interval(obj).getToExclusive();
	}
	
	protected Interval<T> interval(Entry<T, U> entry) {
		return entry == null ? null : interval(entry.getValue());
	}
	
	protected Interval<T> interval(U obj) {
		return obj == null ? null : intervalMapper.apply(obj);
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#contains(C)
	 */
	@Override
	public boolean contains(T obj) {
		Objects.requireNonNull(obj, "obj");
		
		Entry<T, U> entry = map.floorEntry(obj);
		
		return entry != null && to(entry).compareTo(obj) > 0;
	}

	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#minValue()
	 */
	@Override
	public T minValue() {
		if (isEmpty())
			throw new IllegalStateException("set is empty");
		
		return map.firstKey();
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#maxValue()
	 */
	@Override
	public T maxValue() {
		if (isEmpty())
			throw new IllegalStateException("set is empty");
		
		return to(map.lastEntry());
	}
	
	@Override
	public Interval<T> floorInterval(T obj) {
		Entry<T, U> entry = map.floorEntry(obj);
		
		return interval(entry);
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
	
	private class SubSet extends MappedIntervalSet<U, T> {
		
		public SubSet(T fromInclusive, T toExclusive) {
			super(
				makeSubMap(MappedIntervalSet.this, fromInclusive, toExclusive),
				MappedIntervalSet.this.intervalMapper);
		}
		
	}
	
	private static <U, T extends Comparable<? super T>> NavigableMap<T, U> makeSubMap(
		MappedIntervalSet<U, T> self,
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
		
		return new IntervalIterator<>(it);
	}

	@Override
	public Spliterator<Interval<T>> spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), DISTINCT | NONNULL | ORDERED | SORTED);
	}

	@Override
	public Stream<Interval<T>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	public List<Interval<T>> toList() {
		return this.map.values().stream()
			.map(this::interval)
			.collect(Collectors.toList());
	}
	
}
