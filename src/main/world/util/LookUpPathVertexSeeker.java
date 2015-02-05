package world.util;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Function;

import world.Path;

// TODO document
public class LookUpPathVertexSeeker<
	V extends Path.Vertex,
	S extends Path.Segment<? extends V>,
	P extends Path<V, S>>

	extends AbstractPathVertexSeeker<V, S, P>
{
	
	private final TreeMap<Double, V> lookUp;

	public LookUpPathVertexSeeker(P path, Function<V, Double> positionMapper) {
		super(path, positionMapper);
		
		this.lookUp = makeLookUp(path);
	}
	
	private TreeMap<Double, V> makeLookUp(P path) {
		path.vertexStream();
		
		return path.vertexStream()
			.collect(toMap(
				this::position,
				identity(),
				(u, v) -> u,
				TreeMap::new));
	}

	@Override
	public V seekFloor(double position) {
		if (getPath().isEmpty())
			throw new IllegalArgumentException("path is empty");
		if (!Double.isFinite(position))
			throw new IllegalArgumentException("position is not finite");
		
		return evaluateEntry(lookUp.floorEntry(position), position);
	}
	
	@Override
	public V seekCeiling(double position) {
		if (getPath().isEmpty())
			throw new IllegalArgumentException("path is empty");
		if (!Double.isFinite(position))
			throw new IllegalArgumentException("position is not finite");
		
		return evaluateEntry(lookUp.ceilingEntry(position), position);
	}
	
	private V evaluateEntry(Entry<Double, V> entry, double position) {
		if (entry == null)
			throw new IllegalArgumentException("position too small");
		
		V v = entry.getValue();
		
		// if position is beyond the last segment
		if (v.isLast() && position(v) != position)
			throw new IllegalArgumentException("position too big");
		
		return v;
	}

}
