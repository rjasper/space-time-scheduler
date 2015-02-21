package scheduler.util;

import static util.Comparables.*;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleIntervalSet<T extends Comparable<? super T>>
implements ModifiableIntervalSet<T>, Cloneable
{
	
	protected NavigableMap<T, Interval<T>> intervals = new TreeMap<>();
	
	protected boolean sealed = false;
	
	public SimpleIntervalSet() {}
	
	private SimpleIntervalSet(NavigableMap<T, Interval<T>> intervals, boolean sealed) {
		this.intervals = intervals;
		this.sealed = sealed;
	}

	public boolean isSealed() {
		return sealed;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return intervals.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#contains(T)
	 */
	@Override
	public boolean contains(T obj) {
		Objects.requireNonNull(obj, "obj");
		
		Entry<T, Interval<T>> entry = intervals.floorEntry(obj);
		
		return entry != null && entry.getValue().getToExclusive().compareTo(obj) > 0;
	}

	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#intersects(T, T)
	 */
	@Override
	public boolean intersects(T fromInclusive, T toExclusive) {
		checkInterval(fromInclusive, toExclusive);
		
		// short cut if intervals don't overlap
		if (!overlaps(fromInclusive, toExclusive))
			return false;
		
		Entry<T, Interval<T>> entry = intervals.lowerEntry(toExclusive);
		
		return entry != null && entry.getValue().getToExclusive().compareTo(fromInclusive) > 0;
	}

	/* (non-Javadoc)
		 * @see scheduler.util.IntervalSet#intersects(scheduler.util.SimpleIntervalSet)
		 */
		@Override
		public boolean intersects(IntervalSet<T> other) {
			Objects.requireNonNull(other, "other");
			
			// short cut if intervals don't overlap
			if (!overlaps(other))
				return false;
			
			// checks only relevant segments
			
			return makeOverlappingSubSet(other).stream()
				.anyMatch(i -> intersects(i.getFromInclusive(), i.getToExclusive()));
			
	//		T min = minValue(), max = maxValue();
	//		Interval<T> floorInterval = other.floorInterval(min);
	//		
	////		T floorMin = other.intervals.floorKey(min);
	//		T floorMin = floorInterval == null ? min : floorInterval.getFromInclusive();
	//		
	////		return other.intervals.subMap(floorMin, max).entrySet().stream()
	////			.anyMatch(e -> intersects(e.getKey(), e.getValue().getToExclusive()));
	//		
	//		return other.subSet(floorMin, max).stream()
	//			.anyMatch(i -> intersects(i.getFromInclusive(), i.getToExclusive()));
			
	//		T min = other.minValue(), max = other.maxValue();
	//		T floorMin = intervals.floorKey(min);
	//		
	//		if (floorMin == null)
	//			floorMin = min;
	//		
	//		return intervals.subMap(floorMin, max).entrySet().stream()
	//			.anyMatch(e -> intersects(e.getKey(), e.getValue().getToExclusive()));
		}

	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#minValue()
	 */
	@Override
	public T minValue() {
		if (isEmpty())
			throw new IllegalStateException("set is empty");
		
		return intervals.firstKey();
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#maxValue()
	 */
	@Override
	public T maxValue() {
		if (isEmpty())
			throw new IllegalStateException("set is empty");
		
		return intervals.lastEntry().getValue().getToExclusive();
	}
	
	@Override
	public Interval<T> floorInterval(T obj) {
		Entry<T, Interval<T>> entry = intervals.floorEntry(obj);
		
		return entry == null ? null : entry.getValue();
	}

	@Override
	public Interval<T> ceilingInterval(T obj) {
		Entry<T, Interval<T>> entry = intervals.ceilingEntry(obj);
		
		return entry == null ? null : entry.getValue();
	}

	@Override
	public void forEach(Consumer<Interval<T>> consumer) {
		intervals.values().forEach(consumer);
	}

	@Override
	public Stream<Interval<T>> stream() {
		return intervals.values().stream();
	}

	@Override
	public ModifiableIntervalSet<T> subSet(T fromInclusive, T toExclusive) {
		return new SubSet(fromInclusive, toExclusive);
	}
	
	private class SubSet extends SimpleIntervalSet<T> {
		
		public SubSet(T fromInclusive, T toExclusive) {
			super(
				(NavigableMap<T, Interval<T>>)
				SimpleIntervalSet.this.intervals.subMap(fromInclusive, toExclusive),
				SimpleIntervalSet.this.sealed);
		}

		@Override
		public boolean isSealed() {
			return SimpleIntervalSet.this.sealed || this.sealed;
		}
		
	}

	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#clear()
	 */
	@Override
	public void clear() {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		intervals.clear();
	}
	
	private void put(T fromInclusive, T toExclusive) {
		intervals.put(fromInclusive, new Interval<>(fromInclusive, toExclusive));
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#add(T, T)
	 */
	@Override
	public void add(T fromInclusive, T toExclusive) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		checkInterval(fromInclusive, toExclusive);
		
		addImpl(fromInclusive, toExclusive);
	}
	
	private void addImpl(T fromInclusive, T toExclusive) {
		// short cut if this interval doesn't overlap non-strictly
		if (!overlapsNonStrict(fromInclusive, toExclusive)) {
			put(fromInclusive, toExclusive);
			return;
		}
		
		// short cut if [from, to] includes this interval
		if (includedBy(fromInclusive, toExclusive)) {
			intervals.clear();
			put(fromInclusive, toExclusive);
			return;
		}
		
		// from <= core.from <= to
		NavigableMap<T, Interval<T>> core =
			intervals.subMap(fromInclusive, true, toExclusive, true);

		// short cut if interval is already included
		if (core.size() == 1) {
			Entry<T, Interval<T>> coreOnly = core.firstEntry();
			
			// tailOnly.from == from && tailOnly.to == to
			if (coreOnly.getKey()  .compareTo(fromInclusive) == 0 &&
				coreOnly.getValue().getToExclusive().compareTo(toExclusive) == 0)
			{
				return;
			}
		}
		
		// leftNeighbor.from < from
		Entry<T, Interval<T>> leftNeighborEntry =
			intervals.lowerEntry(fromInclusive);
		// last.from <= to
		Entry<T, Interval<T>> lastEntry = core.isEmpty()
			? leftNeighborEntry
			: core.lastEntry();
		
		// determine boundaries

		// leftNeighbor.to >= from
		boolean includeLeftNeighbor =
			leftNeighborEntry != null &&
			leftNeighborEntry.getValue().getToExclusive().compareTo(fromInclusive) >= 0;
		// last.to > to
		boolean includeRightNeighbor =
			lastEntry != null &&
			lastEntry.getValue().getToExclusive().compareTo(toExclusive) > 0;
		
		T left = includeLeftNeighbor
			? leftNeighborEntry.getKey() // leftNeighbor.from
			: fromInclusive;
		T right = includeRightNeighbor
			? lastEntry.getValue().getToExclusive() // last.to
			: toExclusive;
		
		// remove left neighbor if used and core intervals
		
		if (includeLeftNeighbor)
			intervals.remove(left);
		
		core.clear();
		
		// add new interval
		
		put(left, right);
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#add(scheduler.util.IntervalSet)
	 */
	@Override
	public void add(IntervalSet<T> other) {
		Objects.requireNonNull(other, "other");
		
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		if (other == this)
			return;
		else
			other.forEach(i -> addImpl(i.getFromInclusive(), i.getToExclusive()));
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#remove(T, T)
	 */
	@Override
	public void remove(T fromInclusive, T toExclusive) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		checkInterval(fromInclusive, toExclusive);
		
		removeImpl(fromInclusive, toExclusive);
	}
	
	private void removeImpl(T fromInclusive, T toExclusive) {
		// short cut if this interval doesn't overlap
		if (!overlaps(fromInclusive, toExclusive))
			return;
		
		RelevantEntries<T> re = determineRelevantEntries(fromInclusive, toExclusive);
		
		// remove core
		
		re.core.clear();
		
		// add cut left and right neighbors
		
		if (re.leftNeighbor != null)
			// (leftNeighbor.from, from)
			put(re.leftNeighbor.getKey(), fromInclusive);
		if (re.rightNeighbor != null)
			// (to, last.to)
			put(toExclusive, re.rightNeighbor.getValue().getToExclusive());
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#remove(scheduler.util.IntervalSet)
	 */
	@Override
	public void remove(IntervalSet<T> other) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		Objects.requireNonNull(other, "other");
		
		if (other == this) {
			intervals.clear();
		} else {
			// only regard relevant intervals
			
//			T min = minValue(), max = maxValue();
//			T floorMin = other.intervals.floorKey(min);
//			
//			if (floorMin == null)
//				floorMin = min;
//			
//			other.intervals.subMap(floorMin, max)
//				.forEach(this::removeImpl);

			makeOverlappingSubSet(other)
				.forEach(i -> removeImpl(i.getFromInclusive(), i.getToExclusive()));
			
//			T min = other.minValue(), max = other.maxValue();
//			T floorMin = intervals.floorKey(min);
//			
//			if (floorMin == null)
//				floorMin = min;
//			
//			intervals.subMap(floorMin, max)
//				.forEach(this::removeImpl);
		}
	}

	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#intersect(T, T)
	 */
	@Override
	public void intersect(T fromInclusive, T toExclusive) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		checkInterval(fromInclusive, toExclusive);
		
		intersectImpl(fromInclusive, toExclusive);
	}
	
	public void intersectImpl(T fromInclusive, T toExclusive) {
		// short cut if this interval doesn't overlap
		if (!overlaps(fromInclusive, toExclusive)) {
			intervals.clear();
			return;
		}
		
		// short cut if this interval is included
		if (includedBy(fromInclusive, toExclusive))
			return;
		
		// leftMap.from < from
		NavigableMap<T, Interval<T>> leftMap = intervals.headMap(fromInclusive, false);
		// rightMap.from >= to
		NavigableMap<T, Interval<T>> rightMap = intervals.tailMap(toExclusive, true);

		// leftNeighbor.from < from
		Entry<T, Interval<T>> leftNeighborEntry =
			leftMap.lastEntry();
		// lastEntry.from < to
		Entry<T, Interval<T>> lastEntry =
			intervals.lowerEntry(toExclusive);

		// leftNeighbor.to > from
		boolean includeLeftNeighbor =
			leftNeighborEntry != null &&
			leftNeighborEntry.getValue().getToExclusive().compareTo(fromInclusive) > 0;
		// last.to > to
		boolean cutRightNeighbor =
			lastEntry != null &&
			lastEntry.getValue().getToExclusive().compareTo(toExclusive) > 0;
		
		// remove non-intersecting regions
		
		leftMap.clear();
		rightMap.clear();
		
		// include or cut neighbors if necessary
		
		if (includeLeftNeighbor)
			put(fromInclusive, min(leftNeighborEntry.getValue().getToExclusive(), toExclusive));
		if (cutRightNeighbor)
			put(max(lastEntry.getKey(), fromInclusive), toExclusive);
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#intersect(scheduler.util.IntervalSet)
	 */
	@Override
	public void intersect(IntervalSet<T> other) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		// short cut if other is this
		if (other == this)
			return;
		
		intervals = intersection(other).intervals;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#seal()
	 */
	@Override
	public void seal() {
		if (isSealed())
			throw new IllegalStateException("interval already sealed");
		
		sealed = true;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#union(T, T)
	 */
	@Override
	public SimpleIntervalSet<T> union(T fromInclusive, T toExclusive) {
		// no checks since it delegates to public method
		
		SimpleIntervalSet<T> clone = this.clone();
		clone.add(fromInclusive, toExclusive);
		
		return clone;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#union(scheduler.util.SimpleIntervalSet)
	 */
	@Override
	public SimpleIntervalSet<T> union(IntervalSet<T> other) {
		// no checks since it delegates to public method
		
		SimpleIntervalSet<T> clone = this.clone();
		
		// short cut if other is this
		if (other == this)
			return clone;
		
		clone.add(other);
		
		return clone;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#difference(T, T)
	 */
	@Override
	public SimpleIntervalSet<T> difference(T fromInclusive, T toExclusive) {
		// no checks since it delegates to public method
		
		SimpleIntervalSet<T> clone = this.clone();
		clone.remove(fromInclusive, toExclusive);
		
		return clone;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#difference(scheduler.util.SimpleIntervalSet)
	 */
	@Override
	public SimpleIntervalSet<T> difference(IntervalSet<T> other) {
		// no checks since it delegates to public method
		
		SimpleIntervalSet<T> clone = this.clone();
		clone.remove(other);
		
		return clone;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.IntervalSet#intersection(T, T)
	 */
	@Override
	public SimpleIntervalSet<T> intersection(T fromInclusive, T toExclusive) {
		checkInterval(fromInclusive, toExclusive);

		// short cut if this interval doesn't overlap
		if (!overlaps(fromInclusive, toExclusive))
			return new SimpleIntervalSet<>();
		
		// short cut if this interval is included
		if (includedBy(fromInclusive, toExclusive))
			return clone();
		
		RelevantEntries<T> re = determineRelevantEntries(fromInclusive, toExclusive);
		
		SimpleIntervalSet<T> intersection = new SimpleIntervalSet<>();
		
		// include core
		intersection.intervals.putAll(re.core);
		
		// include left neighbor or cut right neighbor if necessary
		if (re.leftNeighbor != null)
			intersection.put(
				fromInclusive, min(re.leftNeighbor.getValue().getToExclusive(), toExclusive));
		if (re.rightNeighbor != null)
			intersection.put(
				max(re.rightNeighbor.getKey(), fromInclusive), toExclusive);
		
		return intersection;
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
		Objects.requireNonNull(other, "other");
		
		// short cut if intervals don't overlap
		if (!overlaps(other))
			return new SimpleIntervalSet<>();

		// regard only relevant intervals
		
//		T min = minValue(), max = maxValue();
//		T floorMin = other.intervals.floorKey(min);
//		
//		if (floorMin == null)
//			floorMin = min;
//		
//		return other.intervals.subMap(floorMin, max).entrySet().stream()
//			.map(e -> intersection(e.getKey(), e.getValue().getToExclusive()))
//			.reduce((lhs, rhs) -> { lhs.add(rhs); return lhs; })
//			.orElse(new SimpleIntervalSet<>());
		
		return makeOverlappingSubSet(other).stream()
			.map(i -> intersection(i.getFromInclusive(), i.getToExclusive()))
			.reduce((lhs, rhs) -> { lhs.add(rhs); return lhs; })
			.orElse(new SimpleIntervalSet<>());
		
//		T min = other.minValue(), max = other.maxValue();
//		T floorMin = intervals.floorKey(min);
//		
//		if (floorMin == null)
//			floorMin = min;
//		
//		return intervals.subMap(floorMin, max).entrySet().stream()
//			.map(e -> intersection(e.getKey(), e.getValue().getToExclusive()))
//			.reduce((lhs, rhs) -> { lhs.add(rhs); return lhs; })
//			.orElse(new SimpleIntervalSet<>());
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
	
	private static class RelevantEntries<T> {
		public final Entry<T, Interval<T>> leftNeighbor;
		public final Entry<T, Interval<T>> rightNeighbor;
		public final NavigableMap<T, Interval<T>> core;
		
		private RelevantEntries(
			Entry<T, Interval<T>> leftNeighbor,
			Entry<T, Interval<T>> rightNeighbor,
			NavigableMap<T, Interval<T>> core)
		{
			this.leftNeighbor = leftNeighbor;
			this.rightNeighbor = rightNeighbor;
			this.core = core;
		}
	}
	
	private RelevantEntries<T> determineRelevantEntries(
		T fromInclusive, T toExclusive)
	{
		// from <= core.from < to
		NavigableMap<T, Interval<T>> core =
			intervals.subMap(fromInclusive, true, toExclusive, false);

		// leftNeighbor.from < from
		Entry<T, Interval<T>> leftNeighbor =
			intervals.lowerEntry(fromInclusive);
		// lastEntry.from < to
		Entry<T, Interval<T>> rightNeighbor = core.isEmpty()
			? leftNeighbor
			: core.lastEntry();

		// leftNeighbor.to > from
		boolean includeLeftNeighbor =
			leftNeighbor != null &&
			leftNeighbor.getValue().getToExclusive().compareTo(fromInclusive) > 0;
		// last.to > to
		boolean includeRightNeighbor =
			rightNeighbor != null &&
			rightNeighbor.getValue().getToExclusive().compareTo(toExclusive) > 0;
		
		return new RelevantEntries<>(
			includeLeftNeighbor  ? leftNeighbor  : null,
			includeRightNeighbor ? rightNeighbor : null,
			core);
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
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		SimpleIntervalSet<T> other = (SimpleIntervalSet<T>) obj;
		if (intervals == null) {
			if (other.intervals != null)
				return false;
		} else if (!intervals.equals(other.intervals))
			return false;
		return true;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public SimpleIntervalSet<T> clone() {
		SimpleIntervalSet<T> clone;
		try {
			clone = (SimpleIntervalSet<T>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException();
		}
		
		clone.intervals = new TreeMap<>(intervals);
		
		return clone;
	}
	
	public List<Interval<T>> toList() {
		return this.intervals.entrySet().stream()
			.map(e -> new Interval<>(e.getKey(), e.getValue().getToExclusive()))
			.collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return intervals.toString();
	}

}
