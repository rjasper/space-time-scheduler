package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.CollisionMatchers.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.PathFactory.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

public abstract class AbstractFixTimePathfinderTest {

	protected abstract AbstractFixTimePathfinder createPathfinder();

	private boolean calcPath(
		AbstractFixTimePathfinder pf,
		Collection<DynamicObstacle> dynObst,
		SpatialPath path,
		double maxSpeed,
		LocalDateTime startTime,
		LocalDateTime finishTime)
	{
		pf.setDynamicObstacles(dynObst);
		pf.setMaxSpeed(maxSpeed);
		pf.setSpatialPath(path);
		pf.setMinArc(0.0);
		pf.setMaxArc(path.length());
		pf.setStartArc(0.0);
		pf.setFinishArc(path.length());
		pf.setStartTime(startTime);
		pf.setFinishTime(finishTime);

		return pf.calculate();
	}

	@Test
	public void test() {
		double maxSpeed = 1.0;
		ImmutableCollection<DynamicObstacle> dynamicObstacles = ImmutableList.of(
			new DynamicObstacle(
				immutableBox(-0.5, -0.5, 0.5, 0.5),
				trajectory(
					2.5, 2.5, 2.5,  2.5,
					5.5, 2.5, 2.5, -0.5,
					0.0, 3.0, 7.0, 10.0)));
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(
			immutablePoint(1., 4.),
			immutablePoint(4., 4.),
			immutablePoint(4., 1.),
			immutablePoint(1., 1.)));
		LocalDateTime startTime = atSecond(0);
		LocalDateTime finishTime = atSecond(11);

		AbstractFixTimePathfinder pf = createPathfinder();

		boolean validPath = calcPath(pf,
			dynamicObstacles,
			spatialPath,
			maxSpeed,
			startTime,
			finishTime);

		assertTrue(validPath);

		Trajectory trajectory = pf.getResultTrajectory();

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
		assertThat("invalid finish time",
			trajectory.getFinishTime(), equalTo(finishTime));
		assertThat(trajectory, not(trajectoryCollidesWith(dynamicObstacles)));
	}

	@Test
	public void testInsufficientTime() {
		AbstractFixTimePathfinder pf = createPathfinder();

		SpatialPath spatialPath = spatialPath(0, 0, 10, 0);

		boolean status = calcPath(pf,
			emptyList(),
			spatialPath,
			1.0,
			atSecond(0),
			atSecond(5));

		assertThat("path found when it shouldn't",
			status, equalTo(false));
	}

}
