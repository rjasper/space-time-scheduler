package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

// TODO document
/**
 * <p>
 * An Object of the class {@code ScheduleResult} describes the result of a
 * schedule operation. Schedule operations do not always succeed and therefore
 * need to indicate whether or not the operation succeeded. Additionally, the
 * changes of a schedule operations are not easily traceable unless they are
 * actively and separately recorded.
 * </p>
 * 
 * <p>
 * An {@code ScheduleResult} provides the means to both inform the caller
 * whether the intended effect was accomplished and which changes were
 * calculated.
 * </p>
 * 
 * <p>
 * Another feature is the transaction ID field which enables to commit/abort the
 * changes of an schedule operation after the changes were calculated.
 * </p>
 * 
 * @author Rico Jasper
 */
public class ScheduleResult {
	
	/**
	 * The transaction ID.
	 */
	private final UUID transactionId;
	
	/**
	 * The jobs to be added.
	 */
	private final Map<UUID, Job> jobs;
	
	/**
	 * The jobs to be removed.
	 */
	private final Map<UUID, Job> jobRemovals;
	
	/**
	 * The trajectory updates.
	 */
	private final Collection<TrajectoryUpdate> trajectories;
	
	/**
	 * An {@code TrajectoryUpdate} is simply a pair of a trajectory and a node
	 * reference.
	 * 
	 * The node reference denotes on which node the new trajectory will be
	 * applied.
	 */
	public static class TrajectoryUpdate {
		
		/**
		 * The trajectory.
		 */
		private final Trajectory trajectory;
		
		/**
		 * The node reference.
		 */
		private final NodeReference nodeRef;

		/**
		 * Constructs a {@code TrajectoryUpdate}.
		 * 
		 * @param trajectory
		 * @param nodeRef
		 */
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
	
	/**
	 * Stores a erroneous {@code ScheduleResult}.
	 */
	private static final ScheduleResult ERRONEOUS_SCHEDULE_RESULT =
		new ScheduleResult(null, emptyMap(), emptyMap(), emptyList());
	
	/**
	 * @return a erroneous {@code ScheduleResult}.
	 */
	public static ScheduleResult error() {
		return ERRONEOUS_SCHEDULE_RESULT;
	}
	
	/**
	 * Returns {@code ScheduleResult} indicating a successful schedule
	 * operation.
	 * 
	 * @param transactionId
	 * @param jobs
	 *            to be added
	 * @param removals
	 *            jobs to be removed
	 * @param trajectories
	 *            trajectories to be updated.
	 * @return a successful {@code ScheduleResult}.
	 */
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

	/**
	 * Constructs a {@code ScheduleResult}. An erroneous result is indicated
	 * by a {@code null} transaction ID.
	 * 
	 * @param transactionId
	 * @param jobs
	 * @param removals
	 * @param trajectories
	 */
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

	/**
	 * @return the transaction ID.
	 * @throws IllegalStateException if is error.
	 */
	public UUID getTransactionId() {
		if (isError())
			throw new IllegalStateException("is error");
		
		return transactionId;
	}

	/**
	 * @return the scheduled jobs.
	 * @throws IllegalStateException if is error.
	 */
	public Map<UUID, Job> getJobs() {
		if (isError())
			throw new IllegalStateException("is error");
		
		return jobs;
	}

	/**
	 * @return the jobs to be removed.
	 * @throws IllegalStateException if is error.
	 */
	public Map<UUID, Job> getJobRemovals() {
		if (isError())
			throw new IllegalStateException("is error");
		
		return jobRemovals;
	}

	/**
	 * @return the updated trajectories.
	 * @throws IllegalStateException if is error.
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
