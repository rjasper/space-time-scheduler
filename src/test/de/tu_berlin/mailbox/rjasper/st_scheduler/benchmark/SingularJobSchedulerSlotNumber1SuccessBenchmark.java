//package de.tu_berlin.mailbox.rjasper.st_scheduler.benchmark;
//
//import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
//import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.*;
//import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
//import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
//import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.*;
//
//import java.time.Duration;
//
//import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
//import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
//import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Job;
//import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
//import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
//import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
//import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
//import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
//import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
//import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
//import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
//
//public class SingularJobSchedulerSlotNumber1SuccessBenchmark implements Benchmarkable {
//
//	private StopWatch sw = new StopWatch();
//
//	@Override
//	public int minProblemSize() {
//		return 1000;
//	}
//
//	@Override
//	public int maxProblemSize() {
//		return 10000;
//	}
//
//	@Override
//	public int stepProblemSize() {
//		return 1000;
//	}
//
//	@Override
//	public Duration benchmark(int n) {
//		sw.reset();
//
//		Scheduler sc = new Scheduler(new World());
//
//		addNode(sc, n);
//
//		JobSpecification spec = JobSpecification.createSS(
//			uuid("job"),
//			immutablePoint(0, 3),
//			atSecond(1), atSecond(12*(n-1)+1),
//			secondsToDurationSafe(3));
//
//		sw.start();
//		ScheduleResult res = sc.schedule(spec);
//		sw.stop();
//
//		if (res.isError())
//			throw new AssertionError("job was not scheduled");
//
////		System.out.println(res.getJobs().get(uuid("job")));
//
//		return sw.duration();
//	}
//
//	public void addNode(Scheduler sc, int n) {
//		NodeSpecification spec = new NodeSpecification(
//			"node",
//			immutableBox(-1, -1, 1, 1),
//			1.0,
//			immutablePoint(0, 0),
//			atSecond(0));
//
//		try {
//			sc.addNode(spec);
//		} catch (CollisionException e) {
//			e.printStackTrace();
//		}
//
//		Node node = sc.__debug__getNode("node");
//		NodeReference ref = node.getReference();
//		ImmutablePoint location = node.getInitialLocation();
//
//		for (int i = 0; i < n; ++i) {
//			Job job = new Job(
//				uuid(Integer.toString(i)),
//				ref,
//				location,
//				atSecond(12*i),
//				secondsToDurationSafe(2));
//			Trajectory traj = trajectory(0, 0, 0, 0, 12*i, 12*i+2);
//
//			node.addJob(job);
//			node.updateTrajectory(traj);
//		}
//	}
//
//}
