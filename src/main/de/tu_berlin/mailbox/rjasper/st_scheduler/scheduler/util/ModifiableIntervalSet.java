package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

public interface ModifiableIntervalSet<T extends Comparable<? super T>>
extends IntervalSet<T>
{

	public abstract ModifiableIntervalSet<T> clear();

	public abstract ModifiableIntervalSet<T> add(T fromInclusive, T toExclusive);

	public abstract ModifiableIntervalSet<T> add(IntervalSet<T> other);

	public abstract ModifiableIntervalSet<T> remove(T fromInclusive, T toExclusive);

	public abstract ModifiableIntervalSet<T> remove(IntervalSet<T> other);

	public abstract ModifiableIntervalSet<T> intersect(T fromInclusive, T toExclusive);

	public abstract ModifiableIntervalSet<T> intersect(IntervalSet<T> other);

	public abstract ModifiableIntervalSet<T> seal();

}