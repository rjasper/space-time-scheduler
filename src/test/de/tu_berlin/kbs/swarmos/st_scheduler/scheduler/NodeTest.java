package de.tu_berlin.kbs.swarmos.st_scheduler.scheduler;

import static de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeConv.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeFactory.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.UUIDFactory.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.world.factories.PathFactory.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.world.factories.TrajectoryFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.IdleSlot;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Job;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Node;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.NodeReference;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.DecomposedTrajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory;

public class NodeTest {
	
	private static final ImmutablePolygon NODE_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double NODE_SPEED = 1.0;
	
	private static Node node(String nodeId, double x, double y) {
		NodeSpecification spec = new NodeSpecification(
			nodeId, NODE_SHAPE, NODE_SPEED, immutablePoint(x, y), atSecond(0));
		
		return new Node(spec);
	}
	
//	@Test
//	public void testIdleSubSet() {
//		Node node = NodeFixtures.withThreeJobs();
//		
//		Collection<IdleSlot> slots = node.idleSlots(
//			atHour( 0),
//			atHour(18)
//		);
//		
//		Collection<IdleSlot> expected = Arrays.asList(
//			new IdleSlot(immutablePoint( 0,  0), immutablePoint(10, 10), atHour( 0), atHour( 6)),
//			new IdleSlot(immutablePoint(10, 10), immutablePoint(20, 10), atHour( 7), atHour(12)),
//			new IdleSlot(immutablePoint(20, 10), immutablePoint(20, 20), atHour(15), atHour(18)));
//		
//		assertThat(slots, equalTo(expected));
//	}
	
	@Test
	public void testCleanUp1() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(1, 1), atSecond(0), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(0, 0), atSecond(4), secondsToDuration(1));
		
		node.updateTrajectory(traj);
		node.addJob(t1);
		node.addJob(t2);
		
		node.cleanUp(atSecond(4.5));
		
		assertThat("did not remove past job",
			node.hasJob(t1), is(false));
		assertThat("removed unfinished job",
			node.hasJob(t2), is(true));
		assertThat("removed wrong number of trajectories",
			node.getTrajectories().size(), is(1));
	}
	
	@Test
	public void testCleanUp2() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		Job job = new Job(uuid("t2"), ref, immutablePoint(0, 0), atSecond(4), secondsToDuration(1));
		
		node.updateTrajectory(traj);
		node.addJob(job);
		
		node.cleanUp(atSecond(3));
		
		assertThat("removed unfinished job",
			node.hasJob(job), is(true));
		assertThat("removed wrong number of trajectories",
			node.getTrajectories().size(), is(1));
	}
	
	@Test
	public void testCleanUp3() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(1, 1), atSecond(0), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(0, 0), atSecond(4), secondsToDuration(1));
		
		node.updateTrajectory(traj);
		node.addJob(t1);
		node.addJob(t2);
		
		node.cleanUp(atSecond(5));
		
		assertThat("did not remove past job",
			node.hasJob(t1), is(false));
		assertThat("removed unfinished job",
			node.hasJob(t2), is(false));
		assertThat("removed wrong number of trajectories",
			node.getTrajectories().size(), is(1));
	}
	
	@Test
	public void testIdleSlotsEmptyInterval() {
		Node node = node("node", 0, 0);
		
		Collection<IdleSlot> slots = node.idleSlots(atSecond(1), atSecond(1));
		
		assertThat(slots.isEmpty(), is(true));
	}
	
	@Test
	public void testIdleSlotsEmptyInitialTime() {
		Node node = node("node", 0, 0);
		
		Collection<IdleSlot> slots = node.idleSlots(LocalDateTime.MIN, atSecond(0));
		
		assertThat(slots.isEmpty(), is(true));
	}
	
	@Test
	public void testIdleSlots() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Trajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 0, 1, 1, 1, 1, 0),
			arcTimePath(0, 0, 1, 1, 1, 2, 2, 3, 2, 4, 3, 5, 3, 6));
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 1), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(1, 1), atSecond(3), secondsToDuration(1));
		Job t3 = new Job(uuid("t3"), ref, immutablePoint(1, 0), atSecond(5), secondsToDuration(1));
		
		node.updateTrajectory(traj);
		node.addJob(t1);
		node.addJob(t2);
		node.addJob(t3);
		
		Collection<IdleSlot> slots = node.idleSlots(atSecond(0.5), atSecond(4.5));
		
		Collection<IdleSlot> expected = Arrays.asList(
			new IdleSlot(immutablePoint(0, 0.5), immutablePoint(0, 1  ), atSecond(0.5), atSecond(1  )),
			new IdleSlot(immutablePoint(0, 1  ), immutablePoint(1, 1  ), atSecond(2  ), atSecond(3  )),
			new IdleSlot(immutablePoint(1, 1  ), immutablePoint(1, 0.5), atSecond(4  ), atSecond(4.5)));
		
		assertThat(slots, equalTo(expected));
	}
	
	@Test
	public void testFloorIdleTimeNull() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		node.addJob(t1);
		
		assertThat(node.floorIdleTimeOrNull(atSecond(1.5)), is(nullValue()));
	}
	
	@Test
	public void testFloorIdleTimeMid() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		node.addJob(t1);
		node.addJob(t2);
		
		assertThat(node.floorIdleTimeOrNull(atSecond(2.5)), equalTo(atSecond(2)));
	}
	
	@Test
	public void testFloorIdleTimeLeft() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		node.addJob(t1);
		node.addJob(t2);
		
		assertThat(node.floorIdleTimeOrNull(atSecond(2)), equalTo(atSecond(2)));
	}
	
	@Test
	public void testFloorIdleTimeRight() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		node.addJob(t1);
		node.addJob(t2);
		
		assertThat(node.floorIdleTimeOrNull(atSecond(3)), equalTo(atSecond(2)));
	}
	
	@Test
	public void testfloorLeftInitialTime() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		node.addJob(t1);
		
		assertThat(node.floorIdleTimeOrNull(atSecond(0)), is(nullValue()));
	}
	
	@Test
	public void testCeilingIdleTimeMid() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		node.addJob(t1);
		node.addJob(t2);
		
		assertThat(node.ceilingIdleTimeOrNull(atSecond(2.5)), equalTo(atSecond(3)));
	}
	
	@Test
	public void testCeilingIdleTimeLeft() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		node.addJob(t1);
		node.addJob(t2);
		
		assertThat(node.ceilingIdleTimeOrNull(atSecond(2)), equalTo(atSecond(3)));
	}
	
	@Test
	public void testCeilingIdleTimeRight() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(0, 0), atSecond(3), secondsToDuration(1));
		
		node.addJob(t1);
		node.addJob(t2);
		
		assertThat(node.ceilingIdleTimeOrNull(atSecond(3)), equalTo(atSecond(3)));
	}
	
	@Test
	public void testCeilLeftInitialTime() {
		Node node = node("node", 0, 0);
		NodeReference ref = node.getReference();
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		node.addJob(t1);
		
		assertThat(node.ceilingIdleTimeOrNull(atSecond(0)), is(nullValue()));
	}
	
	@Test
	public void testCalcLoad() {
		NodeSpecification spec = new NodeSpecification(
			"w", NODE_SHAPE, 4.0, immutablePoint(0, 0), atSecond(-4));
		Node node = new Node(spec);
		NodeReference ref = node.getReference();
		
		Trajectory traj1 = trajectory(
			0, 0  , 3, 3,
			0, 3  , 3, 3,
			1, 2.5, 4, 10);
		Trajectory traj2 = trajectory(
			 3,  3,  3,
			 3,  2,  2,
			10, 11, 14);
		Trajectory traj3 = trajectory(
			 3,  3,  0,
			 2,  0,  0,
			14, 16, 19);

		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(-1), secondsToDuration(2));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(3, 3), atSecond( 6), secondsToDuration(3));
		Job t3 = new Job(uuid("t2"), ref, immutablePoint(3, 2), atSecond(12), secondsToDuration(1));
		
		node.updateTrajectory(traj1);
		node.updateTrajectory(traj2);
		node.updateTrajectory(traj3);
		
		node.addJob(t1);
		node.addJob(t2);
		node.addJob(t3);
		
		assertThat(node.calcLoad(atSecond(0), atSecond(16)), is(0.6875));
	}
	
	@Test
	public void testCalcJobLoad() {
		NodeSpecification spec = new NodeSpecification(
			"w", NODE_SHAPE, 4.0, immutablePoint(0, 0), atSecond(-4));
		Node node = new Node(spec);
		NodeReference ref = node.getReference();
		
		Trajectory traj1 = trajectory(
			0, 0  , 3, 3,
			0, 3  , 3, 3,
			1, 2.5, 4, 10);
		Trajectory traj2 = trajectory(
			 3,  3,  3,
			 3,  2,  2,
			10, 11, 14);
		Trajectory traj3 = trajectory(
			 3,  3,  0,
			 2,  0,  0,
			14, 16, 19);

		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(-1), secondsToDuration(2));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(3, 3), atSecond( 6), secondsToDuration(3));
		Job t3 = new Job(uuid("t2"), ref, immutablePoint(3, 2), atSecond(12), secondsToDuration(1));
		
		node.updateTrajectory(traj1);
		node.updateTrajectory(traj2);
		node.updateTrajectory(traj3);
		
		node.addJob(t1);
		node.addJob(t2);
		node.addJob(t3);
		
		assertThat(node.calcJobLoad(atSecond(0), atSecond(16)), is(0.3125));
	}
	
	@Test
	public void testCalcMotionLoad() {
		NodeSpecification spec = new NodeSpecification(
			"w", NODE_SHAPE, 4.0, immutablePoint(0, 0), atSecond(-4));
		Node node = new Node(spec);
		NodeReference ref = node.getReference();
		
		Trajectory traj1 = trajectory(
			0, 0  , 3, 3,
			0, 3  , 3, 3,
			1, 2.5, 4, 10);
		Trajectory traj2 = trajectory(
			 3,  3,  3,
			 3,  2,  2,
			10, 11, 14);
		Trajectory traj3 = trajectory(
			 3,  3,  0,
			 2,  0,  0,
			14, 16, 19);

		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(-1), secondsToDuration(2));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(3, 3), atSecond( 6), secondsToDuration(3));
		Job t3 = new Job(uuid("t2"), ref, immutablePoint(3, 2), atSecond(12), secondsToDuration(1));
		
		node.updateTrajectory(traj1);
		node.updateTrajectory(traj2);
		node.updateTrajectory(traj3);
		
		node.addJob(t1);
		node.addJob(t2);
		node.addJob(t3);
		
		assertThat(node.calcMotionLoad(atSecond(0), atSecond(16)), is(0.375));
	}
	
	@Test
	public void testCalcStationaryIdleLoad() {
		NodeSpecification spec = new NodeSpecification(
			"w", NODE_SHAPE, 4.0, immutablePoint(0, 0), atSecond(-4));
		Node node = new Node(spec);
		NodeReference ref = node.getReference();
		
		Trajectory traj1 = trajectory(
			0, 0  , 3, 3,
			0, 3  , 3, 3,
			1, 2.5, 4, 10);
		Trajectory traj2 = trajectory(
			 3,  3,  3,
			 3,  2,  2,
			10, 11, 14);
		Trajectory traj3 = trajectory(
			 3,  3,  0,
			 2,  0,  0,
			14, 16, 19);

		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(-1), secondsToDuration(2));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(3, 3), atSecond( 6), secondsToDuration(3));
		Job t3 = new Job(uuid("t2"), ref, immutablePoint(3, 2), atSecond(12), secondsToDuration(1));
		
		node.updateTrajectory(traj1);
		node.updateTrajectory(traj2);
		node.updateTrajectory(traj3);
		
		node.addJob(t1);
		node.addJob(t2);
		node.addJob(t3);
		
		assertThat(node.calcStationaryIdleLoad(atSecond(0), atSecond(16)), is(0.3125));
	}
	
	@Test
	public void testCalcVelocityLoad() {
		NodeSpecification spec = new NodeSpecification(
			"w", NODE_SHAPE, 4.0, immutablePoint(0, 0), atSecond(-4));
		Node node = new Node(spec);
		NodeReference ref = node.getReference();
		
		Trajectory traj1 = trajectory(
			0, 0  , 3, 3,
			0, 3  , 3, 3,
			1, 2.5, 4, 10);
		Trajectory traj2 = trajectory(
			 3,  3,  3,
			 3,  2,  2,
			10, 11, 14);
		Trajectory traj3 = trajectory(
			 3,  3,  0,
			 2,  0,  0,
			14, 16, 19);

		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 0), atSecond(-1), secondsToDuration(2));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(3, 3), atSecond( 6), secondsToDuration(3));
		Job t3 = new Job(uuid("t2"), ref, immutablePoint(3, 2), atSecond(12), secondsToDuration(1));
		
		node.updateTrajectory(traj1);
		node.updateTrajectory(traj2);
		node.updateTrajectory(traj3);
		
		node.addJob(t1);
		node.addJob(t2);
		node.addJob(t3);
		
		assertThat(node.calcVelocityLoad(atSecond(0), atSecond(16)), is(0.140625));
	}

}
