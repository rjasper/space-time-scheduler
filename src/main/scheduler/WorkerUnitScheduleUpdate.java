package scheduler;

import java.time.LocalDateTime;
import java.util.Objects;

import world.Trajectory;

import com.google.common.collect.ImmutableSortedMap;

public class WorkerUnitScheduleUpdate {
	
	private final WorkerUnit worker;
	
	private final ImmutableSortedMap<LocalDateTime, Trajectory> trajectories;
	
	private final ImmutableSortedMap<LocalDateTime, Task> tasks;
	
	private final ImmutableSortedMap<LocalDateTime, Task> taskRemovals;

	public WorkerUnitScheduleUpdate(
		WorkerUnit worker,
		ImmutableSortedMap<LocalDateTime, Trajectory> trajectories,
		ImmutableSortedMap<LocalDateTime, Task> tasks,
		ImmutableSortedMap<LocalDateTime, Task> taskRemovals)
	{
		this.worker       = Objects.requireNonNull(worker      , "worker"      );
		this.trajectories = Objects.requireNonNull(trajectories, "trajectories");
		this.tasks        = Objects.requireNonNull(tasks       , "tasks"       );
		this.taskRemovals = Objects.requireNonNull(taskRemovals, "taskRemovals");
	}

	public WorkerUnit getWorker() {
		return worker;
	}

	public ImmutableSortedMap<LocalDateTime, Trajectory> getTrajectories() {
		return trajectories;
	}

	public ImmutableSortedMap<LocalDateTime, Task> getTasks() {
		return tasks;
	}

	public ImmutableSortedMap<LocalDateTime, Task> getTaskRemovals() {
		return taskRemovals;
	}

}
