package scheduler;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeConv.*;
import static util.TimeFactory.*;
import static util.UUIDFactory.*;
import static world.factories.PathFactory.*;
import static world.factories.TrajectoryFactory.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import scheduler.util.IntervalSet;
import scheduler.util.SimpleIntervalSet;
import util.TimeFactory;
import world.DecomposedTrajectory;
import world.SimpleTrajectory;
import world.Trajectory;

import com.google.common.collect.ImmutableList;

public class WorkerUnitUpdateTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
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
	
	private static <T> List<T> toList(Collection<T> collection) {
		return new ArrayList<>(collection);
	}
	
	private static IntervalSet<LocalDateTime> interval(double from, double to) {
		LocalDateTime fromTime = secondsToTime(from, TimeFactory.BASE_TIME);
		LocalDateTime toTime = secondsToTime(to, TimeFactory.BASE_TIME);
		
		return new SimpleIntervalSet<LocalDateTime>().add(fromTime, toTime);
	}

	@Test
	public void testUpdateTrajectory() {
		WorkerUnitUpdate update = workerUnitUpdate();
		
		Trajectory traj = trajectory(0, 0, 0, 0, 0, 1);
		
		update.updateTrajectory(traj);
		update.seal();
		
		SimpleIntervalSet<LocalDateTime> expected = new SimpleIntervalSet<>();
		expected.add(atSecond(0), atSecond(1));
		
		assertThat(update.getTrajectoryLock(),
			equalTo(expected));
	}

	@Test
	public void testUpdateTrajectoryPredate() {
		WorkerUnitUpdate update = workerUnitUpdate();
		
		Trajectory traj = trajectory(0, 0, 0, 0, -1, 1);
		
		thrown.expect(IllegalArgumentException.class);
		
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
		
		assertThat(update.getTrajectoryLock(),
			equalTo(expected));
	}
	
	@Test
	public void testAddTaskInvalidWorker() {
		WorkerUnitUpdate update = workerUnitUpdate();
		WorkerUnitReference other = workerUnitUpdate().getWorker().getReference();
		
		Task task = new Task(uuid("task"), other, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		thrown.expect(IllegalArgumentException.class);
		
		update.addTask(task);
	}

	@Test
	public void testAddTaskPredate() {
		WorkerUnitUpdate update = workerUnitUpdate();
		WorkerUnitReference ref = update.getWorker().getReference();
		
		Task task = new Task(uuid("task"), ref, immutablePoint(0, 0), atSecond(-1), secondsToDuration(1));
		
		thrown.expect(IllegalArgumentException.class);
		
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
	
	@Test
	public void testAddTaskRemovalInvalidWorker() {
		WorkerUnitUpdate update = workerUnitUpdate();
		WorkerUnitReference other = workerUnitUpdate().getWorker().getReference();
		
		Task task = new Task(uuid("task"), other, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		thrown.expect(IllegalArgumentException.class);
		
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
	
	@Test
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
		
		thrown.expect(IllegalStateException.class);
		
		update.checkSelfConsistency();
	}
	
	@Test
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
		
		thrown.expect(IllegalStateException.class);
		
		update.checkSelfConsistency();
	}
	
	@Test
	public void testCloneIdentical() {
		WorkerUnitUpdate origin = workerUnitUpdate();
		WorkerUnit worker = origin.getWorker();
		WorkerUnitReference ref = worker.getReference();

		Trajectory traj = trajectory(0, 0, 0, 0, 0, 1);
		Task task = new Task(uuid("task"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Task removal = new Task(uuid("removal"), ref, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		origin.updateTrajectory(traj);
		origin.addTask(task);
		origin.addTaskRemoval(removal);
		
		WorkerUnitUpdate clone = origin.clone();
		
		clone.seal();
		
		assertThat("different trajectories",
			toList(clone.getTrajectories()), equalTo(singletonList(traj)));
		assertThat("different tasks",
			clone.getTasks(), equalTo(singleton(task)));
		assertThat("different removals",
			clone.getTaskRemovals(), equalTo(singleton(removal)));
		assertThat("different trajectory lock",
			clone.getTrajectoryLock(), equalTo(interval(0, 2)));
		assertThat("different task lock",
			clone.getTaskLock(), equalTo(interval(1, 2)));
		assertThat("different removal intervals",
			clone.getTaskRemovalIntervals(), equalTo(interval(0, 1)));
	}
	
	@Test
	public void testCloneIndependent() {
		WorkerUnitUpdate origin = workerUnitUpdate();
		WorkerUnit worker = origin.getWorker();
		WorkerUnitReference ref = worker.getReference();
		
		WorkerUnitUpdate clone = origin.clone();

		Trajectory traj = trajectory(0, 0, 0, 0, 0, 1);
		Task task = new Task(uuid("task"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Task removal = new Task(uuid("removal"), ref, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		clone.updateTrajectory(traj);
		clone.addTask(task);
		clone.addTaskRemoval(removal);
		
		origin.seal();
		clone.seal();
		
		assertThat("different trajectories",
			toList(origin.getTrajectories()).isEmpty(), is(true));
		assertThat("different tasks",
			origin.getTasks().isEmpty(), is(true));
		assertThat("different removals",
			origin.getTaskRemovals().isEmpty(), is(true));
		assertThat("different trajectory lock",
			origin.getTrajectoryLock().isEmpty(), is(true));
		assertThat("different task lock",
			origin.getTaskLock().isEmpty(), is(true));
		assertThat("different removal intervals",
			origin.getTaskRemovalIntervals().isEmpty(), is(true));
		
		assertThat("different trajectories",
			toList(clone.getTrajectories()), equalTo(singletonList(traj)));
		assertThat("different tasks",
			clone.getTasks(), equalTo(singleton(task)));
		assertThat("different removals",
			clone.getTaskRemovals(), equalTo(singleton(removal)));
		assertThat("different trajectory lock",
			clone.getTrajectoryLock(), equalTo(interval(0, 2)));
		assertThat("different task lock",
			clone.getTaskLock(), equalTo(interval(1, 2)));
		assertThat("different removal intervals",
			clone.getTaskRemovalIntervals(), equalTo(interval(0, 1)));
	}

}
