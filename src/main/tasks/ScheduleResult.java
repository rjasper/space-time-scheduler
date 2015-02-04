package tasks;

import static java.util.Collections.*;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import util.CollectionsRequire;
import world.Trajectory;

// TODO document

public class ScheduleResult {
	
	private final boolean error;
	
	private final Map<UUID, Task> tasks;
	
	private final List<TrajectoryUpdate> trajectories;
	
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
	
	private static final ScheduleResult ERROR_SCHEDULE_RESULT =
		new ScheduleResult(true, emptyList(), emptyList());
	
	public static ScheduleResult error() {
		return ERROR_SCHEDULE_RESULT;
	}
	
	public static ScheduleResult success(List<Task> tasks, List<TrajectoryUpdate> trajectories) {
		return new ScheduleResult(false, tasks, trajectories);
	}

	private ScheduleResult(boolean error, List<Task> tasks, List<TrajectoryUpdate> trajectories) {
		this.error = error;
		this.tasks = tasks.stream().collect(toMap(Task::getId, identity()));
		this.trajectories = CollectionsRequire.requireContainsNonNull(trajectories, "trajectories");
	}

	/**
	 * @return {@code true} if the scheduling was unsuccessful.
	 */
	public boolean isError() {
		return error;
	}
	
	/**
	 * @return {@code true} if the scheduling was successful.
	 */
	public boolean isSuccess() {
		return !error;
	}

	/**
	 * @return the scheduled tasks.
	 */
	public Map<UUID, Task> getTasks() {
		return tasks;
	}

	/**
	 * @return the updated trajectories.
	 */
	public List<TrajectoryUpdate> getTrajectories() {
		return trajectories;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("tasks=%s, trajectories=%s",
			tasks.toString(), trajectories.toString());
	}
	
}
