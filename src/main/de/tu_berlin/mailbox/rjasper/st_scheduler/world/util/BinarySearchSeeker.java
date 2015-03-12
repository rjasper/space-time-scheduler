package de.tu_berlin.mailbox.rjasper.st_scheduler.world.util;

import java.util.function.Function;

// TODO document
public class BinarySearchSeeker<P extends Comparable<? super P>, T>
extends AbstractSeeker<P, T>
{
	
	public BinarySearchSeeker(
		Function<Integer, ? extends T> accessor,
		Function<? super T, ? extends P> positionMapper,
		int size)
	{
		super(accessor, positionMapper, size);
	}

	@Override
	public int seekFloorImpl(P position) {
		BinarySearch bs = new BinarySearch();
		bs.search(position);
		int index = bs.getFloor();
		
		return index;
	}

	@Override
	public int seekCeilingImpl(P position) {
		BinarySearch bs = new BinarySearch();
		bs.search(position);
		int index = bs.getCeiling();
		
		return index;
	}
	
	private class BinarySearch {
		private int low = 0;
		private int high = size()-1;
		
		public void search(P position) {
			// short cut if left
			if (position(low).compareTo(position) == 0)
				high = low;
			// short cut if right
			else if (position(high).compareTo(position) == 0)
				low = high;
			// regular case
			else while (low <= high) {
				int mid = (low + high) / 2;
				P testPos = position(mid);
				
				if (testPos.compareTo(position) < 0) {
					low = mid+1;
				} else if (testPos.compareTo(position) > 0) {
					high = mid-1;
				} else { // testPos == position
					low = mid;
					high = mid;
					break;
				}
			}
			
			// [high, low] encapsulate the desired position as tight as possible
		}
		
		public int getFloor() {
			return high;
		}
		
		public int getCeiling() {
			return low;
		}
	}

}
