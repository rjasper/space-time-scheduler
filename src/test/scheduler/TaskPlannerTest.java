package scheduler;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static matchers.CollisionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeConv.*;
import static util.TimeFactory.*;
import static util.UUIDFactory.*;
import static world.factories.TrajectoryFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import jts.geom.immutable.ImmutablePolygon;

import org.junit.Test;

import scheduler.factories.NodeFactory;
import world.DynamicObstacle;
import world.RadiusBasedWorldPerspectiveCache;
import world.StaticObstacle;
import world.Trajectory;
import world.World;
import world.WorldPerspective;
import world.WorldPerspectiveCache;
import world.pathfinder.StraightEdgePathfinder;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

public class TaskPlannerTest {

	private static final NodeFactory wFact = NodeFactory.getInstance();
	
	private static Schedule makeSchedule(Node... workers) {
		Schedule schedule = new Schedule();
		
		for (Node w : workers)
			schedule.addWorker(w);
		
		return schedule;
	}
	
	private static boolean planTask(
		World world,
		Schedule schedule,
		Node worker,
		UUID taskId,
		Point location,
		LocalDateTime startTime,
		Duration duration)
	{
		LocalDateTime floorTime = worker.floorIdleTimeOrNull(startTime);
		LocalDateTime ceilTime = worker.ceilingIdleTimeOrNull(startTime);
		
		if (floorTime == null || ceilTime == null)
			return false;
		
		IdleSlot idleSlot = worker.idleSlots(floorTime, ceilTime).iterator().next();
		WorldPerspectiveCache cache = new RadiusBasedWorldPerspectiveCache(
			world, StraightEdgePathfinder.class);
		WorldPerspective perspective = cache.getPerspectiveFor(worker);

		ScheduleAlternative alternative = new ScheduleAlternative();
		
		TaskPlanner tp = new TaskPlanner();

		tp.setTaskId(taskId);
		tp.setWorker(worker);
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
		boolean status = planTask(world, schedule,
			w,
			uuid("task"),
			point(60., 20.),
			atSecond(120.),
			secondsToDurationSafe(30.));
		
		assertThat("unable to plan task",
			status, equalTo(true));
		assertThat("w collided with obstacle",
			w, not(workerCollidesWith(obstacle)));
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
		boolean status = planTask(world, schedule,
			w,
			uuid("task"),
			point(50., 20.),
			atSecond(60.),
			secondsToDurationSafe(30.));
		
		assertThat("unable to plan task",
			status, equalTo(true));
		assertThat("w collided with obstacle",
			w, not(workerCollidesWith(obstacle)));
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
//		status = planTask(world, schedule,
//			w1,
//			uuid("task1"),
//			point(3.0, 1.0),
//			atSecond(10.0),
//			ofSeconds(2.0));
//	
//		assertThat("unable to plan task",
//			status, equalTo(true));
////		assertThat("w1 evaded someone when it shouldn't have",
////			workers, not(areEvadedBy(w1)));
//	
//		// w = w2, P = (5, 3), t = 10, d = 2
//		status = planTask(world, schedule,
//			w2,
//			uuid("task2"),
//			point(5.0, 3.0),
//			atSecond(10.0),
//			ofSeconds(2.0));
//	
//		assertThat("unable to plan task",
//			status, equalTo(true));
////		assertThat("w1 evaded someone when it shouldn't have",
////			workers, not(areEvadedBy(w1)));
////		assertThat("w2 didn't evade w1",
////			w1, isEvadedBy(w2, atSecond(5.0)));
////		assertThat("w2 evaded someone it wasn't supposed to",
////			workers, evadedByNumTimes(w2, 1));
//	
//		// w = w1, P = (1, 3), t = 4, d = 2
//		status = planTask(world, schedule,
//			w1,
//			uuid("task3"),
//			point(1.0, 3.0),
//			atSecond(4.0),
//			ofSeconds(2.0));
//	
//		assertThat("unable to plan task",
//			status, equalTo(true));
////		assertThat("w1 evaded someone when it shouldn't have",
////			workers, not(areEvadedBy(w1)));
////		assertThat("w2 evaded someone when it shouldn't have",
////			workers, not(areEvadedBy(w2)));
//	}
	
	@Test
	public void testTightPlan1() {
		Node w = wFact.createNode(
			"w", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);

		World world = new World();
		Schedule schedule = makeSchedule(w);
		
		boolean status = planTask(world, schedule,
			w,
			uuid("task"),
			point(0, 0),
			atSecond(0),
			secondsToDurationSafe(10));
		
		assertThat("unable to schedule tight task",
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
		status = planTask(world, schedule,
			w,
			uuid("task1"),
			point(3, 1),
			atSecond(2),
			secondsToDurationSafe(1));

		assertThat("unable to plan task",
			status, equalTo(true));

		// P = (3, 1), t = 3, d = 1
		status = planTask(world, schedule,
			w,
			uuid("task2"),
			point(3, 1),
			atSecond(3),
			secondsToDurationSafe(1));

		assertThat("unable to plan task",
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
		status = planTask(world, schedule,
			w,
			uuid("task1"),
			point(3, 1),
			atSecond(3),
			secondsToDurationSafe(1));

		assertThat("unable to plan task",
			status, equalTo(true));

		// P = (3, 1), t = 2, d = 1
		status = planTask(world, schedule,
			w,
			uuid("task2"),
			point(3, 1),
			atSecond(2),
			secondsToDurationSafe(1));

		assertThat("unable to plan task",
			status, equalTo(true));
	}

}
