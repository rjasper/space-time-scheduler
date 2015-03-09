package scheduler;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static matchers.CollisionMatchers.*;
import static matchers.TaskMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeConv.*;
import static util.TimeFactory.*;
import static util.UUIDFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.Test;

import scheduler.ScheduleResult.TrajectoryUpdate;
import scheduler.factories.WorkerUnitFactory;
import util.UUIDFactory;
import world.StaticObstacle;
import world.World;
import world.fixtures.WorldFixtures;

import com.google.common.collect.ImmutableList;

public class SchedulerTest {
	
	private static WorkerUnitFactory wFact = new WorkerUnitFactory();
	
	private static final ImmutablePolygon WORKER_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double WORKER_SPEED = 1.0;
	
	private static WorkerUnitSpecification workerSpec(String workerId, double x, double y) {
		return new WorkerUnitSpecification(
			workerId, WORKER_SHAPE, WORKER_SPEED, immutablePoint(x, y), atSecond(0));
	}
	
	private static TaskSpecification taskSpec(String taskIdSeed, double x, double y, double t, double d) {
		UUID taskId = uuid(taskIdSeed);
		ImmutablePoint location = immutablePoint(x, y);
		LocalDateTime startTime = secondsToTime(t, atSecond(0));
		Duration duration = secondsToDuration(d);
		
		return new TaskSpecification(taskId, location, startTime, startTime, duration);
	}

	private static TaskSpecification taskSpec(
		String taskIdSeed,
		LocalDateTime earliestStartTime,
		LocalDateTime latestFinishTime,
		Duration duration)
	{
		return new TaskSpecification(
			uuid(taskIdSeed),
			immutablePoint(0, 0),
			earliestStartTime,
			latestFinishTime,
			duration);
	}

	private static SimpleDirectedGraph<UUID, DefaultEdge> depGraph() {
		return new SimpleDirectedGraph<>(DefaultEdge.class);
	}

	private static void addDependency(
		SimpleDirectedGraph<UUID, DefaultEdge> graph,
		String taskIdSeed,
		String... dependencies)
	{
		UUID taskId = uuid(taskIdSeed);
		graph.addVertex(taskId);
		
		Arrays.stream(dependencies)
			.map(UUIDFactory::uuid)
			.forEach(d -> graph.addEdge(taskId, d));
	}

	private static ScheduleResult scheduleTask(Scheduler scheduler, TaskSpecification taskSpec) {
		ScheduleResult res = scheduler.schedule(taskSpec);
		scheduler.commit(res.getTransactionId());
		
		return res;
	}
	
	@Test
	public void testNoLocation() {
		StaticObstacle obstacle = new StaticObstacle(immutableBox(10, 10, 20, 20));
		World world = new World(ImmutableList.of(obstacle), ImmutableList.of());
		WorkerUnitSpecification ws =
			wFact.createWorkerUnitSpecification("w", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);
		
		Scheduler sc = new Scheduler(world);
		sc.addWorker(ws);
		
		TaskSpecification spec = new TaskSpecification(
			uuid("spec"),
			immutableBox(12, 12, 18, 18),
			atSecond(0),
			atSecond(60), secondsToDurationSafe(10));
		
		ScheduleResult result = sc.schedule(spec);
		
		assertThat("scheduled task when it shouldn't have",
			result.isError(), equalTo(true));
	}
	
	@Test
	public void testAllBusy() {
		WorkerUnitSpecification ws =
			wFact.createWorkerUnitSpecification("w", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addWorker(ws);
		
		TaskSpecification ts1 = new TaskSpecification(
			uuid("t1"),
			immutableBox(-1, -1, 1, 1),
			atSecond(0),
			atSecond(10), secondsToDurationSafe(60));
		
		ScheduleResult result;
		
		result = sc.schedule(ts1);
		sc.commit(result.getTransactionId());
		
		assertThat("unable to schedule task",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled task doesn't meet specification",
			result.getTasks().get(uuid("t1")), satisfies(ts1));
		
		TaskSpecification ts2 = new TaskSpecification(
			uuid("t2"),
			immutableBox(-1, -1, 1, 1),
			atSecond(20),
			atSecond(30), secondsToDurationSafe(10));
		
		result = sc.schedule(ts2);
		
		assertThat("scheduled task when it shouldn't have",
			result.isError(), equalTo(true));
	}

	@Test
	public void testComplexTaskSet() {
		World world = WorldFixtures.twoRooms();
	
		ImmutablePolygon shape = immutableBox(-0.5, -0.5, 0.5, 0.5);
		
		WorkerUnitSpecification ws1 =
			wFact.createWorkerUnitSpecification("w1", shape, 1.0, 11, 31, 0);
		WorkerUnitSpecification ws2 =
			wFact.createWorkerUnitSpecification("w2", shape, 1.0, 25, 11, 0);
	
		// top right
		TaskSpecification s1 = new TaskSpecification(
			uuid("s1"), immutableBox(21, 27, 27, 33), atSecond(0), atSecond(60), secondsToDurationSafe(30));
		// bottom left
		TaskSpecification s2 = new TaskSpecification(
			uuid("s2"), immutableBox( 9,  7, 15, 13), atSecond(0), atSecond(60), secondsToDurationSafe(30));
		// bottom right
		TaskSpecification s3 = new TaskSpecification(
			uuid("s3"), immutableBox(23,  9, 27, 13), atSecond(60), atSecond(120), secondsToDurationSafe(30));
		// top left
		TaskSpecification s4 = new TaskSpecification(
			uuid("s4"), immutableBox( 9, 29, 13, 33), atSecond(60), atSecond(120), secondsToDurationSafe(30));
		
		Scheduler sc = new Scheduler(world);
		WorkerUnitReference w1 = sc.addWorker(ws1);
		WorkerUnitReference w2 = sc.addWorker(ws2);

		ScheduleResult result;
	
		result = sc.schedule(s1);
		sc.commit(result.getTransactionId());
		
		assertThat("unable to schedule s1",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled task doesn't meet specification",
			result.getTasks().get(uuid("s1")), satisfies(s1));
		assertThat("collision detected",
			w1, not(workerCollidesWith(w2)));
		
		result = sc.schedule(s2);
		sc.commit(result.getTransactionId());
		
		assertThat("unable to schedule s2",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled task doesn't meet specification",
			result.getTasks().get(uuid("s2")), satisfies(s2));
		assertThat("collision detected",
			w1, not(workerCollidesWith(w2)));
		
		result = sc.schedule(s3);
		sc.commit(result.getTransactionId());
		
		assertThat("unable to schedule s3",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled task doesn't meet specification",
			result.getTasks().get(uuid("s3")), satisfies(s3));
		assertThat("collision detected",
			w1, not(workerCollidesWith(w2)));
		
		result = sc.schedule(s4);
		sc.commit(result.getTransactionId());
		
		assertThat("unable to schedule s4",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled task doesn't meet specification",
			result.getTasks().get(uuid("s4")), satisfies(s4));
		assertThat("collision detected",
			w1, not(workerCollidesWith(w2)));
	}
	
	@Test
	public void testIncreasePresentTime() {
		Scheduler sc = new Scheduler(new World());
		
		sc.setPresentTime(atSecond(0));
		
		assertThat("present was not set",
			sc.getPresentTime(), equalTo( atSecond(0) ));
		
		sc.setPresentTime(atSecond(1));

		assertThat("present was not increased",
			sc.getPresentTime(), equalTo( atSecond(1) ));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDecreasePresentTime() {
		Scheduler sc = new Scheduler(new World());
		
		sc.setPresentTime(atSecond(1));
		
		assertThat("present was not set",
			sc.getPresentTime(), equalTo( atSecond(1) ));
		
		sc.setPresentTime(atSecond(0)); // should throw exception
		
		fail("decreased present was accepted");
	}
	
	@Test
	public void testIncreaseFrozenHorizonByPresent() {
		Scheduler sc = new Scheduler(new World());
		
		sc.setPresentTime(atSecond(0));
		sc.setFrozenHorizonDuration(secondsToDurationSafe(1));
		
		assertThat("horizon was not set",
			sc.getFrozenHorizonTime(), equalTo( atSecond(1) ));
		
		sc.setPresentTime(atSecond(1));

		assertThat("horizon was not increased",
			sc.getFrozenHorizonTime(), equalTo( atSecond(2) ));
	}
	
	@Test
	public void testIncreaseFrozenHorizonByDuration() {
		Scheduler sc = new Scheduler(new World());
		
		sc.setPresentTime(atSecond(0));
		sc.setFrozenHorizonDuration(secondsToDurationSafe(1));
		
		assertThat("horizon was not set",
			sc.getFrozenHorizonTime(), equalTo( atSecond(1) ));
		
		sc.setFrozenHorizonDuration(secondsToDurationSafe(2));

		assertThat("horizon was not increased",
			sc.getFrozenHorizonTime(), equalTo( atSecond(2) ));
	}
	
	@Test
	public void testDecreaseFrozenHorizon() {
		Scheduler sc = new Scheduler(new World());
		
		sc.setPresentTime(atSecond(0));
		sc.setFrozenHorizonDuration(secondsToDurationSafe(2));
		
		assertThat("horizon was not set",
			sc.getFrozenHorizonTime(), equalTo( atSecond(2) ));
		
		sc.setFrozenHorizonDuration(secondsToDurationSafe(1));

		assertThat("horizon was modified",
			sc.getFrozenHorizonTime(), equalTo( atSecond(2) ));
		
		sc.setPresentTime(atSecond(2));

		assertThat("horizon was not increased",
			sc.getFrozenHorizonTime(), equalTo( atSecond(3) ));
	}
	
	@Test
	public void testAddWorkerAfterFrozenHorizon() {
		WorkerUnitSpecification ws = new WorkerUnitSpecification(
			"w", WORKER_SHAPE, WORKER_SPEED, immutablePoint(0, 0), atSecond(10));
		
		Scheduler sc = new Scheduler(new World());
		sc.setPresentTime(atSecond(5));
		
		sc.addWorker(ws); // no exception
	}
	
	@Test
	public void testAddWorkerAtFrozenHorizon() {
		WorkerUnitSpecification ws = new WorkerUnitSpecification(
			"w", WORKER_SHAPE, WORKER_SPEED, immutablePoint(0, 0), atSecond(10));
		
		Scheduler sc = new Scheduler(new World());
		sc.setPresentTime(atSecond(10));
		
		sc.addWorker(ws); // no exception
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddWorkerBeforeFrozenHorizon() {
		WorkerUnitSpecification ws = new WorkerUnitSpecification(
			"w", WORKER_SHAPE, WORKER_SPEED, immutablePoint(0, 0), atSecond(10));
		
		Scheduler sc = new Scheduler(new World());
		sc.setPresentTime(atSecond(20));
		
		sc.addWorker(ws);
	}
	
	@Test
	public void testScheduleBeforeFrozenHorizon() {
		WorkerUnitSpecification ws = workerSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addWorker(ws);

		sc.setPresentTime(atSecond(10));
		
		TaskSpecification ts2 = new TaskSpecification(
			uuid("t2"),
			immutablePoint(0, 0),
			atSecond(0),
			atSecond(9),
			secondsToDuration(2));
		
		ScheduleResult res = sc.schedule(ts2);
		
		assertThat("scheduled task when it shouldn't have",
			res.isError(), is(true));
	}
	
	@Test
	public void testScheduleAfterFrozenHorizon() {
		WorkerUnitSpecification ws = workerSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addWorker(ws);

		ScheduleResult res;
		
		TaskSpecification ts1 = taskSpec("t1", 2, 2, 6, 2);
		res = scheduleTask(sc, ts1);
		
		assertThat(res.isSuccess(), is(true));
		
		sc.setPresentTime(atSecond(10));
		LocalDateTime frozenHorizon = sc.getFrozenHorizonTime(); // atSecond(10)
		
		TaskSpecification ts2 = new TaskSpecification(
			uuid("t2"),
			immutablePoint(0, 2),
			atSecond(0),
			atSecond(20),
			secondsToDuration(2));
		
		res = sc.schedule(ts2);
		
		assertThat("schedule failed",
			res.isSuccess(), is(true));
		
		Task t2 = res.getTasks().get(uuid("t2"));
		
		assertThat("task start time before frozen horizon",
			t2.getStartTime().isBefore(frozenHorizon), is(false));
		assertThat("no trajectory updates",
			res.getTrajectoryUpdates().isEmpty(), is(false));
		assertThat("trajectory start time before frozen horizon",
			res.getTrajectoryUpdates().stream()
			.map(TrajectoryUpdate::getTrajectory)
			.allMatch(t -> !t.getStartTime().isBefore(frozenHorizon)),
			is(true));
	}
	
	@Test
	public void testScheduleDependenciesEmpty() {
		Scheduler sc = new Scheduler(new World());
		
		ScheduleResult res = sc.schedule(emptyList(), depGraph());
		
		assertThat("scheduling was no success",
			res.isSuccess(), is(true));
	}
	
	@Test
	public void testScheduleDependenciesSingle() {
		WorkerUnitSpecification ws = workerSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addWorker(ws);
		
		TaskSpecification ts = taskSpec(
			"t1",
			atSecond(0),
			atSecond(0),
			secondsToDuration(1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		addDependency(depGraph, "t1");
		
		ScheduleResult res = sc.schedule(singleton(ts), depGraph);
		
		assertThat("scheduling was no success",
			res.isSuccess(), is(true));
		
		Task t = res.getTasks().get(uuid("t1"));
		
		assertThat("task was not scheduled",
			t, not(is(nullValue())));
		assertThat("task was not correctly scheduled",
			t, satisfies(ts));
	}
	
	@Test
	public void testScheduleDependencyTwo() {
		WorkerUnitSpecification ws = workerSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addWorker(ws);
		
		Duration margin = secondsToDuration(2);
		sc.setInterDependencyMargin(margin);

		TaskSpecification ts1 = taskSpec(
			"t1",
			atSecond(0),
			atSecond(20),
			secondsToDuration(1));
		TaskSpecification ts2 = taskSpec(
			"t2",
			atSecond(0),
			atSecond(20),
			secondsToDuration(1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		addDependency(depGraph, "t1");
		addDependency(depGraph, "t2", "t1");

		ScheduleResult res = sc.schedule(Arrays.asList(ts1, ts2), depGraph);
		
		assertThat("scheduling was no success",
			res.isSuccess(), is(true));
		
		Task t1 = res.getTasks().get(uuid("t1"));
		Task t2 = res.getTasks().get(uuid("t2"));
		
		assertThat("t1 was not scheduled",
			t1, not(is(nullValue())));
		assertThat("t2 was not scheduled",
			t2, not(is(nullValue())));
		assertThat("t1 was not correctly scheduled",
			t1, satisfies(ts1));
		assertThat("t2 was not correctly scheduled",
			t2, satisfies(ts2));
		assertThat("t2 was not correctly scheduled after t1",
			t1.getFinishTime().plus(margin).isAfter(t2.getStartTime()), is(false));
	}

	@Test(expected = IllegalStateException.class)
	public void testScheduleDependencyInconsistence1() {
		WorkerUnitSpecification ws = workerSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addWorker(ws);
		
		TaskSpecification ts = taskSpec(
			"t1",
			atSecond(0),
			atSecond(0),
			secondsToDuration(1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		
		sc.schedule(singleton(ts), depGraph);
	}

	@Test(expected = IllegalStateException.class)
	public void testScheduleDependencyInconsistence2() {
		WorkerUnitSpecification ws = workerSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addWorker(ws);
		
		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		addDependency(depGraph, "t1");
		
		sc.schedule(emptyList(), depGraph);
	}

}
