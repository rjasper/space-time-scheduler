package scheduler;

import static java.util.Collections.*;
import static scheduler.util.IntervalSets.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jts.geom.immutable.ImmutablePoint;
import scheduler.util.IntervalSet;
import scheduler.util.SimpleIntervalSet;
import world.Trajectory;

import com.vividsolutions.jts.geom.Point;

public class Schedule {
	
	private final Map<String, WorkerUnit> workers = new HashMap<>();
	
	private final Set<ScheduleAlternative> alternatives = new HashSet<>();
	
	private final Map<WorkerUnit, WorkerUnitLocks> locks = new IdentityHashMap<>();
	
	private final Map<UUID, Task> tasks = new HashMap<>();
	
	private static class WorkerUnitLocks {
		
		public final SimpleIntervalSet<LocalDateTime> trajectoryLock = new SimpleIntervalSet<>();
		
		public final Set<Task> taskRemovalLock = new HashSet<>();
		
	}
	
	public boolean hasAlternative(ScheduleAlternative alternative) {
		return alternatives.contains(alternative);
	}
	
	public Collection<WorkerUnit> getWorkers() {
		return unmodifiableCollection(workers.values());
	}
	
	public WorkerUnit getWorker(String workerId) {
		Objects.requireNonNull(workerId, "workerId");
		
		WorkerUnit worker = workers.get(workerId);
		
		if (worker == null)
			throw new IllegalArgumentException("unknown worker id");
		
		return worker;
	}

	public void addWorker(WorkerUnit worker) {
		Objects.requireNonNull(worker, "worker");
		
		WorkerUnit previous = workers.putIfAbsent(worker.getId(), worker);
		
		if (previous != null)
			throw new IllegalArgumentException("worker id already assigned");
		
		locks.put(worker, new WorkerUnitLocks());
	}
	
	public void removeWorker(String workerId) {
		Objects.requireNonNull(workerId, "workerId");
		
		WorkerUnit worker = workers.get(workerId);
		
		if (worker == null)
			throw new IllegalArgumentException("unknown worker id");
		if (!worker.isIdle())
			throw new IllegalStateException("worker still has scheduled tasks");
		
		workers.remove(workerId);
		locks.remove(worker);
	}
	
	public Task getTask(UUID taskId) {
		Task task = tasks.get(taskId);
		
		if (task == null)
			throw new IllegalArgumentException("unknown task id");
		
		return task;
	}
	
	public void removeTask(UUID taskId) {
		Task task = getTask(taskId);
		WorkerUnit worker = task.getAssignedWorker().getActual();
		Set<Task> lock = getTaskRemovalLock(worker);
		
		if (lock.contains(task))
			throw new IllegalStateException("given task is locked for removal");
		
		worker.removeTask(task);
		tasks.remove(taskId);
	}
	
	public IntervalSet<LocalDateTime> getTrajectoryLock(WorkerUnit worker) {
		Objects.requireNonNull(worker, "worker");
		
		WorkerUnitLocks workerLocks = locks.get(worker);
		
		if (workerLocks == null)
			throw new IllegalArgumentException("unknown worker");
		
		return unmodifiableIntervalSet(workerLocks.trajectoryLock);
	}
	
	public Set<Task> getTaskRemovalLock(WorkerUnit worker) {
		Objects.requireNonNull(worker, "worker");
		
		WorkerUnitLocks workerLocks = locks.get(worker);
		
		if (workerLocks == null)
			throw new IllegalArgumentException("unknown worker");
		
		return unmodifiableSet(workerLocks.taskRemovalLock);
	}
	
	public Collection<ScheduleAlternative> getAlternatives() {
		return unmodifiableCollection(alternatives);
	}
	
	public void addAlternative(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		
		if (!alternative.isSealed())
			new IllegalArgumentException("alternative not sealed");
		
		checkCompatibility(alternative);
		
		alternatives.add(alternative);
		applyLocks(alternative);
	}
	
	public void integrate(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		
		boolean status = alternatives.remove(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");

		releaseLocks(alternative);
		applyChanges(alternative);
	}
	
	public void integrate(ScheduleAlternative alternative, WorkerUnit worker) {
		Objects.requireNonNull(alternative, "alternative");
		Objects.requireNonNull(worker, "worker");
		
		boolean status = alternatives.contains(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");
		
		WorkerUnitUpdate update = alternative.popUpdate(worker);
		
		if (alternative.isEmpty())
			alternatives.remove(alternative);
		
		releaseLocks(update);
		applyChanges(update);
	}
	
	public void eliminate(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		
		boolean status = alternatives.remove(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");

		releaseLocks(alternative);
	}
	
	public void eliminate(ScheduleAlternative alternative, WorkerUnit worker) {
		Objects.requireNonNull(alternative, "alternative");
		Objects.requireNonNull(worker, "worker");
		
		boolean status = alternatives.contains(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");
		
		WorkerUnitUpdate update = alternative.popUpdate(worker);
		
		if (alternative.isEmpty())
			alternatives.remove(alternative);
		
		releaseLocks(update);
	}

	private void checkCompatibility(ScheduleAlternative alternative) {
		for (WorkerUnitUpdate u : alternative.getUpdates()) {
			WorkerUnit worker = u.getWorker();
			WorkerUnitLocks workerLocks = locks.get(worker);
			
			if (workerLocks == null)
				throw new IllegalArgumentException("unknown worker");

			IntervalSet<LocalDateTime> originTasksIntervals = worker.getTaskIntervals();
			IntervalSet<LocalDateTime> removalsIntervals = u.getTaskRemovalIntervals();
			IntervalSet<LocalDateTime> trajIntervals = u.getTrajectoryIntervals();
			IntervalSet<LocalDateTime> trajLockIntervals = u.getTrajectoryLock();
			
			Collection<Task> tasks = u.getTasks();
			Collection<Task> removals = u.getTaskRemovals();
			
			try {
				u.checkSelfConsistency();
			} catch (IllegalStateException e) {
				throw new IllegalArgumentException(e);
			}

			// no mutual trajectory locks with origin disregarding removed tasks
			if (!trajLockIntervals.intersection(originTasksIntervals)
				.difference(removalsIntervals).isEmpty())
			{
				throw new IllegalArgumentException("trajectory lock violation");
			}
			// no mutual trajectory locks with other alternatives
			if (trajLockIntervals.intersects(workerLocks.trajectoryLock))
				throw new IllegalArgumentException("trajectory lock violation");
			// continuous trajectories
			if (!verifyTrajectoryContinuity(worker, u.getTrajectories()))
				throw new IllegalArgumentException("trajectory continuity violation");
			// non-updated original trajectories lead to tasks
			if (!tasks.stream().allMatch(t -> verifyTaskLocation(t, trajIntervals)))
				throw new IllegalArgumentException("task location violation");
			// no unknown task removals
			if (!removals.stream().allMatch(worker::hasTask))
				throw new IllegalArgumentException("unknown task removal");
			// no mutual task removals
			if (removals.stream().anyMatch(workerLocks.taskRemovalLock::contains))
				throw new IllegalArgumentException("task removal lock violation");
		}
	}

	private boolean verifyTaskLocation(Task task, IntervalSet<LocalDateTime> trajectoryUpdates) {
		WorkerUnit worker = task.getAssignedWorker().getActual();
		Point location = task.getLocation();
		LocalDateTime taskStart = task.getStartTime();
		LocalDateTime taskFinish = task.getFinishTime();
		
		IntervalSet<LocalDateTime> originalSections = new SimpleIntervalSet<LocalDateTime>()
			.add(taskStart, taskFinish)
			.remove(trajectoryUpdates);
		
		return originalSections.isEmpty() ||
			// check if relevant original trajectory sections are stationary
			originalSections.stream()
			.allMatch(i -> {
				LocalDateTime start = i.getFromInclusive();
				LocalDateTime finish = i.getToExclusive();
			
				return
					worker.isStationary(start, finish) &&
					worker.interpolateLocation(start).equals(location);
			});
	}
	
	private boolean verifyTrajectoryContinuity(WorkerUnit worker, Collection<Trajectory> trajectories) {
		if (trajectories.isEmpty())
			return true;
		
		Iterator<Trajectory> it = trajectories.iterator();
		
		Trajectory first = it.next();
		Trajectory last = first;
		
		if (!verifyWorkerLocation(worker, first.getStartLocation(), first.getStartTime()))
			return false;
		
		while (it.hasNext()) {
			Trajectory curr = it.next();

			LocalDateTime finishTime = last.getFinishTime();
			LocalDateTime startTime = curr.getStartTime();
			
			if (!startTime.equals(finishTime) && (
				!verifyWorkerLocation(worker, last.getFinishLocation(), finishTime) ||
				!verifyWorkerLocation(worker, curr.getStartLocation(), startTime)))
			{
				return false;
			}
			
			last = curr;
		}
		
		if (!verifyWorkerLocation(worker, last.getFinishLocation(), last.getFinishTime()))
			return false;
		
		return true;
	}
	
	private boolean verifyWorkerLocation(WorkerUnit worker, ImmutablePoint location, LocalDateTime time) {
		if (time.equals(Scheduler.END_OF_TIME))
			return true;
		
		return worker.interpolateLocation(time).equals(location);
	}
	
	private void applyChanges(ScheduleAlternative alternative) {
		for (WorkerUnitUpdate u : alternative.getUpdates())
			applyChanges(u);
	}

	private void applyChanges(WorkerUnitUpdate update) {
		WorkerUnit worker = update.getWorker();
		
		for (Task t : update.getTaskRemovals()) {
			worker.removeTask(t);
			tasks.remove(t.getId());
		}
		for (Task t : update.getTasks()) {
			worker.addTask(t);
			tasks.put(t.getId(), t);
		}
		for (Trajectory t : update.getTrajectories())
			worker.updateTrajectory(t);
	}

	private void applyLocks(ScheduleAlternative alternative) {
		for (WorkerUnitUpdate u : alternative.getUpdates())
			applyLocks(u);
	}
	
	private void applyLocks(WorkerUnitUpdate update) {
		WorkerUnitLocks workerLocks = locks.get(update.getWorker());
		
		workerLocks.trajectoryLock .add   ( update.getTrajectoryLock() );
		workerLocks.taskRemovalLock.addAll( update.getTaskRemovals()     );
	}
	
	private void releaseLocks(ScheduleAlternative alternative) {
		for (WorkerUnitUpdate u : alternative.getUpdates())
			releaseLocks(u);
	}
	
	private void releaseLocks(WorkerUnitUpdate update) {
		WorkerUnitLocks workerLocks = locks.get(update.getWorker());
		
		workerLocks.trajectoryLock .remove   ( update.getTrajectoryLock() );
		workerLocks.taskRemovalLock.removeAll( update.getTaskRemovals()     );
	}

}
