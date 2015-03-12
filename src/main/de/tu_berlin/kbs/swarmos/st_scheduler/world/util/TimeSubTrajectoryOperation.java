package de.tu_berlin.kbs.swarmos.st_scheduler.world.util;

import java.time.LocalDateTime;

import de.tu_berlin.kbs.swarmos.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory;

// TODO document
public class TimeSubTrajectoryOperation
extends SubTrajectoryOperation<LocalDateTime>
{
	
	public static SimpleTrajectory subPath(
		Trajectory trajectory,
		LocalDateTime startTime,
		LocalDateTime finishTime)
	{
		TimeSubTrajectoryOperation op =
			new TimeSubTrajectoryOperation(trajectory);
		
		return op.subPath(startTime, finishTime);
	}

	public TimeSubTrajectoryOperation(Trajectory trajectory) {
		super(trajectory, v -> v.getTime(), Interpolators.TIME_RELATOR);
	}

}
