package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.IdleSlot;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Schedule;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleAlternative;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.fixtures.NodeFixtures;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.NodeSlotBuilder;
import de.tu_berlin.mailbox.rjasper.util.function.TriFunction;

public class NodeIdleSlotIteratorTest {
	
	private NodeIdleSlotIterator makeIterator(
		Iterable<Node> nodes,
		LocalDateTime frozenHorizonTime,
		Point location,
		LocalDateTime earliest,
		LocalDateTime latest,
		Duration duration)
	{
		Schedule schedule = new Schedule();
		for (Node n : nodes)
			schedule.addNode(n);
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		TriFunction<Node, LocalDateTime, LocalDateTime, Iterator<IdleSlot>> slotGenerator = (node, from, to) -> {
			NodeSlotBuilder builder = new NodeSlotBuilder();

			builder.setOverlapping(true);
			builder.setNode(node);
			builder.setSchedule(schedule);
			builder.setAlternative(alternative);
			builder.setFrozenHorizonTime(frozenHorizonTime);
			builder.setStartTime(from);
			builder.setFinishTime(to);

			return builder.build().iterator();
		};
		
		return new NodeIdleSlotIterator(
			nodes, slotGenerator, frozenHorizonTime, location, earliest, latest, duration);
	}

	@Test
	public void test() {
		Node n1 = NodeFixtures.withTwoJobs1();
		Node n2 = NodeFixtures.withTwoJobs2();

		Collection<Node> nodes = Arrays.asList(n1, n2);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(8.0);
		Duration duration = Duration.ofHours(3L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n2));
		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckStartTimePositive() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(6.5);
		Duration duration = Duration.ofHours(1L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckStartTimeNegative() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(5.5);
		Duration duration = Duration.ofHours(1L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckFinishTimePositive() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(6.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckFinishTimeNegative() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(7.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckDurationPositive() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckDurationNegative() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(3L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckFrozenHorizonStartTimePositive() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(5.0);
		LocalDateTime latest = atHour(6.0);
		Duration duration = Duration.ofHours(1L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckFrozenHorizonStartTimeNegative() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(5.5);
		LocalDateTime latest = atHour(6.0);
		Duration duration = Duration.ofHours(1L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckFrozenHorizonFinishTimePositive() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(6.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckFrozenHorizonFinishTimeNegative() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(6.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

	@Test
	public void testCheckFrozenHorizonDurationPositive() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(5.0);
		LocalDateTime frozenHorizon = atHour(5.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(2L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(n));
	}

	@Test
	public void testCheckFrozenHorizonDurationNegative() {
		Node n = NodeFixtures.withTwoJobs1();

		Collection<Node> nodes = Collections.singleton(n);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(5.0);
		LocalDateTime frozenHorizon = atHour(6.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(2L);

		NodeIdleSlotIterator picker = makeIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

}
