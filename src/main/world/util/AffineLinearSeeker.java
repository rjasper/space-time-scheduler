package world.util;

import java.util.Comparator;
import java.util.function.Function;

// TODO document
public class AffineLinearSeeker<P, T> extends AbstractSeeker<P, T> {
	
	private int lastIndex = 0;
	
	private int currIndex;
	
	public AffineLinearSeeker(
		Function<Integer, ? extends T> accessor,
		Function<? super T, ? extends P> positionMapper,
		Comparator<? super P> comparator,
		int size)
	{
		super(accessor, positionMapper, comparator, size);
	}

	@Override
	public int seekFloorImpl(P position) {
		return seekIndex(position);
	}
	
	@Override
	public int seekCeilingImpl(P position) {
		int index = seekIndex(position);
		
		if (position(index) == position)
			return index;
		else
			return index+1;
	}
	
	private int seekIndex(P position) {
		currIndex = lastIndex;
		
		P currPosition = position(currIndex);
		
		// seek backward
		if (compare(position, currPosition) < 0) {
			do {
				if (currIndex == 0)
					throw new IllegalArgumentException("position too small");
				
				currPosition = position(--currIndex);
			} while (compare(position, currPosition) < 0);
			
			// position >= currPosition
			
			return currIndex;
		// seek forward
		} else { // position >= currPosition
			int n = size();
			
			do {
				if (compare(currPosition, position) == 0)
					return currIndex;
				if (currIndex == n-1)
					throw new IllegalArgumentException("position too big");
				
				currPosition = position(++currIndex);
			} while (compare(position, currPosition) >= 0);
			
			// position < currPosition
			
			return --currIndex;
		}
	}

}
