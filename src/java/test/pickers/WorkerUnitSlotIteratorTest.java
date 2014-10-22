package pickers;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static java.time.Month.*;
import static jts.geom.factories.StaticJtsFactories.geomFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import tasks.SpecificationFactory;
import tasks.WorkerUnit;
import tasks.WorkerUnitFactory;
import tasks.WorkerUnitFixtures;
import world.LocalDateTimeFactory;

public class WorkerUnitSlotIteratorTest {

	private static EnhancedGeometryBuilder gBuilder = EnhancedGeometryBuilder.getInstance();
	private static LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();
	
	@Test
	public void test() {
		WorkerUnit w1 = WorkerUnitFixtures.withTwoTasks1();
		WorkerUnit w2 = WorkerUnitFixtures.withTwoTasks2();
		
		Collection<WorkerUnit> workers = Arrays.asList(w1, w2);

		Point location = gBuilder.point(0., 0.);
		LocalDateTime earliest = timeFact.hours(3L);
		LocalDateTime latest = timeFact.hours(8L);
		Duration duration = Duration.ofHours(3L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), equalTo(w2));
		assertFalse(picker.hasNext());
	}
	
	@Test
	public void testCheckStartTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = gBuilder.point(0., 0.);
		LocalDateTime earliest = timeFact.hours(3L);
		LocalDateTime latest = timeFact.time(6L, 30L);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), equalTo(w));
	}
	
	@Test
	public void testCheckStartTimeNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = gBuilder.point(0., 0.);
		LocalDateTime earliest = timeFact.hours(3L);
		LocalDateTime latest = timeFact.time(5L, 30L);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);

		assertFalse(picker.hasNext());
	}
	
	@Test
	public void testCheckFinishTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = gBuilder.point(0., 0.);
		LocalDateTime earliest = timeFact.time(6L, 30L);
		LocalDateTime latest = timeFact.hours(11L);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), equalTo(w));
	}
	
	@Test
	public void testCheckFinishTimeNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = gBuilder.point(0., 0.);
		LocalDateTime earliest = timeFact.time(7L, 30L);
		LocalDateTime latest = timeFact.hours(11L);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);

		assertFalse(picker.hasNext());
	}
	
	@Test
	public void testCheckDurationPositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = gBuilder.point(0., 0.);
		LocalDateTime earliest = timeFact.hours(3L);
		LocalDateTime latest = timeFact.hours(11L);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);

		picker.next();
		assertThat(picker.getCurrentWorker(), equalTo(w));
	}
	
	@Test
	public void testCheckDurationNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = gBuilder.point(0., 0.);
		LocalDateTime earliest = timeFact.hours(3L);
		LocalDateTime latest = timeFact.hours(11L);
		Duration duration = Duration.ofHours(3L);
		
		WorkerUnitSlotIterator picker = new WorkerUnitSlotIterator(workers, location, earliest, latest, duration);
		
		assertFalse(picker.hasNext());
	}

}
