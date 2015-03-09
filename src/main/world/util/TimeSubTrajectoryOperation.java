package world.util;

import java.time.LocalDateTime;

import world.SimpleTrajectory;
import world.Trajectory;

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
