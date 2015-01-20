package world;

import static java.time.Month.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

import org.junit.Test;

import world.factories.TrajectoryFactory;

import com.google.common.collect.ImmutableList;

public class TrajectoryComposerTest {

	@Test
	public void test() {

		LocalDateTime baseTime = LocalDateTime.of(2000, JANUARY, 1, 0, 0);
		LocalDateTimeFactory timeFact = new LocalDateTimeFactory(baseTime);
		
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(
			point(0., 1.),
			point(2., 1.),
			point(2., 4.),
			point(5., 4.),
			point(5., 1.)));
		ArcTimePath arcTimePath = new ArcTimePath(ImmutableList.of(
			point(0., 0.),
			point(1., 2.),
			point(4., 5.),
			point(6., 6.),
			point(7., 7.),
			point(8., 9.),
			point(8., 10.),
			point(11., 13.)));
		
		TrajectoryComposer composer = new TrajectoryComposer();
		
		composer.setBaseTime(baseTime);
		composer.setSpatialPathComponent(spatialPath);
		composer.setArcTimePathComponent(arcTimePath);
		
		composer.compose();
		
		SimpleTrajectory trajectory = composer.getResultTrajectory();
		
		TrajectoryFactory trajFact = new TrajectoryFactory(timeFact);
		
		double[] x = {0., 1., 2., 2., 2. , 3., 4., 5.,  5.,  5.};
		double[] y = {1., 1., 1., 3., 4. , 4., 4., 4.,  4.,  1.};
		double[] t = {0., 2., 3., 5., 5.5, 6., 7., 9., 10., 13.};
		
		SimpleTrajectory expected = trajFact.trajectory(x, y, t);
		
		assertThat(trajectory, equalTo(expected));
	}

}
