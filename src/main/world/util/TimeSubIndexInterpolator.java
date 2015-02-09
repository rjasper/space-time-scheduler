package world.util;

import static util.DurationConv.*;

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
		double d = inSeconds(Duration.between(t1, t2));
		double d1 = inSeconds(Duration.between(t1, t));
		
		return (double) idx1 + d1 / d;
	}
	
}
