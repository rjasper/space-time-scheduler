package world.pathfinder;

import static util.TimeFactory.*;
import static world.factories.TrajectoryFactory.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import world.DynamicObstacle;
import world.SpatialPath;
import world.Trajectory;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Polygon;

public abstract class MinimumTimeVelocityPathfinderTest {

	protected abstract MinimumTimeVelocityPathfinder createPathfinder();
	
	@Test
	public void test() {
		MinimumTimeVelocityPathfinder pf = createPathfinder();
		
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(
			point(0., 2.), point(3., 2.)));
		Polygon obstacleShape = box(-0.5, -0.5, 0.5, 0.5);
		Trajectory obstacleTrajectory = trajectory(
			1.5, 1.5,
			3.5, 0.5,
			0.0, 3.0);
		DynamicObstacle obstacle = new DynamicObstacle(obstacleShape, obstacleTrajectory);
		Collection<DynamicObstacle> dynamicObstacles = Collections.singleton(obstacle);
		
		double maxSpeed = 1.0;
		LocalDateTime startTime = atSecond(0.);
		LocalDateTime earliestFinishTime = atSecond(3.);
		LocalDateTime latestFinishTime = atSecond(5.0);
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
		
		Trajectory expected = trajectory(
			0, 1, 3,
			2, 2, 2,
			0, 2, 4);
		
		assertEquals(expected, trajectory);
	}

}
