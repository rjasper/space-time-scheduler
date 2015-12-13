package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableCircle;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.point;
import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.util.Collection;
import java.util.stream.Stream;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.StraightEdgePathfinder;

public class SpatialPathfinderBenchmark implements Benchmarkable {

	private StopWatch sw = new StopWatch();

	@Override
	public int minProblemSize() {
		return 100;
	}

	@Override
	public int maxProblemSize() {
		return 1000;
	}

	@Override
	public int stepProblemSize() {
		return 100;
	}

	@Override
	public Duration benchmark(int n) {
		sw.reset();

		StraightEdgePathfinder pf = new StraightEdgePathfinder();

		Collection<StaticObstacle> obstacles = makeStaticObstacles(n);

		pf.setMaxConnectionDistance(Double.POSITIVE_INFINITY);
		pf.setStaticObstacles(obstacles);
		pf.setStartLocation(point(0, 0));
		pf.setFinishLocation(point(13, 13));

		sw.start();
		pf.calculate();
		sw.stop();

//		ImmutablePolygon[] shapes = obstacles.stream()
//			.map(StaticObstacle::getShape)
//			.toArray(m -> new ImmutablePolygon[m]);
//		System.out.println(multiPolygon(shapes));
//
//		SpatialPath path = pf.getResultSpatialPath();
//		System.out.println(path.trace());

		return sw.duration();
	}

	private Collection<StaticObstacle> makeStaticObstacles(int n) {
		return Stream.<ImmutablePolygon>builder()
			.add(immutableCircle(4, 4, 1, n)) // bottom left
			.add(immutableCircle(9, 4, 1, n)) // bottom right
			.add(immutableCircle(4, 9, 1, n)) // top left
			.add(immutableCircle(9, 9, 1, n)) // top right
			.build()
			.map(StaticObstacle::new)
			.collect(toList());
	}

}
