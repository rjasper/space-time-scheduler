package tasks;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static matchers.CollisionMatchers.*;
import static matchers.EvasionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.DurationConv.*;
import static util.NameProvider.*;
import static util.TimeFactory.*;
import static world.factories.PerspectiveCacheFactory.*;
import static world.factories.TrajectoryFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import tasks.factories.WorkerUnitFactory;
import world.DynamicObstacle;
import world.RadiusBasedWorldPerspectiveCache;
import world.StaticObstacle;
import world.Trajectory;
import world.World;
import world.WorldPerspectiveCache;
import world.pathfinder.StraightEdgePathfinder;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class TaskPlannerTest {

	private static final WorkerUnitFactory wuFact = WorkerUnitFactory.getInstance();
	
	// TODO test impossible tasks

	private static boolean planTask(
		TaskPlanner taskPlanner,
		WorkerUnit worker,
		Point location,
		LocalDateTime startTime,
		Duration duration)
	{
		taskPlanner.setWorkerUnit(worker);
		taskPlanner.setLocation(location);
		taskPlanner.setEarliestStartTime(startTime);
		taskPlanner.setLatestStartTime(startTime);
		taskPlanner.setDuration(duration);

		return taskPlanner.plan();
	}

	@Test
	public void testStaticObstacles() {
		StaticObstacle obstacle = new StaticObstacle(
			box(30., 10., 40., 40.));
		WorkerUnit w = wuFact.createWorkerUnit(10.0, 20.0);

		setNameFor(w, "w");

		World world = new World(singleton(obstacle), emptyList());
		WorldPerspectiveCache perspectiveCache =
			new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);

		TaskPlanner tp = new TaskPlanner();

		tp.setWorkerPool(singleton(w));
		tp.setPerspectiveCache(perspectiveCache);
		
		// P = (60, 20), t = 120, d = 30
		boolean status = planTask(tp, w,
			point(60., 20.),
			atSecond(120.),
			ofSeconds(30.));
		
		assertThat("unable to plan task",
			status, equalTo(true));
		assertThat("w collided with obstacle",
			w, not(collideWith(obstacle)));
	}
	
	@Test
	public void testDynamicObstacles() {
		Polygon obstacleShape = box(-5., -5., 5., 5.);
		Trajectory obstacleTrajectory = trajectory(
			30, 30,
			40,  0,
			 0, 40);
		DynamicObstacle obstacle = new DynamicObstacle(obstacleShape, obstacleTrajectory);
		WorkerUnit w = wuFact.createWorkerUnit(10.0, 20.0);

		setNameFor(w, "w");

		World world = new World(emptyList(), singleton(obstacle));
		WorldPerspectiveCache perspectiveCache =
			new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);

		TaskPlanner tp = new TaskPlanner();

		tp.setWorkerPool(singleton(w));
		tp.setPerspectiveCache(perspectiveCache);
		
		// P = (60, 20), t = 120, d = 30
		boolean status = planTask(tp, w,
			point(50., 20.),
			atSecond(60.),
			ofSeconds(30.));
		
		assertThat("unable to plan task",
			status, equalTo(true));
		assertThat("w collided with obstacle",
			w, not(collideWith(obstacle)));
	}

	@Test
	public void testObsoleteEvasions() {
		Polygon shape = box(-0.25, -0.25, 0.25, 0.25);
		WorkerUnit w1 = wuFact.createWorkerUnit(shape, 1.0, 3.0, 5.0, 0.0);
		WorkerUnit w2 = wuFact.createWorkerUnit(shape, 1.0, 2.0, 3.0, 5.0);
	
		setNameFor(w1, "w1");
		setNameFor(w2, "w2");
	
		World world = new World();
		WorldPerspectiveCache perspectiveCache =
			new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);
		Collection<WorkerUnit> workers = Arrays.asList(w1, w2);
	
		TaskPlanner tp = new TaskPlanner();
	
		tp.setWorkerPool(workers);
		tp.setPerspectiveCache(perspectiveCache);
	
		boolean status;
	
		// w = w1, P = (3, 1), t = 10, d = 2
		status = planTask(tp, w1,
			point(3.0, 1.0),
			atSecond(10.0),
			ofSeconds(2.0));
	
		assertThat("unable to plan task",
			status, equalTo(true));
		assertThat("w1 evaded someone when it shouldn't have",
			workers, not(areEvadedBy(w1)));
	
		// w = w2, P = (5, 3), t = 10, d = 2
		status = planTask(tp, w2,
			point(5.0, 3.0),
			atSecond(10.0),
			ofSeconds(2.0));
	
		assertThat("unable to plan task",
			status, equalTo(true));
		assertThat("w1 evaded someone when it shouldn't have",
			workers, not(areEvadedBy(w1)));
		assertThat("w2 didn't evade w1",
			w1, isEvadedBy(w2, atSecond(5.0)));
		assertThat("w2 evaded someone it wasn't supposed to",
			workers, evadedByNumTimes(w2, 1));
	
		// w = w1, P = (1, 3), t = 4, d = 2
		status = planTask(tp, w1,
			point(1.0, 3.0),
			atSecond(4.0),
			ofSeconds(2.0));
	
		assertThat("unable to plan task",
			status, equalTo(true));
		assertThat("w1 evaded someone when it shouldn't have",
			workers, not(areEvadedBy(w1)));
		assertThat("w2 evaded someone when it shouldn't have",
			workers, not(areEvadedBy(w2)));
	}
	
	@Test
	public void testTightPlan1() {
		WorkerUnit w = wuFact.createWorkerUnit(
			box(-0.5, -0.5, 0.5, 0.5), 1.0, 1.0, 1.0, 0.0);
		setNameFor(w, "w");
		
		TaskPlanner tp = new TaskPlanner();
	
		tp.setWorkerPool(singleton(w));
		tp.setPerspectiveCache(emptyPerspectiveCache());
		
		boolean status;

		// P = (3, 1), t = 2, d = 1
		status = planTask(tp, w,
			point(3, 1),
			atSecond(2),
			ofSeconds(1));

		assertThat("unable to plan task",
			status, equalTo(true));

		// P = (3, 1), t = 3, d = 1
		status = planTask(tp, w,
			point(3, 1),
			atSecond(3),
			ofSeconds(1));

		assertThat("unable to plan task",
			status, equalTo(true));
	}
	
	@Test
	public void testTightPlan2() {
		WorkerUnit w = wuFact.createWorkerUnit(
			box(-0.5, -0.5, 0.5, 0.5), 1.0, 1.0, 1.0, 0.0);
		setNameFor(w, "w");
		
		TaskPlanner tp = new TaskPlanner();
	
		tp.setWorkerPool(singleton(w));
		tp.setPerspectiveCache(emptyPerspectiveCache());
		
		boolean status;

		// P = (3, 1), t = 3, d = 1
		status = planTask(tp, w,
			point(3, 1),
			atSecond(3),
			ofSeconds(1));

		assertThat("unable to plan task",
			status, equalTo(true));

		// P = (3, 1), t = 2, d = 1
		status = planTask(tp, w,
			point(3, 1),
			atSecond(2),
			ofSeconds(1));

		assertThat("unable to plan task",
			status, equalTo(true));
	}

}
