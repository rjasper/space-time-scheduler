package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.JobMatchers.satisfies;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToDuration;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToTime;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.atSecond;
import static de.tu_berlin.mailbox.rjasper.util.UUIDFactory.uuid;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Test;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.RadiusBasedWorldPerspectiveCache;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.StraightEdgePathfinder;

public class SingularJobSchedulerTest {

	private static final ImmutablePolygon NODE_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);

	private static final double NODE_SPEED = 1.0;

	private static NodeSpecification nodeSpec(String nodeId, double x, double y) {
		return new NodeSpecification(
			nodeId, NODE_SHAPE, NODE_SPEED, immutablePoint(x, y), atSecond(0));
	}

	private static JobSpecification jobSpec(String jobIdSeed, double x, double y, double t, double d) {
		UUID jobId = uuid(jobIdSeed);
		ImmutablePoint location = immutablePoint(x, y);
		LocalDateTime startTime = secondsToTime(t, atSecond(0));
		Duration duration = secondsToDuration(d);

		return new JobSpecification(jobId, location, startTime, startTime, duration);
	}

	@Test
	public void testAchronologicalScheduling() {
		SingularJobScheduler sc = new SingularJobScheduler();
		
		World world = new World();
		RadiusBasedWorldPerspectiveCache perspectiveCache =
			new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);
		
		Node node = new Node(nodeSpec("node", 0, 0));
		Schedule schedule = new Schedule();
		schedule.addNode(node);
		
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		sc.setWorld(world);
		sc.setPerspectiveCache(perspectiveCache);
		sc.setFrozenHorizonTime(Scheduler.BEGIN_OF_TIME);
		sc.setSchedule(schedule);
		sc.setAlternative(alternative);
		sc.setMaxLocationPicks(1);
		
		boolean status;
		
		JobSpecification js0 = jobSpec("job#0", 1, 0, 20, 1);
		sc.setSpecification(js0);
		status = sc.schedule();
		
		assertThat("unable to schedule job 0",
			status, is(true));

		JobSpecification js1 = jobSpec("job#1", 2, 0, 10, 1);
		sc.setSpecification(js1);
		sc.setSpecification(js1);
		status = sc.schedule();
		
		assertThat("unable to schedule job 1",
			status, is(true));
		
		alternative.seal();
		schedule.addAlternative(alternative);
		schedule.integrate(alternative);
		
		assertThat(schedule.getJob(uuid("job#0")), satisfies(js0));
		assertThat(schedule.getJob(uuid("job#1")), satisfies(js1));
	}

}
