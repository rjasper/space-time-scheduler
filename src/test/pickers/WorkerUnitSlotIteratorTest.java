package pickers;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import scheduler.WorkerUnit;
import tasks.fixtures.WorkerUnitFixtures;

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
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(6.5);
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
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(5.5);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);

		assertThat(picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckFinishTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(6.5);
		LocalDateTime latest = atHour(11.0);
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
		LocalDateTime earliest = atHour(7.5);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);

		assertThat(picker.hasNext(), is(false));
	}
	
	@Test
	public void testCheckDurationPositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = point(0., 0.);
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(11.0);
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
		LocalDateTime earliest = atHour(3.0);
		LocalDateTime latest = atHour(11.0);
		Duration duration = Duration.ofHours(3L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);
		
		assertThat(picker.hasNext(), is(false));
	}

}
