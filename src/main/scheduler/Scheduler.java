package scheduler;

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

import scheduler.ScheduleResult.TrajectoryUpdate;
import world.RadiusBasedWorldPerspectiveCache;
import world.Trajectory;
import world.World;
import world.WorldPerspective;
import world.WorldPerspectiveCache;
import world.pathfinder.StraightEdgePathfinder;

// TODO document
/**
 * <p>The Scheduler manages the distribution of task to a set of
 * {@link Node}s. A new task can be scheduled by providing a
 * {@link TaskSpecification}. The {@link #schedule(TaskSpecification)} method tries
 * to find a realizable configuration which satisfies the specification. In
 * the successful case a task will be created and assigned to an appropriate
 * node.</p>
 *
 * @author Rico Jasper
 */
public class Scheduler {

	public static final LocalDateTime BEGIN_OF_TIME = LocalDateTime.MIN;

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
	
	public void removeNode(String nodeId) {
		Node node = schedule.getNode(nodeId);
		
		node.cleanUp(presentTime);
		
		schedule.removeNode(nodeId);
		perspectiveCache.removePerceiver(node);
	}
	
	public Task getTask(UUID taskId) {
		return schedule.getTask(taskId);
	}
	
	public void removeTask(UUID taskId) {
		schedule.removeTask(taskId);
	}

	/**
	 * @return the present time
	 */
	public LocalDateTime getPresentTime() {
		return presentTime;
	}

	/**
	 * Sets the present time.
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
	 * @param frozenHorizonDuration the frozenHorizonDuration to set
	 */
	public void setFrozenHorizonDuration(Duration frozenHorizonDuration) {
		// also throws NullPointerException
		if (frozenHorizonDuration.isNegative())
			throw new IllegalArgumentException("frozenHorizonDuration be negative");
		
		this.frozenHorizonDuration = frozenHorizonDuration;
		
		updateFrozenHorizonTime();
	}

	public Duration getInterDependencyMargin() {
		return interDependencyMargin;
	}
	
	public boolean hasTransaction(UUID transactionId) {
		return transactions.containsKey(transactionId);
	}

	public void setInterDependencyMargin(Duration interDependencyMargin) {
		Objects.requireNonNull(interDependencyMargin, "interDependencyMargin");
		
		if (interDependencyMargin.isNegative())
			throw new IllegalArgumentException("negative margin");
		
		this.interDependencyMargin = interDependencyMargin;
	}

	/**
	 * The default amount of location picks tried by the scheduler before
	 * giving up.
	 */
	private static final int MAX_LOCATION_PICKS = 10;

	/**
	 * Tries to schedule a new task satisfying the given specification.
	 *
	 * @param spec
	 * @return {@code true} iff a task was scheduled. {@code false} iff no task
	 *         could be scheduled satisfying the specification.
	 */
	public ScheduleResult schedule(TaskSpecification spec) {
		ScheduleAlternative alternative = new ScheduleAlternative();
		boolean status = scheduleImpl(spec, alternative);

		return status ? success(alternative) : error();
	}
	
	private boolean scheduleImpl(TaskSpecification spec, ScheduleAlternative alternative) {
		SingularTaskScheduler sc = new SingularTaskScheduler();
		
		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setSpecification(spec);
		sc.setMaxLocationPicks(MAX_LOCATION_PICKS);
		
		return sc.schedule();
	}
	
	public ScheduleResult schedule(
		Collection<TaskSpecification> specs,
		SimpleDirectedGraph<UUID, DefaultEdge> dependencies)
	{
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		boolean status = scheduleImpl(specs, dependencies, alternative);

		return status ? success(alternative) : error();
	}
	
	private boolean scheduleImpl(
		Collection<TaskSpecification> specs,
		SimpleDirectedGraph<UUID, DefaultEdge> dependencies,
		ScheduleAlternative alternative)
	{
		DependentTaskScheduler sc = new DependentTaskScheduler();
		
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

	public ScheduleResult schedule(PeriodicTaskSpecification spec) {
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		boolean status = scheduleImpl(spec, alternative);
		
		return status ? success(alternative) : error();
	}
	
	private boolean scheduleImpl(PeriodicTaskSpecification spec, ScheduleAlternative alternative) {
		PeriodicTaskScheduler sc = new PeriodicTaskScheduler();

		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setSpecification(spec);
		sc.setMaxLocationPicks(MAX_LOCATION_PICKS);
		
		return sc.schedule();
	}
	
	public ScheduleResult unschedule(UUID taskId) {
		Task task = schedule.getTask(taskId);
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		boolean status = unscheduleImpl(task, alternative);
		
		return status ? success(alternative) : error();
	}
	
	private boolean unscheduleImpl(Task task, ScheduleAlternative alternative) {
		Node node = task.getNodeReference().getActual();
		WorldPerspective perspective = perspectiveCache.getPerspectiveFor(node);
		
		// there should be at least one entry
		Task lastTask = node.getNavigableTasks().lastEntry().getValue();
		boolean fixedEnd = lastTask != task;
		
		TaskRemovalPlanner pl = new TaskRemovalPlanner();
		
		pl.setWorld(world);
		pl.setWorldPerspective(perspective);
		pl.setFrozenHorizonTime(frozenHorizonTime);
		pl.setSchedule(schedule);
		pl.setAlternative(alternative);
		pl.setTask(task);
		pl.setFixedEnd(fixedEnd);

		return pl.plan();
	}
	
	public ScheduleResult reschedule(TaskSpecification spec) {
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		boolean status = rescheduleImpl(spec, alternative);

		return status ? success(alternative) : error();
	}
	
	private boolean rescheduleImpl(TaskSpecification spec, ScheduleAlternative alternative) {
		Task task = getTask(spec.getTaskId());
		
		boolean status;
		
		status = unscheduleImpl(task, alternative);
		
		if (!status)
			return false;
		
		status = scheduleImpl(spec, alternative);
		
		return status;
	}

	public void commit(UUID transactionId) {
		Objects.requireNonNull(transactionId, "transactionId");
		
		Transaction transaction = transactions.get(transactionId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		schedule.integrate(transaction.getAlternative());
		transactions.remove(transactionId);
	}
	
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

	public void abort(UUID transactionId) {
		Objects.requireNonNull(transactionId, "transactionId");
		
		Transaction transaction = transactions.get(transactionId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		schedule.eliminate(transaction.getAlternative());
		transactions.remove(transactionId);
	}
	
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
		
		Map<UUID, Task> tasks = updates.stream()
			.map(NodeUpdate::getTasks)
			.flatMap(Collection::stream)
			.collect(toMap(Task::getId, identity()));

		Map<UUID, Task> removals = updates.stream()
			.map(NodeUpdate::getTaskRemovals)
			.flatMap(Collection::stream)
			.collect(toMap(Task::getId, identity()));
		
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
			transactionId, tasks, removals, trajectories);
		
		// store alternative and transaction
		schedule.addAlternative(alternative);
		transactions.put(transactionId, new Transaction(transactionId, alternative));
		
		return result;
	}

	private ScheduleResult error() {
		return ScheduleResult.error();
	}
	
	public void cleanUp() {
		for (Node w : schedule.getNodes())
			w.cleanUp(presentTime);
	}

}
