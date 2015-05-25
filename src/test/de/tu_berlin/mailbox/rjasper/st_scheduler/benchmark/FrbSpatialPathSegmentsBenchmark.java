package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static java.util.Collections.*;

import java.time.Duration;
import java.util.Collection;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.ForbiddenRegionBuilder;

public class FrbSpatialPathSegmentsBenchmark implements Benchmarkable {

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
	}

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

	private Collection<DynamicObstacle> makeDynamicObstacles(int n) {
		ImmutablePolygon shape = immutableBox(0., -1., 2*(n-1), 1.);
		Trajectory traj = trajectory(1., 1., 2., -2, 0., 4.);

		return singleton(new DynamicObstacle(shape, traj));
	}

	private SpatialPath makeSpatialPath(int n) {
		ImmutableList.Builder<ImmutablePoint> builder = ImmutableList.builder();

		for (int i = 0; i <= n; ++i)
			builder.add(immutablePoint(2*i, 0.));

		return new SpatialPath(builder.build());
	}

}
