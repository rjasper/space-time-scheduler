package util;

import static java.lang.Math.*;

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
	
	// TODO implement robust double 2 duration conversion and vice versa
	
	private static final long MAX_SAFE_NANO_L = 0x1FFFFFFFFFFFFFL; // 2^53-1
	private static final double MAX_SAFE_NANO_D = 0x1.FFFFFFFFFFFFFP+52; // 2^53-1
	
	private static final Duration MIN_DURATION = Duration.ofSeconds(Long.MIN_VALUE, 0L);
	private static final Duration MAX_DURATION = Duration.ofSeconds(Long.MAX_VALUE, 999_999_999L);

	// TODO document
	public static double nanosToSeconds(double nanos) {
		return nanos * 1e-9;
	}

	// TODO document
	public static double secondsToNanos(double seconds) {
		return seconds * 1e9;
	}
	
	// best effort conversions

	// TODO document
	public static Duration nanosToDuration(double nanos) {
		if (nanos >= Long.MIN_VALUE && nanos <= Long.MAX_VALUE)
			return Duration.ofNanos((long) Math.round(nanos));
		else
			return secondsToDuration(nanosToSeconds(nanos));
	}

	// TODO document
	public static Duration secondsToDuration(double seconds) {
		if (seconds < Long.MIN_VALUE)
			return MIN_DURATION;
		else if (seconds > Long.MAX_VALUE)
			return MAX_DURATION;
		
		double floorSeconds = Math.floor(seconds);
		double nano = Math.round(1e9 * (seconds - floorSeconds));

		return Duration.ofSeconds((long) floorSeconds, (long) nano);
	}

	// TODO document
	public static double durationToNanos(Duration duration) {
		double nano = duration.getNano();
		double seconds = duration.getSeconds();
		
		return secondsToNanos(seconds) + nano;
	}

	// TODO document
	public static double durationToSeconds(Duration duration) {
		double nano = duration.getNano();
		double seconds = duration.getSeconds();
		
		return seconds + nanosToSeconds(nano);
	}

	// TODO document
	public static LocalDateTime nanosToTime(double nanos, LocalDateTime baseTime) {
		Duration duration = nanosToDuration(nanos);
		
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
	public static LocalDateTime secondsToTime(double seconds, LocalDateTime baseTime) {
		return nanosToTime( secondsToNanos(seconds), baseTime );
	}

	// TODO document
	public static double timeToNanos(LocalDateTime time, LocalDateTime baseTime) {
		return durationToNanos( Duration.between(baseTime, time) );
	}

	// TODO document
	public static double timeToSeconds(LocalDateTime time, LocalDateTime baseTime) {
		return nanosToSeconds( timeToNanos(time, baseTime) );
	}
	
	// safe and exact conversions

	// TODO document
	public static Duration nanosToDurationSafe(double nanos) {
		if (Double.isNaN(nanos))
			throw new IllegalArgumentException("nanos is NaN");
		if (abs(nanos) > MAX_SAFE_NANO_D)
			throw new ArithmeticException("unsafe conversion to long");
		
		long nanosL = (long) Math.round(nanos);
		
		return Duration.ofNanos(nanosL);
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
	public static Duration secondsToDurationSafe(double seconds) {
		return nanosToDurationSafe( secondsToNanos(seconds) );
	}

	// TODO document
	public static double durationToNanosExact(Duration duration) {
		long nanosL = duration.toNanos();
		
		if (abs(nanosL) > MAX_SAFE_NANO_L)
			throw new ArithmeticException("inexact conversion to double");
		
		return (double) nanosL;
	}

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
	public static double durationToSecondsExact(Duration duration) {
		return nanosToSeconds( durationToNanosExact(duration) );
	}
	
	// TODO document
	public static LocalDateTime nanosToTimeSafe(double nanos, LocalDateTime baseTime) {
		return baseTime.plus( nanosToDurationSafe(nanos) );
	}

	// TODO document
	public static LocalDateTime secondsToTimeSafe(double seconds, LocalDateTime baseTime) {
		return nanosToTimeSafe( secondsToNanos(seconds), baseTime );
	}

	// TODO document
	public static double timeToSecondsExact(LocalDateTime time, LocalDateTime baseTime) {
		return nanosToSeconds( timeToNanosExact(time, baseTime) );
	}
	
	// TODO document
	public static double timeToNanosExact(LocalDateTime time, LocalDateTime baseTime) {
		return durationToNanosExact( Duration.between(baseTime, time) );
	}

}
