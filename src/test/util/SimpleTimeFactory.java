package util;

import java.time.Duration;
import java.time.LocalDateTime;

public final class SimpleTimeFactory {
	
	private static final LocalDateTime BASE_TIME =
		LocalDateTime.of(2000, 1, 1, 0, 0);
	
	private SimpleTimeFactory() {}
	
	public static LocalDateTime atSecond(double second) {
		Duration offset = DurationConv.ofSeconds(second);
		
		return BASE_TIME.plus(offset);
	}

}
