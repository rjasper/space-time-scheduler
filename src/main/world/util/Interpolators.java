package world.util;

import static util.TimeConv.*;

import java.time.Duration;
import java.time.LocalDateTime;

import util.TriFunction;

public final class Interpolators {
	
	private Interpolators() {}

	public static final
	TriFunction<LocalDateTime, LocalDateTime, LocalDateTime, Double> TIME_RELATOR =
		(t, t1, t2) -> {
			double d1 = durationToSeconds(Duration.between(t1, t));
			double d12 = durationToSeconds(Duration.between(t1, t2));
			
			return d1 / d12;
		};

}
