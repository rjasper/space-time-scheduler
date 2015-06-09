package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.*;

import java.time.Duration;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public class SingularJobSchedulerNodeNumberSuccessBenchmark implements Benchmarkable {

	private StopWatch sw = new StopWatch();

	@Override
	public int minProblemSize() {
		return 100;
	}

	@Override
	public int maxProblemSize() {
		return 1000;
	}

	@Override
	public int stepProblemSize() {
		return 100;
	}

	@Override
	public Duration benchmark(int n) {
		sw.reset();

		Scheduler sc = new Scheduler(new World());

		addNodes(sc, n);

		JobSpecification spec = JobSpecification.createSS(
			uuid("job"),
			immutablePoint(0, 0),
			atSecond(0), atSecond(2),
			secondsToDurationSafe(10));

		sw.start();
		ScheduleResult res = sc.schedule(spec);
		sw.stop();

		if (res.isError())
			throw new AssertionError("job was not scheduled");

//		Point[] points = new Point[n];
//		for (int i = 0; i < n; ++i)
//			points[i] = sc.getNodeReference(Integer.toString(i)).getInitialLocation();
//
//		System.out.println(multiPoint(points));
//
//		System.out.println(res.getJobs().get(uuid("job")));

		return sw.duration();
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
