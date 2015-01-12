package main;

import static java.util.Collections.singleton;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import tasks.Scheduler;
import tasks.Specification;
import tasks.WorkerUnit;
import world.DynamicObstacle;
import world.SimpleTrajectory;
import world.StaticObstacle;
import world.Trajectory;
import world.World;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public final class Example {

	public static void main(String[] args) {
		GeometryFactory geomFact = new GeometryFactory();
		
		// scheduler
		
		// world for scheduler
		// static obstacle for world
		// for static obstacle
		Polygon staticObstacleShape = geomFact.createPolygon(new Coordinate[] {
			new Coordinate(1.0, 5.0),
			new Coordinate(3.0, 5.0),
			new Coordinate(3.0, 7.0),
			new Coordinate(1.0, 7.0),
			new Coordinate(1.0, 5.0),
		});
		// static obstacle actual
		StaticObstacle staticObstacle = new StaticObstacle(staticObstacleShape);
		// dynamic obstacle for world
		// for dynamic obstacle
		Polygon dynamicObstacleShape = geomFact.createPolygon(new Coordinate[] {
			new Coordinate(-1.0, -1.0),
			new Coordinate(+1.0, -1.0),
			new Coordinate(+1.0, +1.0),
			new Coordinate(-1.0, +1.0),
			new Coordinate(-1.0, -1.0),
		});
		// trajectory for dynamic obstacle
		// for trajectory
		List<Point> dynamicObstacleTrajectorySpatialPath = Arrays.asList(
			geomFact.createPoint(new Coordinate(7.0, 9.0)),
			geomFact.createPoint(new Coordinate(7.0, 6.0)));
		// for trajectory
		List<LocalDateTime> dynamicObstacleTrajectoryTimes = Arrays.asList(
			LocalDateTime.of(2015, 1, 1, 0, 0,  0),  // 1/1/2015 12:00 AM
			LocalDateTime.of(2015, 1, 1, 0, 0, 30)); // 1/1/2015 12:30 AM
		// trajectory actual
		Trajectory dynamicObstacleTrajectory =
			new SimpleTrajectory(dynamicObstacleTrajectorySpatialPath, dynamicObstacleTrajectoryTimes);
		
		// dynamic obstacle actual
		DynamicObstacle dynamicObstacle =
			new DynamicObstacle(dynamicObstacleShape, dynamicObstacleTrajectory);
		// world actual
		World world = new World(singleton(staticObstacle), singleton(dynamicObstacle));
		// worker for scheduler
		// for worker
		Polygon workerShape = geomFact.createPolygon(new Coordinate[] {
			new Coordinate(-0.5, -0.5),
			new Coordinate(+0.5, -0.5),
			new Coordinate(+0.5, +0.5),
			new Coordinate(-0.5, +0.5),
			new Coordinate(-0.5, -0.5),
		});
		// for worker
		double maxSpeed = 1.0; // m/s
		// for worker
		Point initialLocation = geomFact.createPoint(new Coordinate(2.5, 2.5));
		// for worker
		LocalDateTime initialTime = LocalDateTime.of(2015, 1, 1, 0, 0, 0); // 1/1/2015 12:00 AM
		// worker actual
		WorkerUnit worker = new WorkerUnit(workerShape, maxSpeed, initialLocation, initialTime);
		// scheduler actual
		Scheduler scheduler = new Scheduler(world, singleton(worker));
		
		// specification
		// for specification
		Polygon locationSpace = geomFact.createPolygon(new Coordinate[] {
			new Coordinate(6.0, 1.0),
			new Coordinate(9.0, 1.0),
			new Coordinate(9.0, 4.0),
			new Coordinate(6.0, 4.0),
			new Coordinate(6.0, 1.0),
		});
		LocalDateTime earliestStartTime = LocalDateTime.of(2015, 1, 1, 0, 2, 0);
		LocalDateTime latestStartTime   = LocalDateTime.of(2015, 1, 1, 0, 4, 0);
		Duration duration = Duration.ofSeconds(2L * 60L); // 2 minutes
		// specification actual
		Specification spec = new Specification(locationSpace, earliestStartTime, latestStartTime, duration);
		
		boolean status = scheduler.schedule(spec);
		
		System.out.println(status);
		System.out.println(worker.calcMergedTrajectory());
	}

}
