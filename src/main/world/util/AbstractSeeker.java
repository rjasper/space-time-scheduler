package world.util;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

// TODO document
public abstract class AbstractSeeker<P, T> implements Seeker<P, T> {
	
	private final Function<Integer, ? extends T> accessor;
	
	private final Function<? super T, ? extends P> positionMapper;
	
	private final Comparator<? super P> comparator;
	
	private final int size;

	public AbstractSeeker(
		Function<Integer, ? extends T> accessor,
		Function<? super T, ? extends P> positionMapper,
		Comparator<? super P> comparator,
		int size)
	{
		if (size < 0)
			throw new IllegalArgumentException("size is negative");
		
		this.accessor = Objects.requireNonNull(accessor, "accessor");
		this.positionMapper = Objects.requireNonNull(positionMapper, "positionMapper");
		this.comparator = Objects.requireNonNull(comparator, "comparator");
		this.size = size;
	}
	
	protected T get(int index) {
		return accessor.apply(index);
	}
	
	protected P position(T obj) {
		return positionMapper.apply(obj);
	}
	
	protected P position(int index) {
		return position(get(index));
	}
	
	protected int compare(P lhs, P rhs) {
		return comparator.compare(lhs, rhs);
	}

	public int size() {
		return size;
	}
	
	private void checkPosition(P position) {
		Objects.requireNonNull(position, "position");
		
		if (size() == 0)
			throw new NoSuchElementException("accessable is empty");
		if (compare(position, position(0)) < 0)
			throw new IllegalArgumentException("position too small");
		if (compare(position, position(size()-1)) > 0)
			throw new IllegalArgumentException("position too big");
	}

	/* (non-Javadoc)
	 * @see world.util.Seeker#seekFloor(java.lang.Object)
	 */
	@Override
	public SeekResult<P, T> seekFloor(P position) {
		checkPosition(position);
		
		int index = seekFloorImpl(position);
		T object = get(index);
		P seekPosition = position(object);
		
		return new SeekResult<>(index, seekPosition, object);
	}

	/* (non-Javadoc)
	 * @see world.util.Seeker#seekCeiling(java.lang.Object)
	 */
	@Override
	public SeekResult<P, T> seekCeiling(P position) {
		checkPosition(position);

		int index = seekCeilingImpl(position);
		T object = get(index);
		P seekPosition = position(object);
		
		return new SeekResult<>(index, seekPosition, object);
	}
	
	protected abstract int seekFloorImpl(P position);
	
	protected abstract int seekCeilingImpl(P position);

}
