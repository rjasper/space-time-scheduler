package scheduler;

import static org.hamcrest.CoreMatchers.*;
import static util.TimeConv.*;
import static util.UUIDFactory.*;
import static world.factories.PathFactory.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.junit.Assert.*;
import static util.TimeFactory.*;

import java.util.Arrays;
import java.util.Collection;

import jts.geom.immutable.ImmutablePolygon;

import org.junit.Test;

import scheduler.IdleSlot;
import scheduler.WorkerUnit;
import world.DecomposedTrajectory;
import world.Trajectory;

public class WorkerUnitTest {
	
	private static final ImmutablePolygon WORKER_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double WORKER_SPEED = 1.0;
	
	private static WorkerUnit workerUnit(String workerId, double x, double y) {
		WorkerUnitSpecification spec = new WorkerUnitSpecification(
			workerId, WORKER_SHAPE, WORKER_SPEED, immutablePoint(x, y), atSecond(0));
		
		return new WorkerUnit(spec);
	}
	
//	@Test
//	public void testIdleSubSet() {
//		WorkerUnit worker = WorkerUnitFixtures.withThreeTasks();
//		
//		Collection<IdleSlot> slots = worker.idleSlots(
//			atHour( 0),
//			atHour(18)
//		);
//		
//		Collection<IdleSlot> expected = Arrays.asList(
//			new IdleSlot(immutablePoint( 0,  0), immutablePoint(10, 10), atHour( 0), atHour( 6)),
//			new IdleSlot(immutablePoint(10, 10), immutablePoint(20, 10), atHour( 7), atHour(12)),
//			new IdleSlot(immutablePoint(20, 10), immutablePoint(20, 20), atHour(15), atHour(18)));
//		
//		assertThat(slots, equalTo(expected));
//	}
	
	@Test
	public void testIdleSlots() {
		WorkerUnit worker = workerUnit("worker", 0, 0);
		WorkerUnitReference ref = worker.getReference();
		
		Trajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 0, 1, 1, 1, 1, 0),
			arcTimePath(0, 0, 1, 1, 1, 2, 2, 3, 2, 4, 3, 5, 3, 6));
		
		Task t1 = new Task(uuid("t1"), ref, immutablePoint(0, 1), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), ref, immutablePoint(1, 1), atSecond(3), secondsToDuration(1));
		Task t3 = new Task(uuid("t3"), ref, immutablePoint(1, 0), atSecond(5), secondsToDuration(1));
		
		worker.updateTrajectory(traj);
		worker.addTask(t1);
		worker.addTask(t2);
		worker.addTask(t3);
		
		Collection<IdleSlot> slots = worker.idleSlots(atSecond(0.5), atSecond(4.5));
		
		Collection<IdleSlot> expected = Arrays.asList(
			new IdleSlot(immutablePoint(0, 0.5), immutablePoint(0, 1  ), atSecond(0.5), atSecond(1  )),
			new IdleSlot(immutablePoint(0, 1  ), immutablePoint(1, 1  ), atSecond(2  ), atSecond(3  )),
			new IdleSlot(immutablePoint(1, 1  ), immutablePoint(1, 0.5), atSecond(4  ), atSecond(4.5)));
		
		assertThat(slots, equalTo(expected));
	}
	
	@Test
	public void testfloorIdleTimeNull() {
		WorkerUnit worker = workerUnit("worker", 0, 0);
		WorkerUnitReference ref = worker.getReference();
		
		Task t1 = new Task(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		worker.addTask(t1);
		
		assertThat(worker.floorIdleTimeOrNull(atSecond(1.5)), is(nullValue()));
	}
	
	@Test
	public void testfloorIdleTimeMid() {
		WorkerUnit worker = workerUnit("worker", 0, 0);
		WorkerUnitReference ref = worker.getReference();
		
		Task t1 = new Task(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		worker.addTask(t1);
		worker.addTask(t2);
		
		assertThat(worker.floorIdleTimeOrNull(atSecond(2.5)), equalTo(atSecond(2)));
	}
	
	@Test
	public void testfloorIdleTimeLeft() {
		WorkerUnit worker = workerUnit("worker", 0, 0);
		WorkerUnitReference ref = worker.getReference();
		
		Task t1 = new Task(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		worker.addTask(t1);
		worker.addTask(t2);
		
		assertThat(worker.floorIdleTimeOrNull(atSecond(2)), equalTo(atSecond(2)));
	}
	
	@Test
	public void testfloorIdleTimeRight() {
		WorkerUnit worker = workerUnit("worker", 0, 0);
		WorkerUnitReference ref = worker.getReference();
		
		Task t1 = new Task(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		worker.addTask(t1);
		worker.addTask(t2);
		
		assertThat(worker.floorIdleTimeOrNull(atSecond(3)), equalTo(atSecond(2)));
	}
	
	@Test
	public void testCeilingIdleTimeMid() {
		WorkerUnit worker = workerUnit("worker", 0, 0);
		WorkerUnitReference ref = worker.getReference();
		
		Task t1 = new Task(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		worker.addTask(t1);
		worker.addTask(t2);
		
		assertThat(worker.ceilingIdleTimeOrNull(atSecond(2.5)), equalTo(atSecond(3)));
	}
	
	@Test
	public void testCeilingIdleTimeLeft() {
		WorkerUnit worker = workerUnit("worker", 0, 0);
		WorkerUnitReference ref = worker.getReference();
		
		Task t1 = new Task(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		worker.addTask(t1);
		worker.addTask(t2);
		
		assertThat(worker.ceilingIdleTimeOrNull(atSecond(2)), equalTo(atSecond(3)));
	}
	
	@Test
	public void testCeilingIdleTimeRight() {
		WorkerUnit worker = workerUnit("worker", 0, 0);
		WorkerUnitReference ref = worker.getReference();
		
		Task t1 = new Task(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		worker.addTask(t1);
		worker.addTask(t2);
		
		assertThat(worker.ceilingIdleTimeOrNull(atSecond(3)), equalTo(atSecond(3)));
	}

}
