package scheduler;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import world.Trajectory;

public class ScheduleAlternative {
	
	private final Map<WorkerUnit, WorkerUnitUpdate> updates =
		new IdentityHashMap<>();
	
	private final Map<UUID, Task> tasks = new HashMap<>();
	
	private boolean sealed = false;

	public boolean isSealed() {
		return sealed;
	}
	
	public boolean isEmpty() {
		return updates.isEmpty();
	}
	
	public boolean updatesWorker(WorkerUnit worker) {
		return updates.containsKey(worker);
	}

	public Collection<WorkerUnitUpdate> getUpdates() {
		if (!isSealed())
			throw new IllegalStateException("alternative not sealed");
		
		return unmodifiableCollection(updates.values());
	}
	
	public WorkerUnitUpdate popUpdate(WorkerUnit worker) {
		if (!isSealed())
			throw new IllegalStateException("alternative not sealed");
		
		WorkerUnitUpdate update = updates.remove(worker);
		
		if (update == null)
			throw new IllegalArgumentException("unknown worker");
		
		for (Task t : update.getTasks())
			tasks.remove(t.getId());
		
		return update;
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
	
	public Task getTask(UUID taskId) {
		return tasks.get(taskId);
	}

	public void addTask(Task task) {
		Objects.requireNonNull(task, "task");
		
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");
		
		tasks.put(task.getId(), task);
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
