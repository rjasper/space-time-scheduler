package world.pathfinder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
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

import com.vividsolutions.jts.geom.Point;

public abstract class FixTimeVelocityPathfinderTest {
	
	protected abstract FixTimeVelocityPathfinder getInstance();
	
	@Test
	public void test() {
		LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		TrajectoryFactory trajFact = TrajectoryFactory.getInstance();
		
		double maxSpeed = 1.0;
		double[] xObst = {2.5, 2.5, 2.5, 2.5};
		double[] yObst = {5.5, 2.5, 2.5, -0.5};
		double[] tObst = {0., 3., 7., 10.};
		Collection<DynamicObstacle> dynamicObstacles = Collections.singleton(
			new DynamicObstacle(
				geomBuilder.box(-0.5, -0.5, 0.5, 0.5),
				trajFact.trajectory(xObst, yObst, tObst)));
		List<Point> spatialPath = geomBuilder.points(
			1., 4., 4., 4., 4., 1., 1., 1.);
		LocalDateTime startTime = timeFact.seconds(0.);
		LocalDateTime finishTime = timeFact.seconds(11.);
		
		FixTimeVelocityPathfinder pf = getInstance();

		pf.setMaxSpeed(maxSpeed);
		pf.setDynamicObstacles(dynamicObstacles);
		pf.setSpatialPath(spatialPath);
		pf.setStartTime(startTime);
		pf.setFinishTime(finishTime);
		
		boolean validPath = pf.calculate();
		
		assertTrue(validPath);
		
		Trajectory trajectory = pf.getResultTrajectory().getComposedTrajectory();
		
		double[] x = {1., 2., 4., 4., 3., 1.};
		double[] y = {4., 4., 4., 1., 1., 1.};
		double[] t = {0., 2., 13./3., 47./6., 9., 11.};
		Trajectory expected = trajFact.trajectory(x, y, t);
		
		assertThat(trajectory, equalTo(expected));
	}
	

}
