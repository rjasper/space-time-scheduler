package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public class DependentJobSchedulerBenchmark implements Benchmarkable {

	private StopWatch sw = new StopWatch();

	@Override
	public int minProblemSize() {
		return 1000;
	}

	@Override
	public int maxProblemSize() {
		return 10000;
	}

	@Override
	public int stepProblemSize() {
		return 1000;
	}

	@Override
	public Duration benchmark(int n) {
		sw.reset();

		Scheduler sc = new Scheduler(new World());

		addNode(sc);

		List<JobSpecification> specs = makeSpecs(n);
		SimpleDirectedGraph<UUID, DefaultEdge> dependencies = makeGraph(specs);

		sw.start();
		ScheduleResult res = sc.schedule(specs, dependencies);
		sw.stop();

		if (res.isError())
			throw new AssertionError("jobs were not scheduled");

		return sw.duration();
	}

	private void addNode(Scheduler sc) {
		NodeSpecification spec = new NodeSpecification(
			"node",
			immutableBox(-1, -1, 1, 1),
			1.0,
			immutablePoint(0, 0),
			atSecond(0));

		try {
			sc.addNode(spec);
		} catch (CollisionException e) {
			e.printStackTrace();
		}
	}

	private List<JobSpecification> makeSpecs(int n) {
		List<JobSpecification> specs = new ArrayList<>(n);

		ImmutablePoint location = immutablePoint(0, 0);
		LocalDateTime startTime = atSecond(0);
		LocalDateTime finishTime = atSecond(4*(n+1));

		for (int i = 0; i < n; ++i) {
			specs.add(JobSpecification.createSS(
				uuid(Integer.toString(i)),
				location,
				startTime, finishTime,
				secondsToDurationSafe(2)));
		}

		return specs;
	}

	private SimpleDirectedGraph<UUID, DefaultEdge> makeGraph(List<JobSpecification> specs) {
		Iterator<UUID> ids = specs.stream()
			.map(JobSpecification::getJobId)
			.iterator();

		SimpleDirectedGraph<UUID, DefaultEdge> graph =
			new SimpleDirectedGraph<>(DefaultEdge.class);

		UUID prev = ids.next();
		graph.addVertex(prev);

		while (ids.hasNext()) {
			UUID cur = ids.next();
			graph.addVertex(cur);
			graph.addEdge(cur, prev);

			prev = cur;
		}

		return graph;
	}

}
