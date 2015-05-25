package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

public class BenchmarkExecutor {

	private static final int REPETITIONS = 10;

	private final Collection<Benchmarkable> benchmarks;

	public BenchmarkExecutor(Collection<Class<? extends Benchmarkable>> benchmarks) {
		int n = benchmarks.size();
		Collection<Benchmarkable> instances = new ArrayList<Benchmarkable>(n);

		try {
			for (Class<? extends Benchmarkable> b : benchmarks)
				instances.add(b.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		this.benchmarks = instances;
	}

	private static final Duration MIN_DURATION = Duration.ofSeconds(Long.MIN_VALUE, 0);
	private static final Duration MAX_DURATION = Duration.ofSeconds(Long.MAX_VALUE, 999_999_999L);

	public void benchmark() {
		for (Benchmarkable b : benchmarks) {
			int min = b.minProblemSize();
			int max = b.maxProblemSize();
			int step = b.stepProblemSize();

			System.out.printf("%s: (%d-%d)\n",
				b.getClass().getSimpleName(),
				min, max);

			for (int i = min; i <= max; i += step) {
				Duration dmin = MAX_DURATION;
				Duration dmax = MIN_DURATION;
				Duration dtotal = Duration.ZERO;

				for (int j = 0; j < REPETITIONS; ++j) {
					Duration d = b.benchmark(i);

					if (d.compareTo(dmin) < 0)
						dmin = d;
					if (d.compareTo(dmax) > 0)
						dmax = d;
					dtotal = dtotal.plus(d);
				}

				Duration davr = Duration.ofMillis(dtotal.toMillis() / REPETITIONS);

				System.out.printf("%d: min = %.3f, max = %.3f, avr = %.3f\n",
					i,
					dmin.toMillis() / 1000.,
					dmax.toMillis() / 1000.,
					davr.toMillis() / 1000.);
			}

			System.out.println();
		}
	}

}
