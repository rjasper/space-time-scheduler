package scheduler.util;

public interface ModifiableIntervalSet<T extends Comparable<? super T>>
extends IntervalSet<T>
{

	public abstract void clear();

	public abstract void add(T fromInclusive, T toExclusive);

	public abstract void add(IntervalSet<T> other);

	public abstract void remove(T fromInclusive, T toExclusive);

	public abstract void remove(IntervalSet<T> other);

	public abstract void intersect(T fromInclusive, T toExclusive);

	public abstract void intersect(IntervalSet<T> other);

	public abstract void seal();

}