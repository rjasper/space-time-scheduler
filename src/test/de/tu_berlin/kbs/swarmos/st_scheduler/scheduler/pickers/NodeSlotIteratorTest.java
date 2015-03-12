package de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.pickers;

import static de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Scheduler.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Node;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.fixtures.NodeFixtures;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.pickers.NodeSlotIterator;

public class NodeSlotIteratorTest {

	@Test
	public void test() {
		Node w1 = NodeFixtures.withTwoJobs1();
		Node w2 = NodeFixtures.withTwoJobs2();
		
		Collection<Node> nodes = Arrays.asList(w1, w2);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(8.0);
		Duration duration = Duration.ofHours(3L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentNode(), is(w2));
		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckStartTimePositive() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(6.5);
		Duration duration = Duration.ofHours(1L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentNode(), is(w));
	}
	
	@Test
	public void testCheckStartTimeNegative() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(5.5);
		Duration duration = Duration.ofHours(1L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckFinishTimePositive() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(6.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentNode(), is(w));
	}
	
	@Test
	public void testCheckFinishTimeNegative() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(7.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckDurationPositive() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(w));
	}
	
	@Test
	public void testCheckDurationNegative() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(3L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, BEGIN_OF_TIME, location, earliest, latest, duration);
		
		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckFrozenHorizonStartTimePositive() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(5.0);
		LocalDateTime latest = atHour(6.0);
		Duration duration = Duration.ofHours(1L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentNode(), is(w));
	}
	
	@Test
	public void testCheckFrozenHorizonStartTimeNegative() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(5.5);
		LocalDateTime latest = atHour(6.0);
		Duration duration = Duration.ofHours(1L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckFrozenHorizonFinishTimePositive() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(6.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentNode(), is(w));
	}
	
	@Test
	public void testCheckFrozenHorizonFinishTimeNegative() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(6.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckFrozenHorizonDurationPositive() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(5.0);
		LocalDateTime frozenHorizon = atHour(5.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(2L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentNode(), is(w));
	}
	
	@Test
	public void testCheckFrozenHorizonDurationNegative() {
		Node w = NodeFixtures.withTwoJobs1();
		
		Collection<Node> nodes = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(5.0);
		LocalDateTime frozenHorizon = atHour(6.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(2L);
		
		NodeSlotIterator picker = new NodeSlotIterator(
			nodes, frozenHorizon, location, earliest, latest, duration);
		
		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

}
