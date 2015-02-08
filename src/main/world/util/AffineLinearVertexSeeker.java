package world.util;

import java.util.function.Function;

import world.PointPath;

// TODO document
// TODO test
public class AffineLinearVertexSeeker<
	V extends PointPath.Vertex,
	P extends PointPath<V, ?>>
extends AbstractVertexSeeker<V, P>
{
	
	private int lastIndex = 0;
	
	private int currIndex;
	
	public AffineLinearVertexSeeker(P path, Function<V, Double> positionMapper) {
		super(path, positionMapper);
	}

	@Override
	public V seekFloor(double position) {
		if (getPath().isEmpty())
			throw new IllegalArgumentException("path is empty");
		if (!Double.isFinite(position))
			throw new IllegalArgumentException("position is not finite");
		
		return getPath().getVertex(seekIndex(position));
	}
	
	@Override
	public V seekCeiling(double position) {
		if (getPath().isEmpty())
			throw new IllegalArgumentException("path is empty");
		if (!Double.isFinite(position))
			throw new IllegalArgumentException("position is not finite");
		
		int index = seekIndex(position);
		
		if (position(index) == position)
			return getPath().getVertex(index);
		else
			return getPath().getVertex(index+1);
	}
	
	private int seekIndex(double position) {
		P path = getPath();
		
		currIndex = lastIndex;
		
		double currPosition = position(currIndex);
		
		// seek backward
		if (position < currPosition) {
			do {
				if (currIndex == 0)
					throw new IllegalArgumentException("position too small");
				
				currPosition = position(--currIndex);
			} while (position < currPosition);
			
			// position >= currPosition
			
			return currIndex;
		// seek forward
		} else { // position >= currPosition
			int n = path.size();
			
			do {
				if (currPosition == position)
					return currIndex;
				if (currIndex == n-1)
					throw new IllegalArgumentException("position too big");
				
				currPosition = position(++currIndex);
			} while (position >= currPosition);
			
			// position < currPosition
			
			return --currIndex;
		}
	}

}
