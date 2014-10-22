package tasks;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.BeforeClass;
import org.junit.Test;

import world.World;
import world.WorldFixtures;

public class SchedulerTest {

	private static EnhancedGeometryBuilder gBuilder = EnhancedGeometryBuilder.getInstance();
	private static WorkerUnitFactory wFact = new WorkerUnitFactory();
	private static SpecificationFactory sFact = SpecificationFactory.getInstance();
	
	@BeforeClass
	public static void setUpBeforeClass() {
		wFact.setShape(gBuilder.polygon(
			-0.5, -0.5,  0.5, -0.5,  0.5, 0.5,  -0.5, 0.5,
			-0.5, -0.5));
	}

	@Test
	public void test() {
		World world = WorldFixtures.twoRooms();
		world.ready();
		
		WorkerUnit w1 = wFact.createWorkerUnit(11., 31.);
		WorkerUnit w2 = wFact.createWorkerUnit(25., 11.);
		
		Collection<WorkerUnit> workers = Arrays.asList(w1, w2);
		
		Specification s1 = sFact.specification( 9., 29., 4., 4., 0, 60, 60);
		Specification s2 = sFact.specification(21., 27., 6., 6., 0, 60, 60);
		Specification s3 = sFact.specification( 9.,  7., 6., 6., 0, 60, 60);
		Specification s4 = sFact.specification(23.,  9., 4., 4., 0, 60, 60);
		
		Scheduler sc = new Scheduler(world, workers);
		
		boolean st1 = sc.schedule(s2);
		boolean st2 = sc.schedule(s3);
		
		assertTrue(st1);
		assertTrue(st2);
	}

}
