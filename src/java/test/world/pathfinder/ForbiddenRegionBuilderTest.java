package world.pathfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import world.DynamicObstacle;
import world.LocalDateTimeFactory;
import world.Trajectory;
import world.TrajectoryFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ForbiddenRegionBuilderTest {
	

	private static EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
	private static TrajectoryFactory trajFact = TrajectoryFactory.getInstance();
	private static LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();
	private static LocalDateTime baseTime = timeFact.second(0);
	
	@Test
	public void testRegularCase() {
		List<Point> path = geomBuilder.points(2., 2., 8., 8.);
		
		double[] x = {3., 7.}, y = {7., 3.}, t = {0., 4.};
		Trajectory trajectory = trajFact.trajectory(x, y, t);
		Polygon shape = geomBuilder.polygon(-1., -1., 1., -1., 1., 1., -1., 1., -1., -1.);
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
		Geometry expected = geomBuilder.polygon(
			3.*sqrt2, 1.,
			2.*sqrt2, 2.,
			3.*sqrt2, 3.,
			4.*sqrt2, 2.,
			3.*sqrt2, 1.);
		
		assertTrue(expected.norm().equalsExact( region.getRegion(), 1e-10 ));
	}
	
	@Test
	public void testParallelCase() {
		List<Point> path = geomBuilder.points(2., 4., 10., 4.);
		
		double[] x = {6., 6.}, y = {4., 4.}, t = {0., 1.};
		Trajectory trajectory = trajFact.trajectory(x, y, t);
		Polygon shape = geomBuilder.polygon(-2., -2., 2., -2., 2., 2., -2., 2., -2., -2.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);
		
		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(baseTime);
		builder.setSpatialPath(path);
		builder.setDynamicObstacles(Collections.singleton(obstacle));
		
		builder.calculate();
		
		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();
		
		assertEquals(1, regions.size());
		
		ForbiddenRegion region = regions.iterator().next();

		Geometry expected = geomBuilder.polygon(
			4., 0.,
			4., 1.,
			8., 1.,
			8., 0.,
			4., 0.);
		
		assertTrue(expected.norm().equalsExact( region.getRegion(), 1e-10 ));
	}
	
	@Test
	public void testPathSplit() {
		List<Point> path = geomBuilder.points(2., 4., 6., 8., 10., 4.);
		
		double[] x = {6., 6.}, y = {12., 2.}, t = {0., 5.};
		Trajectory trajectory = trajFact.trajectory(x, y, t);
		Polygon shape = geomBuilder.polygon(-2., -2., 2., -2., 2., 2., -2., 2., -2., -2.);
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
		Geometry expected = geomBuilder.polygon(
			2.*sqrt2, 2.,
			2.*sqrt2, 4.,
			4.*sqrt2, 3.,
			6.*sqrt2, 4.,
			6.*sqrt2, 2.,
			4.*sqrt2, 1.,
			2.*sqrt2, 2.);
		
		assertTrue(expected.norm().equalsExact( region.getRegion(), 1e-10 ));
	}
	
	@Test
	public void testTrajectorySplit() {
		List<Point> path = geomBuilder.points(2., 6., 12., 6.);
		
		double[] x = {3., 9., 9.}, y = {3., 9., 3.}, t = {0., 3., 6.};
		Trajectory trajectory = trajFact.trajectory(x, y, t);
		Polygon shape = geomBuilder.polygon(-1., -1., 1., -1., 1., 1., -1., 1., -1., -1.);
		DynamicObstacle obstacle = new DynamicObstacle(shape, trajectory);
		
		ForbiddenRegionBuilder builder = new ForbiddenRegionBuilder();
		builder.setBaseTime(baseTime);
		builder.setSpatialPath(path);
		builder.setDynamicObstacles(Collections.singleton(obstacle));
		
		builder.calculate();
		
		Collection<ForbiddenRegion> regions = builder.getResultForbiddenRegions();
		
		assertEquals(1, regions.size());
		
		ForbiddenRegion region = regions.iterator().next();
		
		Geometry expected = geomBuilder.multiPolygon(
			geomBuilder.polygon(
				2., 1.,
				4., 2.,
				6., 2.,
				4., 1.,
				2., 1.),
			geomBuilder.polygon(
				6., 4.,
				6., 5.,
				8., 5.,
				8., 4.,
				6., 4.)
		);
		
		assertTrue(expected.norm().equalsExact( region.getRegion(), 1e-10 ));
	}

}
