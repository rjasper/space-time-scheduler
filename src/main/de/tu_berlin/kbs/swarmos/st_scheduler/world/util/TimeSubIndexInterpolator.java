package de.tu_berlin.kbs.swarmos.st_scheduler.world.util;

import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeConv.*;

import java.time.Duration;
import java.time.LocalDateTime;

// TODO document
public class TimeSubIndexInterpolator extends AbstractInterpolator<LocalDateTime, LocalDateTime, Double> {

	public TimeSubIndexInterpolator(Seeker<LocalDateTime, ? extends LocalDateTime> seeker) {
		super(seeker);
	}

	@Override
	protected Double onSpot(int index, LocalDateTime position, LocalDateTime vertex) {
		return (double) index;
	}

	@Override
	protected Double interpolate(
		LocalDateTime t,
		int idx1,
		LocalDateTime p1,
		LocalDateTime t1,
		int idx2, LocalDateTime p2, LocalDateTime t2)
	{
		double d = durationToSeconds(Duration.between(t1, t2));
		double d1 = durationToSeconds(Duration.between(t1, t));
		
		return (double) idx1 + d1 / d;
	}
	
}
