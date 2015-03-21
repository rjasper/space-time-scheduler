package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.CollisionMatchers.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.PathFactory.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

public abstract class AbstractMinimumTimePathfinderTest {

	protected abstract AbstractMinimumTimePathfinder createPathfinder();

	private boolean calcPath(
		AbstractMinimumTimePathfinder pf,
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
		AbstractMinimumTimePathfinder pf = createPathfinder();

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

		Trajectory trajectory = pf.getResultTrajectory().composed();

		assertThat("trajectory is empty",
			trajectory.isEmpty(), is(false));
		assertThat("invalid trace",
			trajectory.trace().equalsTopo(spatialPath.trace()), is(true));
		assertThat("invalid start location",
			trajectory.getStartLocation(), equalTo(spatialPath.getFirstPoint()));
		assertThat("invalid finish location",
			trajectory.getFinishLocation(), equalTo(spatialPath.getLastPoint()));
		assertThat("invalid start time",
			trajectory.getStartTime(), equalTo(startTime));
		assertThat("invalid finish time (too early)",
			trajectory.getFinishTime().compareTo(earliestFinishTime) >= 0, is(true));
		assertThat("invalid finish time (too early)",
			trajectory.getFinishTime().compareTo(latestFinishTime) <= 0, is(true));
		assertThat(trajectory, not(trajectoryCollidesWith(dynamicObstacles)));
	}

	@Test
	public void testInsufficientTime() {
		AbstractMinimumTimePathfinder pf = createPathfinder();

		SpatialPath spatialPath = spatialPath(0, 0, 10, 0);

		boolean status = calcPath(pf,
			emptyList(),
			spatialPath,
			1.0,
			atSecond(0),
			atSecond(5),
			atSecond(6),
			secondsToDurationSafe(0));

		assertThat("path found when it shouldn't",
			status, equalTo(false));
	}

	@Test
	public void testViolateBufferDuration() {
		AbstractMinimumTimePathfinder pf = createPathfinder();

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
			secondsToDurationSafe(10));

		assertThat("path found when it shouldn't",
			status, equalTo(false));
	}

	@Test
	public void testTightBufferDuration() {
		AbstractMinimumTimePathfinder pf = createPathfinder();

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
			secondsToDurationSafe(1.25));

		assertThat("no path found",
			status, equalTo(true));
	}

}
