package util;

import java.time.Duration;

public final class DurationConv {
	
	public static double inSeconds(Duration duration) {
		return (double) duration.toNanos() * 1e-9;
	}

}
