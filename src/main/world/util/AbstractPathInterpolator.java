package world.util;

import java.util.Objects;

import world.PointPath;

// TODO document
public abstract class AbstractPathInterpolator<
	T,
	V extends PointPath.Vertex,
	P extends PointPath<V, ?>>
implements Interpolator<T>
{
	
	private final P path;
	
	private final VertexSeeker<V> seeker;
	
	public AbstractPathInterpolator(P path, VertexSeeker<V> seeker) {
		this.path = Objects.requireNonNull(path, "path");;
		this.seeker = Objects.requireNonNull(seeker, "seeker");
	}

	/* (non-Javadoc)
	 * @see world.util.PathInterpolator#interpolate(double)
	 */
	@Override
	public T interpolate(double position) {
		if (path.isEmpty())
			throw new IllegalArgumentException("cannot interpolate empty path");
		if (!Double.isFinite(position))
			throw new IllegalArgumentException("position is not finite");
		
		V v1 = seeker.seekFloor(position);
		
		double pos1 = seeker.position(v1);
		
		if (pos1 == position)
			return onSpot(v1);
		
		V v2 = seeker.seekCeiling(position);
		
		return interpolate(position, v1, v2);
	}
	
	protected double position(V vertex) {
		return seeker.position(vertex);
	}
	
	protected abstract T interpolate(double position, V v1, V v2);
	
	protected abstract T onSpot(V vertex);

}
