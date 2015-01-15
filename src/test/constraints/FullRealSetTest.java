package constraints;

import static constraints.RealSets.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class FullRealSetTest {

	@Test
	public void testNeg() {
		assertEquals(fullRealSet(), fullRealSet().neg());
	}

	@Test
	public void testAdd() {
		RealSet other = new Singleton(1.);
		
		assertEquals(fullRealSet(), fullRealSet().add(other));
	}

	@Test
	public void testSub() {
		RealSet other = new Singleton(1.);
		
		assertEquals(fullRealSet(), fullRealSet().sub(other));
	}

	@Test
	public void testIntersect() {
		RealSet other = new Singleton(1.);

		assertEquals(other, fullRealSet().intersect(other));
	}

	@Test
	public void testContains() {
		assertTrue(fullRealSet().contains(0.));
	}
	
	@Test
	public void testEquivalent() {
		assertTrue(fullRealSet().equivalent(fullRealSet()));
		assertFalse(fullRealSet().equivalent(emptyRealSet()));
	}

}
