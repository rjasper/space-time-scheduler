package scheduler.util;

import static util.Comparables.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TimeIntervalSet implements Cloneable {
	
	public static class TimeInterval {
		
		private final LocalDateTime fromInclusive;
		
		private final LocalDateTime toExclusive;
	
		private TimeInterval(
			LocalDateTime fromInclusive,
			LocalDateTime toExclusive)
		{
			this.fromInclusive = fromInclusive;
			this.toExclusive = toExclusive;
		}
	
		public LocalDateTime getFromInclusive() {
			return fromInclusive;
		}
	
		public LocalDateTime getToExclusive() {
			return toExclusive;
		}
		
	}

	private TreeMap<LocalDateTime, LocalDateTime> intervals = new TreeMap<>();
	
	private boolean sealed = false;
	
	public boolean isSealed() {
		return sealed;
	}
	
	public boolean isEmpty() {
		return intervals.isEmpty();
	}
	
	public LocalDateTime minValue() {
		if (isEmpty())
			throw new IllegalStateException("set is empty");
		
		return intervals.firstKey();
	}
	
	public LocalDateTime maxValue() {
		if (isEmpty())
			throw new IllegalStateException("set is empty");
		
		return intervals.lastEntry().getValue();
	}
	
	public boolean contains(LocalDateTime time) {
		Objects.requireNonNull(time, "time");
		
		Entry<LocalDateTime, LocalDateTime> entry = intervals.floorEntry(time);
		
		return entry != null && entry.getValue().isAfter(time);
	}
	
	public boolean intersects(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		checkInterval(fromInclusive, toExclusive);
		
		// short cut if intervals don't overlap
		if (!overlaps(fromInclusive, toExclusive))
			return false;
		
		Entry<LocalDateTime, LocalDateTime> entry = intervals.lowerEntry(toExclusive);
		
		return entry != null && entry.getValue().isAfter(fromInclusive);
	}
	
	public boolean intersects(TimeIntervalSet other) {
		Objects.requireNonNull(other, "other");
		
		// short cut if intervals don't overlap
		if (!overlaps(other))
			return false;
		
		// checks only relevant segments
		
		LocalDateTime min = minValue(), max = maxValue();
		LocalDateTime floorMin = other.intervals.floorKey(min);
		
		if (floorMin == null)
			floorMin = min;
		
		return other.intervals.subMap(floorMin, max).entrySet().stream()
			.anyMatch(e -> intersects(e.getKey(), e.getValue()));
	}
	
	public void clear() {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		intervals.clear();
	}
	
	public void add(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		checkInterval(fromInclusive, toExclusive);
		
		addImpl(fromInclusive, toExclusive);
	}
	
	private void addImpl(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		// short cut if this interval doesn't overlap non-strictly
		if (!overlapsNonStrict(fromInclusive, toExclusive)) {
			intervals.put(fromInclusive, toExclusive);
			return;
		}
		
		// short cut if [from, to] includes this interval
		if (includedBy(fromInclusive, toExclusive)) {
			intervals.clear();
			intervals.put(fromInclusive, toExclusive);
			return;
		}
		
		// from <= core.from <= to
		NavigableMap<LocalDateTime, LocalDateTime> core =
			intervals.subMap(fromInclusive, true, toExclusive, true);

		// short cut if interval is already included
		if (core.size() == 1) {
			Entry<LocalDateTime, LocalDateTime> coreOnly = core.firstEntry();
			
			// tailOnly.from == from && tailOnly.to == to
			if (coreOnly.getKey()  .isEqual(fromInclusive) &&
				coreOnly.getValue().isEqual(toExclusive))
			{
				return;
			}
		}
		
		// leftNeighbor.from < from
		Entry<LocalDateTime, LocalDateTime> leftNeighborEntry =
			intervals.lowerEntry(fromInclusive);
		// last.from <= to
		Entry<LocalDateTime, LocalDateTime> lastEntry = core.isEmpty()
			? leftNeighborEntry
			: core.lastEntry();
		
		// determine boundaries

		// leftNeighbor.to >= from
		boolean includeLeftNeighbor =
			leftNeighborEntry != null &&
			!leftNeighborEntry.getValue().isBefore(fromInclusive);
		// last.to > to
		boolean includeRightNeighbor =
			lastEntry != null &&
			lastEntry.getValue().isAfter(toExclusive);
		
		LocalDateTime left = includeLeftNeighbor
			? leftNeighborEntry.getKey() // leftNeighbor.from
			: fromInclusive;
		LocalDateTime right = includeRightNeighbor
			? lastEntry.getValue() // last.to
			: toExclusive;
		
		// remove left neighbor if used and core intervals
		
		if (includeLeftNeighbor)
			intervals.remove(left);
		
		core.clear();
		
		// add new interval
		
		intervals.put(left, right);
	}
	
	public void add(TimeIntervalSet other) {
		Objects.requireNonNull(other, "other");
		
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		if (other == this)
			return;
		else
			other.intervals.forEach(this::addImpl);
	}
	
	public void remove(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		checkInterval(fromInclusive, toExclusive);
		
		removeImpl(fromInclusive, toExclusive);
	}
	
	private void removeImpl(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		// short cut if this interval doesn't overlap
		if (!overlaps(fromInclusive, toExclusive))
			return;
		
		RelevantEntries re = determineRelevantEntries(fromInclusive, toExclusive);
		
		// remove core
		
		re.core.clear();
		
		// add cut left and right neighbors
		
		if (re.leftNeighbor != null)
			// (leftNeighbor.from, from)
			intervals.put(re.leftNeighbor.getKey(), fromInclusive);
		if (re.rightNeighbor != null)
			// (to, last.to)
			intervals.put(toExclusive, re.rightNeighbor.getValue());
	}
	
	public void remove(TimeIntervalSet other) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		Objects.requireNonNull(other, "other");
		
		if (other == this) {
			intervals.clear();
		} else {
			// only regard relevant intervals
			
			LocalDateTime min = minValue(), max = maxValue();
			LocalDateTime floorMin = other.intervals.floorKey(min);
			
			if (floorMin == null)
				floorMin = min;
			
			other.intervals.subMap(floorMin, max)
				.forEach(this::removeImpl);
		}
	}

	public void intersect(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		checkInterval(fromInclusive, toExclusive);
		
		intersectImpl(fromInclusive, toExclusive);
	}
	
	public void intersectImpl(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		// short cut if this interval doesn't overlap
		if (!overlaps(fromInclusive, toExclusive)) {
			intervals.clear();
			return;
		}
		
		// short cut if this interval is included
		if (includedBy(fromInclusive, toExclusive))
			return;
		
		// leftMap.from < from
		NavigableMap<LocalDateTime, LocalDateTime> leftMap =
			intervals.subMap(LocalDateTime.MIN, true, fromInclusive, false);
		// rightMap.from >= to
		NavigableMap<LocalDateTime, LocalDateTime> rightMap =
			intervals.subMap(toExclusive, true, LocalDateTime.MAX, true);

		// leftNeighbor.from < from
		Entry<LocalDateTime, LocalDateTime> leftNeighborEntry =
			leftMap.lastEntry();
		// lastEntry.from < to
		Entry<LocalDateTime, LocalDateTime> lastEntry =
			intervals.lowerEntry(toExclusive);

		// leftNeighbor.to > from
		boolean includeLeftNeighbor =
			leftNeighborEntry != null &&
			leftNeighborEntry.getValue().isAfter(fromInclusive);
		// last.to > to
		boolean cutRightNeighbor =
			lastEntry != null &&
			lastEntry.getValue().isAfter(toExclusive);
		
		// remove non-intersecting regions
		
		leftMap.clear();
		rightMap.clear();
		
		// include or cut neighbors if necessary
		
		if (includeLeftNeighbor)
			intervals.put(fromInclusive, min(leftNeighborEntry.getValue(), toExclusive));
		if (cutRightNeighbor)
			intervals.put(max(lastEntry.getKey(), fromInclusive), toExclusive);
	}
	
	public void intersect(TimeIntervalSet other) {
		if (isSealed())
			throw new IllegalStateException("interval is sealed");
		
		// short cut if other is this
		if (other == this)
			return;
		
		intervals = intersection(other).intervals;
	}
	
	public void seal() {
		if (isSealed())
			throw new IllegalStateException("interval already sealed");
		
		sealed = true;
	}
	
	public TimeIntervalSet union(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		// no checks since it delegates to public method
		
		TimeIntervalSet clone = this.clone();
		clone.add(fromInclusive, toExclusive);
		
		return clone;
	}
	
	public TimeIntervalSet union(TimeIntervalSet other) {
		// no checks since it delegates to public method
		
		TimeIntervalSet clone = this.clone();
		
		// short cut if other is this
		if (other == this)
			return clone;
		
		clone.add(other);
		
		return clone;
	}
	
	public TimeIntervalSet difference(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		// no checks since it delegates to public method
		
		TimeIntervalSet clone = this.clone();
		clone.remove(fromInclusive, toExclusive);
		
		return clone;
	}
	
	public TimeIntervalSet difference(TimeIntervalSet other) {
		// no checks since it delegates to public method
		
		TimeIntervalSet clone = this.clone();
		clone.remove(other);
		
		return clone;
	}
	
	public TimeIntervalSet intersection(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		checkInterval(fromInclusive, toExclusive);

		// short cut if this interval doesn't overlap
		if (!overlaps(fromInclusive, toExclusive))
			return new TimeIntervalSet();
		
		// short cut if this interval is included
		if (includedBy(fromInclusive, toExclusive))
			return clone();
		
		RelevantEntries re = determineRelevantEntries(fromInclusive, toExclusive);
		
		TimeIntervalSet intersection = new TimeIntervalSet();
		
		// include core
		intersection.intervals.putAll(re.core);
		
		// include left neighbor or cut right neighbor if necessary
		if (re.leftNeighbor != null)
			intersection.intervals.put(
				fromInclusive, min(re.leftNeighbor.getValue(), toExclusive));
		if (re.rightNeighbor != null)
			intersection.intervals.put(
				max(re.rightNeighbor.getKey(), fromInclusive), toExclusive);
		
		return intersection;
	}
	
	public TimeIntervalSet intersection(TimeIntervalSet other) {
		Objects.requireNonNull(other, "other");
		
		// short cut if intervals don't overlap
		if (!overlaps(other))
			return new TimeIntervalSet();

		// regard only relevant intervals
		
		LocalDateTime min = minValue(), max = maxValue();
		LocalDateTime floorMin = other.intervals.floorKey(min);
		
		if (floorMin == null)
			floorMin = min;
		
		return other.intervals.subMap(floorMin, max).entrySet().stream()
			.map(e -> intersection(e.getKey(), e.getValue()))
			.reduce((lhs, rhs) -> { lhs.add(rhs); return lhs; })
			.orElse(new TimeIntervalSet());
	}
	
	private void checkInterval(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		Objects.requireNonNull(fromInclusive, "fromInclusive");
		Objects.requireNonNull(toExclusive, "toExclusive");
		
		if (!fromInclusive.isBefore(toExclusive))
			throw new IllegalArgumentException("invalid interval");
	}
	
	private boolean overlapsNonStrict(LocalDateTime fromInclusive, LocalDateTime toInclusive) {
		return !isEmpty() &&
			!fromInclusive.isAfter(maxValue()) && // from <= max
			!toInclusive.isBefore(minValue());    // to   >= min
	}
	
	private boolean overlaps(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		return !isEmpty() &&
			fromInclusive.isBefore(maxValue()) && // from < max
			toExclusive.isAfter(minValue());      // to   > min
	}
	
	private boolean overlaps(TimeIntervalSet other) {
		return !isEmpty() && !other.isEmpty() &&
			other.minValue().isBefore(maxValue()) && // min2 < max1
			other.maxValue().isAfter(minValue());    // max2 > min2
	}
	
	private boolean includedBy(LocalDateTime fromInclusive, LocalDateTime toInclusive) {
		return isEmpty() || (
			!fromInclusive.isAfter(minValue()) && // from <= min
			!toInclusive.isBefore(maxValue()));   // to   >= max
	}
	
	private static class RelevantEntries {
		public final Entry<LocalDateTime, LocalDateTime> leftNeighbor;
		public final Entry<LocalDateTime, LocalDateTime> rightNeighbor;
		public final NavigableMap<LocalDateTime, LocalDateTime> core;
		
		private RelevantEntries(
			Entry<LocalDateTime, LocalDateTime> leftNeighbor,
			Entry<LocalDateTime, LocalDateTime> rightNeighbor,
			NavigableMap<LocalDateTime, LocalDateTime> core)
		{
			this.leftNeighbor = leftNeighbor;
			this.rightNeighbor = rightNeighbor;
			this.core = core;
		}
	}
	
	private RelevantEntries determineRelevantEntries(
		LocalDateTime fromInclusive, LocalDateTime toExclusive)
	{
		// from <= core.from < to
		NavigableMap<LocalDateTime, LocalDateTime> core =
			intervals.subMap(fromInclusive, true, toExclusive, false);

		// leftNeighbor.from < from
		Entry<LocalDateTime, LocalDateTime> leftNeighbor =
			intervals.lowerEntry(fromInclusive);
		// lastEntry.from < to
		Entry<LocalDateTime, LocalDateTime> rightNeighbor = core.isEmpty()
			? leftNeighbor
			: core.lastEntry();

		// leftNeighbor.to > from
		boolean includeLeftNeighbor =
			leftNeighbor != null &&
			leftNeighbor.getValue().isAfter(fromInclusive);
		// last.to > to
		boolean includeRightNeighbor =
			rightNeighbor != null &&
			rightNeighbor.getValue().isAfter(toExclusive);
		
		return new RelevantEntries(
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
		TimeIntervalSet other = (TimeIntervalSet) obj;
		if (intervals == null) {
			if (other.intervals != null)
				return false;
		} else if (!intervals.equals(other.intervals))
			return false;
		return true;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public TimeIntervalSet clone() {
		TimeIntervalSet clone;
		try {
			clone = (TimeIntervalSet) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException();
		}
		
		clone.intervals = (TreeMap<LocalDateTime, LocalDateTime>) intervals.clone();
		
		return clone;
	}
	
	public List<TimeInterval> toList() {
		return this.intervals.entrySet().stream()
			.map(e -> new TimeInterval(e.getKey(), e.getValue()))
			.collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return intervals.toString();
	}

}
