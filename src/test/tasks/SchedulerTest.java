package tasks;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static matchers.CollisionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.DurationConv.*;
import static util.TimeFactory.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import tasks.factories.WorkerUnitFactory;
import world.StaticObstacle;
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

		WorkerUnitSpecification ws1 = wFact.createWorkerUnitSpecification(11., 31.);
		WorkerUnitSpecification ws2 = wFact.createWorkerUnitSpecification(25., 11.);

		Collection<WorkerUnitSpecification> workerSpecs = Arrays.asList(ws1, ws2);

		// top right
		TaskSpecification s1 = new TaskSpecification(
			box(21, 27, 27, 33), atSecond(0), atSecond(60), ofSeconds(30));
		// bottom left
		TaskSpecification s2 = new TaskSpecification(
			box( 9,  7, 15, 13), atSecond(0), atSecond(60), ofSeconds(30));
		// bottom right
		TaskSpecification s3 = new TaskSpecification(
			box(23,  9, 27, 13), atSecond(60), atSecond(120), ofSeconds(30));
		// top left
		TaskSpecification s4 = new TaskSpecification(
			box( 9, 29, 13, 33), atSecond(60), atSecond(120), ofSeconds(30));

		Scheduler sc = new Scheduler(world, workerSpecs);
		
		List<WorkerUnitReference> refs = sc.getWorkerReferences();
		WorkerUnitReference w1 = refs.get(0), w2 = refs.get(1);
		
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

	@Test
	public void testNoLocation() {
		StaticObstacle obstacle = new StaticObstacle(box(10, 10, 20, 20));
		World world = new World(singletonList(obstacle), emptyList());
		WorkerUnitSpecification ws =
			wFact.createWorkerUnitSpecification(box(-1, -1, 1, 1), 1.0, 0, 0, 0);
		
		Scheduler sc = new Scheduler(world, singleton(ws));
		
		TaskSpecification spec = new TaskSpecification(
			box(12, 12, 18, 18),
			atSecond(0),
			atSecond(60),
			ofSeconds(10));
		
		boolean status = sc.schedule(spec);
		
		assertThat("scheduled task when it shouldn't have",
			status, equalTo(false));
	}
	
	// TODO all busy

}
