package util;

import java.time.Duration;
import java.time.LocalDateTime;

public final class TimeFactory {
	
	public static final LocalDateTime BASE_TIME =
		LocalDateTime.of(2000, 1, 1, 0, 0);
	
	private TimeFactory() {}
	
	public static LocalDateTime atSecond(double second) {
		Duration offset = DurationConv.ofSeconds(second);
		
		return BASE_TIME.plus(offset);
	}
	
	public static LocalDateTime atHour(double hour) {
		Duration offset = DurationConv.ofSeconds(hour * 3600.);
		
		return BASE_TIME.plus(offset);
	}

}
