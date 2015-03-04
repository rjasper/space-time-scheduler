package scheduler.pickers;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static scheduler.Scheduler.*;
import static util.TimeFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import scheduler.WorkerUnit;
import scheduler.fixtures.WorkerUnitFixtures;

import com.vividsolutions.jts.geom.Point;

public class WorkerUnitSlotIteratorTest {

	@Test
	public void test() {
		WorkerUnit w1 = WorkerUnitFixtures.withTwoTasks1();
		WorkerUnit w2 = WorkerUnitFixtures.withTwoTasks2();
		
		Collection<WorkerUnit> workers = Arrays.asList(w1, w2);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(8.0);
		Duration duration = Duration.ofHours(3L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, BEGIN_OF_TIME, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), is(w2));
		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckStartTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(6.5);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, BEGIN_OF_TIME, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), is(w));
	}
	
	@Test
	public void testCheckStartTimeNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(5.5);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, BEGIN_OF_TIME, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckFinishTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(6.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, BEGIN_OF_TIME, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), is(w));
	}
	
	@Test
	public void testCheckFinishTimeNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(7.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, BEGIN_OF_TIME, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckDurationPositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, BEGIN_OF_TIME, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentWorker(), is(w));
	}
	
	@Test
	public void testCheckDurationNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(3L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, BEGIN_OF_TIME, location, earliest, latest, duration);
		
		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckFrozenHorizonStartTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(5.0);
		LocalDateTime latest = atHour(6.0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, frozenHorizon, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), is(w));
	}
	
	@Test
	public void testCheckFrozenHorizonStartTimeNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(5.5);
		LocalDateTime latest = atHour(6.0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, frozenHorizon, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckFrozenHorizonFinishTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(6.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, frozenHorizon, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), is(w));
	}
	
	@Test
	public void testCheckFrozenHorizonFinishTimeNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(0);
		LocalDateTime frozenHorizon = atHour(6.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, frozenHorizon, location, earliest, latest, duration);

		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckFrozenHorizonDurationPositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(5.0);
		LocalDateTime frozenHorizon = atHour(5.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(2L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, frozenHorizon, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentWorker(), is(w));
	}
	
	@Test
	public void testCheckFrozenHorizonDurationNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(5.0);
		LocalDateTime frozenHorizon = atHour(6.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(2L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(
			workers, frozenHorizon, location, earliest, latest, duration);
		
		assertThat("picker has next when it shouldn't",
			picker.hasNext(), is(false));
	}

}
