package de.tu_berlin.mailbox.rjasper.time;

import static java.lang.Math.abs;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;

// TODO document
/**
 * {@code DurationConv} provides static conversion functions for
 * {@link Duration}s.
 *
 * @author Rico Jasper
 */
public final class TimeConv {

	private TimeConv() {}
	
	private static final long MAX_SAFE_NANO_L = 0x1FFFFFFFFFFFFFL; // 2^53-1
	private static final double MAX_SAFE_NANO_D = 0x1.FFFFFFFFFFFFFP+52; // 2^53-1
	
	private static final Duration MIN_DURATION = Duration.ofSeconds(Long.MIN_VALUE, 0L);
	private static final Duration MAX_DURATION = Duration.ofSeconds(Long.MAX_VALUE, 999_999_999L);

	public static double nanosToSeconds(double nanos) {
		return nanos * 1e-9;
	}

	public static double secondsToNanos(double seconds) {
		return seconds * 1e9;
	}
	
	// best effort conversions

	public static Duration nanosToDuration(double nanos) {
		if (nanos >= Long.MIN_VALUE && nanos <= Long.MAX_VALUE)
			return Duration.ofNanos((long) Math.round(nanos));
		else
			return secondsToDuration(nanosToSeconds(nanos));
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
	 *             if {@code seconds} is NaN.
	 */
	public static Duration secondsToDuration(double seconds) {
		if (seconds < Long.MIN_VALUE)
			return MIN_DURATION;
		else if (seconds > Long.MAX_VALUE)
			return MAX_DURATION;
		
		double floorSeconds = Math.floor(seconds);
		double nano = Math.round(1e9 * (seconds - floorSeconds));

		return Duration.ofSeconds((long) floorSeconds, (long) nano);
	}

	public static double durationToNanos(Duration duration) {
		double nano = duration.getNano();
		double seconds = duration.getSeconds();
		
		return secondsToNanos(seconds) + nano;
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
	public static double durationToSeconds(Duration duration) {
		double nano = duration.getNano();
		double seconds = duration.getSeconds();
		
		return seconds + nanosToSeconds(nano);
	}

	public static LocalDateTime nanosToTime(double nanos, LocalDateTime baseTime) {
		Duration duration = nanosToDuration(nanos);
		
		try {
			return baseTime.plus(duration);
		} catch (DateTimeException e) {
			// The most likely reason was the inaccuracy of double arithmetic
			// which caused the time to underflow or overflow.
			
			// This checks whether an underflow or overflow was indeed the cause.
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

	public static LocalDateTime secondsToTime(double seconds, LocalDateTime baseTime) {
		return nanosToTime( secondsToNanos(seconds), baseTime );
	}

	public static double timeToNanos(LocalDateTime time, LocalDateTime baseTime) {
		return durationToNanos( Duration.between(baseTime, time) );
	}

	public static double timeToSeconds(LocalDateTime time, LocalDateTime baseTime) {
		return nanosToSeconds( timeToNanos(time, baseTime) );
	}
	
	// safe and exact conversions

	public static Duration nanosToDurationSafe(double nanos) {
		if (Double.isNaN(nanos))
			throw new IllegalArgumentException("nanos is NaN");
		if (abs(nanos) > MAX_SAFE_NANO_D)
			throw new ArithmeticException("unsafe conversion to long");
		
		long nanosL = (long) Math.round(nanos);
		
		return Duration.ofNanos(nanosL);
	}

	public static Duration secondsToDurationSafe(double seconds) {
		return nanosToDurationSafe( secondsToNanos(seconds) );
	}

	public static double durationToNanosExact(Duration duration) {
		long nanosL = duration.toNanos();
		
		if (abs(nanosL) > MAX_SAFE_NANO_L)
			throw new ArithmeticException("inexact conversion to double");
		
		return (double) nanosL;
	}

	public static double durationToSecondsExact(Duration duration) {
		return nanosToSeconds( durationToNanosExact(duration) );
	}
	
	public static LocalDateTime nanosToTimeSafe(double nanos, LocalDateTime baseTime) {
		return baseTime.plus( nanosToDurationSafe(nanos) );
	}

	public static LocalDateTime secondsToTimeSafe(double seconds, LocalDateTime baseTime) {
		return nanosToTimeSafe( secondsToNanos(seconds), baseTime );
	}

	public static double timeToSecondsExact(LocalDateTime time, LocalDateTime baseTime) {
		return nanosToSeconds( timeToNanosExact(time, baseTime) );
	}
	
	public static double timeToNanosExact(LocalDateTime time, LocalDateTime baseTime) {
		return durationToNanosExact( Duration.between(baseTime, time) );
	}

}
