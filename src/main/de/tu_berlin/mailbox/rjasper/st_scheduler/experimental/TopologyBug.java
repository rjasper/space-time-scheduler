package de.tu_berlin.mailbox.rjasper.st_scheduler.experimental;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.immutable;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePolygon;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToDurationSafe;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.CollisionException;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.JobSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleResult;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;

public class TopologyBug {

	private Scheduler scheduler;

	private List<NodeReference> nodeRefs = new LinkedList<>();

	public TopologyBug() {
		StaticObstacle o1 = new StaticObstacle(
			immutablePolygon(0,0,50,0,50,0.1,0,0.1,0,0));
		StaticObstacle o2 = new StaticObstacle(
			immutablePolygon(4.0, 7.0, 9.0, 7.0, 9.0, 6.0, 5.0, 6.0, 5.0, 2.0, 4.0, 2.0, 4.0, 7.0));
		StaticObstacle o3 = new StaticObstacle(
			immutablePolygon(4.0, 9.0, 9.0, 9.0, 9.0, 10.0, 5.0, 10.0, 5.0, 25.0, 4.0, 25.0, 4.0, 9.0));
		StaticObstacle o4 = new StaticObstacle(
			immutablePolygon(11.0, 1.0, 12.0, 1.0, 12.0, 2.0, 11.0, 2.0, 11.0, 1.0));
		StaticObstacle o5 = new StaticObstacle(
			immutablePolygon(11.0, 3.0, 12.0, 3.0, 12.0, 13.0, 7.0, 13.0, 7.0, 12.0, 11.0, 12.0, 11.0, 3.0));
		StaticObstacle o6 = new StaticObstacle(
			immutablePolygon(16.0, 7.0, 17.0, 7.0, 17.0, 0.0, 16.0, 0.0, 16.0, 7.0));
		StaticObstacle o7 = new StaticObstacle(
			immutablePolygon(16.0, 9.0, 17.0, 9.0, 17.0, 35.0, 16.0, 35.0, 16.0, 9.0));
		StaticObstacle o8 = new StaticObstacle(
			immutablePolygon(4, 7, 5, 7, 5, 9, 4, 9, 4, 7));
		StaticObstacle o9 = new StaticObstacle(
			immutablePolygon(7,3, 11,3, 11,4, 7,4, 7,3));
		StaticObstacle o10 = new StaticObstacle(
			immutablePolygon(16,9, 14.1,9, 14.1,3, 13.9,3, 13.9,10, 16,10, 16,9));

		World world = new World(ImmutableList.of(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10), ImmutableList.of());

		scheduler = new Scheduler(world);
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

	public void addJob(Geometry location, LocalDateTime earliest, LocalDateTime latest) {
		UUID jobId = UUID.randomUUID();

		ScheduleResult res = scheduler.schedule(
			JobSpecification.createSF(jobId, immutable(location), earliest, latest, ofSeconds(1)));

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
		TopologyBug ctx = new TopologyBug();

		ctx.addNode(2, 2, atSecond(0));
		ctx.addNode(2, 6, atSecond(0));
//		ctx.addNode(2, 10, atSecond(0));

//		1) 2 60 21 7
//		2) 2 60 10 8
//		3) 2 60 10 11

//		ctx.addJob(immutableBox(12, 3, 14, 5), atSecond(2), atSecond(60));
		ctx.addJob(immutableBox(20, 6, 22, 8), atSecond(2), atSecond(60));
		ctx.addJob(immutableBox(9, 7, 11, 9), atSecond(2), atSecond(60));
		ctx.addJob(immutableBox(9, 10, 11, 12), atSecond(2), atSecond(60));

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
