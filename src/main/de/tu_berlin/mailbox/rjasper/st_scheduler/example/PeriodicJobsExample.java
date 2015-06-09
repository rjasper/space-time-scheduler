package de.tu_berlin.mailbox.rjasper.st_scheduler.example;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static java.util.UUID.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.PeriodicJobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

/**
 * Demonstrates how to schedule a set of periodic jobs.
 *
 * @author Rico Jasper
 */
public final class PeriodicJobsExample {

	public static void main(String[] args) throws CollisionException {
		Scheduler sc = new Scheduler(new World());
		NodeReference nref = sc.addNode(nodeSpec());

		ImmutableList<UUID> jobIds        = generateIds(4);
		ImmutablePolygon    locationSpace = immutableBox(10, 10, 12, 12);
		boolean             sameLocation  = true;
		Duration            duration      = ofSeconds(10);
		LocalDateTime       startTime     = atSecond(30);
		Duration            period        = ofSeconds(20);

		PeriodicJobSpecification spec = new PeriodicJobSpecification(
			jobIds, locationSpace, sameLocation, duration, startTime, period);

		// pass specification to scheduler
		ScheduleResult res = sc.schedule(spec);
		// commit changes
		sc.commit(res.getTransactionId());

		System.out.println("scheduled jobs:");
		System.out.println( nref.getJobs() );
		System.out.println();
		System.out.println("trajectory:");
		System.out.println( nref.calcTrajectory() );
	}

	private static final LocalDateTime BASE_TIME =
		LocalDateTime.of(2000, 1, 1, 0, 0);

	private static LocalDateTime atSecond(double second) {
		return BASE_TIME.plus( ofSeconds(second) );
	}

	private static Duration ofSeconds(double seconds) {
		return secondsToDurationSafe(seconds);
	}

	private static NodeSpecification nodeSpec() {
		String id = "node";
		ImmutablePolygon shape = immutableBox(-0.5, -0.5, 0.5, 0.5);
		double maxSpeed = 1.0;
		ImmutablePoint initialLocation = immutablePoint(0.0, 0.0);
		LocalDateTime initialTime = atSecond(0.0);

		return new NodeSpecification(
			id, shape, maxSpeed, initialLocation, initialTime);
	}

	private static ImmutableList<UUID> generateIds(int n) {
		ImmutableList.Builder<UUID> builder = ImmutableList.builder();

		for (int i = 0; i < n; ++i)
			builder.add(randomUUID());

		return builder.build();
	}

}
