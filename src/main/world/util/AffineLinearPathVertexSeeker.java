package world.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import world.Path;

// TODO document
// TODO test
public class AffineLinearPathVertexSeeker<
	V extends Path.Vertex,
	S extends Path.Segment<? extends V>,
	P extends Path<V, S>>

	extends AbstractPathVertexSeeker<V, S, P>
{
	
	private int lastIndex = 0;
	
	private int currIndex;
	
	private final Iterator<V> iterator;
	
	private List<V> vertices;
	
	public AffineLinearPathVertexSeeker(P path, Function<V, Double> positionMapper) {
		super(path, positionMapper);
		
		this.vertices = new ArrayList<>(path.size());
		this.iterator = path.vertexIterator();
		
		explore();
	}

	@Override
	public V seekFloor(double position) {
		if (getPath().isEmpty())
			throw new IllegalArgumentException("path is empty");
		if (!Double.isFinite(position))
			throw new IllegalArgumentException("position is not finite");
		
		return vertices.get(seekIndex(position));
	}
	
	@Override
	public V seekCeiling(double position) {
		if (getPath().isEmpty())
			throw new IllegalArgumentException("path is empty");
		if (!Double.isFinite(position))
			throw new IllegalArgumentException("position is not finite");
		
		int index = seekIndex(position);
		
		if (position(index) == position)
			return vertices.get(index);
		else
			return vertices.get(index+1);
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
				
				currPosition = backward();
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
				
				currPosition = forward();
			} while (position >= currPosition);
			
			// position < currPosition
			
			return --currIndex;
		}
	}
	
	private double position(int index) {
		return position(vertices.get(index));
	}
	
	private double explore() {
		V vertex = iterator.next();
		
		vertices.add(vertex);
		
		return position(vertex);
	}
	
	private double forward() {
		++currIndex;
		
		if (currIndex == vertices.size())
			return explore();
		else // currIndex < vertices.size()
			return position(currIndex);
	}
	
	private double backward() {
		--currIndex;
		
		return position(currIndex);
	}

}
