package scheduler;

import static java.util.Collections.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.vividsolutions.jts.geom.Point;

import scheduler.util.IntervalSet;
import scheduler.util.SimpleIntervalSet;
import world.Trajectory;

public class Schedule {
	
	private Map<String, WorkerUnit> workers = new HashMap<>();
	
	private Set<ScheduleAlternative> alternatives = new HashSet<>();
	
	private Map<WorkerUnit, WorkerUnitLocks> locks = new IdentityHashMap<>();
	
	private static class WorkerUnitLocks {
		
		public final SimpleIntervalSet<LocalDateTime> trajectoryLock = new SimpleIntervalSet<>();
		
//		public final SimpleIntervalSet<LocalDateTime> tasksLock = new SimpleIntervalSet<>();
		
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
	
	public Collection<ScheduleAlternative> getAlternatives() {
		return unmodifiableCollection(alternatives);
	}
	
	public boolean addAlternative(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		
		if (!alternative.isSealed())
			new IllegalArgumentException("alternative not sealed");
		
		boolean compatible = checkCompatibility(alternative);
		
		if (compatible) {
			alternatives.add(alternative);
			applyLocks(alternative);
		}
		
		return compatible;
	}
	
	public void integrate(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		
		boolean status = alternatives.remove(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");
		
		releaseLocks(alternative);

		for (WorkerUnitScheduleUpdate u : alternative.getUpdates()) {
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
	
	private boolean checkCompatibility(ScheduleAlternative alternative) {
		// TODO also check frozen horizon?
		
		// TODO check trajectories
		// non-consecutive trajectories should touch original
		
		return alternative.getUpdates().stream()
			.allMatch(u -> {
				WorkerUnit worker = u.getWorker();
				WorkerUnitLocks workerLocks = locks.get(worker);
				
				if (workerLocks == null)
					throw new IllegalArgumentException("unknown worker");

				IntervalSet<LocalDateTime> originTasksIntervals = worker.getTaskIntervals();
				IntervalSet<LocalDateTime> removalsIntervals = u.getTaskRemovalIntervals();
				SimpleIntervalSet<LocalDateTime> trajLockIntervals = u.getTrajectoriesLock();
				
				Collection<Task> tasks = u.getTasks();
				Collection<Task> removals = u.getTaskRemovals();
				
				return
//					// no mutual task locks
//					!u.getTasksLock()
//						.intersects(workerLocks.tasksLock) &&

					// no mutual trajectory locks with origin disregarding removed tasks
					trajLockIntervals.intersection(originTasksIntervals)
						.remove(removalsIntervals).isEmpty() &&
					// no mutual trajectory locks with other alternatives
					!trajLockIntervals.intersects(workerLocks.trajectoryLock) &&
					// non-updated original trajectories lead to tasks
					tasks.stream().allMatch(t -> verifyTaskLocation(t, trajLockIntervals)) &&
					// no unknown task removals
					removals.stream().allMatch(worker::hasTask) &&
					// no mutual task removals
					!removals.stream().anyMatch(workerLocks.taskRemovalLock::contains);
			});
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
	
	private void applyLocks(ScheduleAlternative alternative) {
		for (WorkerUnitScheduleUpdate u : alternative.getUpdates()) {
			WorkerUnitLocks workerLocks = locks.get(u.getWorker());
			
//			workerLocks.tasksLock       .add   ( u.getTasksLock()        );
			workerLocks.trajectoryLock.add   ( u.getTrajectoriesLock() );
			workerLocks.taskRemovalLock.addAll( u.getTaskRemovals()     );
		}
	}
	
	private void releaseLocks(ScheduleAlternative alternative) {
		for (WorkerUnitScheduleUpdate u : alternative.getUpdates()) {
			WorkerUnitLocks workerLocks = locks.get(u.getWorker());
			
//			workerLocks.tasksLock       .remove   ( u.getTasksLock()        );
			workerLocks.trajectoryLock.remove   ( u.getTrajectoriesLock() );
			workerLocks.taskRemovalLock.removeAll( u.getTaskRemovals()     );
		}
	}

}
