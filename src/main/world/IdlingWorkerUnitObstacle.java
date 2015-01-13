package world;

import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jts.geom.util.GeometriesRequire;
import tasks.WorkerUnit;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

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
	public IdlingWorkerUnitObstacle(WorkerUnit worker, Point location, LocalDateTime startTime) {
		// see super and buildTrajectory for @throws
		super(worker, buildTrajectory(immutable(location), startTime));
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
	private static Trajectory buildTrajectory(Point location, LocalDateTime startTime) {
		Objects.requireNonNull(location, "location");
		Objects.requireNonNull(startTime, "startTime");
		GeometriesRequire.requireValid2DPoint(location, "location");
		
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(location, location));
		List<LocalDateTime> times = Arrays.asList(startTime, LocalDateTime.MAX);

		return new SimpleTrajectory(spatialPath, times);
	}

}
