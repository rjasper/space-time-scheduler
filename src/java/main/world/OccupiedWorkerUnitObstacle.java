package world;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import tasks.Task;
import tasks.WorkerUnit;

import com.vividsolutions.jts.geom.Point;

public class OccupiedWorkerUnitObstacle extends WorkerUnitObstacle {

	private final Task occupation;

	public OccupiedWorkerUnitObstacle(WorkerUnit worker, Task occupation) {
		super(worker, buildTrajectory(occupation));

		this.occupation = occupation;
	}

	public Task getOccupation() {
		return occupation;
	}

	private static SimpleTrajectory buildTrajectory(Task task) {
		Point location = task.getLocation();
		LocalDateTime startTime = task.getStartTime();
		LocalDateTime finishTime = task.getFinishTime();
		List<Point> spatialPath = Arrays.asList(location, location);
		List<LocalDateTime> times = Arrays.asList(startTime, finishTime);

		return new SimpleTrajectory(spatialPath, times);
	}

}
