package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.PathFactory.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static java.util.Collections.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.ForbiddenRegionBuilder;

public class FrbObstacleTrajectorySegmentsBenchmark implements Benchmarkable {

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
		ImmutablePolygon shape = immutableBox(-1, -1, 1, 1);

		ImmutableList.Builder<ImmutablePoint> pathBuilder = ImmutableList.builder();
		ImmutableList.Builder<LocalDateTime> timeBuilder = ImmutableList.builder();

		ImmutablePoint top = immutablePoint(2, 0.5);
		ImmutablePoint bot = immutablePoint(2, -0.5);
//		ImmutablePoint top = immutablePoint(2, 2);
//		ImmutablePoint bot = immutablePoint(2, -2);

		pathBuilder.add(top);
		timeBuilder.add(atSecond(0));

		for (int i = 0; i < n; ++i) {
			pathBuilder.add(bot);
			pathBuilder.add(top);
			timeBuilder.add(atSecond(8*i + 4));
			timeBuilder.add(atSecond(8*i + 8));
		}

		SpatialPath path = new SpatialPath(pathBuilder.build());
		Trajectory traj = new SimpleTrajectory(path, timeBuilder.build());

		return singleton(new DynamicObstacle(shape, traj));
	}

	private SpatialPath makeSpatialPath() {
		return spatialPath(0., 0., 4., 0.);
	}

}
