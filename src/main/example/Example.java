package example;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.Duration;
import java.time.LocalDateTime;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;
import tasks.Scheduler;
import tasks.TaskSpecification;
import tasks.WorkerUnitReference;
import tasks.WorkerUnitSpecification;
import world.DynamicObstacle;
import world.SimpleTrajectory;
import world.SpatialPath;
import world.StaticObstacle;
import world.Trajectory;
import world.World;

import com.google.common.collect.ImmutableList;

public final class Example {

	public static void main(String[] args) {
		// scheduler
		
		// world for scheduler
		// static obstacle for world
		// for static obstacle
		ImmutablePolygon staticObstacleShape = immutablePolygon(
			1.0, 5.0,
			3.0, 5.0,
			3.0, 7.0,
			1.0, 7.0,
			1.0, 5.0);
		// static obstacle actual
		StaticObstacle staticObstacle = new StaticObstacle(staticObstacleShape);
		// dynamic obstacle for world
		// for dynamic obstacle
		ImmutablePolygon dynamicObstacleShape = immutablePolygon(
			-1.0, -1.0,
			+1.0, -1.0,
			+1.0, +1.0,
			-1.0, +1.0,
			-1.0, -1.0);
		// trajectory for dynamic obstacle
		// for trajectory
		SpatialPath dynamicObstacleTrajectorySpatialPath =
			new SpatialPath(ImmutableList.of(
				immutablePoint(7.0, 9.0),
				immutablePoint(7.0, 6.0)));
		// for trajectory
		ImmutableList<LocalDateTime> dynamicObstacleTrajectoryTimes = ImmutableList.of(
			LocalDateTime.of(2015, 1, 1, 0, 0,  0),  // 1/1/2015 12:00 AM
			LocalDateTime.of(2015, 1, 1, 0, 0, 30)); // 1/1/2015 12:30 AM
		// trajectory actual
		Trajectory dynamicObstacleTrajectory =
			new SimpleTrajectory(dynamicObstacleTrajectorySpatialPath, dynamicObstacleTrajectoryTimes);
		
		// dynamic obstacle actual
		DynamicObstacle dynamicObstacle =
			new DynamicObstacle(dynamicObstacleShape, dynamicObstacleTrajectory);
		// world actual
		World world = new World(ImmutableList.of(staticObstacle), ImmutableList.of(dynamicObstacle));
		// worker for scheduler
		// for worker
		ImmutablePolygon workerShape = immutablePolygon(
			-0.5, -0.5,
			+0.5, -0.5,
			+0.5, +0.5,
			-0.5, +0.5,
			-0.5, -0.5);
		// for worker
		double maxSpeed = 1.0; // m/s
		// for worker
		ImmutablePoint initialLocation = immutablePoint(2.5, 2.5);
		// for worker
		LocalDateTime initialTime = LocalDateTime.of(2015, 1, 1, 0, 0, 0); // 1/1/2015 12:00 AM
		// worker actual
		WorkerUnitSpecification workerSpec =
			new WorkerUnitSpecification(workerShape, maxSpeed, initialLocation, initialTime);
		// scheduler actual
		Scheduler scheduler = new Scheduler(world, singleton(workerSpec));
		
		WorkerUnitReference workerRef = scheduler.getWorkerReferences().get(0);
		
		// specification
		// for specification
		ImmutablePolygon locationSpace = immutablePolygon(
			6.0, 1.0,
			9.0, 1.0,
			9.0, 4.0,
			6.0, 4.0,
			6.0, 1.0);
		LocalDateTime earliestStartTime = LocalDateTime.of(2015, 1, 1, 0, 2, 0);
		LocalDateTime latestStartTime   = LocalDateTime.of(2015, 1, 1, 0, 4, 0);
		Duration duration = Duration.ofSeconds(2L * 60L); // 2 minutes
		// specification actual
		TaskSpecification spec = new TaskSpecification(locationSpace, earliestStartTime, latestStartTime, duration);
		
		boolean status = scheduler.schedule(spec);
		
		System.out.println(status);
		System.out.println(workerRef.calcTrajectory());
	}

}
