package world;

import static java.util.Collections.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jts.geom.immutable.ImmutablePolygon;
import scheduler.WorkerUnit;

/**
 * <p>A {@code WorkerUnitObstacle} is a path section of a worker represented as a
 * dynamic obstacle. A worker typically has multiple path sections which form
 * the entire path the worker follows. Ordinarily one section connects two
 * locations in time which a worker has to visit. The path between those
 * locations is interchangeable. This enables to replace singular sections due
 * to task planning.</p>
 *
 * <p>Another property of a {@code WorkerUnitObstacle} are evasions. Workers
 * might need to evade another worker to avoid collisions. When a worker changes
 * one of its sections due to task planning a previous evasion might become
 * obsolete. A {@code WorkerUnitObstacle} provides the functionality to register
 * the path section of the worker which was evading this one. This enables to
 * detect any obsolete evasions for further actions.</p>
 *
 * @author Rico
 */
public abstract class WorkerUnitObstacle extends DynamicObstacle {

	/**
	 * The worker unit represented as an obstacle.
	 */
	private final WorkerUnit workerUnit;

	/**
	 * Stores the path sections of worker which where evading this one.
	 */
	private final Set<MovingWorkerUnitObstacle> evaders = new HashSet<>();

	/**
	 * Stores an unmodifiable view on {@link #evaders}.
	 */
	private final Set<MovingWorkerUnitObstacle> unmodifiableEvaders = unmodifiableSet(evaders);

	/**
	 * Creates a new {@code WorkerUnitObstacle} for the given worker along the
	 * trajectory.
	 *
	 * @param worker
	 * @param trajectory
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the trajectory is empty.
	 */
	public WorkerUnitObstacle(WorkerUnit worker, Trajectory trajectory) {
		// retrieveShape throws NullPointerException if worker is null
		super(retrieveShape(worker), trajectory);

		this.workerUnit = worker;
	}

	/**
	 * Returns the worker's shape.
	 *
	 * @param worker
	 * @return the shape.
	 * @throws NullPointerException if the worker is {@code null}.
	 */
	private static ImmutablePolygon retrieveShape(WorkerUnit worker) {
		Objects.requireNonNull(worker, "worker");

		return worker.getShape();
	}

	/**
	 * @return the worker unit represented by this obstacle.
	 */
	public WorkerUnit getWorkerUnit() {
		return workerUnit;
	}

	/**
	 * @return other worker's path sections which had to evade this obstacle.
	 */
	public Set<MovingWorkerUnitObstacle> getEvaders() {
		return unmodifiableEvaders;
	}

	/**
	 * Registers a path section which had to evade this one.
	 *
	 * @param evader the section which had to evade.
	 * @throws NullPointerException if the {@code evader} is {@code null}.
	 * @throws IllegalArgumentException if the {@code evader} was already registered.
	 */
	public void addEvader(MovingWorkerUnitObstacle evader) {
		Objects.requireNonNull(evader, "evader");

		boolean status = evaders.add(evader);

		if (!status)
			throw new IllegalArgumentException("evader already present");
	}

	/**
	 * Removes a registered {@code evader} which was evading this one.
	 *
	 * @param evader to be unregistered
	 * @throws NullPointerException if the {@code evader} is {@code null}.
	 * @throws IllegalArgumentException if the {@code evader} was not registered.
	 */
	public void removeEvader(MovingWorkerUnitObstacle evader) {
		Objects.requireNonNull(evader, "evader");

		boolean status = evaders.remove(evader);

		if (!status)
			throw new IllegalArgumentException("unknown evader");
	}
	
	/**
	 * Clears all evasion relations
	 */
	public void clearEvasions() {
		evaders.clear();
	}

}
