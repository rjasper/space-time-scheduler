package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.*;

import java.time.Duration;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public class SingularJobSchedulerNodeNumberErrorBenchmark implements Benchmarkable {

	private StopWatch sw = new StopWatch();

	@Override
	public int minProblemSize() {
		return 10;
	}

	@Override
	public int maxProblemSize() {
		return 100;
	}

	@Override
	public int stepProblemSize() {
		return 10;
	}

	@Override
	public Duration benchmark(int n) {
		sw.reset();

		Scheduler sc = new Scheduler(makeWorld(n));

		addNodes(sc, n);

		JobSpecification spec = JobSpecification.createSS(
			uuid("job"),
			immutablePoint(0, 3),
			atSecond(0), atSecond(3*n+4),
			secondsToDurationSafe(10));

		sw.start();
		ScheduleResult res = sc.schedule(spec);
		sw.stop();

		if (res.isSuccess())
			throw new AssertionError("job was scheduled");

		return sw.duration();
	}

	private World makeWorld(int n) {
		// blocks job location
		DynamicObstacle obstacle = new DynamicObstacle(
			immutableBox(-1, -1, 1, 1),
			trajectory(0, 0, 3, 3, 0, 3*n+4));

		return new World(ImmutableList.of(), ImmutableList.of(obstacle));
	}

	private static final ImmutablePolygon SHAPE = immutableBox(-1, -1, 1, 1);
	private static final double PI2 = 2.0*Math.PI;

	private void addNodes(Scheduler sc, int n) {
		double radius = 3.0*n / PI2;
		for (int i = 0; i < n; ++i) {
			double phi = (i * PI2) / n;
			NodeSpecification spec = new NodeSpecification(
				Integer.toString(i),
				SHAPE,
				radius,
//				immutablePoint(3*i, 0),
				immutablePoint(radius*Math.cos(phi), radius*Math.sin(phi)),
				atSecond(0));

			try {
				sc.addNode(spec);
			} catch (CollisionException e) {
				e.printStackTrace();
			}
		}
	}


}
