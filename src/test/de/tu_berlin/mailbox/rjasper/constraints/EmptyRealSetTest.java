package de.tu_berlin.mailbox.rjasper.constraints;

import static de.tu_berlin.mailbox.rjasper.constraints.RealSets.emptyRealSet;
import static de.tu_berlin.mailbox.rjasper.constraints.RealSets.fullRealSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
	
	@Test
	public void testEquivalent() {
		assertTrue(emptyRealSet().equivalent(emptyRealSet()));
		assertFalse(emptyRealSet().equivalent(fullRealSet()));
	}

}
