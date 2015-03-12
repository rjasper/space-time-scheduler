package scheduler;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeConv.*;
import static util.TimeFactory.*;
import static util.UUIDFactory.*;
import jts.geom.immutable.ImmutablePolygon;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ScheduleAlternativeTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private static final ImmutablePolygon WORKER_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double WORKER_SPEED = 1.0;
	
	private static Node workerUnit(String workerId, double x, double y) {
		NodeSpecification spec = new NodeSpecification(
			workerId, WORKER_SHAPE, WORKER_SPEED, immutablePoint(x, y), atSecond(0));
		
		return new Node(spec);
	}
	
	@Test
	public void testBranch() {
		Node w = workerUnit("w1", 0, 0);
		ScheduleAlternative root = new ScheduleAlternative();
		
		Task task = new Task(uuid("task"), w.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		root.addTask(task);
		
		assertThat("root marked as branched",
			root.isBranched(), is(false));
		
		ScheduleAlternative branch = root.branch();
		
		assertThat("root not marked as branched",
			root.isBranched(), is(true));
		assertThat("root not marked as root",
			root.isRootBranch(), is(true));
		assertThat("branched marked as root",
			branch.isRootBranch(), is(false));
		assertThat("branch does not include task",
			branch.getTask(uuid("task")), equalTo(task));
	}
	
	@Test
	public void testBranchSealed() {
		ScheduleAlternative root = new ScheduleAlternative();
		root.seal();
		
		thrown.expect(IllegalStateException.class);
		
		root.branch();
	}
	
	@Test
	public void testBranchMerge() {
		Node w = workerUnit("w1", 0, 0);
		
		ScheduleAlternative root = new ScheduleAlternative();
		ScheduleAlternative branch = root.branch();
		
		Task task = new Task(uuid("task"), w.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		branch.addTask(task);
		branch.merge();
		
		assertThat("branch does not have task",
			root.hasTask(uuid("task")), is(true));
		assertThat("branch does not have correct task",
			root.getTask(uuid("task")), equalTo(task));
	}
	
	@Test
	public void testBranchMergeBranched() {
		ScheduleAlternative root = new ScheduleAlternative();
		ScheduleAlternative branch = root.branch();
		
		root.branch(); // other branch
		
		thrown.expect(IllegalStateException.class);
		
		branch.merge();
	}
	
	@Test
	public void testBranchMultiple() {
		ScheduleAlternative root = new ScheduleAlternative();
		ScheduleAlternative branch1 = root.branch();
		ScheduleAlternative branch2 = root.branch();
		
		branch1.delete();
		branch2.merge(); // no exception
	}
	
	@Test
	public void testBranchDelete() {
		Node w = workerUnit("w1", 0, 0);
		ScheduleAlternative root = new ScheduleAlternative();
		
		Task task = new Task(uuid("task"), w.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		ScheduleAlternative branch = root.branch();

		assertThat("root not marked as branched",
			root.isBranched(), is(true));
		
		branch.addTask(task);
		branch.delete();
		
		root.seal();
		
		assertThat("root marked as branched",
			root.isBranched(), is(false));
		assertThat("branch not invalid",
			branch.isInvalid(), is(true));
		assertThat("root has unexpected task",
			root.hasTask(uuid("task")), is(false));
	}

}
