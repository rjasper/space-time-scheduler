package util;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * {@code DurationConv} provides static conversion functions for
 * {@link Duration}s.
 *
 * @author Rico Jasper
 */
public final class TimeConv {

	private TimeConv() {}

	/**
	 * <p>Calculates a double representation of the given duration in seconds.</p>
	 *
	 * <p>For example:</p>
	 * <blockquote>
	 * {@code Duration.ofSeconds(1L, 500_000_000L)} becomes {@code 1.5}.
	 * </blockquote>
	 *
	 * @param duration
	 * @return the double representation in seconds.
	 * @throws NullPointerException if {@code duration} is {@code null}.
	 */
	public static double durationToSeconds(Duration duration) {
		double seconds = duration.getSeconds(); // throws NPE
		double nano = duration.getNano();

		return seconds + 1e-9 * nano;
	}

	/**
	 * <p>
	 * Calculates the duration of a double value given in seconds.
	 * </p>
	 *
	 * <p>
	 * For example:
	 * </p>
	 * <blockquote> {@code ofSeconds(0.75)} equals
	 * {@code Duration.ofSeconds(0L, 750_000_000L)} </blockquote>
	 *
	 * @param seconds
	 * @return the duration
	 * @throws IllegalArgumentException
	 *             if {@code seconds} is NaN or not within the range of a long
	 *             value.
	 */
	public static Duration secondsToDuration(double seconds) {
		if (Double.isNaN(seconds))
			throw new IllegalArgumentException("seconds is NaN");
		if (seconds < Long.MIN_VALUE || seconds > Long.MAX_VALUE)
			throw new IllegalArgumentException("seconds exceeds Duration range");

		double floorSeconds = Math.floor(seconds);
		double nano = 1e9 * (seconds - floorSeconds);

		return Duration.ofSeconds((long) floorSeconds, (long) nano);
	}
	
	// TODO document
	public static LocalDateTime secondsToTime(double seconds, LocalDateTime baseTime) {
		Duration duration = secondsToDuration(seconds);
		
		try {
			return baseTime.plus(duration);
		} catch (DateTimeException e) {
			// The most likely reason was the inaccuracy of double arithmetic
			// which caused the time to underflow or overflow.
			
			// This checks whether an underflow or overflow was indeed the cause.
			// check if duration was too large or too low
			
			if (duration.isNegative()) { // possibly underflow
				Duration minDuration = Duration.between(baseTime, LocalDateTime.MIN);
				
				if (duration.compareTo(minDuration) < 0)
					return LocalDateTime.MIN;
			} else { // possibly overflow
				Duration maxDuration = Duration.between(baseTime, LocalDateTime.MAX);

				if (duration.compareTo(maxDuration) > 0)
					return LocalDateTime.MAX;
			}
			
			throw e;
		}
	}

	// TODO document
	public static double timeToSeconds(LocalDateTime time, LocalDateTime baseTime) {
		Duration duration = Duration.between(baseTime, time);
		
		return durationToSeconds(duration);
	}

}
