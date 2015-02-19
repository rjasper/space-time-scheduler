package scheduler.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TimeIntervalSet {
	
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
		Objects.requireNonNull(fromInclusive, "fromInclusive");
		Objects.requireNonNull(toExclusive, "toExclusive");
		
		Entry<LocalDateTime, LocalDateTime> entry = intervals.lowerEntry(toExclusive);
		
		return entry != null && entry.getValue().isAfter(fromInclusive);
	}
	
	public boolean intersects(TimeIntervalSet other) {
		// short cut if one set is empty
		if (isEmpty() || other.isEmpty())
			return false;
		
		LocalDateTime min = minValue(), max = maxValue();
		
		// short cut if sets don't overlap
		if (!other.minValue().isBefore(max) || // other.min >= this.max
			!other.maxValue().isAfter(min))    // other.max <= this.min
		{
			return false;
		}
		
		// checks only relevant segments
		
		LocalDateTime floorMin = other.intervals.floorKey(min);
		
		if (floorMin == null)
			floorMin = min;
		
		return other.intervals.subMap(floorMin, max).entrySet().stream()
			.anyMatch(e -> intersects(e.getKey(), e.getValue()));
	}
	
	public void add(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		Objects.requireNonNull(fromInclusive, "fromInclusive");
		Objects.requireNonNull(toExclusive, "toExclusive");
		
		if (!fromInclusive.isBefore(toExclusive))
			throw new IllegalArgumentException("invalid interval");
		
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
		if (other == this)
			return;
		else
			other.intervals.forEach(this::add);
	}
	
	public void remove(LocalDateTime fromInclusive, LocalDateTime toExclusive) {
		Objects.requireNonNull(fromInclusive, "fromInclusive");
		Objects.requireNonNull(toExclusive, "toExclusive");
		
		if (!fromInclusive.isBefore(toExclusive))
			throw new IllegalArgumentException("invalid interval");

		// from <= core.from < to
		NavigableMap<LocalDateTime, LocalDateTime> core =
			intervals.subMap(fromInclusive, true, toExclusive, false);
		
		// leftNeighbor.from < from
		Entry<LocalDateTime, LocalDateTime> leftNeighborEntry =
			intervals.lowerEntry(fromInclusive);
		// lastEntry.from < to
		Entry<LocalDateTime, LocalDateTime> lastEntry = core.isEmpty()
			? leftNeighborEntry
			: core.lastEntry();

		// leftNeighbor.to > from
		boolean cutLeftNeighbor =
			leftNeighborEntry != null &&
			leftNeighborEntry.getValue().isAfter(fromInclusive);
		// last.to > to
		boolean cutRightNeighbor =
			lastEntry != null &&
			lastEntry.getValue().isAfter(toExclusive);
		
		// remove core
		
		core.clear();
		
		// add cut left and right neighbors
		
		if (cutLeftNeighbor)
			// (leftNeighbor.from, from)
			intervals.put(leftNeighborEntry.getKey(), fromInclusive);
		if (cutRightNeighbor)
			// (to, last.to)
			intervals.put(toExclusive, lastEntry.getValue());
	}
	
	public void remove(TimeIntervalSet other) {
		if (other == this)
			intervals.clear();
		else
			other.intervals.forEach(this::remove);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((intervals == null) ? 0 : intervals.hashCode());
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
