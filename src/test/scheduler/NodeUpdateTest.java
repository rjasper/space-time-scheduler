package scheduler;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeConv.*;
import static util.TimeFactory.*;
import static util.UUIDFactory.*;
import static world.factories.PathFactory.*;
import static world.factories.TrajectoryFactory.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import scheduler.util.IntervalSet;
import scheduler.util.SimpleIntervalSet;
import util.TimeFactory;
import world.DecomposedTrajectory;
import world.SimpleTrajectory;
import world.Trajectory;

import com.google.common.collect.ImmutableList;

public class NodeUpdateTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private static final String NODE_ID = "node";
	
	private static final ImmutablePolygon NODE_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double NODE_SPEED = 1.0;

	private static final ImmutablePoint NODE_INITIAL_LOCATION = immutablePoint(0, 0);

	private static final LocalDateTime NODE_INITIAL_TIME = atSecond(0);
	
	private static NodeUpdate nodeUpdate() {
		NodeSpecification spec = new NodeSpecification(
			NODE_ID, NODE_SHAPE, NODE_SPEED, NODE_INITIAL_LOCATION, NODE_INITIAL_TIME);
		Node node = new Node(spec);
		
		return new NodeUpdate(node);
	}
	
	private static <T> List<T> toList(Collection<T> collection) {
		return new ArrayList<>(collection);
	}
	
	private static IntervalSet<LocalDateTime> interval(double from, double to) {
		LocalDateTime fromTime = secondsToTime(from, TimeFactory.BASE_TIME);
		LocalDateTime toTime = secondsToTime(to, TimeFactory.BASE_TIME);
		
		return new SimpleIntervalSet<LocalDateTime>().add(fromTime, toTime);
	}

	@Test
	public void testUpdateTrajectory() {
		NodeUpdate update = nodeUpdate();
		
		Trajectory traj = trajectory(0, 0, 0, 0, 0, 1);
		
		update.updateTrajectory(traj);
		update.seal();
		
		SimpleIntervalSet<LocalDateTime> expected = new SimpleIntervalSet<>();
		expected.add(atSecond(0), atSecond(1));
		
		assertThat(update.getTrajectoryLock(),
			equalTo(expected));
	}

	@Test
	public void testUpdateTrajectoryPredate() {
		NodeUpdate update = nodeUpdate();
		
		Trajectory traj = trajectory(0, 0, 0, 0, -1, 1);
		
		thrown.expect(IllegalArgumentException.class);
		
		update.updateTrajectory(traj);
	}
	
	@Test
	public void testAddJob() {
		NodeUpdate update = nodeUpdate();
		NodeReference ref = update.getNode().getReference();
		
		Job job = new Job(uuid("job"), ref, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		update.addJob(job);
		update.seal();
		
		SimpleIntervalSet<LocalDateTime> expected = new SimpleIntervalSet<>();
		expected.add(atSecond(0), atSecond(1));
		
		assertThat(update.getTrajectoryLock(),
			equalTo(expected));
	}
	
	@Test
	public void testAddJobInvalidNode() {
		NodeUpdate update = nodeUpdate();
		NodeReference other = nodeUpdate().getNode().getReference();
		
		Job job = new Job(uuid("job"), other, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		thrown.expect(IllegalArgumentException.class);
		
		update.addJob(job);
	}

	@Test
	public void testAddJobPredate() {
		NodeUpdate update = nodeUpdate();
		NodeReference ref = update.getNode().getReference();
		
		Job job = new Job(uuid("job"), ref, immutablePoint(0, 0), atSecond(-1), secondsToDuration(1));
		
		thrown.expect(IllegalArgumentException.class);
		
		update.addJob(job);
	}
	
	@Test
	public void testAddJobRemoval() {
		NodeUpdate update = nodeUpdate();
		NodeReference ref = update.getNode().getReference();
		
		Job job = new Job(uuid("job"), ref, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		update.addJobRemoval(job);
		update.seal();
		
		Iterator<Job> it = update.getJobRemovals().iterator();
		
		assertThat(it.next(), equalTo(job));
		assertThat(it.hasNext(), is(false));
	}
	
	@Test
	public void testAddJobRemovalInvalidNode() {
		NodeUpdate update = nodeUpdate();
		NodeReference other = nodeUpdate().getNode().getReference();
		
		Job job = new Job(uuid("job"), other, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		thrown.expect(IllegalArgumentException.class);
		
		update.addJobRemoval(job);
	}
	
	@Test
	public void testCheckSelfConsistencyPositive() {
		NodeUpdate update = nodeUpdate();
		Node node = update.getNode();
		NodeReference ref = node.getReference();
		
		Trajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 0, 1, 1, 1, 1, 0),
			arcTimePath(0, 0, 1, 1, 1, 2, 2, 3, 2, 4, 3, 5, 3, 6));
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 1), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(1, 1), atSecond(3), secondsToDuration(1));
		Job t3 = new Job(uuid("t3"), ref, immutablePoint(1, 0), atSecond(5), secondsToDuration(1));
		
		update.updateTrajectory(traj);
		update.addJob(t1);
		update.addJob(t2);
		update.addJob(t3);
		
		update.checkSelfConsistency(); // expect no exception
	}
	
	@Test
	public void testCheckSelfConsistencyLocation() {
		NodeUpdate update = nodeUpdate();
		Node node = update.getNode();
		NodeReference ref = node.getReference();
		
		Trajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 0, 1, 1, 1, 1, 2),
			arcTimePath(0, 0, 1, 1, 1, 2, 2, 3, 2, 4, 3, 5, 3, 6));
		
		Job t1 = new Job(uuid("t1"), ref, immutablePoint(0, 1), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), ref, immutablePoint(1, 1), atSecond(3), secondsToDuration(1));
		// t3 location violated
		Job t3 = new Job(uuid("t3"), ref, immutablePoint(1, 0), atSecond(5), secondsToDuration(1));
		
		update.updateTrajectory(traj);
		update.addJob(t1);
		update.addJob(t2);
		update.addJob(t3);
		
		thrown.expect(IllegalStateException.class);
		
		update.checkSelfConsistency();
	}
	
	@Test
	public void testCheckSelfConsistencyContinuity() {
		NodeUpdate update = nodeUpdate();
		
		Trajectory traj1 = new SimpleTrajectory(
			spatialPath(0, 0, 1, 1),
			ImmutableList.of(atSecond(0), atSecond(1)));
		Trajectory traj2 = new SimpleTrajectory(
			spatialPath(2, 2, 3, 3),
			ImmutableList.of(atSecond(1), atSecond(2)));
		
		update.updateTrajectory(traj1);
		update.updateTrajectory(traj2);
		
		thrown.expect(IllegalStateException.class);
		
		update.checkSelfConsistency();
	}
	
	@Test
	public void testCloneIdentical() {
		NodeUpdate origin = nodeUpdate();
		Node node = origin.getNode();
		NodeReference ref = node.getReference();

		Trajectory traj = trajectory(0, 0, 0, 0, 0, 1);
		Job job = new Job(uuid("job"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job removal = new Job(uuid("removal"), ref, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		origin.updateTrajectory(traj);
		origin.addJob(job);
		origin.addJobRemoval(removal);
		
		NodeUpdate clone = origin.clone();
		
		clone.seal();
		
		assertThat("different trajectories",
			toList(clone.getTrajectories()), equalTo(singletonList(traj)));
		assertThat("different jobs",
			clone.getJobs(), equalTo(singleton(job)));
		assertThat("different removals",
			clone.getJobRemovals(), equalTo(singleton(removal)));
		assertThat("different trajectory lock",
			clone.getTrajectoryLock(), equalTo(interval(0, 2)));
		assertThat("different job lock",
			clone.getJobLock(), equalTo(interval(1, 2)));
		assertThat("different removal intervals",
			clone.getJobRemovalIntervals(), equalTo(interval(0, 1)));
	}
	
	@Test
	public void testCloneIndependent() {
		NodeUpdate origin = nodeUpdate();
		Node node = origin.getNode();
		NodeReference ref = node.getReference();
		
		NodeUpdate clone = origin.clone();

		Trajectory traj = trajectory(0, 0, 0, 0, 0, 1);
		Job job = new Job(uuid("job"), ref, immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job removal = new Job(uuid("removal"), ref, immutablePoint(0, 0), atSecond(0), secondsToDuration(1));
		
		clone.updateTrajectory(traj);
		clone.addJob(job);
		clone.addJobRemoval(removal);
		
		origin.seal();
		clone.seal();
		
		assertThat("different trajectories",
			toList(origin.getTrajectories()).isEmpty(), is(true));
		assertThat("different jobs",
			origin.getJobs().isEmpty(), is(true));
		assertThat("different removals",
			origin.getJobRemovals().isEmpty(), is(true));
		assertThat("different trajectory lock",
			origin.getTrajectoryLock().isEmpty(), is(true));
		assertThat("different job lock",
			origin.getJobLock().isEmpty(), is(true));
		assertThat("different removal intervals",
			origin.getJobRemovalIntervals().isEmpty(), is(true));
		
		assertThat("different trajectories",
			toList(clone.getTrajectories()), equalTo(singletonList(traj)));
		assertThat("different jobs",
			clone.getJobs(), equalTo(singleton(job)));
		assertThat("different removals",
			clone.getJobRemovals(), equalTo(singleton(removal)));
		assertThat("different trajectory lock",
			clone.getTrajectoryLock(), equalTo(interval(0, 2)));
		assertThat("different job lock",
			clone.getJobLock(), equalTo(interval(1, 2)));
		assertThat("different removal intervals",
			clone.getJobRemovalIntervals(), equalTo(interval(0, 1)));
	}

}
