package tasks;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeFactory.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import scheduler.IdleSlot;
import scheduler.WorkerUnit;
import tasks.fixtures.WorkerUnitFixtures;

public class WorkerUnitTest {
	

	@Test
	public void testIdleSubSet() {
		WorkerUnit worker = WorkerUnitFixtures.withThreeTasks();
		
		Collection<IdleSlot> slots = worker.idleSlots(
			atHour( 3),
			atHour(17)
		);
		
		Collection<IdleSlot> expected = Arrays.asList(
			new IdleSlot(immutablePoint( 0,  0), immutablePoint(10, 10), atHour( 0), atHour( 6)),
			new IdleSlot(immutablePoint(10, 10), immutablePoint(20, 10), atHour( 7), atHour(12)),
			new IdleSlot(immutablePoint(20, 10), immutablePoint(20, 20), atHour(15), atHour(18)));
		
		assertThat(slots, equalTo(expected));
	}

}
