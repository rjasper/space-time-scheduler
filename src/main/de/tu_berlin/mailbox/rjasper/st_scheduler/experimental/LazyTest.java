package de.tu_berlin.mailbox.rjasper.st_scheduler.experimental;

import static java.util.Collections.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.Duration;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.ForbiddenRegion;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.LazyMeshBuilder;

public class LazyTest {

	private static final DynamicObstacle DUMMY_OBSTACLE;

	static {
		ImmutablePolygon dummyShape = immutableBox(0, 0, 1, 1);
		Trajectory dummyTrajectory = trajectory(0, 1, 0, 1, 0, 1);
		DUMMY_OBSTACLE = new DynamicObstacle(dummyShape, dummyTrajectory);
	}

	private static ForbiddenRegion forbiddenRegion(Geometry geometry) {
		return new ForbiddenRegion(geometry, DUMMY_OBSTACLE);
	}

	public static void main(String[] args) {
		ForbiddenRegion fb = forbiddenRegion( immutableBox(5, 6, 6, 7) );

		LazyMeshBuilder builder = new LazyMeshBuilder();

		builder.setBaseTime(atSecond(0));
		builder.setStartArc(0);
		builder.setFinishArc(10);
		builder.setStartTime(atSecond(0));
		builder.setFinishTime(atSecond(10));
		builder.setForbiddenRegions(singleton(fb));
		builder.setMaxVelocity(2);
		builder.setLazyVelocity(1.25);

		builder.build();
	}

}
