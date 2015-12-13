package de.tu_berlin.mailbox.rjasper.st_scheduler.example;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static java.util.UUID.randomUUID;

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

/**
 * Demonstrates how to initialize the scheduler.
 *
 * @author Rico Jasper
 */
public final class BasicExample {

	public static void main(String[] args) throws CollisionException {
		// initialize scheduler
		Scheduler scheduler = new Scheduler(world());

		scheduler.setPresentTime(LocalDateTime.of(2015, 1, 1, 0, 0, 0)); // 1/1/2015 12:00 AM
		scheduler.setFrozenHorizonDuration(Duration.ofMillis(500L));
		scheduler.setInterDependencyMargin(Duration.ofMillis(500L));

		NodeReference nodeRef = scheduler.addNode(nodeSpec());

		// creating job specification
		ImmutablePolygon locationSpace  = immutableBox(6.0, 1.0, 9.0, 4.0);
		LocalDateTime earliestStartTime = LocalDateTime.of(2015, 1, 1, 0, 2, 0); // 1/1/2015 12:02 AM
		LocalDateTime latestStartTime   = LocalDateTime.of(2015, 1, 1, 0, 4, 0); // 1/1/2015 12:04 AM
		Duration duration = Duration.ofSeconds(2L * 60L); // 2 minutes

		JobSpecification spec = JobSpecification.createSS(
			randomUUID(), locationSpace, earliestStartTime, latestStartTime, duration);

		// passing specification to scheduler
		ScheduleResult result = scheduler.schedule(spec);

		// commit changes
		scheduler.commit(result.getTransactionId());

		System.out.println("schedule update:");
		System.out.println(result);
		System.out.println();
		System.out.println("trajectory:");
		System.out.println(nodeRef.calcTrajectory());
	}

	private static StaticObstacle staticObstacle() {
		ImmutablePolygon shape = immutableBox(1.0, 5.0, 3.0, 7.0);

		return new StaticObstacle(shape);
	}

	private static DynamicObstacle dynamicObstacle() {
		ImmutablePolygon shape = immutableBox(-1.0, -1.0, 1.0, 1.0);

		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(
			immutablePoint(7.0, 9.0),
			immutablePoint(7.0, 6.0)));

		ImmutableList<LocalDateTime> times = ImmutableList.of(
			LocalDateTime.of(2015, 1, 1, 0, 0,  0),  // 1/1/2015 12:00 AM
			LocalDateTime.of(2015, 1, 1, 0, 0, 30)); // 1/1/2015 12:30 AM

		Trajectory trajectory =
			new SimpleTrajectory(spatialPath, times);

		return new DynamicObstacle(shape, trajectory);
	}

	private static World world() {
		StaticObstacle staticObstacle = staticObstacle();
		DynamicObstacle dynamicObstacle = dynamicObstacle();

		return new World(ImmutableList.of(staticObstacle), ImmutableList.of(dynamicObstacle));
	}

	private static NodeSpecification nodeSpec() {
		ImmutablePolygon nodeShape = immutableBox(-0.5, -0.5, +0.5, +0.5);
		double maxSpeed = 1.0; // m/s

		ImmutablePoint initialLocation = immutablePoint(2.5, 2.5);

		LocalDateTime initialTime = LocalDateTime.of(2015, 1, 1, 0, 1, 0); // 1/1/2015 12:01 AM

		return new NodeSpecification("node-id", nodeShape, maxSpeed, initialLocation, initialTime);
	}

}
