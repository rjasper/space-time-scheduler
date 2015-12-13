package de.tu_berlin.mailbox.rjasper.st_scheduler.world.fixtures;

import static de.tu_berlin.mailbox.rjasper.collect.ImmutablesCollectors.toImmutableList;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePolygon;

import java.util.Arrays;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public class WorldFixtures {

	public static World twoRooms() {
		ImmutablePolygon wall = immutablePolygon(
			 2.,  2.,   4.,  2.,   4., 20.,  16., 20.,
			16., 22.,   4., 22.,   4., 38.,  32., 38.,
			32., 22.,  20., 22.,  20., 20.,  32., 20.,
			32.,  2.,  34.,  2.,  34., 40.,   2., 40.,
			 2.,  2.);

		ImmutablePolygon fence = immutablePolygon(
			20., 26.,  28., 26.,  28., 28.,  22., 28.,
			22., 34.,  20., 34.,  20., 26.);

		ImmutablePolygon pillar = immutablePolygon(
			24., 30.,  26., 30.,  26., 32.,  24., 32.,
			24., 30.);

		ImmutablePolygon cage = immutablePolygon(
			 8.,  6.,  16.,  6.,  16., 16.,   8., 16.,
			 8., 14.,  14., 14.,  14.,  8.,   8.,  8.,
			 8.,  6.);

		ImmutableCollection<StaticObstacle> staticObstacles = Arrays
			.asList(wall, fence, pillar, cage)
			.stream()
			.map(StaticObstacle::new)
			.collect(toImmutableList());
		World world = new World(staticObstacles, ImmutableList.of());

		return world;
	}

}
