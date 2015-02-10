package world.util;

import java.util.Comparator;
import java.util.function.Function;

// TODO document
public class BinarySearchSeeker<P, T>
extends AbstractSeeker<P, T>
{
	
	public BinarySearchSeeker(
		Function<Integer, ? extends T> accessor,
		Function<? super T, ? extends P> positionMapper,
		Comparator<? super P> comparator,
		int size)
	{
		super(accessor, positionMapper, comparator, size);
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
			while (low <= high) {
				int mid = (low + high) / 2;
				P testPos = position(mid);
				
				if (compare(testPos, position) < 0) {
					low = mid+1;
				} else if (compare(testPos, position) > 0) {
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
