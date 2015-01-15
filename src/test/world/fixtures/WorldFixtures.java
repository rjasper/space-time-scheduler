package world.fixtures;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.util.Arrays;
import java.util.Collection;

import world.StaticObstacle;
import world.World;

import com.vividsolutions.jts.geom.Polygon;

public class WorldFixtures {

	public static World twoRooms() {
		Polygon wall = polygon(
			 2.,  2.,   4.,  2.,   4., 20.,  16., 20.,
			16., 22.,   4., 22.,   4., 38.,  32., 38.,
			32., 22.,  20., 22.,  20., 20.,  32., 20.,
			32.,  2.,  34.,  2.,  34., 40.,   2., 40.,
			 2.,  2.);

		Polygon fence = polygon(
			20., 26.,  28., 26.,  28., 28.,  22., 28.,
			22., 34.,  20., 34.,  20., 26.);

		Polygon pillar = polygon(
			24., 30.,  26., 30.,  26., 32.,  24., 32.,
			24., 30.);

		Polygon cage = polygon(
			 8.,  6.,  16.,  6.,  16., 16.,   8., 16.,
			 8., 14.,  14., 14.,  14.,  8.,   8.,  8.,
			 8.,  6.);

		Collection<StaticObstacle> staticObstacles = Arrays
			.asList(wall, fence, pillar, cage)
			.stream()
			.map(StaticObstacle::new)
			.collect(toList());
		World world = new World(staticObstacles, emptyList());

		return world;
	}

}
