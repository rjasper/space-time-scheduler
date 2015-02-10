package world.util;

import static util.DurationConv.*;

import java.time.Duration;
import java.time.LocalDateTime;

import world.Trajectory;

// TODO document
public class TimeSubTrajectoryOperation
extends SubTrajectoryOperation<LocalDateTime>
{
	
	public static Trajectory subPath(
		Trajectory trajectory,
		LocalDateTime startTime,
		LocalDateTime finishTime)
	{
		SubPathOperation<Trajectory, LocalDateTime> op =
			new TimeSubTrajectoryOperation(trajectory);
		
		return op.subPath(startTime, finishTime);
	}

	public TimeSubTrajectoryOperation(Trajectory trajectory) {
		super(
			trajectory,
			v -> v.getTime(),
			(t, t1, t2) -> {
				double d1 = inSeconds(Duration.between(t1, t));
				double d12 = inSeconds(Duration.between(t1, t2));
				
				return d1 / d12;
			},
			(t1, t2) -> t1.compareTo(t2));
	}

}
