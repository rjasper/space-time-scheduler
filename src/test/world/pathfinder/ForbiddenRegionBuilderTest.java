package world.pathfinder;

import static java.lang.Math.*;
import static java.util.Collections.*;
import static jts.geom.immutable.ImmutableGeometries.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static matchers.GeometryMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static world.factories.TrajectoryFactory.*;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import util.TimeFactory;
import world.DynamicObstacle;
import world.SpatialPath;
import world.Trajectory;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class ForbiddenRegionBuilderTest {
	
	private static Collection<ForbiddenRegion> buildRegions(
		SpatialPath spatialPath,
		Collection<DynamicObstacle> dynamicObstacles)
	{
		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		
		builder.setBaseTime(TimeFactory.BASE_TIME);
		builder.setSpatialPath(spatialPath);
		builder.setDynamicObstacles(dynamicObstacles);
		
		builder.calculate();
		
		return builder.getResultForbiddenRegions();
	}

	@Test
	public void testStationaryCase() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
			point(2, 3), point(2, 3)));
		DynamicObstacle obstacle = new DynamicObstacle(
			box(-1, -1, 1, 1),
			trajectory(
				2, 2,
				5, 1,
				0, 4));
		
		Collection<ForbiddenRegion> regions =
			buildRegions(path, singleton(obstacle));
		
		Geometry region = regions.iterator().next().getRegion();
		
		assertThat("forbidden region did not contain essential area",
			region, contains( lineString(0, 1, 0, 3) ));
		assertThat("forbidden region did contain more than essential area",
			region, not(contains( lineString(0, 1-ulp(1), 0, 3+ulp(3)) )));
	}

	@Test
	public void testParallelCase() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
			point(0., 4.), point(10., 4.)));
		Trajectory trajectory = trajectory(
			6, 6,
			4, 4,
			0, 1);
		Polygon shape = polygon(-2., -2., 2., -2., 2., 2., -2., 2., -2., -2.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);
	
		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(TimeFactory.BASE_TIME);
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
	public void testRegularCase() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
			point(2., 2.), point(8., 8.)));
		Trajectory trajectory = trajectory(
			3, 7,
			7, 3,
			0, 4);
		Polygon shape = polygon(-1., -1., 1., -1., 1., 1., -1., 1., -1., -1.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(TimeFactory.BASE_TIME);
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
	public void testPathSplit() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
			point(2., 4.), point(6., 8.), point(10., 4.)));
		Trajectory trajectory = trajectory(
			 6, 6,
			12, 2,
			 0, 5);
		Polygon shape = polygon(-2., -2., 2., -2., 2., 2., -2., 2., -2., -2.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(TimeFactory.BASE_TIME);
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
			point(2., 6.), point(12., 6.)));
		Trajectory trajectory = trajectory(
			3, 9, 9,
			3, 9, 3,
			0, 3, 6);
		Polygon shape = polygon(-1., -1., 1., -1., 1., 1., -1., 1., -1., -1.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(TimeFactory.BASE_TIME);
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
