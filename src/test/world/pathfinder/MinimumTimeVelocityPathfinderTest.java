package world.pathfinder;

import static jts.geom.immutable.StaticGeometryBuilder.box;
import static jts.geom.immutable.StaticGeometryBuilder.point;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import world.DynamicObstacle;
import world.LocalDateTimeFactory;
import world.SpatialPath;
import world.Trajectory;
import world.TrajectoryFactory;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Polygon;

public abstract class MinimumTimeVelocityPathfinderTest {

	protected abstract MinimumTimeVelocityPathfinder createPathfinder();
	
	@Test
	public void test() {
		MinimumTimeVelocityPathfinder pf = createPathfinder();
		
		TrajectoryFactory trajFact = TrajectoryFactory.getInstance();
		LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();

		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(
//			points(0., 2., 3., 2.));
			point(0., 2.), point(3., 2.)));
		
		Polygon obstacleShape = box(-0.5, -0.5, 0.5, 0.5);
		double[] xObst = {1.5, 1.5}, yObst = {3.5, 0.5}, tObst = {0., 3.};
		Trajectory obstacleTrajectory = trajFact.trajectory(xObst, yObst, tObst);
		DynamicObstacle obstacle = new DynamicObstacle(obstacleShape, obstacleTrajectory);
		Collection<DynamicObstacle> dynamicObstacles = Collections.singleton(obstacle);
		
		double maxSpeed = 1.0;
		LocalDateTime startTime = timeFact.seconds(0.);
		LocalDateTime earliestFinishTime = timeFact.seconds(3.);
		LocalDateTime latestFinishTime = timeFact.seconds(5.0);
		Duration bufferDuration = Duration.ofSeconds(0L);
		
		pf.setSpatialPath(spatialPath);
		pf.setDynamicObstacles(dynamicObstacles);
		pf.setMaxSpeed(maxSpeed);
		pf.setStartTime(startTime);
		pf.setEarliestFinishTime(earliestFinishTime);
		pf.setLatestFinishTime(latestFinishTime);
		pf.setBufferDuration(bufferDuration);
		
		boolean status = pf.calculate();
		
		assertTrue(status);
		
		Trajectory trajectory = pf.getResultTrajectory().getComposedTrajectory();
		
		double[] x = {0., 1., 3.}, y = {2., 2., 2.}, t = {0., 2., 4.};
		Trajectory expected = trajFact.trajectory(x, y, t);
		
		assertEquals(expected, trajectory);
	}

}
