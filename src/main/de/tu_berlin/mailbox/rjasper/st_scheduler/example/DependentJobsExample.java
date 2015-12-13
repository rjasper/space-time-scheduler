package de.tu_berlin.mailbox.rjasper.st_scheduler.example;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToDurationSafe;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

/**
 * Demonstrates how to schedule a set of dependent jobs.
 *
 * @author Rico Jasper
 */
public final class DependentJobsExample {

	public static void main(String[] args) throws CollisionException {
		Scheduler sc = new Scheduler(new World());
		NodeReference nref = sc.addNode( nodeSpec() );

		// specifications for each job to be scheduled
		JobSpecification js1 = JobSpecification.createSF(
			uuid("j1"),
			immutablePoint(10, 10),
			atSecond(60),
			atSecond(120),
			ofSeconds(5));
		JobSpecification js2 = JobSpecification.createSF(
			uuid("j2"),
			immutablePoint(20, 20),
			atSecond(60),
			atSecond(120),
			ofSeconds(5));

		Collection<JobSpecification> specs = Arrays.asList(js1, js2);

		// construction of dependency graph
		SimpleDirectedGraph<UUID, DefaultEdge> dependencies =
			new SimpleDirectedGraph<>(DefaultEdge.class);

		// js2 depends on js1
		addDependencies(dependencies, uuid("j1"));
		addDependencies(dependencies, uuid("j2"), uuid("j1"));

		// pass specifications and dependencies to scheduler
		ScheduleResult res = sc.schedule(specs, dependencies);
		// commit changes
		sc.commit(res.getTransactionId());

		System.out.println("scheduled jobs:");
		System.out.println( nref.getJobs() );
		System.out.println();
		System.out.println("trajectory:");
		System.out.println( nref.calcTrajectory() );
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

	private static void addDependencies(
		SimpleDirectedGraph<UUID, DefaultEdge> graph,
		UUID jobId,
		UUID... dependencies)
	{
		graph.addVertex(jobId);

		for (UUID d : dependencies)
			graph.addEdge(jobId, d);
	}

}
