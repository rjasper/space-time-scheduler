package scheduler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import scheduler.util.SimpleIntervalSet;
import world.Trajectory;

public class Schedule {
	
	private Map<String, WorkerUnit> workerUnits = new HashMap<>();
	
	private List<ScheduleAlternative> alternatives = new LinkedList<>();
	
	private Map<WorkerUnit, WorkerUnitLocks> locks = new IdentityHashMap<>();
	
	private static class WorkerUnitLocks {
		
		public final SimpleIntervalSet<LocalDateTime> trajectoriesLock = new SimpleIntervalSet<>();
		
		public final SimpleIntervalSet<LocalDateTime> tasksLock = new SimpleIntervalSet<>();
		
		public final Set<Task> taskRemovalsLock = new HashSet<>();
		
	}
	
	public void addWorkerUnit(WorkerUnit worker) {
		Objects.requireNonNull(worker, "worker");
		
		WorkerUnit previous = workerUnits.putIfAbsent(worker.getId(), worker);
		
		if (previous != null)
			throw new IllegalArgumentException("worker id already assigned");
		
		locks.put(worker, new WorkerUnitLocks());
	}
	
	public WorkerUnit getWorkerUnit(String workerId) {
		Objects.requireNonNull(workerId, "workerId");
		
		WorkerUnit worker = workerUnits.get(workerId);
		
		if (worker == null)
			throw new IllegalArgumentException("unknown worker id");
		
		return worker;
	}
	
	public void removeWorkerUnit(String workerId) {
		Objects.requireNonNull(workerId, "workerId");
		
		WorkerUnit worker = workerUnits.remove(workerId);
		
		if (worker == null)
			throw new IllegalArgumentException("unknown worker id");
		
		locks.remove(worker);
	}
	
	public boolean addAlternative(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		
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
		// trajectories should not update fixated original ones unless fixation
		// got released
		
		// TODO check tasks
		// if updated trajectory not present original ones should lead to task
		// task locks should not overlap with scheduled task unless removed
		
		return alternative.getUpdates().stream()
			.allMatch(u -> {
				WorkerUnitLocks workerLocks = locks.get(u.getWorker());
				
				if (workerLocks == null)
					throw new IllegalArgumentException("unknown worker");
				
				return
					// no mutual task locks
					!u.getTasksLock()
						.intersects(workerLocks.tasksLock) &&
					// no mutual trajectory locks
					!u.getTrajectoriesLock()
						.intersects(workerLocks.trajectoriesLock) &&
					// no mutual task removals
					!u.getTaskRemovals().stream()
						.anyMatch(workerLocks.taskRemovalsLock::contains);
			});
	}
	
	private void applyLocks(ScheduleAlternative alternative) {
		for (WorkerUnitScheduleUpdate u : alternative.getUpdates()) {
			WorkerUnitLocks workerLocks = locks.get(u.getWorker());
			
			workerLocks.tasksLock       .add   ( u.getTasksLock()        );
			workerLocks.trajectoriesLock.add   ( u.getTrajectoriesLock() );
			workerLocks.taskRemovalsLock.addAll( u.getTaskRemovals()     );
		}
	}
	
	private void releaseLocks(ScheduleAlternative alternative) {
		for (WorkerUnitScheduleUpdate u : alternative.getUpdates()) {
			WorkerUnitLocks workerLocks = locks.get(u.getWorker());
			
			workerLocks.tasksLock       .remove   ( u.getTasksLock()        );
			workerLocks.trajectoriesLock.remove   ( u.getTrajectoriesLock() );
			workerLocks.taskRemovalsLock.removeAll( u.getTaskRemovals()     );
		}
	}

}
