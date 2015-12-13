package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableCircle;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.PathFactory.spatialPath;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.trajectory;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.BASE_TIME;
import static java.util.Collections.singleton;

import java.time.Duration;
import java.util.Collection;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.ForbiddenRegionBuilder;

public class FrbObstacleShapeDetailBenchmark implements Benchmarkable {

	private StopWatch sw = new StopWatch();

	@Override
	public int minProblemSize() {
		return 100_000;
	}

	@Override
	public int maxProblemSize() {
		return 1_000_000;
	}

	@Override
	public int stepProblemSize() {
		return 100_000;
	}

	@Override
	public Duration benchmark(int n) {
		sw.reset();

		ForbiddenRegionBuilder frb = new ForbiddenRegionBuilder();

		frb.setBaseTime(BASE_TIME);
		frb.setDynamicObstacles(makeDynamicObstacles(n));
		frb.setSpatialPath(makeSpatialPath());

		sw.start();
		frb.calculate();
		sw.stop();

//		Geometry[] regions = frb.getResultForbiddenRegions().stream()
//			.map(ForbiddenRegion::getRegion)
//			.toArray(m -> new Geometry[m]);
//		System.out.println(geometryCollection(regions));

		return sw.duration();
	}

	private Collection<DynamicObstacle> makeDynamicObstacles(int n) {
		ImmutablePolygon shape = immutableCircle(0, 0, 1, n);
		Trajectory traj = trajectory(2, 2, 2, -2, 0, 4);

		return singleton(new DynamicObstacle(shape, traj));
	}

	private SpatialPath makeSpatialPath() {
		return spatialPath(0, 0, 4, 0);
	}

}
