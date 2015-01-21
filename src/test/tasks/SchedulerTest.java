package tasks;

import static matchers.CollisionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.junit.Assert.*;
import static util.DurationConv.*;
import static util.TimeFactory.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import tasks.factories.WorkerUnitFactory;
import world.World;
import world.fixtures.WorldFixtures;

public class SchedulerTest {

	private static WorkerUnitFactory wFact = new WorkerUnitFactory();

	@BeforeClass
	public static void setUpBeforeClass() {
		wFact.setShape(box(-0.5, -0.5, 0.5, 0.5));
	}

	@Test
	public void test() {
		World world = WorldFixtures.twoRooms();

		WorkerUnit w1 = wFact.createWorkerUnit(11., 31.);
		WorkerUnit w2 = wFact.createWorkerUnit(25., 11.);

		Collection<WorkerUnit> workers = Arrays.asList(w1, w2);

		// top right
		Specification s1 = new Specification(
			box(21, 27, 27, 33), atSecond(0), atSecond(60), ofSeconds(30));
		// bottom left
		Specification s2 = new Specification(
			box( 9,  7, 15, 13), atSecond(0), atSecond(60), ofSeconds(30));
		// bottom right
		Specification s3 = new Specification(
			box(23,  9, 27, 13), atSecond(60), atSecond(120), ofSeconds(30));
		// top left
		Specification s4 = new Specification(
			box( 9, 29, 13, 33), atSecond(60), atSecond(120), ofSeconds(30));

		Scheduler sc = new Scheduler(world, workers);
		
		boolean status;

		status = sc.schedule(s1);
		
		assertThat("unable to schedule s1",
			status, equalTo(true));
		assertThat("collision detected",
			w1, not(workerCollidesWith(w2)));
		
		status = sc.schedule(s2);
		
		assertThat("unable to schedule s2",
			status, equalTo(true));
		assertThat("collision detected",
			w1, not(workerCollidesWith(w2)));
		
		status = sc.schedule(s3);
		
		assertThat("unable to schedule s3",
			status, equalTo(true));
		assertThat("collision detected",
			w1, not(workerCollidesWith(w2)));
		
		status = sc.schedule(s4);
		
		assertThat("unable to schedule s4",
			status, equalTo(true));
		assertThat("collision detected",
			w1, not(workerCollidesWith(w2)));
	}

}
