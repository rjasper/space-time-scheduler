package world.util;

import java.util.function.Function;

import world.Trajectory;
import world.Trajectory.Vertex;

// TODO document
public class DoubleSubTrajectoryOperation
extends SubTrajectoryOperation<Double>
{
	public static Trajectory subPath(
		Trajectory trajectory,
		Function<? super Vertex, Double> positionMapper,
		double startPosition, double finishPosition)
	{
		SubPathOperation<Trajectory, Double> op =
			new DoubleSubTrajectoryOperation(trajectory, positionMapper);
		
		return op.subPath(startPosition, finishPosition);
	}

	public DoubleSubTrajectoryOperation(
		Trajectory trajectory,
		Function<? super Vertex, Double> positionMapper)
	{
		super(
			trajectory,
			positionMapper,
			(p, p1, p2) -> (p - p1) / (p2 - p1),
			(d1, d2) -> Double.compare(d1, d2));
	}

}
