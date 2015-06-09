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
//public class SingularJobSchedulerSlotNumber2SuccessBenchmark implements Benchmarkable {
//
//	private StopWatch sw = new StopWatch();
//
//	@Override
//	public int minProblemSize() {
//		return 100;
//	}
//
//	@Override
//	public int maxProblemSize() {
//		return 1000;
//	}
//
//	@Override
//	public int stepProblemSize() {
//		return 100;
//	}
//
//	@Override
//	public Duration benchmark(int n) {
//		sw.reset();
//
//		Scheduler sc = new Scheduler(new World());
//
//		addNodes(sc, n);
//
//		JobSpecification spec = JobSpecification.createSS(
//			uuid("job"),
//			immutablePoint(0, 3),
//			atSecond(1), atSecond(4*(n-1)+1),
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
//	public void addNodes(Scheduler sc, int n) {
//		NodeSpecification spec1 = new NodeSpecification(
//			"node1",
//			immutableBox(-1, -1, 1, 1),
//			1.0,
//			immutablePoint(0, 0),
//			atSecond(0));
//
//		NodeSpecification spec2 = new NodeSpecification(
//			"node2",
//			immutableBox(-1, -1, 1, 1),
//			1.0,
//			immutablePoint(3, 0),
//			atSecond(0));
//
//		try {
//			sc.addNode(spec1);
//			sc.addNode(spec2);
//		} catch (CollisionException e) {
//			e.printStackTrace();
//		}
//
//		Node node2 = sc.__debug__getNode("node2");
//		NodeReference ref = node2.getReference();
//		ImmutablePoint location = node2.getInitialLocation();
//
//		for (int i = 0; i < n; ++i) {
//			Job job = new Job(
//				uuid(Integer.toString(i)),
//				ref,
//				location,
//				atSecond(4*i),
//				secondsToDurationSafe(4));
//			Trajectory traj = trajectory(3, 3, 0, 0, 4*i, 4*i+4);
//
//			node2.addJob(job);
//			node2.updateTrajectory(traj);
//		}
//	}
//
//}
