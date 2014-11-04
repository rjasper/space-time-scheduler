package world;

import static org.junit.Assert.*;
import static java.time.Month.JANUARY;

import java.time.LocalDateTime;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import com.vividsolutions.jts.geom.LineString;

public class TrajectoryFactoryTest {

	@Test
	public void test() {

		LocalDateTime baseTime = LocalDateTime.of(2000, JANUARY, 1, 0, 0);
		LocalDateTimeFactory timeFact = new LocalDateTimeFactory(baseTime);
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		LineString spatialPath = geomBuilder.lineString(
			0., 1., 2., 1., 2., 4., 5., 4., 5., 1.);
		LineString arcTimePath = geomBuilder.lineString(
			0., 0., 1., 2., 4., 5., 6., 6., 7., 7., 8., 9., 8., 10., 11., 13.);
		
		TrajectoryBuilder trajBuilder = new TrajectoryBuilder();
		
		trajBuilder.setBaseTime(baseTime);
		trajBuilder.setSpatialPath(spatialPath);
		trajBuilder.setArcTimePath(arcTimePath);
		
		trajBuilder.build();
		
		Trajectory trajectory = trajBuilder.getResultTrajectory();
		
		TrajectoryFactory trajFact = new TrajectoryFactory(geomBuilder, timeFact);
		
		double[] x = {0., 1., 2., 2., 2. , 3., 4., 5.,  5.,  5.};
		double[] y = {1., 1., 1., 3., 4. , 4., 4., 4.,  4.,  1.};
		double[] t = {0., 2., 3., 5., 5.5, 6., 7., 9., 10., 13.};
		
		Trajectory expected = trajFact.trajectory(x, y, t);
		
		assertEquals(expected, trajectory);
	}

}
