package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.*;

import java.time.Duration;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Job;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public class SingularJobSchedulerSlotNumber3ErrorBenchmark implements Benchmarkable {

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

		Scheduler sc = new Scheduler(makeWorld(n));

		addNodes(sc, n);

		JobSpecification spec = JobSpecification.createSS(
			uuid("job"),
			immutablePoint(3, 0),
			atSecond(1), atSecond(10*(n-1)-1),
			secondsToDurationSafe(1));

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
			trajectory(3, 3, 0, 0, 0, 10*(n-1)+2));

		return new World(ImmutableList.of(), ImmutableList.of(obstacle));
	}

	public void addNodes(Scheduler sc, int n) {
		NodeSpecification spec1 = new NodeSpecification(
			"node1",
			immutableBox(-1, -1, 1, 1),
			1.0,
			immutablePoint(0, 0),
			atSecond(0));

		NodeSpecification spec2 = new NodeSpecification(
			"node2",
			immutableBox(-1, -1, 1, 1),
			1.0,
			immutablePoint(6, 0),
			atSecond(0));

		try {
			sc.addNode(spec1);
			sc.addNode(spec2);
		} catch (CollisionException e) {
			e.printStackTrace();
		}

		Node node1 = sc.__debug__getNode("node1");
		NodeReference ref1 = node1.getReference();
		ImmutablePoint location1 = node1.getInitialLocation();

		Node node2 = sc.__debug__getNode("node2");
		NodeReference ref2 = node2.getReference();
		ImmutablePoint location2 = node2.getInitialLocation();

		for (int i = 0; i < n; ++i) {
			Job job1 = new Job(
				uuid("job1#" + Integer.toString(i)),
				ref1,
				location1,
				atSecond(10*i),
				secondsToDurationSafe(1));
			Job job2 = new Job(
				uuid("job2#" + Integer.toString(i)),
				ref2,
				location2,
				atSecond(10*i),
				secondsToDurationSafe(1));
			Trajectory traj1 = trajectory(0, 0, 0, 0, 10*i, 10*i+1);
			Trajectory traj2 = trajectory(3, 3, 0, 0, 10*i, 10*i+1);

			node1.addJob(job1);
			node1.updateTrajectory(traj1);

			node2.addJob(job2);
			node2.updateTrajectory(traj2);
		}
	}

}
