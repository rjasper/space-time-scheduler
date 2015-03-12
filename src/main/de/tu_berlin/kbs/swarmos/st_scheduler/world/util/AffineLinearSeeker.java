package de.tu_berlin.kbs.swarmos.st_scheduler.world.util;

import java.util.function.Function;

// TODO document
public class AffineLinearSeeker<
	P extends Comparable<? super P>,
	T>
extends AbstractSeeker<P, T>
{
	
	private int lastIndex = 0;
	
	private int currIndex;
	
	public AffineLinearSeeker(
		Function<Integer, ? extends T> accessor,
		Function<? super T, ? extends P> positionMapper,
		int size)
	{
		super(accessor, positionMapper, size);
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
		if (position.compareTo(currPosition) < 0) {
			do {
				if (currIndex == 0)
					throw new IllegalArgumentException("position too small");
				
				currPosition = position(--currIndex);
			} while (position.compareTo(currPosition) < 0);
			
			// position >= currPosition
			
			return currIndex;
		// seek forward
		} else { // position >= currPosition
			int n = size();
			
			do {
				if (currPosition.compareTo(position) == 0)
					return currIndex;
				if (currIndex == n-1)
					throw new IllegalArgumentException("position too big");
				
				currPosition = position(++currIndex);
			} while (position.compareTo(currPosition) >= 0);
			
			// position < currPosition
			
			return --currIndex;
		}
	}

}
