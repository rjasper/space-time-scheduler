package scheduler;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeConv.*;
import static util.TimeFactory.*;
import static util.UUIDFactory.*;
import static world.factories.PathFactory.*;
import static world.factories.TrajectoryFactory.*;

import java.time.LocalDateTime;
import java.util.Iterator;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;

import org.junit.Test;

import scheduler.util.SimpleIntervalSet;
import world.DecomposedTrajectory;
import world.SimpleTrajectory;
import world.Trajectory;

import com.google.common.collect.ImmutableList;

public class WorkerUnitUpdateTest {
	
	private static final String WORKER_ID = "worker";
	
	private static final ImmutablePolygon WORKER_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double WORKER_SPEED = 1.0;

	private static final ImmutablePoint WORKER_INITIAL_LOCATION = immutablePoint(0, 0);

	private static final LocalDateTime WORKER_INITIAL_TIME = atSecond(0);
	
	private static WorkerUnitUpdate workerUnitUpdate() {
		WorkerUnitSpecification spec = new WorkerUnitSpecification(
			WORKER_ID, WORKER_SHAPE, WORKER_SPEED, WORKER_INITIAL_LOCATION, WORKER_INITIAL_TIME);
		WorkerUnit worker = new WorkerUnit(spec);
		
		return new WorkerUnitUpdate(worker);
	}

	@Test
	public void testUpdateTrajectory() {
		WorkerUnitUpdate update = workerUnitUpdate();
		
		Trajectory traj = trajectory(0, 0, 0, 0, 0, 1);
		
		update.updateTrajectory(traj);
		update.seal();
		
		SimpleIntervalSet<LocalDateTime> expected = new SimpleIntervalSet<>();
		expected.add(atSecond(0), atSecond(1));
		
		assertThat(update.getTrajectoriesLock(),
			equalTo(expected));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdateTrajectoryPredate() {
		WorkerUnitUpdate update = workerUnitUpdate();
		
		Trajectory traj = trajectory(0, 0, 0, 0, -1, 1);
		
		update.updateTrajectory(traj);
	}
	
	@Test
	public void testAddTask() {
		WorkerUnitUpdate update = workerUnitUpdate();
		WorkerUnitReference ref = update.getWorker().getReference();
		
		Task task = new Task(uuid("task"), ref, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		update.addTask(task);
		update.seal();
		
		SimpleIntervalSet<LocalDateTime> expected = new SimpleIntervalSet<>();
		expected.add(atSecond(0), atSecond(1));
		
		assertThat(update.getTrajectoriesLock(),
			equalTo(expected));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskInvalidWorker() {
		WorkerUnitUpdate update = workerUnitUpdate();
		WorkerUnitReference other = workerUnitUpdate().getWorker().getReference();
		
		Task task = new Task(uuid("task"), other, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		update.addTask(task);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskPredate() {
		WorkerUnitUpdate update = workerUnitUpdate();
		WorkerUnitReference ref = update.getWorker().getReference();
		
		Task task = new Task(uuid("task"), ref, immutablePoint(0, 0), atSecond(-1), secondsToDuration(1));
		
		update.addTask(task);
	}
	
	@Test
	public void testAddTaskRemoval() {
		WorkerUnitUpdate update = workerUnitUpdate();
		WorkerUnitReference ref = update.getWorker().getReference();
		
		Task task = new Task(uuid("task"), ref, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		update.addTaskRemoval(task);
		update.seal();
		
		Iterator<Task> it = update.getTaskRemovals().iterator();
		
		assertThat(it.next(), equalTo(task));
		assertThat(it.hasNext(), is(false));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddTaskRemovalInvalidWorker() {
		WorkerUnitUpdate update = workerUnitUpdate();
		WorkerUnitReference other = workerUnitUpdate().getWorker().getReference();
		
		Task task = new Task(uuid("task"), other, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		update.addTaskRemoval(task);
	}
	
	@Test
	public void testCheckSelfConsistencyPositive() {
		WorkerUnitUpdate update = workerUnitUpdate();
		WorkerUnit worker = update.getWorker();
		WorkerUnitReference ref = worker.getReference();
		
		Trajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 0, 1, 1, 1, 1, 0),
			arcTimePath(0, 0, 1, 1, 1, 2, 2, 3, 2, 4, 3, 5, 3, 6));
		
		Task t1 = new Task(uuid("t1"), ref, immutablePoint(0, 1), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), ref, immutablePoint(1, 1), atSecond(3), secondsToDuration(1));
		Task t3 = new Task(uuid("t3"), ref, immutablePoint(1, 0), atSecond(5), secondsToDuration(1));
		
		update.updateTrajectory(traj);
		update.addTask(t1);
		update.addTask(t2);
		update.addTask(t3);
		
		update.checkSelfConsistency(); // expect no exception
	}
	
	@Test(expected = IllegalStateException.class)
	public void testCheckSelfConsistencyLocation() {
		WorkerUnitUpdate update = workerUnitUpdate();
		WorkerUnit worker = update.getWorker();
		WorkerUnitReference ref = worker.getReference();
		
		Trajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 0, 1, 1, 1, 1, 2),
			arcTimePath(0, 0, 1, 1, 1, 2, 2, 3, 2, 4, 3, 5, 3, 6));
		
		Task t1 = new Task(uuid("t1"), ref, immutablePoint(0, 1), atSecond(1), secondsToDuration(1));
		Task t2 = new Task(uuid("t2"), ref, immutablePoint(1, 1), atSecond(3), secondsToDuration(1));
		// t3 location violated
		Task t3 = new Task(uuid("t3"), ref, immutablePoint(1, 0), atSecond(5), secondsToDuration(1));
		
		update.updateTrajectory(traj);
		update.addTask(t1);
		update.addTask(t2);
		update.addTask(t3);
		
		update.checkSelfConsistency();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testCheckSelfConsistencyContinuity() {
		WorkerUnitUpdate update = workerUnitUpdate();
		
		Trajectory traj1 = new SimpleTrajectory(
			spatialPath(0, 0, 1, 1),
			ImmutableList.of(atSecond(0), atSecond(1)));
		Trajectory traj2 = new SimpleTrajectory(
			spatialPath(2, 2, 3, 3),
			ImmutableList.of(atSecond(1), atSecond(2)));
		
		update.updateTrajectory(traj1);
		update.updateTrajectory(traj2);
		
		update.checkSelfConsistency();
	}
	
	// TODO test cloning

}
