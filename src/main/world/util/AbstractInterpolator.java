package world.util;

import java.util.Objects;

import world.util.Seeker.SeekResult;

// TODO document
public abstract class AbstractInterpolator<
	P extends Comparable<? super P>,
	I,
	T>
implements Interpolator<P, T>
{
	
	private final Seeker<P, ? extends I> seeker;
	
	public AbstractInterpolator(Seeker<P, ? extends I> seeker) {
		this.seeker = Objects.requireNonNull(seeker, "seeker");
	}

	@Override
	public InterpolationResult<T> interpolate(P position) {
		SeekResult<P, ? extends I> res1 = seeker.seekFloor(position);
		int idx1 = res1.getIndex();
		P p1 = res1.getPosition();
		I v1 = res1.getInterpolation();
		
		if (res1.getPosition().compareTo(position) == 0)
			return InterpolationResult.onSpot(res1.getIndex(), onSpot(idx1, p1, v1));
		
		SeekResult<P, ? extends I> res2 = seeker.seekCeiling(position);
		int idx2 = res1.getIndex();
		P p2 = res2.getPosition();
		I v2 = res2.getInterpolation();
		
		return InterpolationResult.inbetween(
			res1.getIndex(),
			interpolate(position, idx1, p1, v1, idx2, p2, v2));
	}
	
	protected abstract T interpolate(P position, int idx1, P p1, I v1, int idx2, P p2, I v2);
	
	protected abstract T onSpot(int index, P position, I vertex);

}
