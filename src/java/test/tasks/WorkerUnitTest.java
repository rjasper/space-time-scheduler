package tasks;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import world.LocalDateTimeFactory;

public class WorkerUnitTest {
	
	private static final double h = 3600.;
	
	private LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();
	private IdleSlotFactory slotFact = IdleSlotFactory.getInstance();

	@Test
	public void testIdleSubSet() {
		WorkerUnit worker = WorkerUnitFixtures.withThreeTasks();
		
		Collection<IdleSlot> slots = worker.idleSubSet(
			timeFact.hours( 3L),
			timeFact.hours(17L)
		);
		
		Collection<IdleSlot> expected = Arrays.asList(
			slotFact.idleSlot( 0.,  0.,  0.*h, 10., 10.,  6.*h),
			slotFact.idleSlot(10., 10.,  7.*h, 20., 10., 12.*h),
			slotFact.idleSlot(20., 10., 15.*h, 20., 20., 18.*h));
		
		assertThat(slots, equalTo(expected));
	}

}
