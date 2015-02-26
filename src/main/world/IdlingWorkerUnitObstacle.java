package world;

import java.time.LocalDateTime;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.util.GeometriesRequire;
import scheduler.Scheduler;
import scheduler.WorkerUnit;

import com.google.common.collect.ImmutableList;

/**
 * An {@code IdlingWorkerUnitObstacle} represents the very last path segment of
 * a worker. When a worker has completed its last task it has not anywhere to
 * go. Therefore, it stays at its last location.
 * 
 * @author Rico
 */
public class IdlingWorkerUnitObstacle extends WorkerUnitObstacle {

	/**
	 * Constructs a new {@code IdlingWorkerUnitObstacle} of a worker which has
	 * no further destination. From {@code startTime} onwards the worker will
	 * stay at {@code location}.
	 * 
	 * @param worker
	 * @param location
	 *            to stay at
	 * @param startTime
	 *            when idling begins
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if the location is empty or invalid.
	 */
	public IdlingWorkerUnitObstacle(WorkerUnit worker, ImmutablePoint location, LocalDateTime startTime) {
		// see super and buildTrajectory for @throws
		super(worker, buildTrajectory(location, startTime));
	}

	/**
	 * Builds a stationary trajectory starting at the {@code location} and
	 * {@code startTime}.
	 * 
	 * @param location
	 * @param startTime
	 * @return the trajectory
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if the location is empty or invalid.
	 */
	private static Trajectory buildTrajectory(ImmutablePoint location, LocalDateTime startTime) {
		Objects.requireNonNull(location, "location");
		Objects.requireNonNull(startTime, "startTime");
		GeometriesRequire.requireValid2DPoint(location, "location");
		
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(location, location));
		ImmutableList<LocalDateTime> times = ImmutableList.of(startTime, Scheduler.END_OF_TIME);

		return new SimpleTrajectory(spatialPath, times);
	}

}
