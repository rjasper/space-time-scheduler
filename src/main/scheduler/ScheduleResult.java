package scheduler;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import util.CollectionsRequire;
import world.Trajectory;

// TODO document

public class ScheduleResult {
	
	private final UUID transactionId;
	
	private final Map<UUID, Task> tasks;
	
	private final Map<UUID, Task> taskRemovals;
	
	private final Collection<TrajectoryUpdate> trajectories;
	
	public static class TrajectoryUpdate {
		
		private final Trajectory trajectory;
		
		private final WorkerUnitReference workerRef;

		public TrajectoryUpdate(Trajectory trajectory, WorkerUnitReference workerRef) {
			this.trajectory = Objects.requireNonNull(trajectory, "trajectory");
			this.workerRef  = Objects.requireNonNull(workerRef , "workerRef" );
		}

		/**
		 * @return the updated trajectory.
		 */
		public Trajectory getTrajectory() {
			return trajectory;
		}

		/**
		 * @return the worker reference whose trajectory was changed.
		 */
		public WorkerUnitReference getWorkerRef() {
			return workerRef;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("%s:%s",
				workerRef, trajectory);
		}
	}
	
	private static final ScheduleResult ERRONEOUS_SCHEDULE_RESULT =
		new ScheduleResult(null, emptyMap(), emptyMap(), emptyList());
	
	public static ScheduleResult error() {
		return ERRONEOUS_SCHEDULE_RESULT;
	}
	
	public static ScheduleResult success(
		UUID transactionId,
		Map<UUID, Task> tasks,
		Map<UUID, Task> removals,
		Collection<TrajectoryUpdate> trajectories)
	{
		Objects.requireNonNull(transactionId, "transactionId");
		Objects.requireNonNull(tasks, "tasks");
		Objects.requireNonNull(removals, "removals");
		CollectionsRequire.requireNonNull(trajectories, "trajectories");
		
		return new ScheduleResult(transactionId, tasks, removals, trajectories);
	}

	private ScheduleResult(
		UUID transactionId,
		Map<UUID, Task> tasks,
		Map<UUID, Task> removals,
		Collection<TrajectoryUpdate> trajectories)
	{
		this.transactionId = transactionId;
		this.tasks         = tasks;
		this.taskRemovals  = removals;
		this.trajectories  = trajectories;
	}

	/**
	 * @return {@code true} if the scheduling was unsuccessful.
	 */
	public boolean isError() {
		return transactionId == null;
	}
	
	/**
	 * @return {@code true} if the scheduling was successful.
	 */
	public boolean isSuccess() {
		return transactionId != null;
	}

	public UUID getTransactionId() {
		if (isError())
			throw new IllegalStateException("is error");
		
		return transactionId;
	}

	/**
	 * @return the scheduled tasks.
	 */
	public Map<UUID, Task> getTasks() {
		if (isError())
			throw new IllegalStateException("is error");
		
		return tasks;
	}

	public Map<UUID, Task> getTaskRemovals() {
		if (isError())
			throw new IllegalStateException("is error");
		
		return taskRemovals;
	}

	/**
	 * @return the updated trajectories.
	 */
	public Collection<TrajectoryUpdate> getTrajectoryUpdates() {
		if (isError())
			throw new IllegalStateException("is error");
		
		return trajectories;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isSuccess())
			return String.format("success(trajectories=%s, tasks=%s, removales=%s)",
				trajectories, tasks, taskRemovals);
		else
			return "error";
	}
	
}
