package de.tu_berlin.kbs.swarmos.st_scheduler.scheduler;

import static de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeConv.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeFactory.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.UUIDFactory.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.world.factories.TrajectoryFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Job;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Node;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.NodeSpecification;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.Schedule;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.ScheduleAlternative;
import de.tu_berlin.kbs.swarmos.st_scheduler.scheduler.util.SimpleIntervalSet;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory;

public class ScheduleTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private static final ImmutablePolygon NODE_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double NODE_SPEED = 1.0;
	
	private static Node node(String nodeId, double x, double y) {
		NodeSpecification spec = new NodeSpecification(
			nodeId, NODE_SHAPE, NODE_SPEED, immutablePoint(x, y), atSecond(0));
		
		return new Node(spec);
	}

	private static void scheduleJob(Schedule schedule, Job job) {
		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addJob(job);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa);
	}
	
	private static SimpleIntervalSet<LocalDateTime> intervalSet(LocalDateTime from, LocalDateTime to) {
		return new SimpleIntervalSet<LocalDateTime>().add(from, to);
	}
	
	@Test
	public void testJobLock() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Job job = new Job(uuid("job"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		sa.addJob(job);
		sa.seal();
		
		schedule.addAlternative(sa);
		
		assertThat(schedule.getTrajectoryLock(w),
			equalTo(intervalSet(atSecond(1), atSecond(2))));
	}
	
	@Test
	public void testTrajectoryLock() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Trajectory traj = trajectory(0, 0, 0, 0, 0, 1);
		
		sa.updateTrajectory(w, traj);
		sa.seal();
		
		schedule.addAlternative(sa);
		
		assertThat(schedule.getTrajectoryLock(w),
			equalTo(intervalSet(atSecond(0), atSecond(1))));
	}
	
	@Test
	public void testJobRemovalLock() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		Job job = new Job(uuid("job"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, job);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addJobRemoval(job);
		sa.seal();
		
		schedule.addAlternative(sa);
		
		Iterator<Job> removalsLock = schedule.getJobRemovalLock(w).iterator();
		
		assertThat(removalsLock.next(), is(job));
		assertThat(removalsLock.hasNext(), is(false));
	}

	@Test
	public void testUpdatedTrajectoryOriginLocationViolation() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		// set job
		
		Job job = new Job(uuid("job"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, job);
	
		// add new trajectory
		Trajectory traj = trajectory(
			0, 0,
			0, 0,
			0, 3);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(w, traj);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}

	@Test
	public void testJobLockViolation() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		ScheduleAlternative sa1 = new ScheduleAlternative();
		ScheduleAlternative sa2 = new ScheduleAlternative();
		
		Job t1 = new Job(uuid("t1"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), w.getReference(), immutablePoint(10, 10), atSecond(1), secondsToDuration(1));
		
		sa1.addJob(t1);
		sa2.addJob(t2);
		
		sa1.seal();
		sa2.seal();
		
		schedule.addAlternative(sa1);

		thrown.expect(IllegalArgumentException.class);
		schedule.addAlternative(sa2);
	}
	
	@Test
	public void testTrajectoryLockViolation() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		ScheduleAlternative sa1 = new ScheduleAlternative();
		ScheduleAlternative sa2 = new ScheduleAlternative();
		
		Trajectory traj1 = trajectory(0, 0, 0, 0, 0, 1);
		Trajectory traj2 = trajectory(0, 0, 1, 1, 0, 1);
		
		sa1.updateTrajectory(w, traj1);
		sa2.updateTrajectory(w, traj2);
		
		sa1.seal();
		sa2.seal();
		
		schedule.addAlternative(sa1);
		
		thrown.expect(IllegalArgumentException.class);
		schedule.addAlternative(sa2);
	}

	@Test
	public void testContinuousTrajectoryViolation() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Trajectory traj1 = trajectory(1, 1, 1, 1, 1, 2);
		
		sa.updateTrajectory(w, traj1);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	public void testJobLocationViolation() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Job job = new Job(uuid("job"), w.getReference(), immutablePoint(1, 1), atSecond(1), secondsToDuration(1));

		sa.addJob(job);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testUpdatedTrajectoryAtJobPositive() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();

		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		Job job = new Job(uuid("job"), w.getReference(), immutablePoint(1, 1), atSecond(1), secondsToDuration(1));

		sa.updateTrajectory(w, traj);
		sa.addJob(job);
		sa.seal();
		
		schedule.addAlternative(sa); // no exception
	}
	
	@Test
	public void testUpdateTrajectoryAtJobNegative() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		ScheduleAlternative sa = new ScheduleAlternative();

		Trajectory traj = trajectory(
			0, 0,
			0, 0,
			0, 3);
		Job job = new Job(uuid("job"), w.getReference(), immutablePoint(1, 1), atSecond(1), secondsToDuration(1));

		sa.updateTrajectory(w, traj);
		sa.addJob(job);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testJobRemoval() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		// set up job to remove
		
		Job job = new Job(uuid("job"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, job);
		
		// remove job

		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addJobRemoval(job);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa);
		
		assertThat(w.hasJob(job), is(false));
	}
	
	@Test
	public void testJobRemovalUnknown() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		// set up job to remove
		
		Job job = new Job(uuid("job"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		// remove job

		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addJobRemoval(job);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testJobRemovalLockViolation() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		// set up job to remove
		
		Job job = new Job(uuid("job"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, job);
		
		// remove job

		ScheduleAlternative sa1 = new ScheduleAlternative();
		ScheduleAlternative sa2 = new ScheduleAlternative();
		
		sa1.addJobRemoval(job);
		sa2.addJobRemoval(job);
		sa1.seal();
		sa2.seal();
		
		schedule.addAlternative(sa1);

		thrown.expect(IllegalArgumentException.class);
		schedule.addAlternative(sa2);
	}

	@Test
	public void testUpdateTrajectoryOfRemoval() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		// set up job to remove
		
		Job job = new Job(uuid("job"), w.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, job);
		
		// plan trajectory and removal		
		Trajectory traj = trajectory(
			0, 0,
			0, 0,
			0, 3);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(w, traj);
		sa.addJobRemoval(job);
		sa.seal();
		
		schedule.addAlternative(sa); // no exception
	}
	
	@Test
	public void testIntegrate() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		// set up job to remove
		
		Job jobToRemove = new Job(uuid("old job"), w.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, jobToRemove);
		
		// plan trajectory, job and removal
		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		
		Job jobToSchedule = new Job(uuid("new job"), w.getReference(),
			immutablePoint(1, 1), atSecond(1), secondsToDuration(1));
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(w, traj);
		sa.addJob(jobToSchedule);
		sa.addJobRemoval(jobToRemove);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa);
		
		assertThat("job not removed",
			w.hasJob(jobToRemove), is(false));
		assertThat("job not scheduled",
			w.hasJob(jobToSchedule), is(true));
		
		// removed locks
		assertThat("trajectory lock not removed",
			schedule.getTrajectoryLock(w)
			.intersects(atSecond(0), atSecond(3)),
			is(false));
		assertThat("job removal lock not removed",
			schedule.getJobRemovalLock(w)
			.contains(jobToRemove),
			is(false));
		
		// applied changes
		Iterator<Trajectory> trajs = w.getTrajectories(atSecond(0), atSecond(3))
			.iterator();
		assertThat("trajectory update not applied",
			trajs.next(), equalTo(traj));
		assertThat("unexpected trajectory",
			trajs.hasNext(), is(false));
	}
	
	@Test
	public void testEliminate() {
		Node w = node("w", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w);
		
		// set up job to remove
		
		Job jobToRemove = new Job(uuid("old job"), w.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, jobToRemove);
		
		// plan trajectory, job and removal
		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		
		Job jobToSchedule = new Job(uuid("new job"), w.getReference(),
			immutablePoint(1, 1), atSecond(1), secondsToDuration(1));
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(w, traj);
		sa.addJob(jobToSchedule);
		sa.addJobRemoval(jobToRemove);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.eliminate(sa);
		
		assertThat("job removed when shouldn't have been",
			w.hasJob(jobToRemove), is(true));
		assertThat("job scheduled when shouldn't have been",
			w.hasJob(jobToSchedule), is(false));
		
		// removed locks
		assertThat("trajectory lock not removed",
			schedule.getTrajectoryLock(w)
			.intersects(atSecond(0), atSecond(3)),
			is(false));
		assertThat("job removal lock not removed",
			schedule.getJobRemovalLock(w)
			.contains(jobToRemove),
			is(false));
		
		// didn't apply changes
		Iterator<Trajectory> trajs = w.getTrajectories(atSecond(0), atSecond(3))
			.iterator();
		assertThat("trajectory update applied when shouldn't have been",
			trajs.next(), not(equalTo(traj)));
		assertThat("unexpected trajectory",
			trajs.hasNext(), is(false));
	}
	
	@Test
	public void testIntegrateEliminatePartial() {
		Node w1 = node("w1", 0, 0);
		Node w2 = node("w2", 10, 10);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w1);
		schedule.addNode(w2);

		Job t1 = new Job(uuid("t1"), w1.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("t2"), w2.getReference(),
			immutablePoint(10, 10), atSecond(1), secondsToDuration(1));

		ScheduleAlternative sa = new ScheduleAlternative();
		sa.addJob(t1);
		sa.addJob(t2);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa, w2);
		schedule.eliminate(sa, w1);
		
		assertThat("transaction was not removed",
			schedule.hasAlternative(sa), is(false));
		assertThat("t1 was not eliminated",
			w1.hasJob(t1), is(false));
		assertThat("t2 was not integrated",
			w2.hasJob(t2), is(true));
	}
	
	@Test
	public void testDuplicateJobId1() {
		Node w1 = node("w1", 0, 0);
		Node w2 = node("w2", 10, 10);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w1);
		schedule.addNode(w2);

		Job t1 = new Job(uuid("duplicate"), w1.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job t2 = new Job(uuid("duplicate"), w2.getReference(),
			immutablePoint(10, 10), atSecond(1), secondsToDuration(1));

		ScheduleAlternative sa1 = new ScheduleAlternative();
		sa1.addJob(t1);
		sa1.seal();
		schedule.addAlternative(sa1);

		ScheduleAlternative sa2 = new ScheduleAlternative();
		sa2.addJob(t2);
		sa2.seal();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("duplicate job id");
		
		schedule.addAlternative(sa2);
	}
	
	@Test
	public void testDuplicateJobId2() {
		Node w1 = node("w1", 0, 0);
		Node w2 = node("w2", 10, 10);
		
		Schedule schedule = new Schedule();
		schedule.addNode(w1);
		schedule.addNode(w2);
		
		scheduleJob(schedule, new Job(uuid("duplicate"), w1.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1)));

		Job job = new Job(uuid("duplicate"), w2.getReference(),
			immutablePoint(10, 10), atSecond(1), secondsToDuration(1));

		ScheduleAlternative sa2 = new ScheduleAlternative();
		sa2.addJob(job);
		sa2.seal();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("duplicate job id");
		
		schedule.addAlternative(sa2);
	}
	
}
