package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import java.util.Collection;
import java.util.LinkedList;

public class Benchmark {

	public static void main(String[] args) {
		Collection<Class<? extends Benchmarkable>> benchmarks = new LinkedList<>();

//		benchmarks.add(FrbObstacleNumberBenchmark.class);
//		benchmarks.add(FrbSpatialPathSegmentsBenchmark.class);
//		benchmarks.add(FrbObstacleTrajectorySegmentsBenchmark.class);
//		benchmarks.add(FrbObstacleShapeDetailBenchmark.class);
//		benchmarks.add(SimpleVertexConnectorBenchmark.class);
//		benchmarks.add(LazyVertexConnectorBenchmark.class);
//		benchmarks.add(MinimumTimeVertexConnectorBenchmark.class);
		benchmarks.add(SpatialPathfinderBenchmark.class);

		BenchmarkExecutor bexec = new BenchmarkExecutor(benchmarks);

		bexec.benchmark();

//		Benchmarkable benchmark = new SpatialPathfinderBenchmark();
//
//		benchmark.benchmark(20);
	}

}
