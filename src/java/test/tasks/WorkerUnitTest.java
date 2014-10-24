package tasks;

import static java.time.Month.JANUARY;

import java.time.LocalDateTime;
import java.util.Collection;

import org.junit.Test;

public class WorkerUnitTest {

	@Test
	public void testIdleSubSet() {
		// TODO: implement proper test
		
		WorkerUnit worker = WorkerUnitFixtures.withThreeTasks();
		
		Collection<IdleSlot> slots = worker.idleSubSet(
			LocalDateTime.of(2000, JANUARY, 1,  3, 0),
			LocalDateTime.of(2000, JANUARY, 1, 17, 0)
		);
		
		System.out.println(slots);
	}

}
