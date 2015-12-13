package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.point;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler.BEGIN_OF_TIME;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.trajectory;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToDuration;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.atHour;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.atSecond;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.uuid;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Job;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleAlternative;

public class NodeSlotIteratorTest {

	private static final ImmutablePolygon NODE_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);

	private static final double NODE_SPEED = 1.0;

	private static Node node(String nodeId, double x, double y) {
		NodeSpecification spec = new NodeSpecification(
			nodeId, NODE_SHAPE, NODE_SPEED, immutablePoint(x, y), atSecond(0));

		return new Node(spec);
	}

	private static Job job(String jobIdSeed, Node node, double x, double y, double t, double d) {
		return new Job(
			uuid(jobIdSeed),
			node.getReference(),
			immutablePoint(x, y),
			atSecond(t),
			secondsToDuration(d));
	}

	private static int H = 3600;

	private static Node withTwoJobs1() {
		Node node = node("withTwoJobs1", 0., 0.);

		node.updateTrajectory(trajectory(
			0, -3600, -3600,    0,    0,    0,
			0,      0,    0, 3600, 3600,    0,
			0,    2*H,  3*H,  9*H, 12*H, 14*H));
		node.addJob(job("job1", node, -3600.,    0., 2*H, 3*H));
		node.addJob(job("job2", node,     0., 3600., 9*H, 3*H));

		return node;
	}

	private static Node withTwoJobs2() {
		Node node = node("withTwoJobs2", 0., 0.);

		node.updateTrajectory(trajectory(
			0,    0,    0, -3600, -3600,    0,
			0, 3600, 3600,     0,     0,    0,
			0,  2*H,  3*H,   9*H,  12*H, 14*H));
		node.addJob(job("job1", node, -3600.,    0., 2*H, 2*H));
		node.addJob(job("job2", node,     0., 3600., 9*H, 2*H));

		return node;
	}

	private NodeSlotIterator makeIterator(
		Iterable<Node> nodes,
		LocalDateTime frozenHorizonTime,
		Point location,
		LocalDateTime earliest,
		LocalDateTime latest,
		Duration duration)
	{
		ScheduleAlternative alternative = new ScheduleAlternative();

		return new NodeSlotIterator(
			nodes, alternative, frozenHorizonTime, location, earliest, latest, duration);
	}

	@Test
	public void test() {
		Node n1 = withTwoJobs1();
		Node n2 = withTwoJobs2();

		Collection<Node> nodes = Arrays.asList(n1, n2);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(8.0);
		Duration duration = Duration.ofHours(3L);

		NodeSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n2));
		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckStartTimePositive() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(6.5);
		Duration duration = Duration.ofHours(1L);

		NodeSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckStartTimeNegative() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(5.5);
		Duration duration = Duration.ofHours(1L);

		NodeSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckFinishTimePositive() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(6.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);

		NodeSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckFinishTimeNegative() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(7.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);

		NodeSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckDurationPositive() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);

		NodeSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckDurationNegative() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(3L);

		NodeSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckFrozenHorizonStartTimePositive() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(5.0);
		LocalDateTime latest = atHour(6.0);
		Duration duration = Duration.ofHours(1L);

		NodeSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckFrozenHorizonStartTimeNegative() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(5.5);
		LocalDateTime latest = atHour(6.0);
		Duration duration = Duration.ofHours(1L);

		NodeSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckFrozenHorizonFinishTimePositive() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(6.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);

		NodeSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckFrozenHorizonFinishTimeNegative() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(6.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);

		NodeSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckFrozenHorizonDurationPositive() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(5.0);
		LocalDateTime frozenHorizon = atHour(5.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(2L);

		NodeSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckFrozenHorizonDurationNegative() {
		Node n = withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(5.0);
		LocalDateTime frozenHorizon = atHour(6.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(2L);

		NodeSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

}
