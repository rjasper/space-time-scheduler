package world;

import static java.util.Collections.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jts.geom.immutable.ImmutablePolygon;
import tasks.WorkerUnit;

/**
 * <p>A {@code WorkerUnitObstacle} is a path segment of a worker represented as a
 * dynamic obstacle. A worker typically has multiple path segments which form
 * the entire path the worker follows. Ordinarily one segment connects two
 * locations in time which a worker has to visit. The path between those
 * locations is interchangeable. This enables to replace singular segments due
 * to task planning.</p>
 *
 * <p>Another property of a {@code WorkerUnitObstacle} are evasions. Workers
 * might need to evade another worker to avoid collisions. When a worker changes
 * one of its segments due to task planning a previous evasion might become
 * obsolete. A {@code WorkerUnitObstacle} provides the functionality to register
 * the path segment of the worker which was evading this one. This enables to
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
	 * Stores the path segments of worker which where evading this one.
	 */
	private final Set<MovingWorkerUnitObstacle> evasions = new HashSet<>();

	/**
	 * Stores an unmodifiable view on {@link #evasions}.
	 */
	private final Set<MovingWorkerUnitObstacle> unmodifiableEvations = unmodifiableSet(evasions);

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
	 * @return other worker's path segments which had to evade this obstacle.
	 */
	public Set<MovingWorkerUnitObstacle> getEvasions() {
		return unmodifiableEvations;
	}

	/**
	 * Registers a path segment which had to evade this one.
	 *
	 * @param evasion the segment which had to evade.
	 * @throws NullPointerException if the evasion is {@code null}.
	 * @throws IllegalArgumentException if the evasion was already registered.
	 */
	public void addEvasion(MovingWorkerUnitObstacle evasion) {
		Objects.requireNonNull(evasion, "evasion");

		boolean status = evasions.add(evasion);

		if (!status)
			throw new IllegalArgumentException("evasion already present");
	}

	// TODO this method is never called. Someone should do this :P
	/**
	 * Removes a registered evasion which was evading this one.
	 *
	 * @param evasion to be unregistered
	 * @throws NullPointerException if the evasion is {@code null}.
	 * @throws IllegalArgumentException if the evasion was not registered.
	 */
	public void removeEvasion(MovingWorkerUnitObstacle evasion) {
		Objects.requireNonNull(evasion, "evasion");

		boolean status = evasions.remove(evasion);

		if (!status)
			throw new IllegalArgumentException("unknown evasion");
	}

}
