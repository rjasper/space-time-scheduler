package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.*;

import java.time.Duration;
import java.util.UUID;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.PeriodicJobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public class PeriodicJobSchedulerIndependentLocationBenchmark implements Benchmarkable {

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

		sw.start();
		ScheduleResult res = sc.schedule(makeSpec(n));
		sw.stop();

		if (res.isError())
			throw new AssertionError("jobs were not scheduled");

//		System.out.println(res.getJobs().values().stream()
//			.sorted( (j1, j2) -> j1.getStartTime().compareTo(j2.getStartTime()) )
//			.collect(Collectors.toList()));

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

	private PeriodicJobSpecification makeSpec(int n) {
		ImmutableList.Builder<UUID> idsBuilder = ImmutableList.builder();

		for (int i = 0; i < n; ++i)
			idsBuilder.add( uuid(Integer.toString(i)) );

		return new PeriodicJobSpecification(
			idsBuilder.build(),
			immutablePoint(0, 3),
			false,
			secondsToDurationSafe(2),
			atSecond(10),
			secondsToDurationSafe(10));
	}

}
