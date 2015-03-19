package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.CollisionMatchers.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.factories.NodeFactory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.RadiusBasedWorldPerspectiveCache;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspective;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspectiveCache;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.StraightEdgePathfinder;

public class JobPlannerTest {

	private static final NodeFactory wFact = NodeFactory.getInstance();
	
	private static Schedule makeSchedule(Node... nodes) {
		Schedule schedule = new Schedule();
		
		for (Node n : nodes)
			schedule.addNode(n);
		
		return schedule;
	}
	
	private static boolean planJob(
		World world,
		Schedule schedule,
		Node node,
		UUID jobId,
		Point location,
		LocalDateTime startTime,
		Duration duration)
	{
		LocalDateTime floorTime = node.floorIdleTimeOrNull(startTime);
		LocalDateTime ceilTime = node.ceilingIdleTimeOrNull(startTime);
		
		if (floorTime == null || ceilTime == null)
			return false;
		
		SpaceTimeSlot slot = node.idleSlots(floorTime, ceilTime).iterator().next();
		WorldPerspectiveCache cache = new RadiusBasedWorldPerspectiveCache(
			world, StraightEdgePathfinder.class);
		WorldPerspective perspective = cache.getPerspectiveFor(node);

		ScheduleAlternative alternative = new ScheduleAlternative();
		
		JobPlanner tp = new JobPlanner();

		tp.setJobId(jobId);
		tp.setNode(node);
		tp.setLocation(location);
		tp.setEarliestStartTime(startTime);
		tp.setLatestStartTime(startTime);
		tp.setDuration(duration);
		tp.setSlot(slot);
		tp.setWorldPerspective(perspective);
		tp.setSchedule(schedule);
		tp.setScheduleAlternative(alternative);
		tp.setFixedEnd(!ceilTime.isEqual(Scheduler.END_OF_TIME));

		boolean status = tp.plan();

		if (!status)
			return false;
		
		alternative.seal();
		schedule.addAlternative(alternative);
		schedule.integrate(alternative);

		return true;
	}

	@Test
	public void testStaticObstacles() {
		StaticObstacle obstacle = new StaticObstacle(
			immutableBox(30., 10., 40., 40.));
		Node n = wFact.createNode("n", 10.0, 20.0);

		Schedule schedule = makeSchedule(n);
		World world = new World(ImmutableList.of(obstacle), ImmutableList.of());
		
		// P = (60, 20), t = 120, d = 30
		boolean status = planJob(world, schedule,
			n,
			uuid("job"),
			point(60., 20.),
			atSecond(120.),
			secondsToDurationSafe(30.));
		
		assertThat("unable to plan job",
			status, equalTo(true));
		assertThat("n collided with obstacle",
			n, not(nodeCollidesWith(obstacle)));
	}
	
	@Test
	public void testDynamicObstacles() {
		ImmutablePolygon obstacleShape = immutableBox(-5., -5., 5., 5.);
		Trajectory obstacleTrajectory = trajectory(
			30, 30,
			40,  0,
			 0, 40);
		DynamicObstacle obstacle = new DynamicObstacle(obstacleShape, obstacleTrajectory);
		Node n = wFact.createNode("n", 10.0, 20.0);

		Schedule schedule = makeSchedule(n);
		World world = new World(ImmutableList.of(), ImmutableList.of(obstacle));
		
		// P = (60, 20), t = 120, d = 30
		boolean status = planJob(world, schedule,
			n,
			uuid("job"),
			point(50., 20.),
			atSecond(60.),
			secondsToDurationSafe(30.));
		
		assertThat("unable to plan job",
			status, equalTo(true));
		assertThat("n collided with obstacle",
			n, not(nodeCollidesWith(obstacle)));
	}
	
	@Test
	public void testTightPlan1() {
		Node n = wFact.createNode(
			"n", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);

		World world = new World();
		Schedule schedule = makeSchedule(n);
		
		boolean status = planJob(world, schedule,
			n,
			uuid("job"),
			point(0, 0),
			atSecond(0),
			secondsToDurationSafe(10));
		
		assertThat("unable to schedule tight job",
			status, equalTo(true));
	}

	@Test
	public void testTightPlan2() {
		Node n = wFact.createNode(
			"n", immutableBox(-0.5, -0.5, 0.5, 0.5), 1.0, 1.0, 1.0, 0.0);

		World world = new World();
		Schedule schedule = makeSchedule(n);
		
		boolean status;

		// P = (3, 1), t = 2, d = 1
		status = planJob(world, schedule,
			n,
			uuid("job1"),
			point(3, 1),
			atSecond(2),
			secondsToDurationSafe(1));

		assertThat("unable to plan job",
			status, equalTo(true));

		// P = (3, 1), t = 3, d = 1
		status = planJob(world, schedule,
			n,
			uuid("job2"),
			point(3, 1),
			atSecond(3),
			secondsToDurationSafe(1));

		assertThat("unable to plan job",
			status, equalTo(true));
	}
	
	@Test
	public void testTightPlan3() {
		Node n = wFact.createNode(
			"n", immutableBox(-0.5, -0.5, 0.5, 0.5), 1.0, 1.0, 1.0, 0.0);

		World world = new World();
		Schedule schedule = makeSchedule(n);
		
		boolean status;

		// P = (3, 1), t = 3, d = 1
		status = planJob(world, schedule,
			n,
			uuid("job1"),
			point(3, 1),
			atSecond(3),
			secondsToDurationSafe(1));

		assertThat("unable to plan job",
			status, equalTo(true));

		// P = (3, 1), t = 2, d = 1
		status = planJob(world, schedule,
			n,
			uuid("job2"),
			point(3, 1),
			atSecond(2),
			secondsToDurationSafe(1));

		assertThat("unable to plan job",
			status, equalTo(true));
	}

}
