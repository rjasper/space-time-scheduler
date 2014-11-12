package world;

import static java.time.Month.JANUARY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

public class TrajectoryFactoryTest {

	@Test
	public void test() {

		LocalDateTime baseTime = LocalDateTime.of(2000, JANUARY, 1, 0, 0);
		LocalDateTimeFactory timeFact = new LocalDateTimeFactory(baseTime);
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		
		List<Point> spatialPath = geomBuilder.points(
			0., 1., 2., 1., 2., 4., 5., 4., 5., 1.);
		List<Point> arcTimePath = geomBuilder.points(
			0., 0., 1., 2., 4., 5., 6., 6., 7., 7., 8., 9., 8., 10., 11., 13.);
		
		TrajectoryComposer trajBuilder = new TrajectoryComposer();
		
		trajBuilder.setBaseTime(baseTime);
		trajBuilder.setSpatialPath(spatialPath);
		trajBuilder.setArcTimePath(arcTimePath);
		
		trajBuilder.compose();
		
		SimpleTrajectory trajectory = trajBuilder.getResultTrajectory();
		
		TrajectoryFactory trajFact = new TrajectoryFactory(geomBuilder, timeFact);
		
		double[] x = {0., 1., 2., 2., 2. , 3., 4., 5.,  5.,  5.};
		double[] y = {1., 1., 1., 3., 4. , 4., 4., 4.,  4.,  1.};
		double[] t = {0., 2., 3., 5., 5.5, 6., 7., 9., 10., 13.};
		
		SimpleTrajectory expected = trajFact.trajectory(x, y, t);
		
		assertThat(trajectory, equalTo(expected));
	}

}
