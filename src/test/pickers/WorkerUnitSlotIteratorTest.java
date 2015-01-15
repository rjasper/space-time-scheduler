package pickers;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import tasks.WorkerUnit;
import tasks.fixtures.WorkerUnitFixtures;
import world.LocalDateTimeFactory;

import com.vividsolutions.jts.geom.Point;

public class WorkerUnitSlotIteratorTest {

	private static LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();
	
	@Test
	public void test() {
		WorkerUnit w1 = WorkerUnitFixtures.withTwoTasks1();
		WorkerUnit w2 = WorkerUnitFixtures.withTwoTasks2();
		
		Collection<WorkerUnit> workers = Arrays.asList(w1, w2);

		Point location = point(0., 0.);
		LocalDateTime earliest = timeFact.hours(3L);
		LocalDateTime latest = timeFact.hours(8L);
		Duration duration = Duration.ofHours(3L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), is(w2));
		assertThat(picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckStartTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = timeFact.hours(3L);
		LocalDateTime latest = timeFact.time(6L, 30L);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), is(w));
	}
	
	@Test
	public void testCheckStartTimeNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = timeFact.hours(3L);
		LocalDateTime latest = timeFact.time(5L, 30L);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);

		assertThat(picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckFinishTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = timeFact.time(6L, 30L);
		LocalDateTime latest = timeFact.hours(11L);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), is(w));
	}
	
	@Test
	public void testCheckFinishTimeNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = timeFact.time(7L, 30L);
		LocalDateTime latest = timeFact.hours(11L);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);

		assertThat(picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckDurationPositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = timeFact.hours(3L);
		LocalDateTime latest = timeFact.hours(11L);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentWorker(), is(w));
	}
	
	@Test
	public void testCheckDurationNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = timeFact.hours(3L);
		LocalDateTime latest = timeFact.hours(11L);
		Duration duration = Duration.ofHours(3L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);
		
		assertThat(picker.hasNext(), is(false));
	}

}
