package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.UUID;

import org.junit.Test;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Job;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.factories.NodeFactory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSet;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.MappedIntervalSet;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.SimpleIntervalSet;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSet.Interval;
import de.tu_berlin.mailbox.rjasper.util.UUIDFactory;

public class MappedIntervalSetTest {
	
	private static Job job(LocalDateTime startTime, LocalDateTime finishTime) {
		UUID id = UUIDFactory.uuid("don't care");
		Node node = NodeFactory.getInstance()
			.createNode("don't care", 0, 0);
		ImmutablePoint location = StaticGeometryBuilder.immutablePoint(0, 0);
		Duration duration = Duration.between(startTime, finishTime);
		
		return new Job(id, node.getReference(), location, startTime, duration);
	}
	
	private static Interval<LocalDateTime> interval(Job job) {
		return new Interval<>(job.getStartTime(), job.getFinishTime());
	}
	
	private static IntervalSet<LocalDateTime> mappedIntervalSet(LocalDateTime... times) {
		TreeMap<LocalDateTime, Job> jobMap = new TreeMap<>();

		for (int i = 0; i < times.length;) {
			LocalDateTime start = times[i++];
			LocalDateTime finish = times[i++];
			
			jobMap.put(start, job(start, finish));
		}
		
		return new MappedIntervalSet<>(jobMap, MappedIntervalSetTest::interval);
	}
	
	private static IntervalSet<LocalDateTime> simpleIntervalSet(LocalDateTime... times) {
		if (times.length % 2 != 0)
			throw new IllegalArgumentException("invalid number of times");
		
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		for (int i = 0; i < times.length;)
			set.add(times[i++], times[i++]);
		
		return set;
	}

	@Test
	public void testUnionSet() {
		IntervalSet<LocalDateTime> jobSet = mappedIntervalSet(
			atSecond(0), atSecond(1),
			atSecond(4), atSecond(5));
		
		IntervalSet<LocalDateTime> timeSet = simpleIntervalSet(
			atSecond(2), atSecond(3),
			atSecond(6), atSecond(7));
		
		IntervalSet<LocalDateTime> res = jobSet.union(timeSet);
		
		IntervalSet<LocalDateTime> expected = simpleIntervalSet(
			atSecond(0), atSecond(1),
			atSecond(2), atSecond(3),
			atSecond(4), atSecond(5),
			atSecond(6), atSecond(7));
		
		assertThat(res, equalTo(expected));
	}
	
	@Test
	public void testDifferenceSet() {
		IntervalSet<LocalDateTime> jobSet = mappedIntervalSet(
			atSecond(0), atSecond(1),
			atSecond(3), atSecond(5));
		
		IntervalSet<LocalDateTime> timeSet = simpleIntervalSet(
			atSecond(1), atSecond(4),
			atSecond(6), atSecond(7));
		
		IntervalSet<LocalDateTime> res = jobSet.difference(timeSet);
		
		IntervalSet<LocalDateTime> expected = simpleIntervalSet(
			atSecond(0), atSecond(1),
			atSecond(4), atSecond(5));
		
		assertThat(res, equalTo(expected));
	}
	
	@Test
	public void testIntersectionSet() {
		IntervalSet<LocalDateTime> jobSet = mappedIntervalSet(
			atSecond(0), atSecond(2),
			atSecond(3), atSecond(5));
		
		IntervalSet<LocalDateTime> timeSet = simpleIntervalSet(
			atSecond(1), atSecond(4),
			atSecond(6), atSecond(7));
		
		IntervalSet<LocalDateTime> res = jobSet.intersection(timeSet);
		
		IntervalSet<LocalDateTime> expected = simpleIntervalSet(
			atSecond(1), atSecond(2),
			atSecond(3), atSecond(4));
		
		assertThat(res, equalTo(expected));
	}

}
