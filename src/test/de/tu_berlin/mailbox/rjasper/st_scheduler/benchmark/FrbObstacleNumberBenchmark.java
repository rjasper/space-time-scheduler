package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.PathFactory.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.ForbiddenRegionBuilder;

public class FrbObstacleNumberBenchmark implements Benchmarkable {

	private StopWatch sw = new StopWatch();

	@Override
	public int minProblemSize() {
		return 1000;
	}

	@Override
	public int maxProblemSize() {
		return 10000;
	}

	@Override
	public int stepProblemSize() {
		return 1000;
	};

	@Override
	public Duration benchmark(int n) {
		sw.reset();

		ForbiddenRegionBuilder frb = new ForbiddenRegionBuilder();

		frb.setBaseTime(BASE_TIME);
		frb.setDynamicObstacles(makeDynamicObstacles(n));
		frb.setSpatialPath(makeSpatialPath(n));

		sw.start();
		frb.calculate();
		sw.stop();

//		Geometry[] regions = frb.getResultForbiddenRegions().stream()
//			.map(ForbiddenRegion::getRegion)
//			.toArray(m -> new Geometry[m]);
//		System.out.println(geometryCollection(regions));

		return sw.duration();
	}

	private static final ImmutablePolygon SHAPE = immutableBox(-1, -1, 1, 1);

	private Collection<DynamicObstacle> makeDynamicObstacles(int n) {
		Collection<DynamicObstacle> obstacles = new ArrayList<>(n);

		for (int i = 0; i < n; ++i) {
			double x = 3*i + 2;
			Trajectory traj = trajectory(x, x, 2, -2, 0, 4);

			obstacles.add(new DynamicObstacle(SHAPE, traj));
		}

		return obstacles;
	}

	private SpatialPath makeSpatialPath(int n) {
		return spatialPath(0, 0, 3*n + 1, 0);
	}

}
