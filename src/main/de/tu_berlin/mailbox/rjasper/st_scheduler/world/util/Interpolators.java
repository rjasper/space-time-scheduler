package de.tu_berlin.mailbox.rjasper.st_scheduler.world.util;

import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;

import java.time.Duration;
import java.time.LocalDateTime;

import de.tu_berlin.mailbox.rjasper.util.function.TriFunction;

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
