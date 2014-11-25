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

		// TODO use LocalDateTime.MAX as soon as long overflow issue is resolved
//		List<LocalDateTime> times = Arrays.asList(startTime, LocalDateTime.MAX);
		List<LocalDateTime> times = Arrays.asList(startTime, LocalDateTime.of(2100, 1, 1, 0, 0));

		return new SimpleTrajectory(spatialPath, times);
	}

}
