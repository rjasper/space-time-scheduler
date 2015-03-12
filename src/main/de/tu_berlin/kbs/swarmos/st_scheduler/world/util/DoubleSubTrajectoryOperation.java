package de.tu_berlin.kbs.swarmos.st_scheduler.world.util;

import java.util.function.Function;

import de.tu_berlin.kbs.swarmos.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory.Vertex;

// TODO document
public class DoubleSubTrajectoryOperation
extends SubTrajectoryOperation<Double>
{
	public static SimpleTrajectory subPath(
		Trajectory trajectory,
		Function<? super Vertex, Double> positionMapper,
		double startPosition, double finishPosition)
	{
		DoubleSubTrajectoryOperation op =
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
			(p, p1, p2) -> (p - p1) / (p2 - p1));
	}

}
