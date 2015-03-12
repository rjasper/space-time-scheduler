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
	
	private static final ImmutablePolygon NODE_SHAPE = immutableBox(
		-0.5, -0.5, 0.5, 0.5);
	
	private static final double NODE_SPEED = 1.0;
	
	private static Node node(String nodeId, double x, double y) {
		NodeSpecification spec = new NodeSpecification(
			nodeId, NODE_SHAPE, NODE_SPEED, immutablePoint(x, y), atSecond(0));
		
		return new Node(spec);
	}
	
	@Test
	public void testBranch() {
		Node w = node("w1", 0, 0);
		ScheduleAlternative root = new ScheduleAlternative();
		
		Job job = new Job(uuid("job"), w.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		root.addJob(job);
		
		assertThat("root marked as branched",
			root.isBranched(), is(false));
		
		ScheduleAlternative branch = root.branch();
		
		assertThat("root not marked as branched",
			root.isBranched(), is(true));
		assertThat("root not marked as root",
			root.isRootBranch(), is(true));
		assertThat("branched marked as root",
			branch.isRootBranch(), is(false));
		assertThat("branch does not include job",
			branch.getJob(uuid("job")), equalTo(job));
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
		Node w = node("w1", 0, 0);
		
		ScheduleAlternative root = new ScheduleAlternative();
		ScheduleAlternative branch = root.branch();
		
		Job job = new Job(uuid("job"), w.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		branch.addJob(job);
		branch.merge();
		
		assertThat("branch does not have job",
			root.hasJob(uuid("job")), is(true));
		assertThat("branch does not have correct job",
			root.getJob(uuid("job")), equalTo(job));
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
		Node w = node("w1", 0, 0);
		ScheduleAlternative root = new ScheduleAlternative();
		
		Job job = new Job(uuid("job"), w.getReference(),
			immutablePoint(0, 0), atSecond(1), secondsToDuration(1));
		
		ScheduleAlternative branch = root.branch();

		assertThat("root not marked as branched",
			root.isBranched(), is(true));
		
		branch.addJob(job);
		branch.delete();
		
		root.seal();
		
		assertThat("root marked as branched",
			root.isBranched(), is(false));
		assertThat("branch not invalid",
			branch.isInvalid(), is(true));
		assertThat("root has unexpected job",
			root.hasJob(uuid("job")), is(false));
	}

}
