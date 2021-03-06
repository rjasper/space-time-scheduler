package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.mutable;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.box;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePolygon;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.lineString;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.multiPolygon;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.polygon;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.GeometryMatchers.contains;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.PathFactory.spatialPath;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.trajectory;
import static java.lang.Math.ulp;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.time.TimeFactory;

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
	public void testStationaryCase1() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
			immutablePoint(2, 3), immutablePoint(2, 3)));
		DynamicObstacle obstacle = new DynamicObstacle(
			immutableBox(-1, -1, 1, 1),
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
	public void testStationaryCase2() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
			immutablePoint(1.5, 2), immutablePoint(1.5, 2)));
		DynamicObstacle obstacle = new DynamicObstacle(
			immutablePolygon(1.75, -0.25, 0.25, 1.75, -1.75, 0.25, -0.25, -1.75, 1.75, -0.25),
			trajectory(
				0, 3,
				4, 0,
				0, 10));

		Collection<ForbiddenRegion> regions =
			buildRegions(path, singleton(obstacle));

		Geometry region = regions.iterator().next().getRegion();

		assertThat("forbidden region did not contain essential area",
			region, contains( lineString(0, 2.5, 0, 7.5) ));
		assertThat("forbidden region did contain more than essential area",
			region, not(contains( lineString(0, 2.5-ulp(2.5), 0, 7.5+ulp(7.5)) )));
	}

	@Test
	public void testParallelCase() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
			immutablePoint(0., 4.), immutablePoint(10., 4.)));
		Trajectory trajectory = trajectory(
			6, 6,
			4, 4,
			0, 1);
		ImmutablePolygon shape = immutablePolygon(-2., -2., 2., -2., 2., 2., -2., 2., -2., -2.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(TimeFactory.BASE_TIME);
		builder.setSpatialPath(path);
		builder.setDynamicObstacles(Collections.singleton(obstacle));

		builder.calculate();

		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();

		assertEquals(1, regions.size());

		ForbiddenRegion region = regions.iterator().next();

		Geometry expected = immutablePolygon(
			4., 0.,
			4., 1.,
			8., 1.,
			8., 0.,
			4., 0.);

		assertTrue(expected.norm().equalsExact( mutable(region.getRegion()), 1e-10 ));
	}

	@Test
	public void testParallelCasePointIntersection() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
			immutablePoint(5., 0.), immutablePoint(10., 0.)));
		Trajectory trajectory = trajectory(
			0, 4,
			0, 0,
			0, 1);
		ImmutablePolygon shape = immutableBox(-1, -1, 1, 1);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(TimeFactory.BASE_TIME);
		builder.setSpatialPath(path);
		builder.setDynamicObstacles(Collections.singleton(obstacle));

		builder.calculate();

		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();

		assertThat(regions.isEmpty(), is(true));
	}

	@Test
	public void testRegularCase() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
			immutablePoint(2., 2.), immutablePoint(8., 8.)));
		Trajectory trajectory = trajectory(
			3, 7,
			7, 3,
			0, 4);
		ImmutablePolygon shape = immutablePolygon(-1., -1., 1., -1., 1., 1., -1., 1., -1., -1.);
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
		Geometry expected = immutablePolygon(
			3.*sqrt2, 1.,
			2.*sqrt2, 2.,
			3.*sqrt2, 3.,
			4.*sqrt2, 2.,
			3.*sqrt2, 1.);

		assertTrue(expected.norm().equalsExact( mutable(region.getRegion()), 1e-10 ));
	}

	@Test
	public void testPathSplit() {
		SpatialPath path = spatialPath(0, 0, 3, 4, 7, 1);
		Trajectory trajectory = trajectory(
			3, 3,
			8, -2,
			0, 10);
		ImmutablePolygon shape = immutableBox(-1.5, -2, 2, 2);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(TimeFactory.BASE_TIME);
		builder.setSpatialPath(path);
		builder.setDynamicObstacles(Collections.singleton(obstacle));

		builder.calculate();

		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();

		assertEquals(1, regions.size());

		ForbiddenRegion region = regions.iterator().next();

		Geometry expected = immutablePolygon(
			5, 6, 2.5, 8, 2.5, 4, 5., 2, 7.5, 3.5, 7.5, 7.5, 5, 6);

		assertTrue(expected.norm().equalsExact( mutable(region.getRegion()), 1e-10 ));
	}

	@Test
	public void testTrajectorySplit() {
		SpatialPath path = new SpatialPath(ImmutableList.of(
			immutablePoint(2., 6.), immutablePoint(12., 6.)));
		Trajectory trajectory = trajectory(
			3, 9, 9,
			3, 9, 3,
			0, 3, 6);
		ImmutablePolygon shape = immutablePolygon(-1., -1., 1., -1., 1., 1., -1., 1., -1., -1.);
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

	@Test
	public void testSmallRegion() {
		SpatialPath path = spatialPath(0, 0, 0, 0);
		Trajectory traj = trajectory(0, 0, 1, -1, 0, 0.25);
		ImmutablePolygon shape = immutableBox(-1, -1, 1, 1);
		DynamicObstacle obstacle = new DynamicObstacle(shape, traj);

		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(TimeFactory.BASE_TIME);
		builder.setSpatialPath(path);
		builder.setDynamicObstacles(Collections.singleton(obstacle));

		builder.calculate();
		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();

		assertEquals(1, regions.size());

		ForbiddenRegion region = regions.iterator().next();

		Geometry expected = box(0, 0, 0, 0.25);

		assertTrue(expected.norm().equalsExact( mutable(region.getRegion()), 1e-6 ));
		assertThat("calculated region differs from expected region",
			expected.norm().equalsExact( mutable(region.getRegion()), 1e-6 ), is(true));
	}

}
