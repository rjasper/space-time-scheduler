package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

// TODO document

public class ScheduleResult {
	
	private final UUID transactionId;
	
	private final Map<UUID, Job> jobs;
	
	private final Map<UUID, Job> jobRemovals;
	
	private final Collection<TrajectoryUpdate> trajectories;
	
	public static class TrajectoryUpdate {
		
		private final Trajectory trajectory;
		
		private final NodeReference nodeRef;

		public TrajectoryUpdate(Trajectory trajectory, NodeReference nodeRef) {
			this.trajectory = Objects.requireNonNull(trajectory, "trajectory");
			this.nodeRef  = Objects.requireNonNull(nodeRef , "nodeRef" );
		}

		/**
		 * @return the updated trajectory.
		 */
		public Trajectory getTrajectory() {
			return trajectory;
		}

		/**
		 * @return the node reference whose trajectory was changed.
		 */
		public NodeReference getNodeRef() {
			return nodeRef;
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("%s:%s",
				nodeRef, trajectory);
		}
	}
	
	private static final ScheduleResult ERRONEOUS_SCHEDULE_RESULT =
		new ScheduleResult(null, emptyMap(), emptyMap(), emptyList());
	
	public static ScheduleResult error() {
		return ERRONEOUS_SCHEDULE_RESULT;
	}
	
	public static ScheduleResult success(
		UUID transactionId,
		Map<UUID, Job> jobs,
		Map<UUID, Job> removals,
		Collection<TrajectoryUpdate> trajectories)
	{
		Objects.requireNonNull(transactionId, "transactionId");
		Objects.requireNonNull(jobs, "jobs");
		Objects.requireNonNull(removals, "removals");
		CollectionsRequire.requireNonNull(trajectories, "trajectories");
		
		return new ScheduleResult(transactionId, jobs, removals, trajectories);
	}

	private ScheduleResult(
		UUID transactionId,
		Map<UUID, Job> jobs,
		Map<UUID, Job> removals,
		Collection<TrajectoryUpdate> trajectories)
	{
		this.transactionId = transactionId;
		this.jobs         = jobs;
		this.jobRemovals  = removals;
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
	 * @return the scheduled jobs.
	 */
	public Map<UUID, Job> getJobs() {
		if (isError())
			throw new IllegalStateException("is error");
		
		return jobs;
	}

	public Map<UUID, Job> getJobRemovals() {
		if (isError())
			throw new IllegalStateException("is error");
		
		return jobRemovals;
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
			return String.format("success(trajectories=%s, jobs=%s, removales=%s)",
				trajectories, jobs, jobRemovals);
		else
			return "error";
	}
	
}
