package world.pathfinder;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import world.DynamicObstacle;
import world.LocalDateTimeFactory;
import world.SimpleTrajectory;
import world.SpatialPath;
import world.factories.TrajectoryFactory;

import com.google.common.collect.ImmutableList;

public abstract class FixTimeVelocityPathfinderTest {
	
	protected abstract FixTimeVelocityPathfinder createPathfinder();
	
	@Test
	public void test() {
		LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();
		TrajectoryFactory trajFact = TrajectoryFactory.getInstance();
		
		double maxSpeed = 1.0;
		double[] xObst = {2.5, 2.5, 2.5, 2.5};
		double[] yObst = {5.5, 2.5, 2.5, -0.5};
		double[] tObst = {0., 3., 7., 10.};
		Collection<DynamicObstacle> dynamicObstacles = Collections.singleton(
			new DynamicObstacle(
				box(-0.5, -0.5, 0.5, 0.5),
				trajFact.trajectory(xObst, yObst, tObst)));
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(
//			points(1., 4., 4., 4., 4., 1., 1., 1.));
			point(1., 4.),
			point(4., 4.),
			point(4., 1.),
			point(1., 1.)));
		LocalDateTime startTime = timeFact.seconds(0.);
		LocalDateTime finishTime = timeFact.seconds(11.);
		
		FixTimeVelocityPathfinder pf = createPathfinder();

		pf.setMaxSpeed(maxSpeed);
		pf.setDynamicObstacles(dynamicObstacles);
		pf.setSpatialPath(spatialPath);
		pf.setStartTime(startTime);
		pf.setFinishTime(finishTime);
		
		boolean validPath = pf.calculate();
		
		assertTrue(validPath);
		
		SimpleTrajectory trajectory = pf.getResultTrajectory().getComposedTrajectory();
		
		double[] x = {1., 2., 4., 4., 3., 1.};
		double[] y = {4., 4., 4., 1., 1., 1.};
		double[] t = {0., 2., 13./3., 47./6., 9., 11.};
		SimpleTrajectory expected = trajFact.trajectory(x, y, t);
		
		assertThat(trajectory, equalTo(expected));
	}
	

}
