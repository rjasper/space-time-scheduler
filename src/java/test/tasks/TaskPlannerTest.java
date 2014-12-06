package tasks;

import static java.util.Collections.emptyList;
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

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class TaskPlannerTest {

	private static final WorkerUnitFactory wuFact = WorkerUnitFactory.getInstance();
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
		Polygon shape = geomBuilder.box(-0.25, -0.25, 0.25, 0.25);
		WorkerUnit w1 = wuFact.createWorkerUnit(shape, 1.0, 3.0, 5.0, 0.0);
		WorkerUnit w2 = wuFact.createWorkerUnit(shape, 1.0, 2.0, 3.0, 5.0);
		
		setNameFor(w1, "w1");
		setNameFor(w2, "w2");

		Collection<WorkerUnit> workers = Arrays.asList(w1, w2);

		TaskPlanner tp = new TaskPlanner();

		tp.setWorkerPool(workers);
		tp.setStaticObstacles(emptyList());
		tp.setDynamicObstacles(emptyList());

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

}
