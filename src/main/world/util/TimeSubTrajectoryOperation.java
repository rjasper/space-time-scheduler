package world.util;

import static util.DurationConv.*;

import java.time.Duration;
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
		super(
			trajectory,
			v -> v.getTime(),
			// TODO central location for common relators
			(t, t1, t2) -> {
				double d1 = inSeconds(Duration.between(t1, t));
				double d12 = inSeconds(Duration.between(t1, t2));
				
				return d1 / d12;
			});
	}

}
