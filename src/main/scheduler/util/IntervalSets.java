package scheduler.util;

import static java.util.Collections.*;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public final class IntervalSets {
	
	private IntervalSets() {}
	
	public static <T extends Comparable<? super T>> IntervalSet<T>
	unmodifiableIntervalSet(IntervalSet<T> intervalSet) {
		if (intervalSet instanceof UnmodifiableIntervalSet<?>)
			return intervalSet;
		if (intervalSet instanceof EmptyIntervalSet)
			return emptyIntervalSet();
		else
			return new UnmodifiableIntervalSet<>(intervalSet);
	}
	
	private static class UnmodifiableIntervalSet<T extends Comparable<? super T>>
	extends AbstractIntervalSet<T>
	{
		private final IntervalSet<T> intervalSet;

		public UnmodifiableIntervalSet(IntervalSet<T> intervalSet) {
			this.intervalSet = intervalSet;
		}

		@Override
		public Iterator<Interval<T>> iterator() {
			return intervalSet.iterator();
		}

		@Override
		public boolean isEmpty() {
			return intervalSet.isEmpty();
		}

		@Override
		public Interval<T> minInterval() {
			return intervalSet.minInterval();
		}

		@Override
		public Interval<T> maxInterval() {
			return intervalSet.maxInterval();
		}

		@Override
		public Interval<T> floorInterval(T obj) {
			return intervalSet.floorInterval(obj);
		}

		@Override
		public Interval<T> ceilingInterval(T obj) {
			return intervalSet.ceilingInterval(obj);
		}

		@Override
		public Interval<T> lowerInterval(T obj) {
			return intervalSet.lowerInterval(obj);
		}

		@Override
		public Interval<T> higherInterval(T obj) {
			return intervalSet.higherInterval(obj);
		}

		@Override
		public IntervalSet<T> subSet(T fromInclusive, T toExclusive) {
			return unmodifiableIntervalSet(intervalSet.subSet(fromInclusive, toExclusive));
		}
		
	}
	
	private static final IntervalSet<?> EMPTY = new EmptyIntervalSet();
	
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<? super T>> IntervalSet<T>
	emptyIntervalSet() {
		return (IntervalSet<T>) EMPTY;
	}
	
	private static class EmptyIntervalSet implements IntervalSet<Comparable<Object>> {

		@Override
		public Iterator<Interval<Comparable<Object>>> iterator() {
			return new Iterator<Interval<Comparable<Object>>>() {
				@Override
				public boolean hasNext() {
					return false;
				}
				@Override
				public scheduler.util.IntervalSet.Interval<Comparable<Object>> next() {
					throw new NoSuchElementException("set is empty");
				}
			};
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public Interval<Comparable<Object>> minInterval() {
			throw new IllegalStateException("set is empty");
		}

		@Override
		public Interval<Comparable<Object>> maxInterval() {
			throw new IllegalStateException("set is empty");
		}

		@Override
		public Interval<Comparable<Object>> floorInterval(Comparable<Object> obj) {
			throw new IllegalStateException("set is empty");
		}

		@Override
		public Interval<Comparable<Object>> ceilingInterval(Comparable<Object> obj) {
			throw new IllegalStateException("set is empty");
		}

		@Override
		public Interval<Comparable<Object>> lowerInterval(Comparable<Object> obj) {
			throw new IllegalStateException("set is empty");
		}

		@Override
		public Interval<Comparable<Object>> higherInterval(Comparable<Object> obj) {
			throw new IllegalStateException("set is empty");
		}

		@Override
		public Comparable<Object> minValue() {
			throw new IllegalStateException("set is empty");
		}

		@Override
		public Comparable<Object> maxValue() {
			throw new IllegalStateException("set is empty");
		}

		@Override
		public boolean contains(Comparable<Object> obj) {
			return false;
		}

		@Override
		public boolean intersects(Comparable<Object> fromInclusive, Comparable<Object> toExclusive) {
			return false;
		}

		@Override
		public boolean includedBy(Comparable<Object> fromInclusive, Comparable<Object> toInclusive) {
			return true;
		}

		@Override
		public boolean overlaps(Comparable<Object> fromInclusive, Comparable<Object> toExclusive) {
			return false;
		}

		@Override
		public boolean overlapsNonStrict(Comparable<Object> fromInclusive, Comparable<Object> toInclusive) {
			return false;
		}

		@Override
		public boolean overlaps(IntervalSet<Comparable<Object>> other) {
			return false;
		}

		@Override
		public boolean intersects(IntervalSet<Comparable<Object>> other) {
			return false;
		}

		@Override
		public IntervalSet<Comparable<Object>> union(Comparable<Object> fromInclusive, Comparable<Object> toExclusive) {
			return new SimpleIntervalSet<Comparable<Object>>().add(fromInclusive, toExclusive);
		}

		@Override
		public IntervalSet<Comparable<Object>> union(IntervalSet<Comparable<Object>> other) {
			return new SimpleIntervalSet<Comparable<Object>>().add(other);
		}

		@Override
		public IntervalSet<Comparable<Object>> difference(Comparable<Object> fromInclusive, Comparable<Object> toExclusive) {
			return this;
		}

		@Override
		public IntervalSet<Comparable<Object>> difference(IntervalSet<Comparable<Object>> other) {
			return this;
		}

		@Override
		public IntervalSet<Comparable<Object>> intersection(Comparable<Object> fromInclusive, Comparable<Object> toExclusive) {
			return this;
		}

		@Override
		public IntervalSet<Comparable<Object>> intersection(IntervalSet<Comparable<Object>> other) {
			return this;
		}

		@Override
		public IntervalSet<Comparable<Object>> subSet(Comparable<Object> fromInclusive, Comparable<Object> toExclusive) {
			return this;
		}

		@Override
		public Stream<Interval<Comparable<Object>>> stream() {
			return Stream.empty();
		}

		@Override
		public List<Interval<Comparable<Object>>> toList() {
			return emptyList();
		}
		
	}

}
