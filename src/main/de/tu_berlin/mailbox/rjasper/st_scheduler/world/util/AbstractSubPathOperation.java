package de.tu_berlin.mailbox.rjasper.st_scheduler.world.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Path;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.Interpolator.InterpolationResult;
import de.tu_berlin.mailbox.rjasper.util.function.TriFunction;

// TODO document
/**
 * 
 * @author Rico
 *
 * @param <V> vertex type
 * @param <S> segment type
 * @param <P> path type
 * @param <Q> position type
 * @param <I> interpolation type
 */
public abstract class AbstractSubPathOperation<
	V extends Path.Vertex,
	S extends Path.Segment<? extends V>,
	P extends Path<V, S>,
	Q extends Comparable<? super Q>,
	I>
implements SubPathOperation<P, Q>
{
	
	private final P path;
	
	private final Function<? super V, Q> positionMapper;
	
	private final TriFunction<Q, Q, Q, Double> relator;
	
	public AbstractSubPathOperation(
		P path,
		Function<? super V, Q> positionMapper,
		TriFunction<Q, Q, Q, Double> relator)
	{
		this.path = Objects.requireNonNull(path, "path");
		this.positionMapper = Objects.requireNonNull(positionMapper, "positionMapper");
		this.relator = Objects.requireNonNull(relator, "relator");
	}
	
	@Override
	public P subPath(Q startPosition, Q finishPosition) {
		if (startPosition.compareTo(finishPosition) >= 0)
			throw new IllegalArgumentException("invalid position interval");
		
		Q minPosition = positionMapper.apply(path.getFirstVertex());
		Q maxPosition = positionMapper.apply(path.getLastVertex ());
		
		if (startPosition .compareTo(minPosition) == 0 &&
			finishPosition.compareTo(maxPosition) == 0)
		{
			return path;
		}
		
		// prepare seeker and interpolator
		
		Seeker<Q, V> seeker = new BinarySearchSeeker<Q, V>(
			path::getVertex,
			positionMapper,
			path.size());
		Interpolator<Q, I> interpolator = getInterpolator(seeker, relator);
		
		// interpolate start and finish
		
		InterpolationResult<I> res1 =
			interpolator.interpolate(startPosition);
		InterpolationResult<I> res2 =
			interpolator.interpolate(finishPosition);
		
		Iterable<V> innerVertices = () -> 
			new VertexIterator(res1.getStartIndex()+1, res2.getFinishIndex());
		
		return construct(
			res1.get(),
			innerVertices,
			res2.get());
	}
	
	private class VertexIterator implements Iterator<V> {
		private int i;
		private final int finishExclusive;
		
		public VertexIterator(int startInclusive, int finishExclusive) {
			this.i = startInclusive;
			this.finishExclusive = finishExclusive;
		}

		@Override
		public boolean hasNext() {
			return i < finishExclusive;
		}

		@Override
		public V next() {
			return path.getVertex(i++);
		}
	}
	
	protected abstract Interpolator<Q, I> getInterpolator(
		Seeker<Q, V> seeker,
		TriFunction<Q, Q, Q, Double> relator);
	
	protected abstract P construct(I start, Iterable<V> innerVertices, I finish);

}
