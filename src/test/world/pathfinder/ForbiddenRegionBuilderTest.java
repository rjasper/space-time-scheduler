package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import util.LocalDateTimeFactory;
import world.DynamicObstacle;
import world.SpatialPath;
import world.Trajectory;
import world.factories.TrajectoryFactory;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class ForbiddenRegionBuilderTest {

	private static TrajectoryFactory trajFact = TrajectoryFactory.getInstance();
	private static LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();
	private static LocalDateTime baseTime = timeFact.second(0);

	// TODO test stationary case

	@Test
	public void testRegularCase() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
//			points(2., 2., 8., 8.));
			point(2., 2.), point(8., 8.)));

		double[] x = {3., 7.}, y = {7., 3.}, t = {0., 4.};
		Trajectory trajectory = trajFact.trajectory(x, y, t);
		Polygon shape = polygon(-1., -1., 1., -1., 1., 1., -1., 1., -1., -1.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(baseTime);
		builder.setSpatialPath(path);
		builder.setDynamicObstacles(Collections.singleton(obstacle));

		builder.calculate();

		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();

		assertEquals(1, regions.size());

		ForbiddenRegion region = regions.iterator().next();

		double sqrt2 = Math.sqrt(2.);
		Geometry expected = polygon(
			3.*sqrt2, 1.,
			2.*sqrt2, 2.,
			3.*sqrt2, 3.,
			4.*sqrt2, 2.,
			3.*sqrt2, 1.);

		assertTrue(expected.norm().equalsExact( mutable(region.getRegion()), 1e-10 ));
	}

	@Test
	public void testParallelCase() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
//			points(0., 4., 10., 4.));
			point(0., 4.), point(10., 4.)));

		double[] x = {6., 6.}, y = {4., 4.}, t = {0., 1.};
		Trajectory trajectory = trajFact.trajectory(x, y, t);
		Polygon shape = polygon(-2., -2., 2., -2., 2., 2., -2., 2., -2., -2.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(baseTime);
		builder.setSpatialPath(path);
		builder.setDynamicObstacles(Collections.singleton(obstacle));

		builder.calculate();

		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();

		assertEquals(1, regions.size());

		ForbiddenRegion region = regions.iterator().next();

		Geometry expected = polygon(
			4., 0.,
			4., 1.,
			8., 1.,
			8., 0.,
			4., 0.);

		assertTrue(expected.norm().equalsExact( mutable(region.getRegion()), 1e-10 ));
	}

	@Test
	public void testPathSplit() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
//			points(2., 4., 6., 8., 10., 4.));
			point(2., 4.), point(6., 8.), point(10., 4.)));

		double[] x = {6., 6.}, y = {12., 2.}, t = {0., 5.};
		Trajectory trajectory = trajFact.trajectory(x, y, t);
		Polygon shape = polygon(-2., -2., 2., -2., 2., 2., -2., 2., -2., -2.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(baseTime);
		builder.setSpatialPath(path);
		builder.setDynamicObstacles(Collections.singleton(obstacle));

		builder.calculate();

		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();

		assertEquals(1, regions.size());

		ForbiddenRegion region = regions.iterator().next();

		double sqrt2 = Math.sqrt(2.);
		Geometry expected = polygon(
			2.*sqrt2, 2.,
			2.*sqrt2, 4.,
			4.*sqrt2, 3.,
			6.*sqrt2, 4.,
			6.*sqrt2, 2.,
			4.*sqrt2, 1.,
			2.*sqrt2, 2.);
		
		assertTrue(expected.norm().equalsExact( mutable(region.getRegion()), 1e-10 ));
	}

	@Test
	public void testTrajectorySplit() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
//			points(2., 6., 12., 6.));
			point(2., 6.), point(12., 6.)));

		double[] x = {3., 9., 9.}, y = {3., 9., 3.}, t = {0., 3., 6.};
		Trajectory trajectory = trajFact.trajectory(x, y, t);
		Polygon shape = polygon(-1., -1., 1., -1., 1., 1., -1., 1., -1., -1.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(baseTime);
		builder.setSpatialPath(path);
		builder.setDynamicObstacles(Collections.singleton(obstacle));

		builder.calculate();

		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();

		assertEquals(1, regions.size());

		ForbiddenRegion region = regions.iterator().next();

		Geometry expected = multiPolygon(
			polygon(
				2., 1.,
				4., 2.,
				6., 2.,
				4., 1.,
				2., 1.),
			polygon(
				6., 4.,
				6., 5.,
				8., 5.,
				8., 4.,
				6., 4.)
		);
		
		assertTrue(expected.norm().equalsExact( mutable(region.getRegion()), 1e-10 ));
	}

}
