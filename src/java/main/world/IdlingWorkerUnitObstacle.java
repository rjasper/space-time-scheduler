package world;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import tasks.WorkerUnit;

import com.vividsolutions.jts.geom.Point;

public class IdlingWorkerUnitObstacle extends WorkerUnitObstacle {

	public IdlingWorkerUnitObstacle(WorkerUnit worker, Point location, LocalDateTime startTime) {
		super(worker, buildTrajectory(location, startTime));
	}

	private static Trajectory buildTrajectory(Point location, LocalDateTime startTime) {
		List<Point> spatialPath = Arrays.asList(location, location);
		List<LocalDateTime> times = Arrays.asList(startTime, LocalDateTime.MAX);

		return new SimpleTrajectory(spatialPath, times);
	}

}
