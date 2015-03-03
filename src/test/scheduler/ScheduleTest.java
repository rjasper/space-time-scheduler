package scheduler;

import static org.hamcrest.CoreMatchers.*;
import static world.factories.TrajectoryFactory.*;
import static util.TimeConv.*;
import static util.UUIDFactory.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.junit.Assert.*;
import static util.TimeFactory.*;
import jts.geom.immutable.ImmutablePolygon;

import org.junit.Test;

import world.Trajectory;

public class ScheduleTest {
	
	private static final ImmutablePolygon WORKER_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double WORKER_SPEED = 1.0;
	
	private static WorkerUnit workerUnit(String workerId, double x, double y) {
		WorkerUnitSpecification spec = new WorkerUnitSpecification(
			workerId, WORKER_SHAPE, WORKER_SPEED, immutablePoint(x, y), atSecond(0));
		
		return new WorkerUnit(spec);
	}

	private static void scheduleTask(Schedule schedule, Task task) {
		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addTask(task);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa);
	}

//	private static void scheduleTrajectory(Schedule schedule, WorkerUnit worker, Trajectory trajectory) {
//		ScheduleAlternative sa = new ScheduleAlternative();
//		
//		sa.updateTrajectory(worker, trajectory);
//		sa.seal();
//		
//		schedule.addAlternative(sa);
//		schedule.integrate(sa);
//	}
//	
//	private static void unscheduleTask(Schedule schedule, Task task) {
//		ScheduleAlternative sa = new ScheduleAlternative();
//		
//		sa.addTaskRemoval(task);
//		sa.seal();
//		
//		schedule.addAlternative(sa);
//		schedule.integrate(sa);
//	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdatedTrajectoryOriginLocationViolation() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		// set task
		
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleTask(schedule, task);
	
		// add new trajectory
		Trajectory traj = trajectory(
			0, 0,
			0, 0,
			0, 3);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(w, traj);
		sa.seal();
		
		schedule.addAlternative(sa);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTaskLockViolation() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		ScheduleAlternative sa1 = new ScheduleAlternative();
		ScheduleAlternative sa2 = new ScheduleAlternative();
		
		Task t1 = new Task(uuid("t1"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), w.getReference(), immutablePoint(10, 10), atSecond(1), secondsToDuration(1));
		
		sa1.addTask(t1);
		sa2.addTask(t2);
		
		sa1.seal();
		sa2.seal();
		
		schedule.addAlternative(sa1);
		schedule.addAlternative(sa2);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testTrajectoryLockViolation() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		ScheduleAlternative sa1 = new ScheduleAlternative();
		ScheduleAlternative sa2 = new ScheduleAlternative();
		
		Trajectory traj1 = trajectory(0, 0, 0, 0, 0, 1);
		Trajectory traj2 = trajectory(0, 0, 1, 1, 0, 1);
		
		sa1.updateTrajectory(w, traj1);
		sa2.updateTrajectory(w, traj2);
		
		sa1.seal();
		sa2.seal();
		
		schedule.addAlternative(sa1);
		schedule.addAlternative(sa2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testContinuousTrajectoryViolation() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Trajectory traj1 = trajectory(1, 1, 1, 1, 1, 2);
		
		sa.updateTrajectory(w, traj1);
		sa.seal();
		
		schedule.addAlternative(sa);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testTaskLocationViolation() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(1, 1), atSecond(1), secondsToDuration(1));

		sa.addTask(task);
		sa.seal();
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testUpdatedTrajectoryAtTaskPositive() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();

		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(1, 1), atSecond(1), secondsToDuration(1));

		sa.updateTrajectory(w, traj);
		sa.addTask(task);
		sa.seal();
		
		schedule.addAlternative(sa); // no exception
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateTrajectoryAtTaskNegative() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();

		Trajectory traj = trajectory(
			0, 0,
			0, 0,
			0, 3);
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(1, 1), atSecond(1), secondsToDuration(1));

		sa.updateTrajectory(w, traj);
		sa.addTask(task);
		sa.seal();
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testTaskRemoval() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		// set up task to remove
		
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleTask(schedule, task);
		
		// remove task

		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addTaskRemoval(task);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa);
		
		assertThat(w.hasTask(task), is(false));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testTaskRemovalUnknown() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		// set up task to remove
		
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		// remove task

		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addTaskRemoval(task);
		sa.seal();
		
		schedule.addAlternative(sa);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testTaskRemovalLockViolation() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		// set up task to remove
		
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleTask(schedule, task);
		
		// remove task

		ScheduleAlternative sa1 = new ScheduleAlternative();
		ScheduleAlternative sa2 = new ScheduleAlternative();
		
		sa1.addTaskRemoval(task);
		sa2.addTaskRemoval(task);
		sa1.seal();
		sa2.seal();
		
		schedule.addAlternative(sa1);
		schedule.addAlternative(sa2);
	}

	@Test
	public void testUpdateTrajectoryOfRemoval() {
		WorkerUnit w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		// set up task to remove
		
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleTask(schedule, task);
		
		// plan trajectory and removal		
		Trajectory traj = trajectory(
			0, 0,
			0, 0,
			0, 3);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(w, traj);
		sa.addTaskRemoval(task);
		sa.seal();
		
		schedule.addAlternative(sa); // no exception
	}

}
