package scheduler.util;

import static util.Comparables.*;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.stream.Stream;

public class SimpleIntervalSet<T extends Comparable<? super T>>
extends AbstractIntervalSet<T>
implements ModifiableIntervalSet<T>
{
	
	protected NavigableMap<T, Interval<T>> intervals;
	
	protected boolean sealed;
	
	public SimpleIntervalSet() {
		this(new TreeMap<>(), false);
	}
	
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
	
	@Override
	public Interval<T> minInterval() {
		return isEmpty() ? null : intervals.firstEntry().getValue();
	}

	@Override
	public Interval<T> maxInterval() {
		return isEmpty() ? null : intervals.lastEntry().getValue();
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
	public Interval<T> lowerInterval(T obj) {
		Entry<T, Interval<T>> entry = intervals.lowerEntry(obj);
		
		return entry == null ? null : entry.getValue();
	}

	@Override
	public Interval<T> higherInterval(T obj) {
		Entry<T, Interval<T>> entry = intervals.higherEntry(obj);
		
		return entry == null ? null : entry.getValue();
	}

	@Override
	public Iterator<Interval<T>> iterator() {
		return intervals.values().iterator();
	}
	
	@Override
	public Spliterator<Interval<T>> spliterator() {
		return intervals.values().spliterator();
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
				makeSubMap(SimpleIntervalSet.this, fromInclusive, toExclusive),
				SimpleIntervalSet.this.isSealed());
		}

		@Override
		public boolean isSealed() {
			return this.sealed || SimpleIntervalSet.this.isSealed();
		}
		
	}
	
	private static <T extends Comparable<? super T>>
	NavigableMap<T, Interval<T>> makeSubMap(
		SimpleIntervalSet<T> self,
		T fromInclusive, T toExclusive)
	{
		Interval<T> lowerInterval = self.lowerInterval(fromInclusive);
		T from;

		if (lowerInterval != null && lowerInterval.getToExclusive().compareTo(fromInclusive) > 0)
			from = lowerInterval.getFromInclusive();
		else
			from = fromInclusive;
		
		return (NavigableMap<T, Interval<T>>) self.intervals.subMap(from, toExclusive);
	}

	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#clear()
	 */
	@Override
	public SimpleIntervalSet<T> clear() {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		intervals.clear();
		
		return this;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#seal()
	 */
	@Override
	public SimpleIntervalSet<T> seal() {
		if (isSealed())
			throw new IllegalStateException("interval already sealed");
		
		sealed = true;
		
		return this;
	}

	private void put(T fromInclusive, T toExclusive) {
		intervals.put(fromInclusive, new Interval<>(fromInclusive, toExclusive));
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#add(T, T)
	 */
	@Override
	public SimpleIntervalSet<T> add(T fromInclusive, T toExclusive) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		checkInterval(fromInclusive, toExclusive);
		
		addImpl(fromInclusive, toExclusive);
		
		return this;
	}
	
	protected void addImpl(T fromInclusive, T toExclusive) {
		// short cut
		if (!overlapsNonStrict(fromInclusive, toExclusive)) {
			put(fromInclusive, toExclusive);
			return;
		}
		
		// short cut
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
			Interval<T> coreOnly = core.firstEntry().getValue();
			
			// coreOnly.from == from && coreOnly.to == to
			if (coreOnly.getFromInclusive().compareTo(fromInclusive) == 0 &&
				coreOnly.getToExclusive  ().compareTo(toExclusive  ) == 0)
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
	public SimpleIntervalSet<T> add(IntervalSet<T> other) {
		Objects.requireNonNull(other, "other");
		
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		if (other != this)
			other.forEach(i -> addImpl(i.getFromInclusive(), i.getToExclusive()));
		
		return this;
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#remove(T, T)
	 */
	@Override
	public SimpleIntervalSet<T> remove(T fromInclusive, T toExclusive) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		checkInterval(fromInclusive, toExclusive);
		
		removeImpl(fromInclusive, toExclusive);
		
		return this;
	}
	
	protected void removeImpl(T fromInclusive, T toExclusive) {
		// short cut if this interval doesn't overlap
		if (!overlaps(fromInclusive, toExclusive))
			return;
		
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
		
		// remove core
		
		core.clear();
		
		// add cut left and right neighbors
		
		if (includeLeftNeighbor)
			// (leftNeighbor.from, from)
			put(leftNeighbor.getKey(), fromInclusive);
		if (includeRightNeighbor)
			// (to, last.to)
			put(toExclusive, rightNeighbor.getValue().getToExclusive());
	}
	
	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#remove(scheduler.util.IntervalSet)
	 */
	@Override
	public SimpleIntervalSet<T> remove(IntervalSet<T> other) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		Objects.requireNonNull(other, "other");
		
		if (other == this) {
			intervals.clear();
		} else if (!other.isEmpty()){
			makeOverlappingSubSet(other).forEach(i ->
				removeImpl(i.getFromInclusive(), i.getToExclusive()));
		}
		
		return this;
	}

	/* (non-Javadoc)
	 * @see scheduler.util.ModifiableIntervalSet#intersect(T, T)
	 */
	@Override
	public SimpleIntervalSet<T> intersect(T fromInclusive, T toExclusive) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		checkInterval(fromInclusive, toExclusive);
		
		intersectImpl(fromInclusive, toExclusive);
		
		return this;
	}
	
	protected void intersectImpl(T fromInclusive, T toExclusive) {
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
	public SimpleIntervalSet<T> intersect(IntervalSet<T> other) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		// short cut if other is this
		if (other == this)
			return this;
		
		Optional<SimpleIntervalSet<T>> option = makeOverlappingSubSet(other).stream()
			.map(i -> {
				T from = i.getFromInclusive();
				T to = i.getToExclusive();
				
				SimpleIntervalSet<T> set = new SimpleIntervalSet<>();
				
				set.add(subSet(from, to));
				set.intersectImpl(from, to);
				
				return set;
			})
			.reduce((lhs, rhs) -> { lhs.add(rhs); return lhs; });
		
		if (option.isPresent())
			intervals = option.get().intervals;
		else
			intervals.clear();
		
		return this;
	}

}
