package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

public class BenchmarkExecutor {

	private static final int MIN_REPETITIONS = 5;
	private static final int MAX_REPETITIONS = 20;
	private static final Duration MIN_REPETITION_DURATION = Duration.ofSeconds(1);

	private final Collection<Benchmarkable> benchmarks;

	private StopWatch sw = new StopWatch();

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

//	private static final Duration MIN_DURATION = Duration.ofSeconds(Long.MIN_VALUE, 0);
	private static final Duration MAX_DURATION = Duration.ofSeconds(Long.MAX_VALUE, 999_999_999L);

	private static final DateTimeFormatter FILE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss");

	public void benchmark() throws IOException {
		LocalDateTime creationTime = LocalDateTime.now();

		int k = 0;
		for (Benchmarkable b : benchmarks) {
			String benchmarkName = b.getClass().getSimpleName();

			int min = b.minProblemSize();
			int max = b.maxProblemSize();
			int step = b.stepProblemSize();

			System.out.printf("%d/%d %s: (%d-%d)\n",
				++k, benchmarks.size(),
				benchmarkName,
				min, max);

			String fileName = String.format("%s-%s.csv",
				benchmarkName, creationTime.format(FILE_TIME_FORMATTER));
			Writer writer = new PrintWriter(fileName, "UTF-8");

			Duration benchTotal = Duration.ZERO;

			for (int i = min; i <= max; i += step) {
				sw.reset();
				Duration dmin = MAX_DURATION;

				for (
					int j = 0;
					(j < MIN_REPETITIONS ||
						sw.duration().compareTo(MIN_REPETITION_DURATION) < 0) &&
						j < MAX_REPETITIONS;
					++j)
				{
					sw.start();
					Duration d = b.benchmark(i);
					sw.stop();

					if (d.compareTo(dmin) < 0)
						dmin = d;
				}

				Duration dtotal = sw.duration();
				benchTotal = benchTotal.plus(dtotal);

				System.out.printf("%d: min = %.3fs, total = %.1fs\n",
					i,
					dmin.toMillis() / 1000.,
					dtotal.toMillis() / 1000.);

				writer.write(String.format(
					"%d; %d;\n",
					i, dmin.toMillis()));
			}

			System.out.printf("bench total = %.1fs\n",
				benchTotal.toMillis() / 1000.);

			writer.close();

			System.out.println();
		}
	}

}
