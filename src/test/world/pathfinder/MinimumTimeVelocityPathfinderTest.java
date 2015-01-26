package world.pathfinder;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.DurationConv.*;
import static util.TimeFactory.*;
import static world.factories.PathFactory.*;
import static world.factories.TrajectoryFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import jts.geom.immutable.ImmutablePolygon;

import org.junit.Test;

import world.DynamicObstacle;
import world.SpatialPath;
import world.Trajectory;

import com.google.common.collect.ImmutableList;

public abstract class MinimumTimeVelocityPathfinderTest {

	protected abstract MinimumTimeVelocityPathfinder createPathfinder();
	
	@Test
	public void test() {
		MinimumTimeVelocityPathfinder pf = createPathfinder();
		
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(
			immutablePoint(0., 2.), immutablePoint(3., 2.)));
		ImmutablePolygon obstacleShape = immutableBox(-0.5, -0.5, 0.5, 0.5);
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
	
	@Test
	public void testInsufficientTime() {
		MinimumTimeVelocityPathfinder pf = createPathfinder();
		
		pf.setSpatialPath       ( spatialPath(0, 0, 10, 0) );
		pf.setDynamicObstacles  ( emptyList()              );
		pf.setMaxSpeed          ( 1.0                      );
		pf.setStartTime         ( atSecond(0)              );
		pf.setEarliestFinishTime( atSecond(5)              );
		pf.setLatestFinishTime  ( atSecond(6)              );
		pf.setBufferDuration    ( ofSeconds(0)             );
		
		boolean status = pf.calculate();

		assertThat("path found when it shouldn't",
			status, equalTo(false));
	}
	
	@Test
	public void testViolateBufferDuration() {
		MinimumTimeVelocityPathfinder pf = createPathfinder();
		
		DynamicObstacle obstacle = new DynamicObstacle(
			immutableBox(-1, -1, 1, 1),
			trajectory(
				 5,  5,
				-2,  2,
				10, 15));
		
		pf.setSpatialPath       ( spatialPath(0, 0, 5, 0) );
		pf.setDynamicObstacles  ( singletonList(obstacle) );
		pf.setMaxSpeed          ( 1.0                     );
		pf.setStartTime         ( atSecond(0)             );
		pf.setEarliestFinishTime( atSecond(10)            );
		pf.setLatestFinishTime  ( atSecond(10)            );
		pf.setBufferDuration    ( ofSeconds(10)           );
		
		boolean status = pf.calculate();

		assertThat("path found when it shouldn't",
			status, equalTo(false));
	}
	
	@Test
	public void testTightBufferDuration() {
		MinimumTimeVelocityPathfinder pf = createPathfinder();
		
		DynamicObstacle obstacle = new DynamicObstacle(
			immutableBox(-1, -1, 1, 1),
			trajectory(
				 5,  5,
				-2,  2,
				10, 15));
		
		pf.setSpatialPath       ( spatialPath(0, 0, 5, 0) );
		pf.setDynamicObstacles  ( singletonList(obstacle) );
		pf.setMaxSpeed          ( 1.0                     );
		pf.setStartTime         ( atSecond(0)             );
		pf.setEarliestFinishTime( atSecond(10)            );
		pf.setLatestFinishTime  ( atSecond(10)            );
		pf.setBufferDuration    ( ofSeconds(1.25)         );
		
		boolean status = pf.calculate();

		assertThat("no path found",
			status, equalTo(true));
	}

}
