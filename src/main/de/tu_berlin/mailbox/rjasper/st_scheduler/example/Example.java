package de.tu_berlin.mailbox.rjasper.st_scheduler.example;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.UUID.*;

import java.time.Duration;
import java.time.LocalDateTime;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public final class Example {

	public static void main(String[] args) throws CollisionException {
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
		// node for scheduler
		// for node
		ImmutablePolygon nodeShape = immutablePolygon(
			-0.5, -0.5,
			+0.5, -0.5,
			+0.5, +0.5,
			-0.5, +0.5,
			-0.5, -0.5);
		// for node
		double maxSpeed = 1.0; // m/s
		// for node
		ImmutablePoint initialLocation = immutablePoint(2.5, 2.5);
		// for node
		LocalDateTime initialTime = LocalDateTime.of(2015, 1, 1, 0, 0, 0); // 1/1/2015 12:00 AM
		// node actual
		NodeSpecification nodeSpec =
			new NodeSpecification("node-id", nodeShape, maxSpeed, initialLocation, initialTime);
		// scheduler actual
		Scheduler scheduler = new Scheduler(world);
		NodeReference nodeRef = scheduler.addNode(nodeSpec);

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
		JobSpecification spec = new JobSpecification(randomUUID(), locationSpace, earliestStartTime, latestStartTime, duration);

		ScheduleResult result = scheduler.schedule(spec);

		scheduler.commit(result.getTransactionId());

		System.out.println(result);
		System.out.println(nodeRef.calcTrajectory());
	}

}
