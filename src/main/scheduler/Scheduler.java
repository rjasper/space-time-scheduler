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
 * {@link WorkerUnit}s. A new task can be scheduled by providing a
 * {@link TaskSpecification}. The {@link #schedule(TaskSpecification)} method tries
 * to find a realizable configuration which satisfies the specification. In
 * the successful case a task will be created and assigned to an appropriate
 * worker.</p>
 *
 * @author Rico Jasper
 */
public class Scheduler {

	public static final LocalDateTime BEGIN_OF_TIME = LocalDateTime.MIN;

	public static final LocalDateTime END_OF_TIME = LocalDateTime.MAX;

	/**
	 * The physical outside world representation where the workers are located.
	 */
	private final World world;

	/**
	 * A cache of the {@link WorldPerspective perspectives} of the
	 * {@link WorkerUnit workers}.
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
	 * Constructs a scheduler using the given world and set of workers.
	 * The workers are expected to be managed exclusively by this scheduler.
	 *
	 * @param world
	 * @param workerPool
	 * @throws NullPointerException if world or workers is null
	 */
	public Scheduler(World world) {
		Objects.requireNonNull(world, "world");
		
		this.world = world;
		this.perspectiveCache = new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);
	}

	/**
	 * Adds a new {@link WorkerUnit} to the scheduler. The given specification
	 * is used to create the worker.
	 * 
	 * @param spec
	 * @return a reference to the worker.
	 * @throws NullPointerException
	 *             if {@code spec} is {@code null}
	 * @throws IllegalArgumentException
	 *             if worker ID is already assigned.
	 */
	public WorkerUnitReference addWorker(WorkerUnitSpecification spec) {
		// also throws NullPointerException
		if (spec.getInitialTime().isBefore(getFrozenHorizonTime()))
			throw new IllegalArgumentException("initial time violates frozen horizon");
		
		WorkerUnit worker = new WorkerUnit(spec);

		// TODO check validity of worker placement
		// don't overlap with static or dynamic obstacles or with other workers
		// only allow after frozen horizon
		
		schedule.addWorker(worker);
		
		return worker.getReference();
	}
	
	/**
	 * Returns the reference to the worker with the given id.
	 * 
	 * @param workerId
	 * @return the reference.
	 * @throws NullPointerException
	 *             if {@code workerId} is {@code null}
	 * @throws IllegalArgumentException
	 *             if worker ID is unassigned.
	 */
	public WorkerUnitReference getWorkerReference(String workerId) {
		return schedule.getWorker(workerId).getReference();
	}
	
	public void removeWorker(String workerId) {
		WorkerUnit worker = schedule.getWorker(workerId);
		
		worker.cleanUp(presentTime);
		
		schedule.removeWorker(workerId);
		perspectiveCache.removePerceiver(worker);
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
	 * @param specification
	 * @return {@code true} iff a task was scheduled. {@code false} iff no task
	 *         could be scheduled satisfying the specification.
	 */
	public ScheduleResult schedule(TaskSpecification specification) {
		ScheduleAlternative alternative = new ScheduleAlternative();
		SingularTaskScheduler sc = new SingularTaskScheduler();
		
		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setSpecification(specification);
		sc.setMaxLocationPicks(MAX_LOCATION_PICKS);
		
		boolean status = sc.schedule();

		return status ? success(alternative) : error();
	}
	
	public ScheduleResult schedule(
		Collection<TaskSpecification> specs,
		SimpleDirectedGraph<UUID, DefaultEdge> dependencies)
	{
		ScheduleAlternative alternative = new ScheduleAlternative();
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
		
		boolean status = sc.schedule();

		return status ? success(alternative) : error();
	}
	
	public ScheduleResult schedule(PeriodicTaskSpecification periodicSpec) {
		ScheduleAlternative alternative = new ScheduleAlternative();
		PeriodicTaskScheduler sc = new PeriodicTaskScheduler();

		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(frozenHorizonTime);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setSpecification(periodicSpec);
		sc.setMaxLocationPicks(MAX_LOCATION_PICKS);
		
		boolean status = sc.schedule();
		
		return status ? success(alternative) : error();
	}
	
	public ScheduleResult reschedule(TaskSpecification spec) {
		// TODO implement
		throw new UnsupportedOperationException("nyi");
	}
	
	// TODO test
	public ScheduleResult unschedule(UUID taskId) {
		// TODO implement
		
		Task task = schedule.getTask(taskId);
		WorkerUnit worker = task.getWorkerReference().getActual();
		WorldPerspective perspective = perspectiveCache.getPerspectiveFor(worker);
		
		ScheduleAlternative alternative = new ScheduleAlternative();
		TaskRemovalPlanner pl = new TaskRemovalPlanner();
		
		pl.setWorld(world);
		pl.setWorldPerspective(perspective);
		pl.setFrozenHorizonTime(frozenHorizonTime);
		pl.setSchedule(schedule);
		pl.setAlternative(alternative);
		pl.setTask(task);
//		pl.setFixedEnd(fixedEnd); // TODO
		
		return null;
	}
	
	public void removeTask(String workerId, UUID taskId) {
		schedule.removeTask(taskId);
	}

	public void commit(UUID transactionId) {
		Objects.requireNonNull(transactionId, "transactionId");
		
		Transaction transaction = transactions.get(transactionId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		schedule.integrate(transaction.getAlternative());
		transactions.remove(transactionId);
	}
	
	public void commit(UUID transactionId, String workerId) {
		Objects.requireNonNull(transactionId, "transactionId");

		Transaction transaction = transactions.get(transactionId);
		WorkerUnit worker = schedule.getWorker(workerId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		ScheduleAlternative alternative = transaction.getAlternative();
		
		schedule.integrate(alternative, worker);
		
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
	
	public void abort(UUID transactionId, String workerId) {
		Objects.requireNonNull(transactionId, "transactionId");

		Transaction transaction = transactions.get(transactionId);
		WorkerUnit worker = schedule.getWorker(workerId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		ScheduleAlternative alternative = transaction.getAlternative();
		
		schedule.eliminate(alternative, worker);
		
		if (alternative.isEmpty())
			transactions.remove(transactionId);
	}

	private ScheduleResult success(ScheduleAlternative alternative) {
		alternative.seal();
		
		Collection<WorkerUnitUpdate> updates = alternative.getUpdates();
		
		// collect result information
		
		UUID transactionId;
		do transactionId = randomUUID();
		while (transactions.containsKey(transactionId)); // just to be sure of uniqueness
		
		Map<UUID, Task> tasks = updates.stream()
			.map(WorkerUnitUpdate::getTasks)
			.flatMap(Collection::stream)
			.collect(toMap(Task::getId, identity()));

		Map<UUID, Task> removals = updates.stream()
			.map(WorkerUnitUpdate::getTaskRemovals)
			.flatMap(Collection::stream)
			.collect(toMap(Task::getId, identity()));
		
		Collection<TrajectoryUpdate> trajectories = updates.stream()
			.flatMap(u -> {
				WorkerUnitReference w = u.getWorker().getReference();
				
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
		for (WorkerUnit w : schedule.getWorkers())
			w.cleanUp(presentTime);
	}

}
