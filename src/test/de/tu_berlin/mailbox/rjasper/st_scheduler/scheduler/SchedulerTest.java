package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.CollisionMatchers.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.JobMatchers.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.PathFactory.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.*;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Job;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.PeriodicJobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult.TrajectoryUpdate;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.factories.NodeFactory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.fixtures.WorldFixtures;
import de.tu_berlin.mailbox.rjasper.util.UUIDFactory;

public class SchedulerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private static NodeFactory wFact = new NodeFactory();
	
	private static final ImmutablePolygon NODE_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double NODE_SPEED = 1.0;
	
	private static NodeSpecification nodeSpec(String nodeId, double x, double y) {
		return new NodeSpecification(
			nodeId, NODE_SHAPE, NODE_SPEED, immutablePoint(x, y), atSecond(0));
	}
	
	private static JobSpecification jobSpec(String jobIdSeed, double x, double y, double t, double d) {
		UUID jobId = uuid(jobIdSeed);
		ImmutablePoint location = immutablePoint(x, y);
		LocalDateTime startTime = secondsToTime(t, atSecond(0));
		Duration duration = secondsToDuration(d);
		
		return new JobSpecification(jobId, location, startTime, startTime, duration);
	}

	private static JobSpecification jobSpec(
		String jobIdSeed,
		LocalDateTime earliestStartTime,
		LocalDateTime latestFinishTime,
		Duration duration)
	{
		return new JobSpecification(
			uuid(jobIdSeed),
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
		String jobIdSeed,
		String... dependencies)
	{
		UUID jobId = uuid(jobIdSeed);
		graph.addVertex(jobId);
		
		Arrays.stream(dependencies)
			.map(UUIDFactory::uuid)
			.forEach(d -> graph.addEdge(jobId, d));
	}

	private static ScheduleResult scheduleJob(Scheduler scheduler, JobSpecification jobSpec) {
		ScheduleResult res = scheduler.schedule(jobSpec);
		scheduler.commit(res.getTransactionId());
		
		return res;
	}
	
	@Test
	public void testNoLocation() {
		StaticObstacle obstacle = new StaticObstacle(immutableBox(10, 10, 20, 20));
		World world = new World(ImmutableList.of(obstacle), ImmutableList.of());
		NodeSpecification ws =
			wFact.createNodeSpecification("w", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);
		
		Scheduler sc = new Scheduler(world);
		sc.addNode(ws);
		
		JobSpecification spec = new JobSpecification(
			uuid("spec"),
			immutableBox(12, 12, 18, 18),
			atSecond(0),
			atSecond(60), secondsToDurationSafe(10));
		
		ScheduleResult result = sc.schedule(spec);
		
		assertThat("scheduled job when it shouldn't have",
			result.isError(), equalTo(true));
	}
	
	@Test
	public void testAllBusy() {
		NodeSpecification ws =
			wFact.createNodeSpecification("w", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		
		JobSpecification ts1 = new JobSpecification(
			uuid("t1"),
			immutableBox(-1, -1, 1, 1),
			atSecond(0),
			atSecond(10), secondsToDurationSafe(60));
		
		ScheduleResult result;
		
		result = sc.schedule(ts1);
		sc.commit(result.getTransactionId());
		
		assertThat("unable to schedule job",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled job doesn't meet specification",
			result.getJobs().get(uuid("t1")), satisfies(ts1));
		
		JobSpecification ts2 = new JobSpecification(
			uuid("t2"),
			immutableBox(-1, -1, 1, 1),
			atSecond(20),
			atSecond(30), secondsToDurationSafe(10));
		
		result = sc.schedule(ts2);
		
		assertThat("scheduled job when it shouldn't have",
			result.isError(), equalTo(true));
	}
	
	@Test
	public void testScheduleBetweenJobs() {
		NodeSpecification ns = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);
		
		scheduleJob(sc, jobSpec("job1", 0, 1, 3, 1));
		scheduleJob(sc, jobSpec("job2", 1, 2, 9, 1));
		
		JobSpecification spec = jobSpec("job3", 1, 1, 6, 1);
		ScheduleResult res = sc.schedule(spec);
		
		assertThat("unable to schedule job",
			res.isSuccess(), is(true));
		
		assertThat("job does not satisfy specification",
			res.getJobs().get(uuid("job3")), satisfies(spec));
	}

	@Test
	public void testComplexJobSet() {
		World world = WorldFixtures.twoRooms();
	
		ImmutablePolygon shape = immutableBox(-0.5, -0.5, 0.5, 0.5);
		
		NodeSpecification ws1 =
			wFact.createNodeSpecification("w1", shape, 1.0, 11, 31, 0);
		NodeSpecification ws2 =
			wFact.createNodeSpecification("w2", shape, 1.0, 25, 11, 0);
	
		// top right
		JobSpecification s1 = new JobSpecification(
			uuid("s1"), immutableBox(21, 27, 27, 33), atSecond(0), atSecond(60), secondsToDurationSafe(30));
		// bottom left
		JobSpecification s2 = new JobSpecification(
			uuid("s2"), immutableBox( 9,  7, 15, 13), atSecond(0), atSecond(60), secondsToDurationSafe(30));
		// bottom right
		JobSpecification s3 = new JobSpecification(
			uuid("s3"), immutableBox(23,  9, 27, 13), atSecond(60), atSecond(120), secondsToDurationSafe(30));
		// top left
		JobSpecification s4 = new JobSpecification(
			uuid("s4"), immutableBox( 9, 29, 13, 33), atSecond(60), atSecond(120), secondsToDurationSafe(30));
		
		Scheduler sc = new Scheduler(world);
		NodeReference w1 = sc.addNode(ws1);
		NodeReference w2 = sc.addNode(ws2);

		ScheduleResult result;
	
		result = sc.schedule(s1);
		sc.commit(result.getTransactionId());
		
		assertThat("unable to schedule s1",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled job doesn't meet specification",
			result.getJobs().get(uuid("s1")), satisfies(s1));
		assertThat("collision detected",
			w1, not(nodeCollidesWith(w2)));
		
		result = sc.schedule(s2);
		sc.commit(result.getTransactionId());
		
		assertThat("unable to schedule s2",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled job doesn't meet specification",
			result.getJobs().get(uuid("s2")), satisfies(s2));
		assertThat("collision detected",
			w1, not(nodeCollidesWith(w2)));
		
		result = sc.schedule(s3);
		sc.commit(result.getTransactionId());
		
		assertThat("unable to schedule s3",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled job doesn't meet specification",
			result.getJobs().get(uuid("s3")), satisfies(s3));
		assertThat("collision detected",
			w1, not(nodeCollidesWith(w2)));
		
		result = sc.schedule(s4);
		sc.commit(result.getTransactionId());
		
		assertThat("unable to schedule s4",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled job doesn't meet specification",
			result.getJobs().get(uuid("s4")), satisfies(s4));
		assertThat("collision detected",
			w1, not(nodeCollidesWith(w2)));
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
	
	@Test
	public void testDecreasePresentTime() {
		Scheduler sc = new Scheduler(new World());
		
		sc.setPresentTime(atSecond(1));
		
		assertThat("present was not set",
			sc.getPresentTime(), equalTo( atSecond(1) ));
		
		thrown.expect(IllegalArgumentException.class);
		
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
	public void testAddNodeAfterFrozenHorizon() {
		NodeSpecification ws = new NodeSpecification(
			"w", NODE_SHAPE, NODE_SPEED, immutablePoint(0, 0), atSecond(10));
		
		Scheduler sc = new Scheduler(new World());
		sc.setPresentTime(atSecond(5));
		
		sc.addNode(ws); // no exception
	}
	
	@Test
	public void testAddNodeAtFrozenHorizon() {
		NodeSpecification ws = new NodeSpecification(
			"w", NODE_SHAPE, NODE_SPEED, immutablePoint(0, 0), atSecond(10));
		
		Scheduler sc = new Scheduler(new World());
		sc.setPresentTime(atSecond(10));
		
		sc.addNode(ws); // no exception
	}
	
	@Test
	public void testAddNodeBeforeFrozenHorizon() {
		NodeSpecification ws = new NodeSpecification(
			"w", NODE_SHAPE, NODE_SPEED, immutablePoint(0, 0), atSecond(10));
		
		Scheduler sc = new Scheduler(new World());
		sc.setPresentTime(atSecond(20));
		
		thrown.expect(IllegalArgumentException.class);
		
		sc.addNode(ws);
	}
	
	@Test
	public void testScheduleBeforeFrozenHorizon() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);

		sc.setPresentTime(atSecond(10));
		
		JobSpecification ts2 = new JobSpecification(
			uuid("t2"),
			immutablePoint(0, 0),
			atSecond(0),
			atSecond(9),
			secondsToDuration(2));
		
		ScheduleResult res = sc.schedule(ts2);
		
		assertThat("scheduled job when it shouldn't have",
			res.isError(), is(true));
	}
	
	@Test
	public void testScheduleAfterFrozenHorizon() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);

		ScheduleResult res;
		
		JobSpecification ts1 = jobSpec("t1", 2, 2, 6, 2);
		res = scheduleJob(sc, ts1);
		
		assertThat(res.isSuccess(), is(true));
		
		sc.setPresentTime(atSecond(10));
		LocalDateTime frozenHorizon = sc.getFrozenHorizonTime(); // atSecond(10)
		
		JobSpecification ts2 = new JobSpecification(
			uuid("t2"),
			immutablePoint(0, 2),
			atSecond(0),
			atSecond(20),
			secondsToDuration(2));
		
		res = sc.schedule(ts2);
		
		assertThat("schedule failed",
			res.isSuccess(), is(true));
		
		Job t2 = res.getJobs().get(uuid("t2"));
		
		assertThat("job start time before frozen horizon",
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
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		
		JobSpecification ts = jobSpec(
			"t1",
			atSecond(0),
			atSecond(0),
			secondsToDuration(1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		addDependency(depGraph, "t1");
		
		ScheduleResult res = sc.schedule(singleton(ts), depGraph);
		
		assertThat("scheduling was no success",
			res.isSuccess(), is(true));
		
		Job t = res.getJobs().get(uuid("t1"));
		
		assertThat("job was not scheduled",
			t, not(is(nullValue())));
		assertThat("job was not correctly scheduled",
			t, satisfies(ts));
	}
	
	@Test
	public void testScheduleDependencyTwo() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		
		Duration margin = secondsToDuration(2);
		sc.setInterDependencyMargin(margin);

		JobSpecification ts1 = jobSpec(
			"t1",
			atSecond(0),
			atSecond(20),
			secondsToDuration(1));
		JobSpecification ts2 = jobSpec(
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
		
		Job t1 = res.getJobs().get(uuid("t1"));
		Job t2 = res.getJobs().get(uuid("t2"));
		
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

	@Test
	public void testScheduleDependencyInconsistence1() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		
		JobSpecification ts = jobSpec(
			"t1",
			atSecond(0),
			atSecond(0),
			secondsToDuration(1));
		
		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		
		thrown.expect(IllegalStateException.class);
		
		sc.schedule(singleton(ts), depGraph);
	}

	@Test
	public void testScheduleDependencyInconsistence2() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		
		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		addDependency(depGraph, "t1");
		
		thrown.expect(IllegalStateException.class);
		
		sc.schedule(emptyList(), depGraph);
	}
	
	@Test
	public void testSchedulePeriodicSameLocation() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		
		ImmutableList<UUID> jobIds = ImmutableList.of(uuid("t1"), uuid("t2"), uuid("t3"));
		
		PeriodicJobSpecification ps = new PeriodicJobSpecification(
			jobIds,
			immutablePoint(0, 0),
			true,
			secondsToDuration(1),
			atSecond(0),
			secondsToDuration(2));
		
		ScheduleResult res = sc.schedule(ps);
		
		assertThat(res.getJobs().values(), satisfy(ps));
	}
	
	@Test
	public void testSchedulePeriodicIndependentLocation() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		
		ImmutableList<UUID> jobIds = ImmutableList.of(uuid("t1"), uuid("t2"), uuid("t3"));
		
		PeriodicJobSpecification ps = new PeriodicJobSpecification(
			jobIds,
			immutablePoint(0, 0),
			false,
			secondsToDuration(1),
			atSecond(0),
			secondsToDuration(2));
		
		ScheduleResult res = sc.schedule(ps);
		
		assertThat(res.getJobs().values(), satisfy(ps));
	}
	
	@Test
	public void testUnschedule() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		NodeReference wref = sc.getNodeReference("w");
		
		scheduleJob(sc, jobSpec("job", 1, 1, 2, 1));
		
		Job job = sc.getJob(uuid("job"));
		
		ScheduleResult res = sc.unschedule(uuid("job"));
		
		assertThat("job wasn't unscheduled",
			res.isSuccess(), is(true));
		
		sc.commit(res.getTransactionId());
		
		assertThat("job still known",
			wref.hasJob(job), is(false));
		assertThat("didn't replan trajectory as expected",
			wref.interpolateLocation(atSecond(2)), equalTo(immutablePoint(0, 0)));
	}
	
	@Test
	public void testUnscheduleUnknown() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		
		thrown.expect(IllegalArgumentException.class);
		
		sc.unschedule(uuid("job"));
	}
	
	@Test
	public void testUnscheduleWithinFrozenHorizon() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		NodeReference wref = sc.getNodeReference("w");
		
		scheduleJob(sc, jobSpec("job", 1, 0, 2, 1));
		
		Job job = sc.getJob(uuid("job"));
		ImmutablePoint expectedLocation = wref.interpolateLocation(atSecond(1));

		sc.setPresentTime(atSecond(1));

		ScheduleResult res = sc.unschedule(uuid("job"));
		
		assertThat("job wasn't unscheduled",
			res.isSuccess(), is(true));
		
		sc.commit(res.getTransactionId());
		
		assertThat("job still known",
			wref.hasJob(job), is(false));
		assertThat("didn't replan trajectory as expected",
			wref.interpolateLocation(atSecond(2)), equalTo(expectedLocation));
	}
	
	@Test
	public void testUnscheduleBetweenJobs() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		NodeReference wref = sc.getNodeReference("w");
		
		scheduleJob(sc, jobSpec("t1", 0, 1, 3, 1));
		scheduleJob(sc, jobSpec("t2", 1, 2, 6, 1));
		scheduleJob(sc, jobSpec("t3", 2, 1, 9, 1));
		
		Job t2 = sc.getJob(uuid("t2"));

		ScheduleResult res = sc.unschedule(uuid("t2"));
		
		assertThat("job wasn't unscheduled",
			res.isSuccess(), is(true));
		
		sc.commit(res.getTransactionId());
		
		assertThat("job still known",
			wref.hasJob(t2), is(false));
		
		Trajectory traj = wref.getTrajectories(atSecond(4), atSecond(9)).iterator().next();
		
		assertThat("didn't replan trajectory as expected",
			traj.getSpatialPath(), equalTo(spatialPath(0, 1, 2, 1)));
	}
	
	@Test
	public void testRescheduleJob() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		NodeReference wref = sc.getNodeReference("w");
		
		scheduleJob(sc, jobSpec("job", 0, 0, 0, 1));
		
		Job job = sc.getJob(uuid("job"));

		JobSpecification newSpec = jobSpec("job", 0, 0, 20, 1);
		ScheduleResult res = sc.reschedule(newSpec);
		
		assertThat("job wasn't rescheduled",
			res.isSuccess(), is(true));
		
		sc.commit(res.getTransactionId());
		
		assertThat("job unknown",
			wref.hasJob(job), is(false));
		
		Job rescheduled = sc.getJob(uuid("job"));
		
		assertThat(rescheduled, satisfies(newSpec));
	}
	
	@Test
	public void testRemoveJob() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		
		NodeReference wref = sc.getNodeReference("w");

		ScheduleResult res = scheduleJob(sc, jobSpec("job", 0, 0, 0, 1));
		
		assertThat("job was not scheduled",
			res.isSuccess(), is(true));
		
		Job job = res.getJobs().get(uuid("job"));
		
		assertThat("job was not assigned to node",
			wref.hasJob(job), is(true));
		
		sc.removeJob(uuid("job"));

		assertThat("job to be removed was not removed",
			wref.hasJob(job), is(false));
	}
	
	@Test
	public void testRemoveLockedJob() {
		NodeSpecification ws = nodeSpec("w", 0, 0);
		
		Scheduler sc = new Scheduler(new World());
		sc.addNode(ws);
		
		NodeReference wref = sc.getNodeReference("w");

		ScheduleResult res;
		
		res = scheduleJob(sc, jobSpec("job", 0, 0, 0, 1));
		Job job = res.getJobs().get(uuid("job"));
		
		assertThat("job was not scheduled",
			res.isSuccess(), is(true));
		
		assertThat("job was not assigned to node",
			wref.hasJob(job), is(true));
		
		res = sc.unschedule(uuid("job")); // pending job removal
		
		assertThat("job not scheduled for removal",
			res.isSuccess(), is(true));
		
		thrown.expect(IllegalStateException.class);
		
		sc.removeJob(uuid("job"));
	}

}
