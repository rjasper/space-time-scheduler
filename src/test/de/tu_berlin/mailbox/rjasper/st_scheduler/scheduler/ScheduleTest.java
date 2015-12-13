package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.trajectory;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToDuration;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.atSecond;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.uuid;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.SimpleIntervalSet;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;

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
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Job job = new Job(uuid("job"), n.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		sa.addJob(job);
		sa.seal();
		
		schedule.addAlternative(sa);
		
		assertThat(n.getTrajectoryLock(),
			equalTo(intervalSet(atSecond(1), atSecond(2))));
	}
	
	@Test
	public void testTrajectoryLock() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Trajectory traj = trajectory(0, 0, 0, 0, 0, 1);
		
		sa.updateTrajectory(n, traj);
		sa.seal();
		
		schedule.addAlternative(sa);
		
		assertThat(n.getTrajectoryLock(),
			equalTo(intervalSet(atSecond(0), atSecond(1))));
	}
	
	@Test
	public void testJobRemovalLock() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		Job job = new Job(uuid("job"), n.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, job);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addJobRemoval(job);
		sa.seal();
		
		schedule.addAlternative(sa);
		
		Iterator<Job> removalsLock = n.getJobRemovalLock().iterator();
		
		assertThat(removalsLock.next(), is(job));
		assertThat(removalsLock.hasNext(), is(false));
	}

	@Test
	public void testUpdatedTrajectoryOriginLocationViolation() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		// set job
		
		Job job = new Job(uuid("job"), n.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, job);
	
		// add new trajectory
		Trajectory traj = trajectory(
			0, 0,
			0, 0,
			0, 3);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(n, traj);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}

	@Test
	public void testJobLockViolation() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		ScheduleAlternative sa1 = new ScheduleAlternative();
		ScheduleAlternative sa2 = new ScheduleAlternative();
		
		Job j1 = new Job(uuid("j1"), n.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job j2 = new Job(uuid("j2"), n.getReference(), immutablePoint(10, 10), atSecond(1), secondsToDuration(1));
		
		sa1.addJob(j1);
		sa2.addJob(j2);
		
		sa1.seal();
		sa2.seal();
		
		schedule.addAlternative(sa1);

		thrown.expect(IllegalArgumentException.class);
		schedule.addAlternative(sa2);
	}
	
	@Test
	public void testTrajectoryLockViolation() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		ScheduleAlternative sa1 = new ScheduleAlternative();
		ScheduleAlternative sa2 = new ScheduleAlternative();
		
		Trajectory traj1 = trajectory(0, 0, 0, 0, 0, 1);
		Trajectory traj2 = trajectory(0, 0, 1, 1, 0, 1);
		
		sa1.updateTrajectory(n, traj1);
		sa2.updateTrajectory(n, traj2);
		
		sa1.seal();
		sa2.seal();
		
		schedule.addAlternative(sa1);
		
		thrown.expect(IllegalArgumentException.class);
		schedule.addAlternative(sa2);
	}

	@Test
	public void testContinuousTrajectoryViolation() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Trajectory traj1 = trajectory(1, 1, 1, 1, 1, 2);
		
		sa.updateTrajectory(n, traj1);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	public void testJobLocationViolation() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		
		Job job = new Job(uuid("job"), n.getReference(), immutablePoint(1, 1), atSecond(1), secondsToDuration(1));

		sa.addJob(job);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testUpdatedTrajectoryAtJobPositive() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		ScheduleAlternative sa = new ScheduleAlternative();

		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		Job job = new Job(uuid("job"), n.getReference(), immutablePoint(1, 1), atSecond(1), secondsToDuration(1));

		sa.updateTrajectory(n, traj);
		sa.addJob(job);
		sa.seal();
		
		schedule.addAlternative(sa); // no exception
	}
	
	@Test
	public void testUpdateTrajectoryAtJobNegative() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		ScheduleAlternative sa = new ScheduleAlternative();

		Trajectory traj = trajectory(
			0, 0,
			0, 0,
			0, 3);
		Job job = new Job(uuid("job"), n.getReference(), immutablePoint(1, 1), atSecond(1), secondsToDuration(1));

		sa.updateTrajectory(n, traj);
		sa.addJob(job);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testJobRemoval() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		// set up job to remove
		
		Job job = new Job(uuid("job"), n.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, job);
		
		// remove job

		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addJobRemoval(job);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa);
		
		assertThat(n.hasJob(job), is(false));
	}
	
	@Test
	public void testJobRemovalUnknown() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		// set up job to remove
		
		Job job = new Job(uuid("job"), n.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		// remove job

		ScheduleAlternative sa = new ScheduleAlternative();
		
		sa.addJobRemoval(job);
		sa.seal();
		
		thrown.expect(IllegalArgumentException.class);
		
		schedule.addAlternative(sa);
	}
	
	@Test
	public void testJobRemovalLockViolation() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		// set up job to remove
		
		Job job = new Job(uuid("job"), n.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
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
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		// set up job to remove
		
		Job job = new Job(uuid("job"), n.getReference(), immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, job);
		
		// plan trajectory and removal		
		Trajectory traj = trajectory(
			0, 0,
			0, 0,
			0, 3);
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(n, traj);
		sa.addJobRemoval(job);
		sa.seal();
		
		schedule.addAlternative(sa); // no exception
	}
	
	@Test
	public void testIntegrate() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		// set up job to remove
		
		Job jobToRemove = new Job(uuid("old job"), n.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, jobToRemove);
		
		// plan trajectory, job and removal
		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		
		Job jobToSchedule = new Job(uuid("new job"), n.getReference(),
			immutablePoint(1, 1), atSecond(1), secondsToDuration(1));
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(n, traj);
		sa.addJob(jobToSchedule);
		sa.addJobRemoval(jobToRemove);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa);
		
		assertThat("job not removed",
			n.hasJob(jobToRemove), is(false));
		assertThat("job not scheduled",
			n.hasJob(jobToSchedule), is(true));
		
		// removed locks
		assertThat("trajectory lock not removed",
			n.getTrajectoryLock().intersects(atSecond(0), atSecond(3)),
			is(false));
		assertThat("job removal lock not removed",
			n.getJobRemovalLock().contains(jobToRemove),
			is(false));
		
		// applied changes
		Iterator<Trajectory> trajs = n.getTrajectories(atSecond(0), atSecond(3))
			.iterator();
		assertThat("trajectory update not applied",
			trajs.next(), equalTo(traj));
		assertThat("unexpected trajectory",
			trajs.hasNext(), is(false));
	}
	
	@Test
	public void testEliminate() {
		Node n = node("n", 0, 0);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n);
		
		// set up job to remove
		
		Job jobToRemove = new Job(uuid("old job"), n.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		scheduleJob(schedule, jobToRemove);
		
		// plan trajectory, job and removal
		Trajectory traj = trajectory(
			0, 1, 1, 0,
			0, 1, 1, 0,
			0, 1, 2, 3);
		
		Job jobToSchedule = new Job(uuid("new job"), n.getReference(),
			immutablePoint(1, 1), atSecond(1), secondsToDuration(1));
		
		ScheduleAlternative sa = new ScheduleAlternative();
		sa.updateTrajectory(n, traj);
		sa.addJob(jobToSchedule);
		sa.addJobRemoval(jobToRemove);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.eliminate(sa);
		
		assertThat("job removed when shouldn't have been",
			n.hasJob(jobToRemove), is(true));
		assertThat("job scheduled when shouldn't have been",
			n.hasJob(jobToSchedule), is(false));
		
		// removed locks
		assertThat("trajectory lock not removed",
			n.getTrajectoryLock().intersects(atSecond(0), atSecond(3)),
			is(false));
		assertThat("job removal lock not removed",
			n.getJobRemovalLock().contains(jobToRemove),
			is(false));
		
		// didn't apply changes
		Iterator<Trajectory> trajs = n.getTrajectories(atSecond(0), atSecond(3))
			.iterator();
		assertThat("trajectory update applied when shouldn't have been",
			trajs.next(), not(equalTo(traj)));
		assertThat("unexpected trajectory",
			trajs.hasNext(), is(false));
	}
	
	@Test
	public void testIntegrateEliminatePartial() {
		Node n1 = node("n1", 0, 0);
		Node n2 = node("n2", 10, 10);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n1);
		schedule.addNode(n2);

		Job j1 = new Job(uuid("j1"), n1.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job j2 = new Job(uuid("j2"), n2.getReference(),
			immutablePoint(10, 10), atSecond(1), secondsToDuration(1));

		ScheduleAlternative sa = new ScheduleAlternative();
		sa.addJob(j1);
		sa.addJob(j2);
		sa.seal();
		
		schedule.addAlternative(sa);
		schedule.integrate(sa, n2);
		schedule.eliminate(sa, n1);
		
		assertThat("transaction was not removed",
			schedule.hasAlternative(sa), is(false));
		assertThat("j1 was not eliminated",
			n1.hasJob(j1), is(false));
		assertThat("j2 was not integrated",
			n2.hasJob(j2), is(true));
	}
	
	@Test
	public void testDuplicateJobId1() {
		Node n1 = node("n1", 0, 0);
		Node n2 = node("n2", 10, 10);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n1);
		schedule.addNode(n2);

		Job j1 = new Job(uuid("duplicate"), n1.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		Job j2 = new Job(uuid("duplicate"), n2.getReference(),
			immutablePoint(10, 10), atSecond(1), secondsToDuration(1));

		ScheduleAlternative sa1 = new ScheduleAlternative();
		sa1.addJob(j1);
		sa1.seal();
		schedule.addAlternative(sa1);

		ScheduleAlternative sa2 = new ScheduleAlternative();
		sa2.addJob(j2);
		sa2.seal();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("duplicate job id");
		
		schedule.addAlternative(sa2);
	}
	
	@Test
	public void testDuplicateJobId2() {
		Node n1 = node("n1", 0, 0);
		Node n2 = node("n2", 10, 10);
		
		Schedule schedule = new Schedule();
		schedule.addNode(n1);
		schedule.addNode(n2);
		
		scheduleJob(schedule, new Job(uuid("duplicate"), n1.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1)));

		Job job = new Job(uuid("duplicate"), n2.getReference(),
			immutablePoint(10, 10), atSecond(1), secondsToDuration(1));

		ScheduleAlternative sa2 = new ScheduleAlternative();
		sa2.addJob(job);
		sa2.seal();
		
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("duplicate job id");
		
		schedule.addAlternative(sa2);
	}
	
}
