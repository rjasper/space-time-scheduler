package scheduler.constraints;

import static org.junit.Assert.*;

import org.junit.Test;

import static scheduler.constraints.RealSets.emptyRealSet;

public class EmptyRealSetTest {

	@Test
	public void testNeg() {
		assertEquals(emptyRealSet(), emptyRealSet().neg());
	}

	@Test
	public void testAdd() {
		RealSet other = new Singleton(1.);
		
		assertEquals(emptyRealSet(), emptyRealSet().add(other));
	}

	@Test
	public void testSub() {
		RealSet other = new Singleton(1.);
		
		assertEquals(emptyRealSet(), emptyRealSet().sub(other));
	}

	@Test
	public void testIntersect() {
		RealSet other = new Singleton(1.);

		assertEquals(emptyRealSet(), emptyRealSet().intersect(other));
	}

	@Test
	public void testContains() {
		assertFalse(emptyRealSet().contains(0.));
	}

}
