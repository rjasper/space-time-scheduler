package de.tu_berlin.mailbox.rjasper.st_scheduler.example;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public class Example2 {
	
	public static void main(String[] args) {
		ImmutablePolygon nodeShape = immutableBox(-0.5, -0.5, 0.5, 0.5);
		double maxSpeed = 0.5;
		
		NodeSpecification ws1 = new NodeSpecification(
			"w1", nodeShape, maxSpeed, immutablePoint(3, 1), atSecond(0));
		NodeSpecification ws2 = new NodeSpecification(
			"w2", nodeShape, maxSpeed, immutablePoint(7, 1), atSecond(0));
		
		Scheduler sc = new Scheduler(new World());
		NodeReference w1 = sc.addNode(ws1);
		NodeReference w2 = sc.addNode(ws2);
		
		scheduleAndCommit(sc, new JobSpecification(
			uuid("ts1"),
			immutablePoint(3, 1),
			atSecond(0),
			atSecond(0),
			ofSeconds(10)));
		
		scheduleAndCommit(sc, new JobSpecification(
			uuid("ts2"),
			immutablePoint(1, 3),
			atSecond(30),
			atSecond(30),
			ofSeconds(10)));
		
		scheduleAndCommit(sc, new JobSpecification(
			uuid("ts3"),
			immutablePoint(3, 5),
			atSecond(60),
			atSecond(60),
			ofSeconds(10)));

		scheduleAndCommit(sc, new JobSpecification(
			uuid("ts4"),
			immutablePoint(7, 1),
			atSecond(0),
			atSecond(0),
			ofSeconds(20)));
		
		scheduleAndCommit(sc, new JobSpecification(
			uuid("ts5"),
			immutablePoint(7, 5),
			atSecond(50),
			atSecond(50),
			ofSeconds(20)));
		
		ImmutablePolygon locationSpace = immutableBox(4, 2, 6, 4);
		LocalDateTime earliest = atSecond(20);
		LocalDateTime latest = atSecond(50);
		Duration duration = ofSeconds(10);
		
		ScheduleResult result = sc.schedule(new JobSpecification(
			uuid("ts6"),
			locationSpace,
			earliest,
			latest,
			duration));
		
		System.out.println(result);
		
		for (NodeReference ref : Arrays.asList(w1, w2)) {
			System.out.println(ref.calcTrajectory().trace());
		}
	}
	
	private static void scheduleAndCommit(Scheduler sc, JobSpecification spec) {
		ScheduleResult res = sc.schedule(spec);
		sc.commit(res.getTransactionId());
	}
	
	private static UUID uuid(String name) {
		return UUID.nameUUIDFromBytes(name.getBytes());
	}

	private static final LocalDateTime BASE_TIME =
		LocalDateTime.of(2000, 1, 1, 0, 0);

	private static LocalDateTime atSecond(double second) {
		return BASE_TIME.plus( ofSeconds(second) );
	}

	private static Duration ofSeconds(double seconds) {
		return secondsToDurationSafe(seconds);
	}
	
}
