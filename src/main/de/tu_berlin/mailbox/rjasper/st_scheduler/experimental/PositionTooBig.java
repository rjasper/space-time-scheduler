package de.tu_berlin.mailbox.rjasper.st_scheduler.experimental;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;

public final class PositionTooBig {

	private Scheduler scheduler;

	private List<NodeReference> nodeRefs = new LinkedList<>();

	public PositionTooBig() {
		scheduler = new Scheduler(new World());
	}

	private static LocalDateTime time(String timeString) {
		return LocalDateTime.parse(timeString);
	}

	private static final ImmutablePolygon NODE_SHAPE = immutableBox(-0.5, -0.5, 0.5, 0.5);

	public void addNode(double x, double y, LocalDateTime time) {
		String nodeId = String.format("node_%d", nodeRefs.size());

		try {
			NodeReference nodeRef = scheduler.addNode(
				new NodeSpecification(nodeId, NODE_SHAPE, 1.0, immutablePoint(x, y), time));

			nodeRefs.add(nodeRef);
		} catch (CollisionException e) {
			e.printStackTrace();
		}
	}

	public List<NodeReference> getNodeRefs() {
		return nodeRefs;
	}

	public void schedule(ImmutablePolygon locationSpace, LocalDateTime earliest, LocalDateTime latest, Duration duration) {
		UUID jobId = UUID.randomUUID();
		JobSpecification spec = new JobSpecification(jobId, locationSpace, earliest, latest, duration);

		ScheduleResult res = scheduler.schedule(new JobSpecification(jobId, locationSpace, earliest, latest, duration));

//		if (res.isError())
//			throw new RuntimeException("schedule unsuccessful");
		if (res.isError())
			System.out.println("failed to schedule " + spec);
		else
			scheduler.commit(res.getTransactionId());
	}

	private static final LocalDateTime BASE_TIME =
		LocalDateTime.of(2000, 1, 1, 0, 0);

	private static LocalDateTime atSecond(double second) {
		return BASE_TIME.plus( ofSeconds(second) );
	}

	private static Duration ofSeconds(double seconds) {
		return secondsToDurationSafe(seconds);
	}

	public static void main(String[] args) throws CollisionException {
		PositionTooBig ctx = new PositionTooBig();


		ctx.addNode(0.0, 1.75, atSecond(0));
		ctx.addNode(0.0, 5.75, atSecond(0));
		ctx.addNode(0.0, 9.75, atSecond(0));

//		(POLYGON ((4 5, 5.5 5, 5.5 6.5, 4 6.5, 4 5)), 2015-07-12T16:08:30.148, 2015-07-12T16:08:32.148, PT1S)
//
//		(POLYGON ((6 9, 7.5 9, 7.5 10.5, 6 10.5, 6 9)), 2015-07-12T16:08:34.148, 2015-07-12T16:08:36.148, PT1S)
//
//		(POLYGON ((8 1, 9.5 1, 9.5 2.5, 8 2.5, 8 1)), 2015-07-12T16:08:38.148, 2015-07-12T16:08:40.148, PT1S)
//
//		(POLYGON ((10 5, 11.5 5, 11.5 6.5, 10 6.5, 10 5)), 2015-07-12T16:08:42.148, 2015-07-12T16:08:44.148, PT1S)
//
//		(POLYGON ((10 9, 11.5 9, 11.5 10.5, 10 10.5, 10 9)), 2015-07-12T16:08:42.148, 2015-07-12T16:08:44.148, PT1S)
//
//		(POLYGON ((6 5, 7.5 5, 7.5 6.5, 6 6.5, 6 5)), 2015-07-12T16:08:34.148, 2015-07-12T16:08:36.148, PT1S)
//
//		(POLYGON ((4 9, 5.5 9, 5.5 10.5, 4 10.5, 4 9)), 2015-07-12T16:08:30.148, 2015-07-12T16:08:32.148, PT1S)
//
//		(POLYGON ((10 1, 11.5 1, 11.5 2.5, 10 2.5, 10 1)), 2015-07-12T16:08:42.148, 2015-07-12T16:08:44.148, PT1S)
//
//		(POLYGON ((6 1, 7.5 1, 7.5 2.5, 6 2.5, 6 1)), 2015-07-12T16:08:34.148, 2015-07-12T16:08:36.148, PT1S)
//
//		(POLYGON ((8 5, 9.5 5, 9.5 6.5, 8 6.5, 8 5)), 2015-07-12T16:08:38.148, 2015-07-12T16:08:40.148, PT1S)
//
//		(POLYGON ((4 1, 5.5 1, 5.5 2.5, 4 2.5, 4 1)), 2015-07-12T16:08:30.148, 2015-07-12T16:08:32.148, PT1S)
//
//		(POLYGON ((8 9, 9.5 9, 9.5 10.5, 8 10.5, 8 9)), 2015-07-12T16:08:38.148, 2015-07-12T16:08:40.148, PT1S)

//		ctx.schedule(immutableBox(4, 5, 5.5, 6.5), atSecond(30), atSecond(32), ofSeconds(1));
//		ctx.schedule(immutableBox(6, 9, 7.5, 10.5), atSecond(42), atSecond(44), ofSeconds(1));
//		ctx.schedule(immutableBox(8, 1, 9.5, 2.5), atSecond(54), atSecond(56), ofSeconds(1));
//		ctx.schedule(immutableBox(10, 5, 11.5, 6.5), atSecond(66), atSecond(68), ofSeconds(1));
//		ctx.schedule(immutableBox(10, 9, 11.5, 10.5), atSecond(66), atSecond(68), ofSeconds(1));
//		ctx.schedule(immutableBox(6, 5, 7.5, 6.5), atSecond(42), atSecond(44), ofSeconds(1));
//		ctx.schedule(immutableBox(4, 9, 5.5, 10.5), atSecond(30), atSecond(32), ofSeconds(1));
//		ctx.schedule(immutableBox(10, 1, 11.5, 2.5), atSecond(66), atSecond(68), ofSeconds(1));
//		ctx.schedule(immutableBox(6, 1, 7.5, 2.5), atSecond(42), atSecond(44), ofSeconds(1));
//		ctx.schedule(immutableBox(8, 5, 9.5, 6.5), atSecond(54), atSecond(56), ofSeconds(1));
//		ctx.schedule(immutableBox(4, 1, 5.5, 2.5), atSecond(30), atSecond(32), ofSeconds(1));
//		ctx.schedule(immutableBox(8, 9, 9.5, 10.5), atSecond(54), atSecond(56), ofSeconds(1));

//		ctx.schedule(immutableBox(4, 1, 5.5, 2.5), atSecond(30), atSecond(32), ofSeconds(1));
//		ctx.schedule(immutableBox(6, 1, 7.5, 2.5), atSecond(42), atSecond(44), ofSeconds(1));
//		ctx.schedule(immutableBox(8, 1, 9.5, 2.5), atSecond(54), atSecond(56), ofSeconds(1));
//		ctx.schedule(immutableBox(10, 1, 11.5, 2.5), atSecond(66), atSecond(68), ofSeconds(1));
//
//		ctx.schedule(immutableBox(4, 5, 5.5, 6.5), atSecond(30), atSecond(32), ofSeconds(1));
//		ctx.schedule(immutableBox(6, 5, 7.5, 6.5), atSecond(42), atSecond(44), ofSeconds(1));
//		ctx.schedule(immutableBox(8, 5, 9.5, 6.5), atSecond(54), atSecond(56), ofSeconds(1));
//		ctx.schedule(immutableBox(10, 5, 11.5, 6.5), atSecond(66), atSecond(68), ofSeconds(1));
//
//		ctx.schedule(immutableBox(4, 9, 5.5, 10.5), atSecond(30), atSecond(32), ofSeconds(1));
//		ctx.schedule(immutableBox(6, 9, 7.5, 10.5), atSecond(42), atSecond(44), ofSeconds(1));
//		ctx.schedule(immutableBox(8, 9, 9.5, 10.5), atSecond(54), atSecond(56), ofSeconds(1));
//		ctx.schedule(immutableBox(10, 9, 11.5, 10.5), atSecond(66), atSecond(68), ofSeconds(1));

//		(POLYGON ((8 5, 9.5 5, 9.5 6.5, 8 6.5, 8 5)), 2015-07-12T19:10:02.052, 2015-07-12T19:10:04.052, PT1S)
//		(POLYGON ((10 9, 11.5 9, 11.5 10.5, 10 10.5, 10 9)), 2015-07-12T19:10:06.052, 2015-07-12T19:10:08.052, PT1S)
//		(POLYGON ((6 5, 7.5 5, 7.5 6.5, 6 6.5, 6 5)), 2015-07-12T19:09:58.052, 2015-07-12T19:10:00.052, PT1S)
//		(POLYGON ((4 1, 5.5 1, 5.5 2.5, 4 2.5, 4 1)), 2015-07-12T19:09:54.052, 2015-07-12T19:09:56.052, PT1S)
//		(POLYGON ((6 1, 7.5 1, 7.5 2.5, 6 2.5, 6 1)), 2015-07-12T19:09:58.052, 2015-07-12T19:10:00.052, PT1S)
//		(POLYGON ((10 1, 11.5 1, 11.5 2.5, 10 2.5, 10 1)), 2015-07-12T19:10:06.052, 2015-07-12T19:10:08.052, PT1S)
//		(POLYGON ((4 5, 5.5 5, 5.5 6.5, 4 6.5, 4 5)), 2015-07-12T19:09:54.052, 2015-07-12T19:09:56.052, PT1S)
//		(POLYGON ((4 9, 5.5 9, 5.5 10.5, 4 10.5, 4 9)), 2015-07-12T19:09:54.052, 2015-07-12T19:09:56.052, PT1S)
//		(POLYGON ((6 9, 7.5 9, 7.5 10.5, 6 10.5, 6 9)), 2015-07-12T19:09:58.052, 2015-07-12T19:10:00.052, PT1S)
//		(POLYGON ((10 5, 11.5 5, 11.5 6.5, 10 6.5, 10 5)), 2015-07-12T19:10:06.052, 2015-07-12T19:10:08.052, PT1S)
//		(POLYGON ((8 9, 9.5 9, 9.5 10.5, 8 10.5, 8 9)), 2015-07-12T19:10:02.052, 2015-07-12T19:10:04.052, PT1S)
//		(POLYGON ((8 1, 9.5 1, 9.5 2.5, 8 2.5, 8 1)), 2015-07-12T19:10:02.052, 2015-07-12T19:10:04.052, PT1S)

		ctx.schedule(immutableBox(4, 1, 5.5, 2.5), time("2015-07-12T19:09:54.052"), time("2015-07-12T19:09:56.052"), ofSeconds(1));
		ctx.schedule(immutableBox(6, 1, 7.5, 2.5), time("2015-07-12T19:09:58.052"), time("2015-07-12T19:10:00.052"), ofSeconds(1));
		ctx.schedule(immutableBox(8, 1, 9.5, 2.5), time("2015-07-12T19:10:02.052"), time("2015-07-12T19:10:04.052"), ofSeconds(1));
		ctx.schedule(immutableBox(10, 1, 11.5, 2.5), time("2015-07-12T19:10:06.052"), time("2015-07-12T19:10:08.052"), ofSeconds(1));

		ctx.schedule(immutableBox(4, 5, 5.5, 6.5), time("2015-07-12T19:09:54.052"), time("2015-07-12T19:09:56.052"), ofSeconds(1));
		ctx.schedule(immutableBox(6, 5, 7.5, 6.5), time("2015-07-12T19:09:58.052"), time("2015-07-12T19:10:00.052"), ofSeconds(1));
		ctx.schedule(immutableBox(8, 5, 9.5, 6.5), time("2015-07-12T19:10:02.052"), time("2015-07-12T19:10:04.052"), ofSeconds(1));
		ctx.schedule(immutableBox(10, 5, 11.5, 6.5), time("2015-07-12T19:10:06.052"), time("2015-07-12T19:10:08.052"), ofSeconds(1));

		ctx.schedule(immutableBox(4, 9, 5.5, 10.5), time("2015-07-12T19:09:54.052"), time("2015-07-12T19:09:56.052"), ofSeconds(1));
		ctx.schedule(immutableBox(6, 9, 7.5, 10.5), time("2015-07-12T19:09:58.052"), time("2015-07-12T19:10:00.052"), ofSeconds(1));
		ctx.schedule(immutableBox(8, 9, 9.5, 10.5), time("2015-07-12T19:10:02.052"), time("2015-07-12T19:10:04.052"), ofSeconds(1));
		ctx.schedule(immutableBox(10, 9, 11.5, 10.5), time("2015-07-12T19:10:06.052"), time("2015-07-12T19:10:08.052"), ofSeconds(1));

		for (NodeReference ref : ctx.getNodeRefs()) {
			System.out.println(ref.getId());
			System.out.println("jobs:");
			System.out.println(ref.getJobs());
			System.out.println("trajectory:");
			Trajectory traj = ref.calcTrajectory();
			System.out.println(traj);
			System.out.println("trace");
			System.out.println(traj.trace());
			System.out.println();
		}
	}

}
