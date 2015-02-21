package scheduler.util;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// XXX last edition
// TODO generalize type
public class TreeMapIntervalSet<C, T extends Comparable<? super T>>
implements IntervalSet<T>
{
	
	private final NavigableMap<T, C> map;
	
	private final Function<C, Interval<T>> intervalMapper;

	public TreeMapIntervalSet(
		NavigableMap<T, C> map,
		Function<C, Interval<T>> intervalMapper)
	{
		this.map = Objects.requireNonNull(map, "map");
		this.intervalMapper = Objects.requireNonNull(intervalMapper, "intervalMapper");
	}
	
	protected T to(Entry<T, C> entry) {
		return to(entry.getValue());
	}
	
	protected T from(Entry<T, C> entry) {
		return from(entry.getValue());
	}
	
	protected T from(C obj) {
		return interval(obj).getFromInclusive();
	}
	
	protected T to(C obj) {
		return interval(obj).getToExclusive();
	}
	
	protected Interval<T> interval(Entry<T, C> entry) {
		return entry == null ? null : interval(entry.getValue());
	}
	
	protected Interval<T> interval(C obj) {
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
		
		Entry<T, C> entry = map.floorEntry(obj);
		
		return entry != null && to(entry).compareTo(obj) > 0;
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
		
		Entry<T, C> entry = map.lowerEntry(toExclusive);
		
		return entry != null && to(entry).compareTo(fromInclusive) > 0;
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
		Entry<T, C> entry = map.floorEntry(obj);
		
		return interval(entry);
	}

	@Override
	public Interval<T> ceilingInterval(T obj) {
		return interval( map.ceilingEntry(obj) );
	}

	@Override
	public void forEach(Consumer<Interval<T>> consumer) {
		stream().forEach(consumer);
	}

	@Override
	public Stream<Interval<T>> stream() {
		return map.values().stream()
			.map(this::interval);
	}

	@Override
	public IntervalSet<T> subSet(T fromInclusive, T toExclusive) {
		return new SubSet(fromInclusive, toExclusive);
	}
	
	private class SubSet extends TreeMapIntervalSet<C, T> {
		
		public SubSet(T fromInclusive, T toExclusive) {
			super(
				(NavigableMap<T, C>)
				TreeMapIntervalSet.this.map.subMap(fromInclusive, toExclusive),
				TreeMapIntervalSet.this.intervalMapper);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#union(T, T)
	 */
	@Override
	public SimpleIntervalSet<T> union(T fromInclusive, T toExclusive) {
		// no checks since it delegates to public method
		
		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();

		// short cut
		if (!includedBy(fromInclusive, toExclusive))
			set.add(this);
		
		set.add(fromInclusive, toExclusive);
		
		return set;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#union(scheduler.util.SimpleIntervalSet)
	 */
	@Override
	public SimpleIntervalSet<T> union(IntervalSet<T> other) {
		// no checks since it delegates to public method

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
		// no checks since it delegates to public method

		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();
		
		// short cut
		if (!includedBy(fromInclusive, toExclusive)) {
			set.add(this);
			set.remove(fromInclusive, toExclusive);
		}
		
		return set;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#difference(scheduler.util.SimpleIntervalSet)
	 */
	@Override
	public SimpleIntervalSet<T> difference(IntervalSet<T> other) {
		// no checks since it delegates to public method

		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();
		
		if (other != this) {
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
		// no checks since it delegates to public method

		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();
		
		// short cut
		if (!overlaps(fromInclusive, toExclusive))
			return set;
		
		set.add(this);
		set.intersection(fromInclusive, toExclusive);
		
		return set;
	}
	
	private IntervalSet<T> makeOverlappingSubSet(IntervalSet<T> other) {
		T min = minValue(), max = maxValue();
		Interval<T> floorInterval = other.floorInterval(min);
		T floorMin = floorInterval == null ? min : floorInterval.getFromInclusive();
		
		return other.subSet(floorMin, max);
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#intersection(scheduler.util.SimpleIntervalSet)
	 */
	@Override
	public SimpleIntervalSet<T> intersection(IntervalSet<T> other) {
		// no checks since it delegates to public method

		SimpleIntervalSet<T> set = new SimpleIntervalSet<>();
		
		// short cut
		if (!overlaps(other))
			return set;
		
		set.add(this);
		set.intersection(other);
		
		return set;
	}
	
	private void checkInterval(T fromInclusive, T toExclusive) {
		Objects.requireNonNull(fromInclusive, "fromInclusive");
		Objects.requireNonNull(toExclusive, "toExclusive");
		
		if (fromInclusive.compareTo(toExclusive) >= 0)
			throw new IllegalArgumentException("invalid interval");
	}
	
	private boolean overlapsNonStrict(T fromInclusive, T toInclusive) {
		return !isEmpty() &&
			fromInclusive.compareTo(maxValue()) <= 0 && // from <= max
			toInclusive  .compareTo(minValue()) >= 0;   // to   >= min
	}
	
	private boolean overlaps(T fromInclusive, T toExclusive) {
		return !isEmpty() &&
			fromInclusive.compareTo(maxValue()) < 0 && // from < max
			toExclusive  .compareTo(minValue()) > 0;   // to   > min
	}
	
	private boolean overlaps(IntervalSet<T> other) {
		return !isEmpty() && !other.isEmpty() &&
			other.minValue().compareTo(maxValue()) < 0 && // min2 < max1
			other.maxValue().compareTo(minValue()) > 0;   // max2 > min2
	}
	
	private boolean includedBy(T fromInclusive, T toInclusive) {
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

	// TODO reimplement
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		@SuppressWarnings("unchecked")
//		SimpleIntervalSet<T> other = (SimpleIntervalSet<T>) obj;
//		if (map == null) {
//			if (other.map != null)
//				return false;
//		} else if (!map.equals(other.map))
//			return false;
//		return true;
//	}
	
	public List<Interval<T>> toList() {
		return this.map.values().stream()
			.map(this::interval)
			.collect(Collectors.toList());
	}
	
	// TODO reimplement
//	@Override
//	public String toString() {
//		return map.toString();
//	}
	
}
