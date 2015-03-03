package scheduler.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.UUID;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.StaticGeometryBuilder;

import org.junit.Test;

import scheduler.Task;
import scheduler.WorkerUnit;
import scheduler.factories.WorkerUnitFactory;
import scheduler.util.IntervalSet.Interval;
import util.UUIDFactory;

public class MappedIntervalSetTest {
	
	private static Task task(LocalDateTime startTime, LocalDateTime finishTime) {
		UUID id = UUIDFactory.uuid("don't care");
		WorkerUnit worker = WorkerUnitFactory.getInstance()
			.createWorkerUnit("don't care", 0, 0);
		ImmutablePoint location = StaticGeometryBuilder.immutablePoint(0, 0);
		Duration duration = Duration.between(startTime, finishTime);
		
		return new Task(id, worker.getReference(), location, startTime, duration);
	}
	
	private static Interval<LocalDateTime> interval(Task task) {
		return new Interval<>(task.getStartTime(), task.getFinishTime());
	}
	
	private static IntervalSet<LocalDateTime> mappedIntervalSet(LocalDateTime... times) {
		TreeMap<LocalDateTime, Task> taskMap = new TreeMap<>();

		for (int i = 0; i < times.length;) {
			LocalDateTime start = times[i++];
			LocalDateTime finish = times[i++];
			
			taskMap.put(start, task(start, finish));
		}
		
		return new MappedIntervalSet<>(taskMap, MappedIntervalSetTest::interval);
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
		IntervalSet<LocalDateTime> taskSet = mappedIntervalSet(
			atSecond(0), atSecond(1),
			atSecond(4), atSecond(5));
		
		IntervalSet<LocalDateTime> timeSet = simpleIntervalSet(
			atSecond(2), atSecond(3),
			atSecond(6), atSecond(7));
		
		IntervalSet<LocalDateTime> res = taskSet.union(timeSet);
		
		IntervalSet<LocalDateTime> expected = simpleIntervalSet(
			atSecond(0), atSecond(1),
			atSecond(2), atSecond(3),
			atSecond(4), atSecond(5),
			atSecond(6), atSecond(7));
		
		assertThat(res, equalTo(expected));
	}
	
	@Test
	public void testDifferenceSet() {
		IntervalSet<LocalDateTime> taskSet = mappedIntervalSet(
			atSecond(0), atSecond(1),
			atSecond(3), atSecond(5));
		
		IntervalSet<LocalDateTime> timeSet = simpleIntervalSet(
			atSecond(1), atSecond(4),
			atSecond(6), atSecond(7));
		
		IntervalSet<LocalDateTime> res = taskSet.difference(timeSet);
		
		IntervalSet<LocalDateTime> expected = simpleIntervalSet(
			atSecond(0), atSecond(1),
			atSecond(4), atSecond(5));
		
		assertThat(res, equalTo(expected));
	}
	
	@Test
	public void testIntersectionSet() {
		IntervalSet<LocalDateTime> taskSet = mappedIntervalSet(
			atSecond(0), atSecond(2),
			atSecond(3), atSecond(5));
		
		IntervalSet<LocalDateTime> timeSet = simpleIntervalSet(
			atSecond(1), atSecond(4),
			atSecond(6), atSecond(7));
		
		IntervalSet<LocalDateTime> res = taskSet.intersection(timeSet);
		
		IntervalSet<LocalDateTime> expected = simpleIntervalSet(
			atSecond(1), atSecond(2),
			atSecond(3), atSecond(4));
		
		assertThat(res, equalTo(expected));
	}

}
