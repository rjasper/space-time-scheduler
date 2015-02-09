package world.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

// TODO document
public class IndexSeeker<T> implements Seeker<Double, T> {
	
	private final Function<Integer, T> accessor;
	
	private final int size;

	public IndexSeeker(Function<Integer, T> accessor, int size) {
		if (size < 0)
			throw new IllegalArgumentException("size is negative");
		
		this.accessor = Objects.requireNonNull(accessor, "accessor");
		this.size = size;
	}
	
	private void checkPosition(double position) {
		if (size == 0)
			throw new NoSuchElementException("accessable is empty");
		if (Double.isNaN(position))
			throw new IllegalArgumentException("position is NaN");
		if (position < 0)
			throw new IllegalArgumentException("position too small");
		if (position >= size)
			throw new IllegalArgumentException("position too big");
	}

	@Override
	public SeekResult<Double, T> seekFloor(Double position) {
		checkPosition(position);
		
		double seekPosition = Math.floor(position);
		int index = (int) seekPosition;
		T object = accessor.apply(index);
		
		return new SeekResult<>(index, seekPosition, object);
	}

	@Override
	public SeekResult<Double, T> seekCeiling(Double position) {
		checkPosition(position);
		
		double seekPosition = Math.ceil(position);
		int index = (int) seekPosition;
		T object = accessor.apply(index);
		
		return new SeekResult<>(index, seekPosition, object);
	}

}
