package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static java.util.UUID.*;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult.TrajectoryUpdate;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.RadiusBasedWorldPerspectiveCache;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspective;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspectiveCache;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.StraightEdgePathfinder;

// TODO document
/**
 * <p>The Scheduler manages the distribution of job to a set of
 * {@link Node}s. A new job can be scheduled by providing a
 * {@link JobSpecification}. The {@link #schedule(JobSpecification)} method tries
 * to find a realizable configuration which satisfies the specification. In
 * the successful case a job will be created and assigned to an appropriate
 * node.</p>
 *
 * @author Rico Jasper
 */
public class Scheduler {

	/**
	 * The earliest time possible used by the scheduler.
	 */
	public static final LocalDateTime BEGIN_OF_TIME = LocalDateTime.MIN;

	/**
	 * The latest time possible used by the scheduler.
	 */
	public static final LocalDateTime END_OF_TIME = LocalDateTime.MAX;

	/**
	 * The physical outside world representation where the nodes are located.
	 */
	private final World world;

	/**
	 * A cache of the {@link WorldPerspective perspectives} of the
	 * {@link Node nodes}.
	 */
	private final WorldPerspectiveCache perspectiveCache;

	/**
	 * The schedule managed by this scheduler.
	 */
	private final Schedule schedule = new Schedule();
	
	/**
	 * Stores the transactions.
	 */
	private final Map<UUID, Transaction> transactions = new HashMap<>();
	
	/**
	 * The present time.
	 */
	private LocalDateTime presentTime = BEGIN_OF_TIME;
	
	/**
	 * The time of the frozen horizon.
	 */
	private LocalDateTime frozenHorizonTime = BEGIN_OF_TIME;
	
	/**
	 * The duration from {@link #presentTime} to {@link #frozenHorizonTime}.
	 */
	private Duration frozenHorizonDuration = Duration.ZERO;
	
	/**
	 * The safety margin between to dependent task.
	 */
	private Duration interDependencyMargin = Duration.ZERO;
	
	/**
	 * Constructs a scheduler using the given world and set of nodes.
	 * The nodes are expected to be managed exclusively by this scheduler.
	 *
	 * @param world
	 * @param nodePool
	 * @throws NullPointerException if world or nodes is null
	 */
	public Scheduler(World world) {
		Objects.requireNonNull(world, "world");
		
		this.world = world;
		this.perspectiveCache = new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);
	}

	/**
	 * Adds a new {@link Node} to the scheduler. The given specification
	 * is used to create the node.
	 * 
	 * @param spec
	 * @return a reference to the node.
	 * @throws NullPointerException
	 *             if {@code spec} is {@code null}
	 * @throws IllegalArgumentException
	 *             if node ID is already assigned.
	 */
	public NodeReference addNode(NodeSpecification spec) {
		// also throws NullPointerException
		if (spec.getInitialTime().isBefore(getFrozenHorizonTime()))
			throw new IllegalArgumentException("initial time violates frozen horizon");
		
		Node node = new Node(spec);

		// TODO check validity of node placement
		// don't overlap with static or dynamic obstacles or with other nodes
		// only allow after frozen horizon
		
		schedule.addNode(node);
		
		return node.getReference();
	}
	
	/**
	 * Returns the reference to the node with the given id.
	 * 
	 * @param nodeId
	 * @return the reference.
	 * @throws NullPointerException
	 *             if {@code nodeId} is {@code null}
	 * @throws IllegalArgumentException
	 *             if node ID is unassigned.
	 */
	public NodeReference getNodeReference(String nodeId) {
		return schedule.getNode(nodeId).getReference();
	}
	
	/**
	 * Removes a node from the schedule. Only idle nodes can be removed.
	 * 
	 * @param nodeId
	 * @throws NullPointerException
	 *             if {@code nodeId} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code nodeId} is unknown.
	 * @throws IllegalStateException
	 *             if the node is not idle.
	 */
	public void removeNode(String nodeId) {
		Node node = schedule.getNode(nodeId);
		
		node.cleanUp(presentTime);
		
		schedule.removeNode(nodeId);
		perspectiveCache.removePerceiver(node);
	}
	
	/**
	 * <p>
	 * Retrieves the job by the given job id.
	 * </p>
	 * 
	 * <p>
	 * Note that only scheduled (committed) jobs can be retrieved.
	 * </p>
	 * 
	 * @param jobId
	 * @return the job.
	 * @throws NullPointerException
	 *             if {@code jobId} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code jobId} is unknown.
	 */
	public Job getJob(UUID jobId) {
		return schedule.getJob(jobId);
	}
	
	/**
	 * <p>
	 * Removes a job from the schedule.
	 * </p>
	 * 
	 * <p>
	 * Be careful to not remove finished jobs since they might have been removed
	 * by {@link #cleanUp()}.
	 * </p>
	 * 
	 * @param jobId
	 * @throws NullPointerException
	 *             if {@code jobId} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code jobId} is unknown.
	 * @throws IllegalStateException
	 *             if the job is locked for removal.
	 */
	public void removeJob(UUID jobId) {
		schedule.removeJob(jobId);
	}

	/**
	 * @return the present time.
	 */
	public LocalDateTime getPresentTime() {
		return presentTime;
	}

	/**
	 * Sets the present time. In the regular case setting the present time will push the frozen
	 * horizon forward.
	 * 
	 * @param presentTime
	 * @throws NullPointerException
	 *             if {@code presentTime} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code presentTime} would decrease.
	 */
	public void setPresentTime(LocalDateTime presentTime) {
		// also throws NullPointerException
		if (presentTime.compareTo(this.presentTime) < 0)
			throw new IllegalArgumentException("presentTime cannot decrease");
		
		this.presentTime = presentTime;
		
		updateFrozenHorizonTime();
	}

	/**
	 * @return the frozenHorizonTime
	 */
	public LocalDateTime getFrozenHorizonTime() {
		return frozenHorizonTime;
	}

	/**
	 * @param frozenHorizonTime the frozenHorizonTime to set
	 */
	private void updateFrozenHorizonTime() {
		LocalDateTime tmp = presentTime.plus(frozenHorizonDuration);
		
		// frozen horizon cannot go backwards
		if (tmp.isAfter(frozenHorizonTime))
			frozenHorizonTime = tmp;
	}

	/**
	 * @return the frozenHorizonDuration
	 */
	public Duration getFrozenHorizonDuration() {
		return frozenHorizonDuration;
	}

	/**
	 * <p>
	 * Sets the duration between the frozen horizon and the present time.
	 * </p>
	 * 
	 * <p>
	 * Note that in the regular case increasing the duration will also push the
	 * frozen horizon forward. However, decreasing the duration will never
	 * change the frozen horizon. The frozen horizon cannot be pulled back.
	 * </p>
	 * 
	 * @param frozenHorizonDuration
	 *            the frozenHorizonDuration to set
	 */
	public void setFrozenHorizonDuration(Duration frozenHorizonDuration) {
		// also throws NullPointerException
		if (frozenHorizonDuration.isNegative())
			throw new IllegalArgumentException("frozenHorizonDuration be negative");
		
		this.frozenHorizonDuration = frozenHorizonDuration;
		
		updateFrozenHorizonTime();
	}

	/**
	 * @return the current safety margin kept between to dependent tasks.
	 */
	public Duration getInterDependencyMargin() {
		return interDependencyMargin;
	}
	
	/**
	 * Sets the safety margin kept between to dependent tasks.
	 * 
	 * @param interDependencyMargin
	 * @see Scheduler#schedule(Collection, SimpleDirectedGraph)
	 * @throws NullPointerException
	 *             if {@code interDependencyMargin} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code interDependencyMargin} is negative.
	 */
	public void setInterDependencyMargin(Duration interDependencyMargin) {
		Objects.requireNonNull(interDependencyMargin, "interDependencyMargin");
		
		if (interDependencyMargin.isNegative())
			throw new IllegalArgumentException("negative margin");
		
		this.interDependencyMargin = interDependencyMargin;
	}

	/**
	 * Returns if the given transaction is known.
	 * 
	 * @param transactionId
	 * @return {@code true} if the transaction to the given id is known.
	 */
	public boolean hasTransaction(UUID transactionId) {
		Objects.requireNonNull(transactionId, "transactionId");
		
		return transactions.containsKey(transactionId);
	}

	/**
	 * The default amount of location picks tried by the scheduler before
	 * giving up.
	 */
	private static final int MAX_LOCATION_PICKS = 5;

	/**
	 * <p>
	 * Tries to schedule a new job satisfying the given specification.
	 * </p>
	 * 
	 * <p>
	 * After returning the changes to the schedule have to be commit to be
	 * actually applied. The current schedule will not be updated otherwise.
	 * Eventually the change must either be committed or aborted. However, if
	 * the scheduling was unsuccessful, neither commit nor abort must be called.
	 * An unsuccessful scheduling is indicated by the returned
	 * {@link ScheduleResult}.
	 * </p>
	 *
	 * @param spec
	 * @return a schedule result.
	 * @throws NullPointerException
	 *             if {@code spec} is {@code null}.
	 * @see #commit(UUID)
	 * @see #commit(UUID, String)
	 * @see #abort(UUID)
	 * @see #abort(UUID, String)
	 */
	public ScheduleResult schedule(JobSpecification spec) {
		ScheduleAlternative alternative = new ScheduleAlternative();
		boolean status = scheduleImpl(spec, alternative);

		return status ? success(alternative) : error();
	}
	
	private boolean scheduleImpl(JobSpecification spec, ScheduleAlternative alternative) {
		SingularJobScheduler sc = new SingularJobScheduler();
		
		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setSpecification(spec);
		sc.setMaxLocationPicks(MAX_LOCATION_PICKS);
		
		return sc.schedule();
	}
	
	/**
	 * <p>
	 * Tries to schedule multiple jobs satisfying the given specifications.
	 * Additionally, a dependency graph has to be supplied. Dependent jobs may
	 * have an edge pointing to the job's id required to be finished before
	 * being executed itself.
	 * </p>
	 * 
	 * <p>
	 * The UUIDs of each job to be schedules must be present as vertex in the
	 * graph. The graph must not contain any other vertices than the job ids
	 * given by the specifications.
	 * </p>
	 * 
	 * <p>
	 * After returning the changes to the schedule have to be commit to be
	 * actually applied. The current schedule will not be updated otherwise.
	 * Eventually the change must either be committed or aborted. However, if
	 * the scheduling was unsuccessful, neither commit nor abort must be called.
	 * An unsuccessful scheduling is indicated by the returned
	 * {@link ScheduleResult}.
	 * </p>
	 * 
	 * @param specs
	 * @param dependencies
	 * @return a schedule result.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the specs' job ids are inconsistent to the graph's
	 *             vertices.
	 * @see #commit(UUID)
	 * @see #commit(UUID, String)
	 * @see #abort(UUID)
	 * @see #abort(UUID, String)
	 */
	public ScheduleResult schedule(
		Collection<JobSpecification> specs,
		SimpleDirectedGraph<UUID, DefaultEdge> dependencies)
	{
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		boolean status = scheduleImpl(specs, dependencies, alternative);

		return status ? success(alternative) : error();
	}
	
	private boolean scheduleImpl(
		Collection<JobSpecification> specs,
		SimpleDirectedGraph<UUID, DefaultEdge> dependencies,
		ScheduleAlternative alternative)
	{
		DependentJobScheduler sc = new DependentJobScheduler();
		
		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setSpecifications(specs);
		sc.setDependencies(dependencies);
		sc.setInterDependencyMargin(interDependencyMargin);
		sc.setMaxLocationPicks(MAX_LOCATION_PICKS);
		
		return sc.schedule();
	}

	/**
	 * <p>
	 * Tries to schedule multiple job repetitions periodically.
	 * </p>
	 * 
	 * <p>
	 * After returning the changes to the schedule have to be commit to be
	 * actually applied. The current schedule will not be updated otherwise.
	 * Eventually the change must either be committed or aborted. However, if
	 * the scheduling was unsuccessful, neither commit nor abort must be called.
	 * An unsuccessful scheduling is indicated by the returned
	 * {@link ScheduleResult}.
	 * </p>
	 * 
	 * @param spec
	 * @return a schedule result.
	 * @throws NullPointerException
	 *             if {@code spec} is {@code null}.
	 * @see #commit(UUID)
	 * @see #commit(UUID, String)
	 * @see #abort(UUID)
	 * @see #abort(UUID, String)
	 */
	public ScheduleResult schedule(PeriodicJobSpecification spec) {
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		boolean status = scheduleImpl(spec, alternative);
		
		return status ? success(alternative) : error();
	}
	
	private boolean scheduleImpl(PeriodicJobSpecification spec, ScheduleAlternative alternative) {
		PeriodicJobScheduler sc = new PeriodicJobScheduler();

		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setSpecification(spec);
		sc.setMaxLocationPicks(MAX_LOCATION_PICKS);
		
		return sc.schedule();
	}
	
	/**
	 * <p>
	 * Tries to unschedule the job with the given id. Unscheduling includes the
	 * removal of the job and the recalculation of the assigned node's
	 * trajectory.
	 * </p>
	 * 
	 * <p>
	 * After returning the changes to the schedule have to be commit to be
	 * actually applied. The current schedule will not be updated otherwise.
	 * Eventually the change must either be committed or aborted. However, if
	 * the scheduling was unsuccessful, neither commit nor abort must be called.
	 * An unsuccessful scheduling is indicated by the returned
	 * {@link ScheduleResult}.
	 * </p>
	 * 
	 * @param jobId
	 * @return a schedule result.
	 * @throws NullPointerException
	 *             if {@code jobId} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code jobId} is unknown.
	 * @throws IllegalStateException
	 *             if the job is already locked for removal.
	 * @see #commit(UUID)
	 * @see #commit(UUID, String)
	 * @see #abort(UUID)
	 * @see #abort(UUID, String)
	 */
	public ScheduleResult unschedule(UUID jobId) {
		Job job = schedule.getJob(jobId);
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		boolean status = unscheduleImpl(job, alternative);
		
		return status ? success(alternative) : error();
	}
	
	private boolean unscheduleImpl(Job job, ScheduleAlternative alternative) {
		if (schedule.isJobLockedForRemoval(job))
			throw new IllegalStateException("job is locked for removal");
		
		Node node = job.getNodeReference().getActual();
		WorldPerspective perspective = perspectiveCache.getPerspectiveFor(node);
		
		// there should be at least one entry
		Job lastJob = node.getNavigableJobs().lastEntry().getValue();
		boolean fixedEnd = lastJob != job;
		
		JobRemovalPlanner pl = new JobRemovalPlanner();
		
		pl.setWorld(world);
		pl.setWorldPerspective(perspective);
		pl.setFrozenHorizonTime(frozenHorizonTime);
		pl.setSchedule(schedule);
		pl.setAlternative(alternative);
		pl.setJob(job);
		pl.setFixedEnd(fixedEnd);

		return pl.plan();
	}
	
	/**
	 * <p>
	 * Tries to reschedule a scheduled task to meet the given specifications.
	 * This is a atomic composition of unschedule and schedule. First the scheduled
	 * job with the id given by the specification is unscheduled and afterwards scheduled
	 * using the new specification.
	 * </p>
	 * 
	 * <p>
	 * After returning the changes to the schedule have to be commit to be
	 * actually applied. The current schedule will not be updated otherwise.
	 * Eventually the change must either be committed or aborted. However, if
	 * the scheduling was unsuccessful, neither commit nor abort must be called.
	 * An unsuccessful scheduling is indicated by the returned
	 * {@link ScheduleResult}.
	 * </p>
	 * 
	 * @param spec
	 * @return a schedule result.
	 * @throws NullPointerException
	 *             if {@code spec} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if job id given by spec is unknown.
	 * @throws IllegalStateException
	 *             if the job is already locked for removal.
	 * @see #commit(UUID)
	 * @see #commit(UUID, String)
	 * @see #abort(UUID)
	 * @see #abort(UUID, String)
	 */
	public ScheduleResult reschedule(JobSpecification spec) {
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		boolean status = rescheduleImpl(spec, alternative);

		return status ? success(alternative) : error();
	}
	
	private boolean rescheduleImpl(JobSpecification spec, ScheduleAlternative alternative) {
		Job job = getJob(spec.getJobId()); // throws NPE

		if (schedule.isJobLockedForRemoval(job))
			throw new IllegalStateException("job is locked for removal");
		
		boolean status;
		
		status = unscheduleImpl(job, alternative);
		
		if (!status)
			return false;
		
		status = scheduleImpl(spec, alternative);
		
		return status;
	}

	/**
	 * Commits the schedule transaction given by its id. Any previously
	 * partially aborted node updates stay aborted. The changes will be applied
	 * to the schedule.
	 * 
	 * @param transactionId
	 * @throws NullPointerException
	 *             if {@code tranactionId} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code tranactionId} is unknown.
	 */
	public void commit(UUID transactionId) {
		Objects.requireNonNull(transactionId, "transactionId");
		
		Transaction transaction = transactions.get(transactionId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		schedule.integrate(transaction.getAlternative());
		transactions.remove(transactionId);
	}
	
	/**
	 * Partially commits the schedule transaction given by its id. Only the node
	 * updates given by the node id are committed. The changes will be applied
	 * to the schedule.
	 * 
	 * @param transactionId
	 * @param nodeId
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code transactionId} is unknown or if there is no node
	 *             updated for the node given by {@code nodeId}.
	 */
	public void commit(UUID transactionId, String nodeId) {
		Objects.requireNonNull(transactionId, "transactionId");

		Transaction transaction = transactions.get(transactionId);
		Node node = schedule.getNode(nodeId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		ScheduleAlternative alternative = transaction.getAlternative();
		
		schedule.integrate(alternative, node);
		
		if (alternative.isEmpty())
			transactions.remove(transactionId);
	}

	/**
	 * Aborts the schedule transaction given by its id. Any previously
	 * partially committed node updates stay committed. The changes will be discarded.
	 * 
	 * @param transactionId
	 * @throws NullPointerException
	 *             if {@code tranactionId} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code tranactionId} is unknown.
	 */
	public void abort(UUID transactionId) {
		Objects.requireNonNull(transactionId, "transactionId");
		
		Transaction transaction = transactions.get(transactionId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		schedule.eliminate(transaction.getAlternative());
		transactions.remove(transactionId);
	}
	
	/**
	 * Partially aborts the schedule transaction given by its id. Only the node
	 * updates given by the node id are aborted. The changes will be discarded.
	 * 
	 * @param transactionId
	 * @param nodeId
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code transactionId} is unknown or if there is no node
	 *             updated for the node given by {@code nodeId}.
	 */
	public void abort(UUID transactionId, String nodeId) {
		Objects.requireNonNull(transactionId, "transactionId");

		Transaction transaction = transactions.get(transactionId);
		Node node = schedule.getNode(nodeId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		ScheduleAlternative alternative = transaction.getAlternative();
		
		schedule.eliminate(alternative, node);
		
		if (alternative.isEmpty())
			transactions.remove(transactionId);
	}

	private ScheduleResult success(ScheduleAlternative alternative) {
		alternative.seal();
		
		Collection<NodeUpdate> updates = alternative.getUpdates();
		
		// collect result information
		
		UUID transactionId;
		do transactionId = randomUUID();
		while (transactions.containsKey(transactionId)); // just to be sure of uniqueness
		
		Map<UUID, Job> jobs = updates.stream()
			.map(NodeUpdate::getJobs)
			.flatMap(Collection::stream)
			.collect(toMap(Job::getId, identity()));

		Map<UUID, Job> removals = updates.stream()
			.map(NodeUpdate::getJobRemovals)
			.flatMap(Collection::stream)
			.collect(toMap(Job::getId, identity()));
		
		Collection<TrajectoryUpdate> trajectories = updates.stream()
			.flatMap(u -> {
				NodeReference w = u.getNode().getReference();
				
				// circumvents nested lambda expression
				// t -> new TrajectoryUpdate(t, w)
				return u.getTrajectories().stream()
					.map(new Function<Trajectory, TrajectoryUpdate>() {
						@Override
						public TrajectoryUpdate apply(Trajectory t) {
							return new TrajectoryUpdate(t, w);
						}
					});
			})
			.collect(toList());
		
		// make result
		ScheduleResult result = ScheduleResult.success(
			transactionId, jobs, removals, trajectories);
		
		// store alternative and transaction
		schedule.addAlternative(alternative);
		transactions.put(transactionId, new Transaction(transactionId, alternative));
		
		return result;
	}

	private ScheduleResult error() {
		return ScheduleResult.error();
	}
	
	/**
	 * Removes finished jobs and trajectories from the schedule.
	 */
	public void cleanUp() {
		schedule.cleanUp(presentTime);
	}

}
