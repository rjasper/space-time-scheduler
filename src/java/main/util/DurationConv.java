package util;

import java.time.Duration;

public final class DurationConv {
	
	public static double inSeconds(Duration duration) {
		double seconds = duration.getSeconds();
		double nano = duration.getNano();
		
		return seconds + 1e-9 * nano;
	}
	
	public static Duration ofSeconds(double seconds) {
		if (seconds < Long.MIN_VALUE || seconds > Long.MAX_VALUE)
			throw new IllegalArgumentException("seconds exceeds Duration range");
		
		double floorSeconds = Math.floor(seconds);
		double nano = 1e9 * (seconds - floorSeconds);
		
		return Duration.ofSeconds((long) floorSeconds, (long) nano);
	}

}
