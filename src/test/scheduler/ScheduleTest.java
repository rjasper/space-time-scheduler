package scheduler;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeConv.*;
import static util.TimeFactory.*;
import static util.UUIDFactory.*;
import static world.factories.TrajectoryFactory.*;

import java.time.LocalDateTime;
import java.util.Iterator;

import jts.geom.immutable.ImmutablePolygon;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import scheduler.util.SimpleIntervalSet;
import world.Trajectory;

public class ScheduleTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private static final ImmutablePolygon WORKER_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double WORKER_SPEED = 1.0;
	
	private static Node workerUnit(String workerId, double x, double y) {
		NodeSpecification spec = new NodeSpecification(
			workerId, WORKER_SHAPE, WORKER_SPEED, immutablePoint(x, y), atSecond(0));
		
		return new Node(spec);
	}

	private static void scheduleTask(Schedule schedule, Task task) {
		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addTask(task);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa);
	}
	
	private static SimpleIntervalSet<LocalDateTime> intervalSet(LocalDateTime from, LocalDateTime to) {
		return new SimpleIntervalSet<LocalDateTime>().add(from, to);
	}
	
	@Test
	public void testTaskLock() {
		Node w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		sa.addTask(task);
		sa.seal();
		
		schedule.addAlternative(sa);
		
		assertThat(schedule.getTrajectoryLock(w),
			equalTo(intervalSet(atSecond(1), atSecond(2))));
	}
	
	@Test
	public void testTrajectoryLock() {
		Node w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Trajectory traj = trajectory(0, 0, 0, 0, 0, 1);
		
		sa.updateTrajectory(w, traj);
		sa.seal();
		
		schedule.addAlternative(sa);
		
		assertThat(schedule.getTrajectoryLock(w),
			equalTo(intervalSet(atSecond(0), atSecond(1))));
	}
	
	@Test
	public void testTaskRemovalLock() {
		Node w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleTask(schedule, task);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addTaskRemoval(task);
		sa.seal();
		
		schedule.addAlternative(sa);
		
		Iterator<Task> removalsLock = schedule.getTaskRemovalLock(w).iterator();
		
		assertThat(removalsLock.next(), is(task));
		assertThat(removalsLock.hasNext(), is(false));
	}

	@Test
	public void testUpdatedTrajectoryOriginLocationViolation() {
		Node w = workerUnit("w", 0, 0);
		
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
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}

	@Test
	public void testTaskLockViolation() {
		Node w = workerUnit("w", 0, 0);
		
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

		thrown.expect(IllegalArgumentException.class);
		schedule.addAlternative(sa2);
	}
	
	@Test
	public void testTrajectoryLockViolation() {
		Node w = workerUnit("w", 0, 0);
		
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
		
		thrown.expect(IllegalArgumentException.class);
		schedule.addAlternative(sa2);
	}

	@Test
	public void testContinuousTrajectoryViolation() {
		Node w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Trajectory traj1 = trajectory(1, 1, 1, 1, 1, 2);
		
		sa.updateTrajectory(w, traj1);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	public void testTaskLocationViolation() {
		Node w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(1, 1), atSecond(1), secondsToDuration(1));

		sa.addTask(task);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testUpdatedTrajectoryAtTaskPositive() {
		Node w = workerUnit("w", 0, 0);
		
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
	
	@Test
	public void testUpdateTrajectoryAtTaskNegative() {
		Node w = workerUnit("w", 0, 0);
		
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
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testTaskRemoval() {
		Node w = workerUnit("w", 0, 0);
		
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
	
	@Test
	public void testTaskRemovalUnknown() {
		Node w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		// set up task to remove
		
		Task task = new Task(uuid("task"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		// remove task

		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addTaskRemoval(task);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testTaskRemovalLockViolation() {
		Node w = workerUnit("w", 0, 0);
		
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

		thrown.expect(IllegalArgumentException.class);
		schedule.addAlternative(sa2);
	}

	@Test
	public void testUpdateTrajectoryOfRemoval() {
		Node w = workerUnit("w", 0, 0);
		
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
	
	@Test
	public void testIntegrate() {
		Node w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		// set up task to remove
		
		Task taskToRemove = new Task(uuid("old task"), w.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleTask(schedule, taskToRemove);
		
		// plan trajectory, task and removal
		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		
		Task taskToSchedule = new Task(uuid("new task"), w.getReference(),
			immutablePoint(1, 1), atSecond(1), secondsToDuration(1));
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(w, traj);
		sa.addTask(taskToSchedule);
		sa.addTaskRemoval(taskToRemove);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa);
		
		assertThat("task not removed",
			w.hasTask(taskToRemove), is(false));
		assertThat("task not scheduled",
			w.hasTask(taskToSchedule), is(true));
		
		// removed locks
		assertThat("trajectory lock not removed",
			schedule.getTrajectoryLock(w)
			.intersects(atSecond(0), atSecond(3)),
			is(false));
		assertThat("task removal lock not removed",
			schedule.getTaskRemovalLock(w)
			.contains(taskToRemove),
			is(false));
		
		// applied changes
		Iterator<Trajectory> trajs = w.getTrajectories(atSecond(0), atSecond(3))
			.iterator();
		assertThat("trajectory update not applied",
			trajs.next(), equalTo(traj));
		assertThat("unexpected trajectory",
			trajs.hasNext(), is(false));
	}
	
	@Test
	public void testEliminate() {
		Node w = workerUnit("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w);
		
		// set up task to remove
		
		Task taskToRemove = new Task(uuid("old task"), w.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleTask(schedule, taskToRemove);
		
		// plan trajectory, task and removal
		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		
		Task taskToSchedule = new Task(uuid("new task"), w.getReference(),
			immutablePoint(1, 1), atSecond(1), secondsToDuration(1));
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(w, traj);
		sa.addTask(taskToSchedule);
		sa.addTaskRemoval(taskToRemove);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.eliminate(sa);
		
		assertThat("task removed when shouldn't have been",
			w.hasTask(taskToRemove), is(true));
		assertThat("task scheduled when shouldn't have been",
			w.hasTask(taskToSchedule), is(false));
		
		// removed locks
		assertThat("trajectory lock not removed",
			schedule.getTrajectoryLock(w)
			.intersects(atSecond(0), atSecond(3)),
			is(false));
		assertThat("task removal lock not removed",
			schedule.getTaskRemovalLock(w)
			.contains(taskToRemove),
			is(false));
		
		// didn't apply changes
		Iterator<Trajectory> trajs = w.getTrajectories(atSecond(0), atSecond(3))
			.iterator();
		assertThat("trajectory update applied when shouldn't have been",
			trajs.next(), not(equalTo(traj)));
		assertThat("unexpected trajectory",
			trajs.hasNext(), is(false));
	}
	
	@Test
	public void testIntegrateEliminatePartial() {
		Node w1 = workerUnit("w1", 0, 0);
		Node w2 = workerUnit("w2", 10, 10);
		
		Schedule schedule = new Schedule();
		schedule.addWorker(w1);
		schedule.addWorker(w2);

		Task t1 = new Task(uuid("t1"), w1.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), w2.getReference(),
			immutablePoint(10, 10), atSecond(1), secondsToDuration(1));

		ScheduleAlternative sa = new ScheduleAlternative();
		sa.addTask(t1);
		sa.addTask(t2);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa, w2);
		schedule.eliminate(sa, w1);
		
		assertThat("transaction was not removed",
			schedule.hasAlternative(sa), is(false));
		assertThat("t1 was not eliminated",
			w1.hasTask(t1), is(false));
		assertThat("t2 was not integrated",
			w2.hasTask(t2), is(true));
	}
	
}
