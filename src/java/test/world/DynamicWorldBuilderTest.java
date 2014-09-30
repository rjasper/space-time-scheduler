package world;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import tasks.Task;
import tasks.TaskFactory;
import tasks.WorkerUnit;
import tasks.WorkerUnitFactory;

public class DynamicWorldBuilderTest {
	
	private WorkerUnitFactory wFact;
	private TaskFactory taskFact;
	private LocalDateTimeFactory timeFact;

	@Before
	public void setUp() throws Exception {
		wFact = WorkerUnitFactory.getInstance();
		taskFact = TaskFactory.getInstance();
		timeFact = LocalDateTimeFactory.getInstance();
	}

	@Test
	public void testBuild() {
//		fail("Not yet implemented");
		
		WorkerUnit w1 = wFact.createWorkerUnit(10.0,  5.0);
		WorkerUnit w2 = wFact.createWorkerUnit(35.0, 30.0);
		
		Task t1 = taskFact.createTask(10.0, 15.0,  3,  7);
		Task t2 = taskFact.createTask(15.0, 25.0, 10, 12);
		Task t3 = taskFact.createTask(25.0, 20.0,  2,  5);
		Task t4 = taskFact.createTask(25.0, 10.0,  8, 14);
		
		LocalDateTime endTime = timeFact.second(15);
		
		// TODO fix addition of task
		
		w1.addTask(t1);
		w1.addTask(t2);
		w2.addTask(t3);
		w2.addTask(t4);
		
		DynamicWorldBuilder builder = new DynamicWorldBuilder();
		
		builder.setEndTime(endTime);
		builder.setWorkers(Arrays.asList(w1, w2));
		
		builder.build();
		
		List<DynamicObstacle> obstacles = builder.getObstacles();
		
		System.out.println(obstacles);
	}

}
