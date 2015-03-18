package de.tu_berlin.mailbox.rjasper.st_scheduler.experimental;

import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static java.util.stream.Collectors.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Collections.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.ForbiddenRegion;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.LazyMeshBuilder;
import de.tu_berlin.mailbox.rjasper.time.TimeConv;

public class LazyTest {

	public static final LocalDateTime BASE_TIME =
		LocalDateTime.of(2000, 1, 1, 0, 0);

	public static LocalDateTime atSecond(double second) {
		Duration offset = TimeConv.secondsToDurationSafe(second);

		return BASE_TIME.plus(offset);
	}

	private static final DynamicObstacle DUMMY_OBSTACLE;

	static {
		ImmutablePolygon dummyShape = immutableBox(0, 0, 1, 1);
		Trajectory dummyTrajectory = new SimpleTrajectory(
			new SpatialPath(ImmutableList.of(
				immutablePoint(0, 0),
				immutablePoint(1, 1))),
			ImmutableList.of(
				atSecond(0),
				atSecond(1)));
		DUMMY_OBSTACLE = new DynamicObstacle(dummyShape, dummyTrajectory);
	}

	private static ForbiddenRegion forbiddenRegion(Geometry geometry) {
		return new ForbiddenRegion(geometry, DUMMY_OBSTACLE);
	}

	public static void main(String[] args) {
		Collection<ForbiddenRegion> frs = Stream.<Geometry>builder()
			.add( immutableBox(5, 6, 6, 7) )
			.add( immutableBox(1.5, 5, 2.5, 6) )
			.add( immutableBox(1, 3, 2, 4) )
			.add( immutableBox(3.5, 3, 4.5, 4) )
			.add( immutableBox(1, 1, 2, 2) )
			.build()
			.map(LazyTest::forbiddenRegion)
			.collect(toList());

		LazyMeshBuilder builder = new LazyMeshBuilder();

		builder.setBaseTime(atSecond(0));
		builder.setStartArc(0);
		builder.setFinishArc(10);
		builder.setStartTime(atSecond(0));
		builder.setFinishTime(atSecond(10));
		builder.setForbiddenRegions(frs);
		builder.setMaxVelocity(2);
		builder.setLazyVelocity(1.25);
//		builder.setMinStopDuration(secondsToDuration(2));

		builder.build();
	}

}
