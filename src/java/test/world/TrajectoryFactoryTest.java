package world;

import static org.junit.Assert.*;
import static java.time.Month.JANUARY;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class TrajectoryFactoryTest {

	@Test
	public void test() {
//		fail("Not yet implemented"); // TODO

		LocalDateTime baseTime = LocalDateTime.of(2000, JANUARY, 1, 0, 0);
		LocalDateTimeFactory timeFact = new LocalDateTimeFactory(baseTime);
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		LineString spatialPath = geomBuilder.lineString(
			0., 1., 2., 1., 2., 4., 5., 4., 5., 1.);
		
		List<Point> arcTimePath = Arrays.asList(
			geomBuilder.point(0., 0.),
			geomBuilder.point(1., 2.),
			geomBuilder.point(4., 5.),
			geomBuilder.point(6., 6.),
			geomBuilder.point(7., 7.),
			geomBuilder.point(8., 9.),
			geomBuilder.point(8., 10.),
			geomBuilder.point(11., 13.));
		
		TrajectoryBuilder trajBuilder = new TrajectoryBuilder();
		
		trajBuilder.setBaseTime(baseTime);
		trajBuilder.setSpatialPath(spatialPath);
		trajBuilder.setArcTimePath(arcTimePath);
		
		trajBuilder.build();
		
		Trajectory trajectory = trajBuilder.getResultTrajectory();
		
		TrajectoryFactory trajFact = new TrajectoryFactory(geomBuilder, timeFact);
		
		double[] x = {0., 1., 2., 2., 2., 3., 4., 5., 5., 5.};
		double[] y = {1., 1., 1., 3., 4., 4., 4., 4., 4., 1.};
		double[] t = {0., 2., 3., 5., 5.5, 6., 7., 9., 10., 13};
		
		Trajectory expected = trajFact.trajectory(x, y, t);
		
		assertEquals(expected, trajectory);
	}

}
