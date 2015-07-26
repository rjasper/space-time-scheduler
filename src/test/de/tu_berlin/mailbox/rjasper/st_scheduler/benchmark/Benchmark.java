package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public class Benchmark {

	public static void main(String[] args) {
		Collection<Class<? extends Benchmarkable>> benchmarks = new LinkedList<>();

//		benchmarks.add(FrbSpatialPathSegmentsBenchmark.class);
//		benchmarks.add(FrbObstacleTrajectorySegmentsBenchmark.class);
//		benchmarks.add(FrbObstacleShapeDetailBenchmark.class);
//		benchmarks.add(FrbObstacleNumberBenchmark.class);
//		benchmarks.add(SimpleVertexConnectorBenchmark.class);
//		benchmarks.add(LazyVertexConnectorBenchmark.class);
//		benchmarks.add(MinimumTimeVertexConnectorBenchmark.class);
////		benchmarks.add(SpatialPathfinderBenchmark.class);
//		benchmarks.add(SingularJobSchedulerNodeNumberSuccessBenchmark.class);
//		benchmarks.add(SingularJobSchedulerNodeNumberErrorBenchmark.class);
//		benchmarks.add(SingularJobSchedulerSlotNumber1SuccessBenchmark.class);
//		benchmarks.add(SingularJobSchedulerSlotNumber1ErrorBenchmark.class);
//		benchmarks.add(SingularJobSchedulerSlotNumber2SuccessBenchmark.class);
//		benchmarks.add(SingularJobSchedulerSlotNumber2ErrorBenchmark.class);
		benchmarks.add(SingularJobSchedulerSlotNumber2SuccessBenchmark.class);
		benchmarks.add(SingularJobSchedulerSlotNumber3ErrorBenchmark.class);
//		benchmarks.add(PeriodicJobSchedulerSameLocationBenchmark.class);
//		benchmarks.add(PeriodicJobSchedulerIndependentLocationBenchmark.class);
//		benchmarks.add(DependentJobSchedulerBenchmark.class);

		BenchmarkExecutor bexec = new BenchmarkExecutor(benchmarks);

		try {
			bexec.benchmark();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		Benchmarkable benchmark = new FrbObstacleTrajectorySegmentsBenchmark();
//
//		benchmark.benchmark(10);
	}

}
