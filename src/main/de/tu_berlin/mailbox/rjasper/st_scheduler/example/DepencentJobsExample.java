package de.tu_berlin.mailbox.rjasper.st_scheduler.example;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public final class DepencentJobsExample {
	
	public static void main(String[] args) {
		Scheduler sc = new Scheduler(new World());
		NodeReference nref = sc.addNode( makeNodeSpec() );
		
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
		
		SimpleDirectedGraph<UUID, DefaultEdge> depGraph =
			new SimpleDirectedGraph<>(DefaultEdge.class);
		
		Collection<JobSpecification> specs = Arrays.asList(js1, js2);
		
		// js2 depends on js1
		addDependencies(depGraph, uuid("j1"));
		addDependencies(depGraph, uuid("j2"), uuid("j1"));
		
		ScheduleResult res = sc.schedule(specs, depGraph);
		sc.commit(res.getTransactionId());
		
		System.out.println( nref.calcTrajectory() );
		System.out.println( nref.getJobs() );
	}

	private static NodeSpecification makeNodeSpec() {
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
		
		Arrays.stream(dependencies)
			.forEach(d -> graph.addEdge(jobId, d));
	}

}
