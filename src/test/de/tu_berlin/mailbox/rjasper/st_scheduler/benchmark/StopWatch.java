package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import java.time.Duration;

public class StopWatch {

	private long startTime = -1;
	private Duration duration = Duration.ZERO;

	public void reset() {
		startTime = -1;
		duration = Duration.ZERO;
	}

	public void start() {
		startTime = System.nanoTime();
	}

	public void stop() {
		long endTime = System.nanoTime();

		if (startTime < 0)
			throw new IllegalStateException("no start time captured");

		duration = duration.plusNanos(endTime - startTime);
		startTime = -1;
	}

	public Duration duration() {
		return duration;
	}

}
