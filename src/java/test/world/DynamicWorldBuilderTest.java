package world;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import tasks.WorkerUnit;
import tasks.WorkerUnitFactory;

public class DynamicWorldBuilderTest {
	
	private WorkerUnitFactory wFact;
	private LocalDateTimeFactory timeFact;

	@Before
	public void setUp() throws Exception {
		wFact = WorkerUnitFactory.getInstance();
		timeFact = LocalDateTimeFactory.getInstance();
	}

	@Test
	public void testBuild() {
		// TODO implement proper test
//		fail("Not yet implemented");
		
		WorkerUnit w1 = wFact.createWorkerUnit(10.0,  5.0);
		WorkerUnit w2 = wFact.createWorkerUnit(35.0, 30.0);
		
		LocalDateTime endTime = timeFact.second(150L);
		
		wFact.addTask(w1, 10.0, 15.0,  30L,  70L);
		wFact.addTask(w1, 15.0, 25.0, 100L, 120L);
		wFact.addTask(w2, 25.0, 20.0,  20L,  50L);
		wFact.addTask(w2, 25.0, 10.0,  80L, 140L);
		
		DynamicWorldBuilder builder = new DynamicWorldBuilder();
		
		builder.setEndTime(endTime);
		builder.setWorkers(Arrays.asList(w1, w2));
		
		builder.build();
		
		List<DynamicObstacle> obstacles = builder.getObstacles();
		
		System.out.println(obstacles);
	}

}
