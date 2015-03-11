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
	
	private final ScheduleAlternative parent;
	
	private int branches = 0;
	
	private boolean invalid = false;
	
	private boolean sealed = false;

	private Map<WorkerUnit, WorkerUnitUpdate> updates =
		new IdentityHashMap<>();
	
	private Map<UUID, Task> tasks = new HashMap<>();
	
	public ScheduleAlternative() {
		this.parent = null;
	}
	
	private ScheduleAlternative(ScheduleAlternative parent) {
		this.parent = parent;
		
		parent.updates.values().stream()
			.map(WorkerUnitUpdate::clone)
			.forEach(u -> this.updates.put(u.getWorker(), u));
		
		this.tasks.putAll(parent.tasks);
	}
	
	public boolean isRootBranch() {
		return parent == null;
	}
	
	public boolean isBranched() {
		return branches > 0;
	}
	
	public boolean isInvalid() {
		return invalid;
	}
	
	public boolean isModifiable() {
		return !isBranched() && !isInvalid() && !isSealed();
	}
	
	public boolean isSealed() {
		return sealed;
	}
	
	public boolean isEmpty() {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		
		return updates.isEmpty();
	}
	
	public boolean updatesWorker(WorkerUnit worker) {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		
		return updates.containsKey(worker);
	}

	public Collection<WorkerUnitUpdate> getUpdates() {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		if (!isSealed())
			throw new IllegalStateException("alternative not sealed");
		
		return unmodifiableCollection(updates.values());
	}
	
	public WorkerUnitUpdate popUpdate(WorkerUnit worker) {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
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

		if (!isModifiable())
			throw new IllegalStateException("alternative is unmodifiable");
		
		getUpdate(worker).updateTrajectory(trajectory);
	}
	
	public Collection<WorkerUnit> getWorkers() {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		
		return unmodifiableCollection(updates.keySet());
	}
	
	public Collection<Trajectory> getTrajectoryUpdates(WorkerUnit worker) {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		
		return updatesWorker(worker)
			? getUpdate(worker).getTrajectories()
			: emptyList();
	}
	
	public boolean hasTask(UUID taskId) {
		return tasks.containsKey(taskId);
	}
	
	public Task getTask(UUID taskId) {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		
		return tasks.get(taskId);
	}

	public void addTask(Task task) {
		Objects.requireNonNull(task, "task");

		if (!isModifiable())
			throw new IllegalStateException("alternative is unmodifiable");
		
		tasks.put(task.getId(), task);
		getUpdate(task.getWorkerReference().getActual())
			.addTask(task);
	}
	
	public void addTaskRemoval(Task task) {
		Objects.requireNonNull(task, "task");

		if (!isModifiable())
			throw new IllegalStateException("alternative is unmodifiable");
		
		getUpdate(task.getWorkerReference().getActual())
			.addTaskRemoval(task);
	}
	
	public ScheduleAlternative branch() {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");
		
		++branches;
		
		return new ScheduleAlternative(this);
	}
	
	public ScheduleAlternative merge() {
		if (isRootBranch())
			throw new IllegalStateException("cannot merge root branch");
		if (!isModifiable())
			throw new IllegalStateException("alternative is unmodifiable");
		
		parent.mergeBranch(this);
		
		return parent;
	}
	
	private void mergeBranch(ScheduleAlternative branch) {
		if (branches > 1)
			throw new IllegalStateException("cannot merge while there are multiple branches");
		
		updates = branch.updates;
		tasks = branch.tasks;
		
		--branches;
	}
	
	public ScheduleAlternative delete() {
		if (isRootBranch())
			throw new IllegalStateException("cannot delete root branch");
		if (!isModifiable())
			throw new IllegalStateException("alternative is unmodifiable");
		
		parent.deleteBranch();

		invalid = true;
		updates = null;
		tasks = null;
		
		return parent;
	}
	
	private void deleteBranch() {
		--branches;
	}
	
	public void seal() {
		if (!isRootBranch())
			throw new IllegalStateException("can only seal root branch");
		if (isBranched())
			throw new IllegalStateException("cannot seal branched alternative");
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");
		
		for (WorkerUnitUpdate u : updates.values())
			u.seal();
		
		sealed = true;
	}

}
