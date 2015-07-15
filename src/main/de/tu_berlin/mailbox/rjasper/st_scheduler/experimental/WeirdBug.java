package de.tu_berlin.mailbox.rjasper.st_scheduler.experimental;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public final class WeirdBug {

	private Scheduler scheduler;

	private List<NodeReference> nodeRefs = new LinkedList<>();

	public WeirdBug() {
		scheduler = new Scheduler(new World());
	}

	public List<NodeReference> getNodeRefs() {
		return nodeRefs;
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

	public void addJob(double x, double y, LocalDateTime earliest, LocalDateTime latest) {
		UUID jobId = UUID.randomUUID();
		ImmutablePoint location = immutablePoint(x, y);

		ScheduleResult res = scheduler.schedule(
			JobSpecification.createSF(jobId, location, earliest, latest, ofSeconds(1)));

		if (res.isError())
			System.err.println("errorenous result");
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

	public static void main(String[] args) {
		WeirdBug ctx = new WeirdBug();

		ctx.addNode(10, 2, atSecond(0));
		ctx.addNode(5, 2, atSecond(0));

		ctx.addJob(5, 15, atSecond(30), atSecond(40));

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
