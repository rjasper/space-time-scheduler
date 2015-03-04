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

import jts.geom.immutable.ImmutablePoint;
import scheduler.util.IntervalSet;
import scheduler.util.SimpleIntervalSet;
import world.Trajectory;

import com.vividsolutions.jts.geom.Point;

public class Schedule {
	
	private Map<String, WorkerUnit> workers = new HashMap<>();
	
	private Set<ScheduleAlternative> alternatives = new HashSet<>();
	
	private Map<WorkerUnit, WorkerUnitLocks> locks = new IdentityHashMap<>();
	
	private static class WorkerUnitLocks {
		
		public final SimpleIntervalSet<LocalDateTime> trajectoryLock = new SimpleIntervalSet<>();
		
		public final Set<Task> taskRemovalLock = new HashSet<>();
		
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
		
		WorkerUnit worker = workers.remove(workerId);
		
		if (worker == null)
			throw new IllegalArgumentException("unknown worker id");
		
		locks.remove(worker);
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

		for (WorkerUnitUpdate u : alternative.getUpdates()) {
			WorkerUnit worker = u.getWorker();
			
			for (Task t : u.getTaskRemovals())
				worker.removeTask(t);
			for (Task t : u.getTasks())
				worker.addTask(t);
			for (Trajectory t : u.getTrajectories())
				worker.updateTrajectory(t);
		}
	}
	
	public void eliminate(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		
		boolean status = alternatives.remove(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");

		releaseLocks(alternative);
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
			IntervalSet<LocalDateTime> trajLockIntervals = u.getTrajectoriesLock();
			
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
	
	private void applyLocks(ScheduleAlternative alternative) {
		for (WorkerUnitUpdate u : alternative.getUpdates()) {
			WorkerUnitLocks workerLocks = locks.get(u.getWorker());
			
			workerLocks.trajectoryLock .add   ( u.getTrajectoriesLock() );
			workerLocks.taskRemovalLock.addAll( u.getTaskRemovals()     );
		}
	}
	
	private void releaseLocks(ScheduleAlternative alternative) {
		for (WorkerUnitUpdate u : alternative.getUpdates()) {
			WorkerUnitLocks workerLocks = locks.get(u.getWorker());
			
			workerLocks.trajectoryLock .remove   ( u.getTrajectoriesLock() );
			workerLocks.taskRemovalLock.removeAll( u.getTaskRemovals()     );
		}
	}

}
