package world.util;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.Iterator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Map.Entry;

import world.AbstractPath;

// TODO document
public abstract class AbstractPathInterpolator<
	T,
	V extends AbstractPath.Vertex,
	S extends AbstractPath.Segment<? extends V>,
	P extends AbstractPath<V, S>>
{
	
	private final P path;
	
	private final TreeMap<Double, V> lookUp;
	
	public AbstractPathInterpolator(P path, boolean lookUp) {
		Objects.requireNonNull(path, "spatialPath");
		
		this.path = path;
		this.lookUp = lookUp ? makeLookUp(path) : null;
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
	
	public boolean hasLookUp() {
		return lookUp != null;
	}
	
	protected abstract double position(V vertex);

	public T interpolate(double position) {
		if (path.isEmpty())
			throw new IllegalArgumentException("cannot interpolate empty path");
		if (!Double.isFinite(position))
			throw new IllegalArgumentException("arc is not finite");
		
		if (hasLookUp())
			return seekWithLookUp(position);
		else
			return seekNormal(position);
	}
	
	private T seekNormal(double position) {
		Iterator<V> it = path.vertexIterator();
		
		V v1 = it.next();
		double s1 = position(v1);
		
		if (s1 > position)
			throw new IllegalArgumentException("arc too small");
		
		while (it.hasNext()) {
			if (s1 > position)
				continue;
			if (s1 == position)
				return onSpot(v1);
			
			V v2 = it.next();
			
			double s2 = position(v2);
			
			if (s2 < position)
				return interpolate(position, v1, v2);
			
			v1 = v2;
			s1 = s2;
		}

		throw new IllegalArgumentException("arc too big");
	}
	
	private T seekWithLookUp(double position) {
		Entry<Double, V> floor = lookUp.floorEntry(position);
		
		if (floor == null)
			throw new IllegalArgumentException("arc too small");
		
		double s1 = floor.getKey();
		V v1 = floor.getValue();
		
		if (s1 == position)
			return onSpot(v1);
		
		Entry<Double, V> ceiling = lookUp.ceilingEntry(position);
		
		if (ceiling == null)
			throw new IllegalArgumentException("arc too big");
		
		V v2 = ceiling.getValue();
		
		return interpolate(position, v1, v2);
	}
	
	protected abstract T interpolate(double position, V v1, V v2);
	
	protected abstract T onSpot(V vertex);

}
