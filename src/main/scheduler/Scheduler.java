package scheduler;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static util.Comparables.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import pickers.LocationIterator;
import pickers.WorkerUnitSlotIterator;
import pickers.WorkerUnitSlotIterator.WorkerUnitSlot;
import scheduler.ScheduleResult.TrajectoryUpdate;
import world.RadiusBasedWorldPerspectiveCache;
import world.Trajectory;
import world.World;
import world.WorldPerspective;
import world.WorldPerspectiveCache;
import world.pathfinder.StraightEdgePathfinder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

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

	/**
	 * The default amount of location picks tried by the scheduler before
	 * giving up.
	 */
	private static final int MAX_LOCATION_PICKS = 10;

	// TODO document
	public static final LocalDateTime BEGIN_OF_TIME = LocalDateTime.MIN;
	
	// TODO document
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
		// TODO use an envelope based cache
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
	
	// TODO document
	public void removeWorker(String workerId) {
		WorkerUnit worker = schedule.getWorker(workerId);
		
		if (!worker.isIdle(frozenHorizonTime, END_OF_TIME))
			throw new IllegalStateException("worker still has scheduled tasks");
		
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

	/**
	 * Tries to schedule a new task satisfying the given specification.
	 *
	 * @param specification
	 * @return {@code true} iff a task was scheduled. {@code false} iff no task
	 *         could be scheduled satisfying the specification.
	 */
	public ScheduleResult schedule(TaskSpecification specification) {
		Objects.requireNonNull(specification, "specification");

		Geometry locationSpace = world.space(specification.getLocationSpace());
		UUID taskId = specification.getTaskId();
		LocalDateTime earliest = max(
			specification.getEarliestStartTime(), frozenHorizonTime);
		LocalDateTime latest = specification.getLatestStartTime();
		Duration duration = specification.getDuration();
		
		if (latest.compareTo(getFrozenHorizonTime()) < 0)
			throw new IllegalArgumentException("frozen horizon violation");
		
		ScheduleAlternative alternative = new ScheduleAlternative();

		TaskPlanner tp = new TaskPlanner();

		tp.setSchedule(schedule);
		tp.setScheduleAlternative(alternative);
		tp.setTaskId(taskId);
		tp.setDuration(duration);

		// iterate over possible locations

		// TODO prefilter the workers who have time without considering their location
		// return if no workers remain
		// TODO workers which already are in position shouldn't need to move.

		Iterable<Point> locations = () -> new LocationIterator(
			locationSpace, MAX_LOCATION_PICKS);

		for (Point locaction : locations) {
			tp.setLocation(locaction);

			// iterate over possible worker time slots.

			// Worker units have different perspectives of the world.
			// The LocationIterator might pick a location which is inaccessible
			// for a unit. Therefore, the workers are filtered by the location
			
			// TODO communicate frozen horizon
			Iterable<WorkerUnitSlot> workerSlots = () -> new WorkerUnitSlotIterator(
				filterByLocation(locaction),
				frozenHorizonTime,
				locaction,
				earliest, latest, duration);

			for (WorkerUnitSlot ws : workerSlots) {
				WorkerUnit w = ws.getWorkerUnit();
				IdleSlot s = ws.getIdleSlot();
				WorldPerspective perspective = perspectiveCache.getPerspectiveFor(w);

				tp.setFixedEnd(s.getFinishTime().isBefore(END_OF_TIME));
				tp.setWorldPerspective(perspective);
				tp.setWorker(w);
				tp.setIdleSlot(s);
				tp.setEarliestStartTime( earliest );
				tp.setLatestStartTime  ( latest   );

				// plan the routes of affected workers and schedule task
				boolean status = tp.plan();

				if (status)
					return success(alternative);
			}
		}

		// all possible variable combinations are exhausted without being able
		// to schedule a task
		return error();
	}
	
	// TODO document
	public void commit(UUID transactionId) {
		Transaction transaction = transactions.get(transactionId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		schedule.integrate(transaction.getAlternative());
		transactions.remove(transactionId);
	}

	// TODO document
	public void abort(UUID transactionId) {
		Transaction transaction = transactions.get(transactionId);
		
		if (transaction == null)
			throw new IllegalArgumentException("unknown transaction");
		
		schedule.eliminate(transaction.getAlternative());
		transactions.remove(transactionId);
	}

	// TODO document
	private ScheduleResult success(ScheduleAlternative alternative) {
		alternative.seal();
		
		Collection<WorkerUnitScheduleUpdate> updates = alternative.getUpdates();
		
		// collect result information
		
		UUID transactionId = UUID.randomUUID();
		
		Map<UUID, Task> tasks = updates.stream()
			.map(WorkerUnitScheduleUpdate::getTasks)
			.flatMap(Collection::stream)
			.collect(toMap(Task::getId, identity()));

		Map<UUID, Task> removals = updates.stream()
			.map(WorkerUnitScheduleUpdate::getTaskRemovals)
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

	// TODO document
	private ScheduleResult error() {
		return ScheduleResult.error();
	}

	/**
	 * Checks if a worker is able to reach a location in regard to its size.
	 *
	 * @param location
	 * @param worker
	 * @return {@code true} iff worker is able to reach the location.
	 */
	private boolean checkLocationFor(Point location, WorkerUnit worker) {
		WorldPerspective perspective = perspectiveCache.getPerspectiveFor(worker);
		Geometry map = perspective.getView().getMap();

		return !map.contains(location);
	}

	/**
	 * Filters the pool of workers which are able to reach a location in
	 * regard to their individual size.
	 *
	 * @param location
	 * @return the filtered workers which are able to reach the location.
	 */
	private Collection<WorkerUnit> filterByLocation(Point location) {
		return schedule.getWorkers().stream()
			.filter(w -> checkLocationFor(location, w))
			.collect(toList());
	}

}
