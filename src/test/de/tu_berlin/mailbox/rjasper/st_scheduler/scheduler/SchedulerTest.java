package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.CollisionMatchers.nodeCollidesWith;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.JobMatchers.satisfies;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.JobMatchers.satisfy;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.PathFactory.spatialPath;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.trajectory;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToDuration;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToDurationSafe;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToTime;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.atSecond;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.uuid;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult.TrajectoryUpdate;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.factories.NodeFactory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.fixtures.WorldFixtures;
import de.tu_berlin.mailbox.rjasper.util.UUIDFactory;

public class SchedulerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static NodeFactory nFact = new NodeFactory();

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
	public void testNoLocation() throws CollisionException {
		StaticObstacle obstacle = new StaticObstacle(immutableBox(10, 10, 20, 20));
		World world = new World(ImmutableList.of(obstacle), ImmutableList.of());
		NodeSpecification ns =
			nFact.createNodeSpecification("n", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);

		Scheduler sc = new Scheduler(world);
		sc.addNode(ns);

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
	public void testAllBusy() throws CollisionException {
		NodeSpecification ns =
			nFact.createNodeSpecification("n", immutableBox(-1, -1, 1, 1), 1.0, 0, 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		JobSpecification js1 = new JobSpecification(
			uuid("j1"),
			immutableBox(-1, -1, 1, 1),
			atSecond(0),
			atSecond(10), secondsToDurationSafe(60));

		ScheduleResult result;

		result = sc.schedule(js1);
		sc.commit(result.getTransactionId());

		assertThat("unable to schedule job",
			result.isSuccess(), equalTo(true));
		assertThat("scheduled job doesn't meet specification",
			result.getJobs().get(uuid("j1")), satisfies(js1));

		JobSpecification js2 = new JobSpecification(
			uuid("j2"),
			immutableBox(-1, -1, 1, 1),
			atSecond(20),
			atSecond(30), secondsToDurationSafe(10));

		result = sc.schedule(js2);

		assertThat("scheduled job when it shouldn't have",
			result.isError(), equalTo(true));
	}

	@Test
	public void testScheduleBetweenJobs() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

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
	public void testComplexJobSet() throws CollisionException {
		World world = WorldFixtures.twoRooms();

		ImmutablePolygon shape = immutableBox(-0.5, -0.5, 0.5, 0.5);

		NodeSpecification ns1 =
			nFact.createNodeSpecification("w1", shape, 1.0, 11, 31, 0);
		NodeSpecification ns2 =
			nFact.createNodeSpecification("w2", shape, 1.0, 25, 11, 0);

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
		NodeReference w1 = sc.addNode(ns1);
		NodeReference w2 = sc.addNode(ns2);

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
	public void testAddNodeAfterFrozenHorizon() throws CollisionException {
		NodeSpecification ns = new NodeSpecification(
			"n", NODE_SHAPE, NODE_SPEED, immutablePoint(0, 0), atSecond(10));

		Scheduler sc = new Scheduler(new World());
		sc.setPresentTime(atSecond(5));

		sc.addNode(ns); // no exception
	}

	@Test
	public void testAddNodeAtFrozenHorizon() throws CollisionException {
		NodeSpecification ns = new NodeSpecification(
			"n", NODE_SHAPE, NODE_SPEED, immutablePoint(0, 0), atSecond(10));

		Scheduler sc = new Scheduler(new World());
		sc.setPresentTime(atSecond(10));

		sc.addNode(ns); // no exception
	}

	@Test
	public void testAddNodeBeforeFrozenHorizon() throws CollisionException {
		NodeSpecification ns = new NodeSpecification(
			"n", NODE_SHAPE, NODE_SPEED, immutablePoint(0, 0), atSecond(10));

		Scheduler sc = new Scheduler(new World());
		sc.setPresentTime(atSecond(20));

		thrown.expect(IllegalArgumentException.class);
		sc.addNode(ns);
	}

	@Test
	public void testAddNodeWithinStaticObstacle() throws CollisionException {
		StaticObstacle obstacle = new StaticObstacle(immutableBox(-1, -1, 1, 1));
		World world = new World(ImmutableList.of(obstacle), ImmutableList.of());
		Scheduler sc = new Scheduler(world);

		NodeSpecification spec = nodeSpec("node", 0, 0);

		thrown.expect(CollisionException.class);
		sc.addNode(spec);
	}

	@Test
	public void testAddNodeWithinDynamicObstacle() throws CollisionException {
		DynamicObstacle obstacle = new DynamicObstacle(
			immutableBox(-1, -1, 1, 1),
			trajectory(-1, 1, -1, 1, -1, 1));
		World world = new World(ImmutableList.of(), ImmutableList.of(obstacle));
		Scheduler sc = new Scheduler(world);

		NodeSpecification spec = nodeSpec("node", 0, 0);

		thrown.expect(CollisionException.class);
		sc.addNode(spec);
	}

	@Test
	public void testAddNodeWithinNodeObstacle() throws CollisionException {
		NodeSpecification spec1 = nodeSpec("node1", 0, 0);
		NodeSpecification spec2 = nodeSpec("node2", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(spec1);

		thrown.expect(CollisionException.class);
		sc.addNode(spec2);
	}

	@Test
	public void testScheduleBeforeFrozenHorizon() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		sc.setPresentTime(atSecond(10));

		JobSpecification js2 = new JobSpecification(
			uuid("j2"),
			immutablePoint(0, 0),
			atSecond(0),
			atSecond(9),
			secondsToDuration(2));

		ScheduleResult res = sc.schedule(js2);

		assertThat("scheduled job when it shouldn't have",
			res.isError(), is(true));
	}

	@Test
	public void testScheduleAfterFrozenHorizon() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		ScheduleResult res;

		JobSpecification js1 = jobSpec("j1", 2, 2, 6, 2);
		res = scheduleJob(sc, js1);

		assertThat(res.isSuccess(), is(true));

		sc.setPresentTime(atSecond(10));
		LocalDateTime frozenHorizon = sc.getFrozenHorizonTime(); // atSecond(10)

		JobSpecification js2 = new JobSpecification(
			uuid("j2"),
			immutablePoint(0, 2),
			atSecond(0),
			atSecond(20),
			secondsToDuration(2));

		res = sc.schedule(js2);

		assertThat("schedule failed",
			res.isSuccess(), is(true));

		Job j2 = res.getJobs().get(uuid("j2"));

		assertThat("job start time before frozen horizon",
			j2.getStartTime().isBefore(frozenHorizon), is(false));
		assertThat("no trajectory updates",
			res.getTrajectoryUpdates().isEmpty(), is(false));
		assertThat("trajectory start time before frozen horizon",
			res.getTrajectoryUpdates().stream()
			.map(TrajectoryUpdate::getTrajectory)
			.allMatch(t -> !t.getStartTime().isBefore(frozenHorizon)),
			is(true));
	}

	@Test
	public void testScheduleMultipleAlternatives() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		ScheduleResult res;

		// splits the origin trajectory
		// otherwise j1 would lock the whole trajectory
		res = scheduleJob(sc, JobSpecification.createSS(
			uuid("j0"),
			immutablePoint(0, 0),
			atSecond(10),
			atSecond(10),
			secondsToDuration(1)));

		assertThat("j0 was not scheduled",
			res.isSuccess(), is(true));

		JobSpecification js1 = new JobSpecification(
			uuid("j1"),
			immutablePoint(1, 1),
			atSecond( 0),
			atSecond(20),
			secondsToDuration(1));

		res = sc.schedule(js1);

		assertThat("j1 was not scheduled",
			res.isSuccess(), is(true));
		assertThat(res.getJobs().get(uuid("j1")), satisfies(js1));

		JobSpecification js2 = new JobSpecification(
			uuid("j2"),
			immutablePoint(2, 2),
			atSecond( 0),
			atSecond(20),
			secondsToDuration(1));

		res = sc.schedule(js2);

		assertThat("j2 was not scheduled",
			res.isSuccess(), is(true));
		assertThat(res.getJobs().get(uuid("j2")), satisfies(js2));
	}

	@Test
	public void testScheduleDependenciesEmpty() {
		Scheduler sc = new Scheduler(new World());

		ScheduleResult res = sc.schedule(emptyList(), depGraph());

		assertThat("scheduling was no success",
			res.isSuccess(), is(true));
	}

	@Test
	public void testScheduleDependenciesSingle() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		JobSpecification js = JobSpecification.createSS(
			uuid("j1"),
			immutablePoint(1, 1),
			atSecond(0),
			atSecond(20),
			secondsToDuration(1));

		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		addDependency(depGraph, "j1");

		ScheduleResult res = sc.schedule(singleton(js), depGraph);

		assertThat("scheduling was no success",
			res.isSuccess(), is(true));

		Job j = res.getJobs().get(uuid("j1"));

		assertThat("job was not scheduled",
			j, not(is(nullValue())));
		assertThat("job was not correctly scheduled",
			j, satisfies(js));
	}

	@Test
	public void testScheduleDependencyTwo() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		JobSpecification js1 = JobSpecification.createSS(
			uuid("j1"),
			immutablePoint(1, 1),
			atSecond(0),
			atSecond(20),
			secondsToDuration(1));
		JobSpecification js2 = JobSpecification.createSS(
			uuid("j2"),
			immutablePoint(2, 2),
			atSecond(0),
			atSecond(20),
			secondsToDuration(1));

		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		addDependency(depGraph, "j1");
		addDependency(depGraph, "j2", "j1");

		ScheduleResult res = sc.schedule(Arrays.asList(js1, js2), depGraph);

		assertThat("scheduling was no success",
			res.isSuccess(), is(true));

		Job j1 = res.getJobs().get(uuid("j1"));
		Job j2 = res.getJobs().get(uuid("j2"));

		assertThat("j1 was not scheduled",
			j1, not(is(nullValue())));
		assertThat("j2 was not scheduled",
			j2, not(is(nullValue())));
		assertThat("j1 was not correctly scheduled",
			j1, satisfies(js1));
		assertThat("j2 was not correctly scheduled",
			j2, satisfies(js2));
		assertThat("j2 was not correctly scheduled after j1",
			j1.getFinishTime().isAfter(j2.getStartTime()), is(false));
	}

	@Test
	public void testScheduleDependencyTwoMargin() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		Duration margin = secondsToDuration(2);
		sc.setInterDependencyMargin(margin);

		JobSpecification js1 = JobSpecification.createSS(
			uuid("j1"),
			immutablePoint(1, 1),
			atSecond(0),
			atSecond(20),
			secondsToDuration(1));
		JobSpecification js2 = JobSpecification.createSS(
			uuid("j2"),
			immutablePoint(2, 2),
			atSecond(0),
			atSecond(20),
			secondsToDuration(1));

		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		addDependency(depGraph, "j1");
		addDependency(depGraph, "j2", "j1");

		ScheduleResult res = sc.schedule(Arrays.asList(js1, js2), depGraph);

		assertThat("scheduling was no success",
			res.isSuccess(), is(true));

		Job j1 = res.getJobs().get(uuid("j1"));
		Job j2 = res.getJobs().get(uuid("j2"));

		assertThat("j1 was not scheduled",
			j1, not(is(nullValue())));
		assertThat("j2 was not scheduled",
			j2, not(is(nullValue())));
		assertThat("j1 was not correctly scheduled",
			j1, satisfies(js1));
		assertThat("j2 was not correctly scheduled",
			j2, satisfies(js2));
		assertThat("j2 was not correctly scheduled after j1",
			j1.getFinishTime().plus(margin).isAfter(j2.getStartTime()), is(false));
	}

	@Test
	public void testScheduleDependencyInconsistence1() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		JobSpecification js = jobSpec(
			"j1",
			atSecond(0),
			atSecond(0),
			secondsToDuration(1));

		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("specifications and dependencies inconsistent");

		sc.schedule(singleton(js), depGraph);
	}

	@Test
	public void testScheduleDependencyInconsistence2() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		SimpleDirectedGraph<UUID, DefaultEdge> depGraph = depGraph();
		addDependency(depGraph, "j1");

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("specifications and dependencies inconsistent");

		sc.schedule(emptyList(), depGraph);
	}

	@Test
	public void testSchedulePeriodicSameLocation() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		ImmutableList<UUID> jobIds = ImmutableList.of(uuid("j1"), uuid("j2"), uuid("j3"));

		PeriodicJobSpecification ps = new PeriodicJobSpecification(
			jobIds,
			immutablePoint(0, 0),
			true,
			secondsToDuration(1),
			atSecond(0),
			secondsToDuration(2));

		ScheduleResult res = sc.schedule(ps);

		assertThat("unable to schedule job",
			res.isSuccess(), equalTo(true));
		assertThat(res.getJobs().values(), satisfy(ps));
	}

	@Test
	public void testSchedulePeriodicIndependentLocation() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		ImmutableList<UUID> jobIds = ImmutableList.of(uuid("j1"), uuid("j2"), uuid("j3"));

		PeriodicJobSpecification ps = new PeriodicJobSpecification(
			jobIds,
			immutablePoint(0, 0),
			false,
			secondsToDuration(1),
			atSecond(0),
			secondsToDuration(2));

		ScheduleResult res = sc.schedule(ps);

		assertThat("unable to schedule job",
			res.isSuccess(), equalTo(true));
		assertThat(res.getJobs().values(), satisfy(ps));
	}

	@Test
	public void testSchedulePeriodicSameLocationTight() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		ImmutableList<UUID> jobIds = ImmutableList.of(uuid("j1"), uuid("j2"), uuid("j3"));

		PeriodicJobSpecification ps = new PeriodicJobSpecification(
			jobIds,
			immutablePoint(0, 0),
			true,
			secondsToDuration(1),
			atSecond(0),
			secondsToDuration(1));

		ScheduleResult res = sc.schedule(ps);

		assertThat("unable to schedule job",
			res.isSuccess(), equalTo(true));
		assertThat(res.getJobs().values(), satisfy(ps));
	}

	@Test
	public void testSchedulePeriodicIndependentLocationTight() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		ImmutableList<UUID> jobIds = ImmutableList.of(uuid("j1"), uuid("j2"), uuid("j3"));

		PeriodicJobSpecification ps = new PeriodicJobSpecification(
			jobIds,
			immutablePoint(0, 0),
			false,
			secondsToDuration(1),
			atSecond(0),
			secondsToDuration(1));

		ScheduleResult res = sc.schedule(ps);

		assertThat("unable to schedule job",
			res.isSuccess(), equalTo(true));
		assertThat(res.getJobs().values(), satisfy(ps));
	}

	@Test
	public void testSchedulePeriodicSameLocationTooTight() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		ImmutableList<UUID> jobIds = ImmutableList.of(uuid("j1"), uuid("j2"), uuid("j3"));

		PeriodicJobSpecification ps = new PeriodicJobSpecification(
			jobIds,
			immutablePoint(0, 0),
			true,
			secondsToDuration(1),
			atSecond(-0.5),
			secondsToDuration(1));

		ScheduleResult res = sc.schedule(ps);

		assertThat("scheduled impossible periodic job",
			res.isError(), is(true));
	}

	@Test
	public void testSchedulePeriodicIndependentLocationTooTight() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		ImmutableList<UUID> jobIds = ImmutableList.of(uuid("j1"), uuid("j2"), uuid("j3"));

		PeriodicJobSpecification ps = new PeriodicJobSpecification(
			jobIds,
			immutablePoint(0, 0),
			false,
			secondsToDuration(1),
			atSecond(-0.5),
			secondsToDuration(1));

		ScheduleResult res = sc.schedule(ps);

		assertThat("scheduled impossible periodic job",
			res.isError(), is(true));
	}

	@Test
	public void testUnschedule() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);
		NodeReference wref = sc.getNodeReference("n");

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
	public void testUnscheduleUnknown() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		thrown.expect(IllegalArgumentException.class);

		sc.unschedule(uuid("job"));
	}

	@Test
	public void testUnscheduleWithinFrozenHorizon() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);
		NodeReference wref = sc.getNodeReference("n");

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
	public void testUnscheduleBetweenJobs() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);
		NodeReference wref = sc.getNodeReference("n");

		scheduleJob(sc, jobSpec("j1", 0, 1, 3, 1));
		scheduleJob(sc, jobSpec("j2", 1, 2, 6, 1));
		scheduleJob(sc, jobSpec("j3", 2, 1, 9, 1));

		Job j2 = sc.getJob(uuid("j2"));

		ScheduleResult res = sc.unschedule(uuid("j2"));

		assertThat("job wasn't unscheduled",
			res.isSuccess(), is(true));

		sc.commit(res.getTransactionId());

		assertThat("job still known",
			wref.hasJob(j2), is(false));

		Trajectory traj = wref.getTrajectories(atSecond(4), atSecond(9)).iterator().next();

		assertThat("didn't replan trajectory as expected",
			traj.getSpatialPath().trace(), equalTo(spatialPath(0, 1, 2, 1).trace()));
	}

	@Test
	public void testRescheduleJobDifferentLocation() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);
		NodeReference wref = sc.getNodeReference("n");

		scheduleJob(sc, jobSpec("job", 1, 1, 10, 1));

		Job job = sc.getJob(uuid("job"));

		JobSpecification newSpec = jobSpec("job", 2, 2, 10, 1);
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
	public void testRescheduleJobDifferentTime() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);
		NodeReference wref = sc.getNodeReference("n");

		scheduleJob(sc, jobSpec("job", 1, 1, 10, 1));

		Job job = sc.getJob(uuid("job"));

		JobSpecification newSpec = jobSpec("job", 1, 1, 20, 1);
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
	public void testRemoveJob() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		NodeReference wref = sc.getNodeReference("n");

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
	public void testRemoveLockedJob() throws CollisionException {
		NodeSpecification ns = nodeSpec("n", 0, 0);

		Scheduler sc = new Scheduler(new World());
		sc.addNode(ns);

		NodeReference wref = sc.getNodeReference("n");

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
