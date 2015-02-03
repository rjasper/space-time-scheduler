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
	
	private boolean calcPath(
		MinimumTimeVelocityPathfinder pf,
		Collection<DynamicObstacle> dynObst,
		SpatialPath path,
		double maxSpeed,
		LocalDateTime startTime,
		LocalDateTime earliestFinishTime,
		LocalDateTime latestFinishTime,
		Duration bufferDuration)
	{
		pf.setDynamicObstacles(dynObst);
		pf.setMaxSpeed(maxSpeed);
		pf.setSpatialPath(path);
		pf.setStartArc(0.0);
		pf.setFinishArc(path.length());
		pf.setMinArc(0.0);
		pf.setMaxArc(path.length());
		pf.setStartTime(startTime);
		pf.setEarliestFinishTime(earliestFinishTime);
		pf.setLatestFinishTime(latestFinishTime);
		pf.setBufferDuration(bufferDuration);
		
		return pf.calculate();
	}
	
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
		
		boolean status = calcPath(pf,
			dynamicObstacles,
			spatialPath,
			maxSpeed,
			startTime,
			earliestFinishTime,
			latestFinishTime,
			bufferDuration);
		
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
		
		SpatialPath spatialPath = spatialPath(0, 0, 10, 0);
		
		boolean status = calcPath(pf,
			emptyList(),
			spatialPath,
			1.0,
			atSecond(0),
			atSecond(5),
			atSecond(6),
			ofSeconds(0));

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
		
		SpatialPath spatialPath = spatialPath(0, 0, 5, 0);

		boolean status = calcPath(pf,
			singletonList(obstacle),
			spatialPath,
			1.0,
			atSecond(0),
			atSecond(10),
			atSecond(10),
			ofSeconds(10));

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
		SpatialPath spatialPath = spatialPath(0, 0, 5, 0);

		boolean status = calcPath(pf,
			singletonList(obstacle),
			spatialPath,
			1.0,
			atSecond(0),
			atSecond(10),
			atSecond(10),
			ofSeconds(1.25));

		assertThat("no path found",
			status, equalTo(true));
	}

}
