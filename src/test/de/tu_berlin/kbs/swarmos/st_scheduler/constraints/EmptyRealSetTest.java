package de.tu_berlin.kbs.swarmos.st_scheduler.constraints;

import static de.tu_berlin.kbs.swarmos.st_scheduler.constraints.RealSets.*;
import static org.junit.Assert.*;

import org.junit.Test;

import de.tu_berlin.kbs.swarmos.st_scheduler.constraints.RealSet;
import de.tu_berlin.kbs.swarmos.st_scheduler.constraints.Singleton;

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
