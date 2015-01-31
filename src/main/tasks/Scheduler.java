package tasks;

import static java.util.stream.Collectors.*;
import static util.Comparables.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.collections4.iterators.IteratorIterable;

import pickers.LocationIterator;
import pickers.WorkerUnitSlotIterator;
import pickers.WorkerUnitSlotIterator.WorkerUnitSlot;
import world.RadiusBasedWorldPerspectiveCache;
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
	public static final int MAX_LOCATION_PICKS = 10;

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
	 * The workers managed by this scheduler.
	 */
	private final Map<String, WorkerUnit> workerPool = new HashMap<>();
	
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

		// TODO check validity of world and workerPool
		//      (e.g. no overlapping of obstacles)
		
		this.world = world;
		this.perspectiveCache = new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);
	}

	/**
	 * @return the physical outside world representation where the workers are located.
	 */
	private World getWorld() {
		return world;
	}

	/**
	 * @return the perspective cache.
	 */
	private WorldPerspectiveCache getPerspectiveCache() {
		return perspectiveCache;
	}

	/**
	 * @return the workers.
	 */
	private Map<String, WorkerUnit> getWorkerPool() {
		return workerPool;
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
		
		WorkerUnit previous = workerPool.putIfAbsent(worker.getId(), worker);
		
		if (previous != null)
			throw new IllegalArgumentException("worker id already assigned");
		
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
		WorkerUnit worker = workerPool.get(
			Objects.requireNonNull(workerId, "workerId"));
		
		if (worker == null)
			throw new IllegalArgumentException("unknown worker id");
		
		return worker.getReference();
	}
	
	/**
	 * Tries to schedule a new task satisfying the given specification.
	 *
	 * @param specification
	 * @return {@code true} iff a task was scheduled. {@code false} iff no task
	 *         could be scheduled satisfying the specification.
	 */
	public boolean schedule(TaskSpecification specification) {
		Objects.requireNonNull(specification, "specification");

		World world = getWorld();
		Collection<WorkerUnit> pool = getWorkerPool().values();
		WorldPerspectiveCache perspectiveCache = getPerspectiveCache();
		Geometry locationSpace = world.space(specification.getLocationSpace());
		UUID taskId = specification.getTaskId();
		LocalDateTime earliest = specification.getEarliestStartTime();
		LocalDateTime latest = specification.getLatestStartTime();
		Duration duration = specification.getDuration();

		TaskPlanner tp = new TaskPlanner();

		tp.setWorkerPool(pool);
		tp.setPerspectiveCache(perspectiveCache);
		tp.setTaskId(taskId);
		tp.setDuration(duration);

		// iterate over possible locations

		// TODO prefilter the workers who have time without considering their location
		// return if no workers remain
		// TODO workers which already are in position shouldn't need to move.

		Iterable<Point> locations = new IteratorIterable<>(
			new LocationIterator(locationSpace, MAX_LOCATION_PICKS));

		for (Point loc : locations) {
			tp.setLocation(loc);

			// iterate over possible worker time slots.

			// Worker units have different perspectives of the world.
			// The LocationIterator might pick a location which is inaccessible
			// for a unit. Therefore, the workers are filtered by the location
			Iterable<WorkerUnitSlot> workerSlots = new IteratorIterable<>(
				new WorkerUnitSlotIterator(filterByLocation(loc), loc, earliest, latest, duration));

			for (WorkerUnitSlot ws : workerSlots) {
				WorkerUnit w = ws.getWorkerUnit();
				IdleSlot s = ws.getIdleSlot();
				LocalDateTime slotStartTime = s.getStartTime();
				LocalDateTime slotFinishTime = s.getFinishTime();

				tp.setWorkerUnit(w);
				// don't exceed the slot's time window
				tp.setEarliestStartTime( max(earliest, slotStartTime ) );
				tp.setLatestStartTime  ( min(latest  , slotFinishTime) );

				// plan the routes of affected workers and schedule task
				boolean status = tp.plan();

				if (status)
					return true;
			}
		}

		// all possible variable combinations are depleted without being able
		// to schedule a task
		return false;
	}

	/**
	 * Checks if a worker is able to reach a location in regard to its size.
	 *
	 * @param location
	 * @param worker
	 * @return {@code true} iff worker is able to reach the location.
	 */
	private boolean checkLocationFor(Point location, WorkerUnit worker) {
		WorldPerspectiveCache cache = getPerspectiveCache();
		WorldPerspective perspective = cache.getPerspectiveFor(worker);
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
		Collection<WorkerUnit> pool = getWorkerPool().values();

		return pool.stream()
			.filter(w -> checkLocationFor(location, w))
			.collect(toList());
	}

}
