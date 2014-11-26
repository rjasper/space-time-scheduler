package tasks;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import util.DurationConv;
import world.LocalDateTimeFactory;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class TaskPlannerTest {

	private static final WorkerUnitFactory wuFact = WorkerUnitFactory.getInstance();
	private static final LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();
	private static final EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

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
	public void test() {
//		fail("Not yet implemented"); // TODO

		Polygon shape = geomBuilder.box(-0.5, -0.5, 0.5, 0.5);
		WorkerUnit w1 = wuFact.createWorkerUnit(shape, 1.0, 3.0, 5.0, 0.0);
		WorkerUnit w2 = wuFact.createWorkerUnit(shape, 1.0, 2.0, 3.0, 5.0);

		Collection<WorkerUnit> workerPool = Arrays.asList(w1, w2);

		TaskPlanner tp = new TaskPlanner();

		tp.setWorkerPool(workerPool);
		tp.setStaticObstacles(emptyList());
		tp.setDynamicObstacles(emptyList());

		boolean status;

		// w = w1, P = (3, 1), t = 10, d = 2
		status = planTask(tp, w1,
			geomBuilder.point(3.0, 1.0),
			timeFact.seconds(10.0),
			DurationConv.ofSeconds(2.0));

		assertThat("unable to plan task", status, equalTo(true));

		// w = w2, P = (5, 3), t = 10, d = 2
		status = planTask(tp, w2,
			geomBuilder.point(5.0, 3.0),
			timeFact.seconds(10.0),
			DurationConv.ofSeconds(2.0));

		assertThat("unable to plan task", status, equalTo(true));

		// TODO check evasions

		// w = w1, P = (1, 3), t = 4, d = 2
		status = planTask(tp, w2,
			geomBuilder.point(1.0, 3.0),
			timeFact.seconds(4.0),
			DurationConv.ofSeconds(2.0));

		assertThat("unable to plan task", status, equalTo(true));

		// TODO check evasions
	}

}
