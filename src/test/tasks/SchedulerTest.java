package tasks;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static matchers.CollisionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.DurationConv.*;
import static util.TimeFactory.*;
import static util.UUIDFactory.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jts.geom.immutable.ImmutablePolygon;

import org.junit.Test;

import tasks.factories.WorkerUnitFactory;
import world.StaticObstacle;
import world.World;
import world.fixtures.WorldFixtures;

import com.google.common.collect.ImmutableList;

public class SchedulerTest {

	private static WorkerUnitFactory wFact = new WorkerUnitFactory();

	@Test
	public void testNoLocation() {
		StaticObstacle obstacle = new StaticObstacle(immutableBox(10, 10, 20, 20));
		World world = new World(ImmutableList.of(obstacle), ImmutableList.of());
		WorkerUnitSpecification ws =
			wFact.createWorkerUnitSpecification("w", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);
		
		Scheduler sc = new Scheduler(world, singleton(ws));
		
		TaskSpecification spec = new TaskSpecification(
			uuid("spec"),
			immutableBox(12, 12, 18, 18),
			atSecond(0),
			atSecond(60), ofSeconds(10));
		
		boolean status = sc.schedule(spec);
		
		assertThat("scheduled task when it shouldn't have",
			status, equalTo(false));
	}
	
	@Test
	public void testAllBusy() {
		WorkerUnitSpecification ws =
			wFact.createWorkerUnitSpecification("w", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);
		
		Scheduler sc = new Scheduler(new World(), singleton(ws));
		
		TaskSpecification ts1 = new TaskSpecification(
			uuid("ts1"),
			immutableBox(-1, -1, 1, 1),
			atSecond(0),
			atSecond(10), ofSeconds(60));
		
		boolean status;
		
		status = sc.schedule(ts1);
		
		assertThat("unable to schedule task",
			status, equalTo(status));
		
		TaskSpecification ts2 = new TaskSpecification(
			uuid("ts2"),
			immutableBox(-1, -1, 1, 1),
			atSecond(20),
			atSecond(30), ofSeconds(10));
		
		status = sc.schedule(ts2);
		
		assertThat("scheduled task when it shouldn't have",
			status, equalTo(false));
	}

	@Test
	public void testComplexTaskSet() {
		World world = WorldFixtures.twoRooms();
	
		ImmutablePolygon shape = immutableBox(-0.5, -0.5, 0.5, 0.5);
		
		WorkerUnitSpecification ws1 =
			wFact.createWorkerUnitSpecification("w1", shape, 1.0, 11, 31, 0);
		WorkerUnitSpecification ws2 =
			wFact.createWorkerUnitSpecification("w2", shape, 1.0, 25, 11, 0);
	
		Collection<WorkerUnitSpecification> workerSpecs = Arrays.asList(ws1, ws2);
	
		// top right
		TaskSpecification s1 = new TaskSpecification(
			uuid("s1"), immutableBox(21, 27, 27, 33), atSecond(0), atSecond(60), ofSeconds(30));
		// bottom left
		TaskSpecification s2 = new TaskSpecification(
			uuid("s2"), immutableBox( 9,  7, 15, 13), atSecond(0), atSecond(60), ofSeconds(30));
		// bottom right
		TaskSpecification s3 = new TaskSpecification(
			uuid("s3"), immutableBox(23,  9, 27, 13), atSecond(60), atSecond(120), ofSeconds(30));
		// top left
		TaskSpecification s4 = new TaskSpecification(
			uuid("s4"), immutableBox( 9, 29, 13, 33), atSecond(60), atSecond(120), ofSeconds(30));
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

}
