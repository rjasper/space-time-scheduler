package world;

// TODO move to tests or replace by SimpleTimeFactory

import static java.time.Month.JANUARY;

import java.time.Duration;
import java.time.LocalDateTime;

import util.DurationConv;

//TODO document
public class LocalDateTimeFactory {

	private static final LocalDateTime DEFAULT_BASE_TIME =
		LocalDateTime.of(2000, JANUARY, 1, 0, 0);

	private static LocalDateTimeFactory instance = null;

	private LocalDateTime baseTime;

	public LocalDateTimeFactory() {
		this(DEFAULT_BASE_TIME);
	}

	public LocalDateTimeFactory(LocalDateTime baseTime) {
		this.baseTime = baseTime;
	}

	public static LocalDateTimeFactory getInstance() {
		if (instance == null)
			instance = new LocalDateTimeFactory();

		return instance;
	}

	private LocalDateTime getBaseTime() {
		return baseTime;
	}

	public void setBaseTime(LocalDateTime baseTime) {
		this.baseTime = baseTime;
	}

	public LocalDateTime second(long second) {
		LocalDateTime baseTime = getBaseTime();

		return baseTime.plusSeconds(second);
	}

	public LocalDateTime seconds(double seconds) {
		LocalDateTime baseTime = getBaseTime();
		Duration d = DurationConv.ofSeconds(seconds);

		return baseTime.plus(d);
	}

	public LocalDateTime hours(long hours) {
		LocalDateTime baseTime = getBaseTime();

		return baseTime.plusHours(hours);
	}

	public LocalDateTime time(long hour, long minute) {
		LocalDateTime baseTime = getBaseTime();

		return baseTime.plusHours(hour).plusMinutes(minute);
	}

}
