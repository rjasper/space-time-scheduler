package world.util;

import java.util.function.Function;

import world.PointPath;

// TODO document
public class BinarySearchVertexSeeker<
	V extends PointPath.Vertex,
	P extends PointPath<V, ?>>
extends AbstractVertexSeeker<V, P>
{
	
	private final double minPosition;
	
	private final double maxPosition;

	public BinarySearchVertexSeeker(P path, Function<V, Double> positionMapper) {
		super(path, positionMapper);
		
		boolean empty = path.isEmpty();
		
		this.minPosition = empty ? Double.NaN : position(path.getFirstVertex());
		this.maxPosition = empty ? Double.NaN : position(path.getLastVertex());
	}
	
	private void checkPosition(double position) {
		if (!Double.isFinite(position))
			throw new IllegalArgumentException("position not finite");
		if (position < minPosition)
			throw new IllegalArgumentException("position too small");
		if (position > maxPosition)
			throw new IllegalArgumentException("position too big");
	}

	@Override
	public V seekFloor(double position) {
		checkPosition(position);
		
		BinarySearch bs = new BinarySearch();
		bs.search(position);
		int index = bs.getFloor();
		
		return getPath().getVertex(index);
	}

	@Override
	public V seekCeiling(double position) {
		checkPosition(position);
		
		BinarySearch bs = new BinarySearch();
		bs.search(position);
		int index = bs.getCeiling();
		
		return getPath().getVertex(index);
	}
	
	private class BinarySearch {
		private int low = 0;
		private int high = getPath().size()-1;
		
		public void search(double position) {
			while (low <= high) {
				int mid = (low + high) / 2;
				double testPos = position(mid);
				
				if (testPos < position) {
					low = mid+1;
				} else if (testPos > position) {
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
