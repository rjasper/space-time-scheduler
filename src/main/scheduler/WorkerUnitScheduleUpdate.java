package scheduler;

import java.util.Objects;

import scheduler.util.SimpleIntervalSet;
import world.Trajectory;

import com.google.common.collect.ImmutableList;

public class WorkerUnitScheduleUpdate {
	
	private final WorkerUnit worker;
	
	private final ImmutableList<Trajectory> trajectories;
	
	private final ImmutableList<Task> tasks;
	
	private final ImmutableList<Task> taskRemovals;
	
	private final SimpleIntervalSet trajectoriesLock = new SimpleIntervalSet();
	
	private final SimpleIntervalSet tasksLock = new SimpleIntervalSet();

	public WorkerUnitScheduleUpdate(
		WorkerUnit worker,
		ImmutableList<Trajectory> trajectories,
		ImmutableList<Task> tasks,
		ImmutableList<Task> taskRemovals)
	{
		this.worker       = Objects.requireNonNull(worker      , "worker"      );
		this.trajectories = Objects.requireNonNull(trajectories, "trajectories");
		this.tasks        = Objects.requireNonNull(tasks       , "tasks"       );
		this.taskRemovals = Objects.requireNonNull(taskRemovals, "taskRemovals");
		
		// TODO updates should not predate initialTime
		
		// TODO check tasks
		// tasks should not overlap
		
		// TODO check trajectories
		// consecutive trajectories should touch
		// trajectories if present should lead to tasks

		// lock new tasks
		for (Task t : tasks)
			tasksLock.add(t.getStartTime(), t.getFinishTime());
		tasksLock.seal();
		// lock trajectory updates
		for (Trajectory t : trajectories)
			trajectoriesLock.add(t.getStartTime(), t.getFinishTime());
		// lock fixated trajectories
		trajectoriesLock.add(tasksLock);
		trajectoriesLock.seal();
	}

	public WorkerUnit getWorker() {
		return worker;
	}

	public ImmutableList<Trajectory> getTrajectories() {
		return trajectories;
	}

	public ImmutableList<Task> getTasks() {
		return tasks;
	}

	public ImmutableList<Task> getTaskRemovals() {
		return taskRemovals;
	}

	public SimpleIntervalSet getTrajectoriesLock() {
		return trajectoriesLock;
	}

	public SimpleIntervalSet getTasksLock() {
		return tasksLock;
	}

}
