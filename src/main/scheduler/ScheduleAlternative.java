package scheduler;

import java.time.LocalDateTime;
import java.util.Objects;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;

public class ScheduleAlternative {
	
//	private final ImmutableSortedMap<LocalDateTime, WorkerUnitTrajectory> trajectories;
//	
//	private final ImmutableSortedMap<LocalDateTime, Task> tasks;
//	
//	private final ImmutableSortedMap<LocalDateTime, Task> taskRemovals;
	
	private final ImmutableMap<WorkerUnit, WorkerUnitScheduleUpdate> updates;

	public ScheduleAlternative(
		ImmutableMap<WorkerUnit, WorkerUnitScheduleUpdate> updates)
	{
		this.updates = Objects.requireNonNull(updates, "updates");
	}

//	public static class Builder {
//		
//		private final ImmutableSortedMap.Builder<LocalDateTime, WorkerUnitTrajectory>
//			trajectoriesBuilder = ImmutableSortedMap.naturalOrder();
//
//		private final ImmutableSortedMap.Builder<LocalDateTime, Task>
//			tasksBuilder = ImmutableSortedMap.naturalOrder();
//		
//		private final ImmutableSortedMap.Builder<LocalDateTime, Task>
//			taskRemovalsBuilder = ImmutableSortedMap.naturalOrder();
//		
//		public void addTrajectory(WorkerUnit workerUnit, Trajectory trajectory) {
//			Objects.requireNonNull(workerUnit, "workerUnit");
//			Objects.requireNonNull(trajectory, "trajectory");
//			
//			WorkerUnitTrajectory workerUnitTrajectory =
//				new WorkerUnitTrajectory(workerUnit, trajectory);
//			
//			trajectoriesBuilder.put(trajectory.getStartTime(), workerUnitTrajectory);
//		}
//		
//		public void addTask(Task task) {
//			Objects.requireNonNull(task, "task");
//			
//			tasksBuilder.put(task.getStartTime(), task);
//		}
//		
//		public void removeTask(Task task) {
//			Objects.requireNonNull(task, "task");
//			
//			taskRemovalsBuilder.put(task.getStartTime(), task);
//		}
//		
//		public ScheduleAlternative build() {
//			return new ScheduleAlternative(
//				trajectoriesBuilder.build(),
//				tasksBuilder.build(),
//				taskRemovalsBuilder.build());
//		}
//		
//	}

//	public ImmutableSortedMap<LocalDateTime, WorkerUnitTrajectory> getTrajectories() {
//		return trajectories;
//	}
//
//	public ImmutableSortedMap<LocalDateTime, Task> getTasks() {
//		return tasks;
//	}
//
//	public ImmutableSortedMap<LocalDateTime, Task> getTaskRemovals() {
//		return taskRemovals;
//	}

}
