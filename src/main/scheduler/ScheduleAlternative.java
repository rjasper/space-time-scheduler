package scheduler;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import world.Trajectory;

public class ScheduleAlternative {
	
	private final Map<WorkerUnit, WorkerUnitUpdate> updates =
		new IdentityHashMap<>();
	
	private boolean sealed = false;

	public boolean isSealed() {
		return sealed;
	}
	
	public boolean updatesWorker(WorkerUnit worker) {
		return updates.containsKey(worker);
	}

	public Collection<WorkerUnitUpdate> getUpdates() {
		if (!isSealed())
			throw new IllegalStateException("alternative not sealed");
		
		return unmodifiableCollection(updates.values());
	}
	
	private WorkerUnitUpdate getUpdate(WorkerUnit worker) {
		return updates.computeIfAbsent(worker, WorkerUnitUpdate::new);
	}

	public void updateTrajectory(WorkerUnit worker, Trajectory trajectory) {
		Objects.requireNonNull(worker, "worker");
		Objects.requireNonNull(trajectory, "trajectory");
		
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");
		
		getUpdate(worker).updateTrajectory(trajectory);
	}
	
	public Collection<WorkerUnit> getWorkers() {
		return unmodifiableCollection(updates.keySet());
	}
	
	public Collection<Trajectory> getTrajectoryUpdates(WorkerUnit worker) {
		return updatesWorker(worker)
			? getUpdate(worker).getTrajectories()
			: emptyList();
	}
	
	public void addTask(Task task) {
		Objects.requireNonNull(task, "task");
		
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");
		
		getUpdate(task.getAssignedWorker().getActual())
			.addTask(task);
	}
	
	public void addTaskRemoval(Task task) {
		Objects.requireNonNull(task, "task");
		
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");
		
		getUpdate(task.getAssignedWorker().getActual())
			.addTaskRemoval(task);
	}
	
	public void seal() {
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");
		
		for (WorkerUnitUpdate u : updates.values())
			u.seal();
		
		sealed = true;
	}

}
