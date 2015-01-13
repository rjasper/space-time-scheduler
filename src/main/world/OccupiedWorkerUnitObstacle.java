package world;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import tasks.Task;
import tasks.WorkerUnit;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;

/**
 * An {@code OccupiedWorkerUnitObstacle} represents the stationary path segment
 * of a worker being occupied with a task.
 * 
 * @author Rico
 */
public class OccupiedWorkerUnitObstacle extends WorkerUnitObstacle {

	/**
	 * The task the worker is occupied with.
	 */
	private final Task occupation;

	/**
	 * Constructs a new {@code OccupiedWorkerUnitObstacle} of the worker having
	 * an occupation.
	 * 
	 * @param worker
	 * @param occupation
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	public OccupiedWorkerUnitObstacle(WorkerUnit worker, Task occupation) {
		// throws NullPointerException
		super(worker, buildTrajectory(occupation));
		
		Objects.requireNonNull(occupation, "occupation");

		this.occupation = occupation;
	}

	/**
	 * Builds the stationary trajectory of the worker during its occupation.
	 * 
	 * @param occupation
	 * @return the trajectory.
	 * @throws NullPointerException if the occupation is {@code null}.
	 */
	private static Trajectory buildTrajectory(Task occupation) {
		Objects.requireNonNull(occupation, "occupation");
		
		Point location = occupation.getLocation();
		LocalDateTime startTime = occupation.getStartTime();
		LocalDateTime finishTime = occupation.getFinishTime();
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(location, location));
		List<LocalDateTime> times = Arrays.asList(startTime, finishTime);
	
		return new SimpleTrajectory(spatialPath, times);
	}

	/**
	 * @return the occupation of the worker.
	 */
	public Task getOccupation() {
		return occupation;
	}

}
