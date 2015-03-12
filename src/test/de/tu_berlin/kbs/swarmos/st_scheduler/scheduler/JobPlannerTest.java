package de.tu_berlin.kbs.swarmos.st_scheduler.scheduler;

import static de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.matchers.CollisionMatchers.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeConv.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeFactory.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.UUIDFactory.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.world.factories.TrajectoryFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.IdleSlot;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.JobPlanner;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Node;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Schedule;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.ScheduleAlternative;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.factories.NodeFactory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.RadiusBasedWorldPerspectiveCache;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.StaticObstacle;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.World;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.WorldPerspective;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.WorldPerspectiveCache;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.pathfinder.StraightEdgePathfinder;

public class JobPlannerTest {

	private static final NodeFactory wFact = NodeFactory.getInstance();
	
	private static Schedule makeSchedule(Node... nodes) {
		Schedule schedule = new Schedule();
		
		for (Node w : nodes)
			schedule.addNode(w);
		
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
		
		IdleSlot idleSlot = node.idleSlots(floorTime, ceilTime).iterator().next();
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
		tp.setIdleSlot(idleSlot);
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
		Node w = wFact.createNode("w", 10.0, 20.0);

		Schedule schedule = makeSchedule(w);
		World world = new World(ImmutableList.of(obstacle), ImmutableList.of());
		
		// P = (60, 20), t = 120, d = 30
		boolean status = planJob(world, schedule,
			w,
			uuid("job"),
			point(60., 20.),
			atSecond(120.),
			secondsToDurationSafe(30.));
		
		assertThat("unable to plan job",
			status, equalTo(true));
		assertThat("w collided with obstacle",
			w, not(nodeCollidesWith(obstacle)));
	}
	
	@Test
	public void testDynamicObstacles() {
		ImmutablePolygon obstacleShape = immutableBox(-5., -5., 5., 5.);
		Trajectory obstacleTrajectory = trajectory(
			30, 30,
			40,  0,
			 0, 40);
		DynamicObstacle obstacle = new DynamicObstacle(obstacleShape, obstacleTrajectory);
		Node w = wFact.createNode("w", 10.0, 20.0);

		Schedule schedule = makeSchedule(w);
		World world = new World(ImmutableList.of(), ImmutableList.of(obstacle));
		
		// P = (60, 20), t = 120, d = 30
		boolean status = planJob(world, schedule,
			w,
			uuid("job"),
			point(50., 20.),
			atSecond(60.),
			secondsToDurationSafe(30.));
		
		assertThat("unable to plan job",
			status, equalTo(true));
		assertThat("w collided with obstacle",
			w, not(nodeCollidesWith(obstacle)));
	}
	
//	@Test
//	public void testObsoleteEvasions() {
//		ImmutablePolygon shape = immutableBox(-0.25, -0.25, 0.25, 0.25);
//		Node w1 = wFact.createNode("w1", shape, 1.0, 3.0, 5.0, 0.0);
//		Node w2 = wFact.createNode("w2", shape, 1.0, 2.0, 3.0, 5.0);
//
//		Schedule schedule = makeSchedule(w1, w2);
//		World world = new World();
//		
//		boolean status;
//	
//		// w = w1, P = (3, 1), t = 10, d = 2
//		status = planJob(world, schedule,
//			w1,
//			uuid("job1"),
//			point(3.0, 1.0),
//			atSecond(10.0),
//			ofSeconds(2.0));
//	
//		assertThat("unable to plan job",
//			status, equalTo(true));
////		assertThat("w1 evaded someone when it shouldn't have",
////			nodes, not(areEvadedBy(w1)));
//	
//		// w = w2, P = (5, 3), t = 10, d = 2
//		status = planJob(world, schedule,
//			w2,
//			uuid("job2"),
//			point(5.0, 3.0),
//			atSecond(10.0),
//			ofSeconds(2.0));
//	
//		assertThat("unable to plan job",
//			status, equalTo(true));
////		assertThat("w1 evaded someone when it shouldn't have",
////			nodes, not(areEvadedBy(w1)));
////		assertThat("w2 didn't evade w1",
////			w1, isEvadedBy(w2, atSecond(5.0)));
////		assertThat("w2 evaded someone it wasn't supposed to",
////			nodes, evadedByNumTimes(w2, 1));
//	
//		// w = w1, P = (1, 3), t = 4, d = 2
//		status = planJob(world, schedule,
//			w1,
//			uuid("job3"),
//			point(1.0, 3.0),
//			atSecond(4.0),
//			ofSeconds(2.0));
//	
//		assertThat("unable to plan job",
//			status, equalTo(true));
////		assertThat("w1 evaded someone when it shouldn't have",
////			nodes, not(areEvadedBy(w1)));
////		assertThat("w2 evaded someone when it shouldn't have",
////			nodes, not(areEvadedBy(w2)));
//	}
	
	@Test
	public void testTightPlan1() {
		Node w = wFact.createNode(
			"w", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);

		World world = new World();
		Schedule schedule = makeSchedule(w);
		
		boolean status = planJob(world, schedule,
			w,
			uuid("job"),
			point(0, 0),
			atSecond(0),
			secondsToDurationSafe(10));
		
		assertThat("unable to schedule tight job",
			status, equalTo(true));
	}

	@Test
	public void testTightPlan2() {
		Node w = wFact.createNode(
			"w", immutableBox(-0.5, -0.5, 0.5, 0.5), 1.0, 1.0, 1.0, 0.0);

		World world = new World();
		Schedule schedule = makeSchedule(w);
		
		boolean status;

		// P = (3, 1), t = 2, d = 1
		status = planJob(world, schedule,
			w,
			uuid("job1"),
			point(3, 1),
			atSecond(2),
			secondsToDurationSafe(1));

		assertThat("unable to plan job",
			status, equalTo(true));

		// P = (3, 1), t = 3, d = 1
		status = planJob(world, schedule,
			w,
			uuid("job2"),
			point(3, 1),
			atSecond(3),
			secondsToDurationSafe(1));

		assertThat("unable to plan job",
			status, equalTo(true));
	}
	
	@Test
	public void testTightPlan3() {
		Node w = wFact.createNode(
			"w", immutableBox(-0.5, -0.5, 0.5, 0.5), 1.0, 1.0, 1.0, 0.0);

		World world = new World();
		Schedule schedule = makeSchedule(w);
		
		boolean status;

		// P = (3, 1), t = 3, d = 1
		status = planJob(world, schedule,
			w,
			uuid("job1"),
			point(3, 1),
			atSecond(3),
			secondsToDurationSafe(1));

		assertThat("unable to plan job",
			status, equalTo(true));

		// P = (3, 1), t = 2, d = 1
		status = planJob(world, schedule,
			w,
			uuid("job2"),
			point(3, 1),
			atSecond(2),
			secondsToDurationSafe(1));

		assertThat("unable to plan job",
			status, equalTo(true));
	}

}
