package tasks;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static matchers.CollisionMatchers.collideWith;
import static matchers.EvasionMatchers.areEvadedBy;
import static matchers.EvasionMatchers.evadedByNumTimes;
import static matchers.EvasionMatchers.isEvadedBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static util.DurationConv.ofSeconds;
import static util.NameProvider.setNameFor;
import static util.SimpleTimeFactory.atSecond;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import tasks.factories.WorkerUnitFactory;
import world.DynamicObstacle;
import world.RadiusBasedWorldPerspectiveCache;
import world.Trajectory;
import world.TrajectoryFactory;
import world.World;
import world.WorldPerspectiveCache;
import world.pathfinder.StraightEdgePathfinder;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class TaskPlannerTest {

	private static final WorkerUnitFactory wuFact = WorkerUnitFactory.getInstance();
	private static final EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
	private static final TrajectoryFactory trajFact = TrajectoryFactory.getInstance();
	
	// TODO test tight plan

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
	public void testObsoleteEvasions() {
		Polygon shape = geomBuilder.box(-0.25, -0.25, 0.25, 0.25);
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
			geomBuilder.point(3.0, 1.0),
			atSecond(10.0),
			ofSeconds(2.0));

		assertThat("unable to plan task",
			status, equalTo(true));
		assertThat("w1 evaded someone when it shouldn't have",
			workers, not(areEvadedBy(w1)));

		// w = w2, P = (5, 3), t = 10, d = 2
		status = planTask(tp, w2,
			geomBuilder.point(5.0, 3.0),
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
			geomBuilder.point(1.0, 3.0),
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
	public void testStaticObstacles() {
		Polygon obstacle = geomBuilder.box(30., 10., 40., 40.);
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
			geomBuilder.point(60., 20.),
			atSecond(120.),
			ofSeconds(30.));
		
		assertThat("unable to plan task",
			status, equalTo(true));
		assertThat("w collided with obstacle",
			w, not(collideWith(obstacle)));
	}
	
	@Test
	public void testDynamicObstacles() {
		Polygon obstacleShape = geomBuilder.box(-5., -5., 5., 5.);
		Trajectory obstacleTrajectory = trajFact.trajectory(
			new double[] {30., 30.},
			new double[] {40.,  0.},
			new double[] { 0., 40.});
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
			geomBuilder.point(50., 20.),
			atSecond(60.),
			ofSeconds(30.));
		
		assertThat("unable to plan task",
			status, equalTo(true));
		assertThat("w collided with obstacle",
			w, not(collideWith(obstacle)));
		
		System.out.println(w.calcMergedTrajectory().getTrace());
	}

}
