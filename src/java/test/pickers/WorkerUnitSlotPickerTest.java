package pickers;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static java.time.Month.*;
import static jts.geom.factories.StaticJtsFactories.geomFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import tasks.WorkerUnit;
import tasks.WorkerUnitFixtures;

public class WorkerUnitSlotPickerTest {

	@Test
	public void test() {
		WorkerUnit w1 = WorkerUnitFixtures.withTwoTasks1();
		WorkerUnit w2 = WorkerUnitFixtures.withTwoTasks2();
		
		Vector<WorkerUnit> workers = new Vector<>(2);
		workers.add(w1);
		workers.add(w2);
		
		Point location = geomFactory().createPoint(new Coordinate(0., 0.));
		LocalDateTime earliest = LocalDateTime.of(2000, JANUARY, 1, 1, 0);
		LocalDateTime latest = LocalDateTime.of(2000, JANUARY, 1, 6, 0);
		Duration duration = Duration.ofHours(3L);
		
		WorkerUnitSlotPicker picker = new WorkerUnitSlotPicker(workers, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), equalTo(w2));
		assertFalse(picker.hasNext());
	}
	
	@Test
	public void testCheckStartTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = geomFactory().createPoint(new Coordinate(0., 0.));
		LocalDateTime earliest = LocalDateTime.of(2000, JANUARY, 1, 1, 0);
		LocalDateTime latest = LocalDateTime.of(2000, JANUARY, 1, 4, 30);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotPicker picker = new WorkerUnitSlotPicker(workers, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), equalTo(w));
	}
	
	@Test
	public void testCheckStartTimeNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = geomFactory().createPoint(new Coordinate(0., 0.));
		LocalDateTime earliest = LocalDateTime.of(2000, JANUARY, 1, 1, 0);
		LocalDateTime latest = LocalDateTime.of(2000, JANUARY, 1, 3, 30);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotPicker picker = new WorkerUnitSlotPicker(workers, location, earliest, latest, duration);

		assertFalse(picker.hasNext());
	}
	
	@Test
	public void testCheckFinishTimePositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = geomFactory().createPoint(new Coordinate(0., 0.));
		LocalDateTime earliest = LocalDateTime.of(2000, JANUARY, 1, 5, 30);
		LocalDateTime latest = LocalDateTime.of(2000, JANUARY, 1, 9, 0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotPicker picker = new WorkerUnitSlotPicker(workers, location, earliest, latest, duration);
		
		picker.next();
		assertThat(picker.getCurrentWorker(), equalTo(w));
	}
	
	@Test
	public void testCheckFinishTimeNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = geomFactory().createPoint(new Coordinate(0., 0.));
		LocalDateTime earliest = LocalDateTime.of(2000, JANUARY, 1, 6, 30);
		LocalDateTime latest = LocalDateTime.of(2000, JANUARY, 1, 9, 0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotPicker picker = new WorkerUnitSlotPicker(workers, location, earliest, latest, duration);

		assertFalse(picker.hasNext());
	}
	
	@Test
	public void testCheckDurationPositive() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = geomFactory().createPoint(new Coordinate(0., 0.));
		LocalDateTime earliest = LocalDateTime.of(2000, JANUARY, 1, 1, 0);
		LocalDateTime latest = LocalDateTime.of(2000, JANUARY, 1, 9, 0);
		Duration duration = Duration.ofHours(1L);
		
		WorkerUnitSlotPicker picker = new WorkerUnitSlotPicker(workers, location, earliest, latest, duration);

//		assertThat(picker.next(), equalTo(w));
		picker.next();
		assertThat(picker.getCurrentWorker(), equalTo(w));
	}
	
	@Test
	public void testCheckDurationNegative() {
		WorkerUnit w = WorkerUnitFixtures.withTwoTasks1();
		
		Collection<WorkerUnit> workers = Collections.singleton(w);

		Point location = geomFactory().createPoint(new Coordinate(0., 0.));
		LocalDateTime earliest = LocalDateTime.of(2000, JANUARY, 1, 1, 0);
		LocalDateTime latest = LocalDateTime.of(2000, JANUARY, 1, 9, 0);
		Duration duration = Duration.ofHours(3L);
		
		WorkerUnitSlotPicker picker = new WorkerUnitSlotPicker(workers, location, earliest, latest, duration);
		
		assertFalse(picker.hasNext());
	}

}
