package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.trajectory;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToDuration;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.atSecond;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.uuid;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Job;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleAlternative;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.SpaceTimeSlot;

public class NodeSlotBuilderTest {

	private static final ImmutablePolygon NODE_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);

	private static final double NODE_SPEED = 1.0;

	private static Node node(String nodeId, double x, double y) {
		NodeSpecification spec = new NodeSpecification(
			nodeId, NODE_SHAPE, NODE_SPEED, immutablePoint(x, y), atSecond(0));

		return new Node(spec);
	}

	private static SpaceTimeSlot slot(
		double x1, double y1, double t1,
		double x2, double y2, double t2)
	{
		return new SpaceTimeSlot(
			immutablePoint(x1, y1),
			immutablePoint(x2, y2),
			atSecond(t1),
			atSecond(t2));
	}

	private static Job job(String idSeed, Node node, double x, double y, double t, double d) {
		return new Job(uuid(idSeed), node.getReference(), immutablePoint(x, y), atSecond(t), secondsToDuration(d));
	}

	private static IntervalSet<LocalDateTime> interval(double from, double to) {
		return new SimpleIntervalSet<LocalDateTime>()
			.add(atSecond(from), atSecond(to));
	}

	@Test
	public void testFull() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			0, 10, 20, 30));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(false);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			slot(1, 1, 10, 2, 2, 20));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testScheduledJob() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  1.5,  1.5,  2,  0,
			0,  1,  1.5,  1.5,  2,  0,
			0, 10, 12.5, 17.5, 20, 30));
		node.addJob(job("job", node, 1.5, 1.5, 12.5, 5));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(false);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = Arrays.asList(
			slot(1, 1, 10, 1.5, 1.5, 12.5),
			slot(1.5, 1.5, 17.5, 2, 2, 20));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testTrajectoryLock() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  1.5,  1.5,  2,  0,
			0,  1,  1.5,  1.5,  2,  0,
			0, 10, 12.5, 17.5, 20, 30));
		node.addTrajectoryLock(interval(12.5, 17.5));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(false);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = Arrays.asList(
			slot(1, 1, 10, 1.5, 1.5, 12.5),
			slot(1.5, 1.5, 17.5, 2, 2, 20));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testJobUpdate() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  1.5,  1.5,  2,  0,
			0,  1,  1.5,  1.5,  2,  0,
			0, 10, 12.5, 17.5, 20, 30));

		ScheduleAlternative alternative = new ScheduleAlternative();
		alternative.addJob(job("job", node, 1.5, 1.5, 12.5, 5));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(alternative);
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(false);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = Arrays.asList(
			slot(1, 1, 10, 1.5, 1.5, 12.5),
			slot(1.5, 1.5, 17.5, 2, 2, 20));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testTrajectoryUpdate() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			0, 10, 20, 30));

		ScheduleAlternative alternative = new ScheduleAlternative();
		alternative.updateTrajectory(node, trajectory(
			1,  1.5,  1.5,   2,
			1,  1.5,  1.5,   2,
			10, 12.5, 17.5, 20));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(alternative);
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(false);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = Arrays.asList(
			slot(1, 1, 10, 2, 2, 20));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testJobTrajectoryUpdate() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			0, 10, 20, 30));

		ScheduleAlternative alternative = new ScheduleAlternative();
		alternative.addJob(job("job", node, 1.5, 1.5, 12.5, 5));
		alternative.updateTrajectory(node, trajectory(
			1,  1.5,  1.5,   2,
			1,  1.5,  1.5,   2,
			10, 12.5, 17.5, 20));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(alternative);
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(false);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = Arrays.asList(
			slot(1, 1, 10, 1.5, 1.5, 12.5),
			slot(1.5, 1.5, 17.5, 2, 2, 20));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testJobRemoval() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  1.5,  1.5,  2,  0,
			0,  1,  1.5,  1.5,  2,  0,
			0, 10, 12.5, 17.5, 20, 30));
		Job job = job("job", node, 1.5, 1.5, 12.5, 5);
		node.addJob(job);

		ScheduleAlternative alternative = new ScheduleAlternative();
		alternative.addJobRemoval(job);

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(alternative);
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(false);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			slot(1, 1, 10, 2, 2, 20));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testInitialTime() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			0, 10, 20, 30));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(-5));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(false);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			slot(0, 0, 0, 2, 2, 20));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testFrozenHorizon() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			0, 10, 20, 30));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(atSecond(15));
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(false);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			slot(1.5, 1.5, 15, 2, 2, 20));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testFullOverlapping() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			0, 10, 20, 30));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(true);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			new SpaceTimeSlot(immutablePoint(0, 0), immutablePoint(0, 0), atSecond(0), LocalDateTime.MAX));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testFullSingularityOverlapping() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			0, 10, 20, 30));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(10));
		builder.setOverlapping(true);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			new SpaceTimeSlot(immutablePoint(0, 0), immutablePoint(0, 0), atSecond(0), LocalDateTime.MAX));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testScheduledJobsOverlapping() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			5, 10, 20, 25));
		node.addJob(job("job1", node, 0, 0, 0, 2));
		node.addJob(job("job2", node, 0, 0, 28, 2));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(true);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			slot(0, 0, 2, 0, 0, 28));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testTrajectoryLockOverlapping() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			5, 10, 20, 25));
		node.addTrajectoryLock(interval(0, 2));
		node.addTrajectoryLock(interval(28, 30));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(true);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			slot(0, 0, 2, 0, 0, 28));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testJobRemovalOverlapping() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			5, 10, 20, 25));
		Job job1 = job("job1", node, 0, 0, 0, 2);
		Job job2 = job("job2", node, 0, 0, 28, 2);
		node.addJob(job1);
		node.addJob(job2);

		ScheduleAlternative alternative = new ScheduleAlternative();
		alternative.addJobRemoval(job1);
		alternative.addJobRemoval(job2);

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(alternative);
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(true);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			new SpaceTimeSlot(immutablePoint(0, 0), immutablePoint(0, 0), atSecond(0), LocalDateTime.MAX));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testFullInitialTimeOverlapping() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			0, 10, 20, 30));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(LocalDateTime.MIN);
		builder.setStartTime(atSecond(-5));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(true);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			new SpaceTimeSlot(immutablePoint(0, 0), immutablePoint(0, 0), atSecond(0), LocalDateTime.MAX));

		assertThat(slots, equalTo(expected));
	}

	@Test
	public void testFullFrozenHorizonOverlapping() {
		Node node = node("node", 0, 0);
		node.updateTrajectory(trajectory(
			0,  1,  2,  0,
			0,  1,  2,  0,
			0, 10, 20, 30));

		NodeSlotBuilder builder = new NodeSlotBuilder();

		builder.setNode(node);
		builder.setAlternative(new ScheduleAlternative());
		builder.setFrozenHorizonTime(atSecond(5));
		builder.setStartTime(atSecond(10));
		builder.setFinishTime(atSecond(20));
		builder.setOverlapping(true);

		List<SpaceTimeSlot> slots = builder.build();

		List<SpaceTimeSlot> expected = singletonList(
			new SpaceTimeSlot(immutablePoint(0.5, 0.5), immutablePoint(0, 0), atSecond(5), LocalDateTime.MAX));

		assertThat(slots, equalTo(expected));
	}

}
