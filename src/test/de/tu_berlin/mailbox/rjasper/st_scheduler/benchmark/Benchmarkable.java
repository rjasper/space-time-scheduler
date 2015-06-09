package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import java.time.Duration;

public interface Benchmarkable {

	public abstract int minProblemSize();

	public abstract int maxProblemSize();

	public abstract int stepProblemSize();

	public abstract Duration benchmark(int problemSize);

}
