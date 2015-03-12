package de.tu_berlin.mailbox.rjasper.st_scheduler.world.util;

import java.util.Objects;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.PointPath;
import de.tu_berlin.mailbox.rjasper.util.function.TriFunction;

public class DoubleSubPointPathOperation<
	V extends PointPath.Vertex,
	S extends PointPath.Segment<? extends V>,
	P extends PointPath<V, S>>
extends AbstractSubPathOperation<V, S, P, Double, ImmutablePoint>
{
	
	public static <
		V extends PointPath.Vertex,
		S extends PointPath.Segment<? extends V>,
		P extends PointPath<V, S>>
	P subPath(
		P path,
		Function<? super V, Double> positionMapper,
		Function<ImmutableList<ImmutablePoint>, P> constructor,
		double startPosition,
		double finishPosition)
	{
		SubPathOperation<P, Double> op =
			new DoubleSubPointPathOperation<>(path, positionMapper, constructor);
		
		return op.subPath(startPosition, finishPosition);
	}
	
	private final Function<ImmutableList<ImmutablePoint>, P> constructor;

	public DoubleSubPointPathOperation(
		P path,
		Function<? super V, Double> positionMapper,
		Function<ImmutableList<ImmutablePoint>, P> constructor)
	{
		super(path,
			positionMapper,
			(p, p1, p2) -> (p - p1) / (p2 - p1));
		
		this.constructor = Objects.requireNonNull(constructor, "constructor");
	}

	@Override
	protected Interpolator<Double, ImmutablePoint> getInterpolator(
		Seeker<Double, V> seeker,
		TriFunction<Double, Double, Double, Double> relator)
	{
		return new PointPathInterpolator<>(seeker);
	}

	@Override
	protected P construct(
		ImmutablePoint start,
		Iterable<V> innerVertices,
		ImmutablePoint finish)
	{
		ImmutableList.Builder<ImmutablePoint> builder = ImmutableList.builder();
		
		builder.add(start);
		
		for (V v : innerVertices)
			builder.add(v.getPoint());

		builder.add(finish);
		
		return constructor.apply(builder.build());
	}

}
