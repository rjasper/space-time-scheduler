package world.pathfinder;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import world.DynamicObstacle;
import world.LocalDateTimeFactory;
import world.Trajectory;
import world.TrajectoryFactory;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public abstract class MinimumTimeVelocityPathfinderTest {

	protected abstract MinimumTimeVelocityPathfinder getPathfinder();
	
	@Test
	public void test() {
		MinimumTimeVelocityPathfinder pf = getPathfinder();
		
		EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
		TrajectoryFactory trajFact = TrajectoryFactory.getInstance();
		LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();

		LineString spatialPath = geomBuilder.lineString(0., 2., 3., 2.);
		
		Polygon obstacleShape = geomBuilder.box(-0.5, -0.5, 0.5, 0.5);
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
		
		boolean status = pf.calculateTrajectory();
		
		assertTrue(status);
		
		Trajectory trajectory = pf.getResultTrajectory();
		
		double[] x = {0., 1., 3.}, y = {2., 2., 2.}, t = {0., 2., 4.};
		Trajectory expected = trajFact.trajectory(x, y, t);
		
		assertEquals(expected, trajectory);
	}

}
