package de.tu_berlin.mailbox.rjasper.constraints;

import static de.tu_berlin.mailbox.rjasper.constraints.RealSets.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class IntervalTest {

	@Test
	public void testTrueIntervalCreation() {
		try {
			new Interval(0., 1.);
		} catch (IllegalArgumentException e) {
			fail("Could not create true Interval.");
		}
	}
	
	@Test
	public void testSingletonIntervalCreation() {
		try {
			new Interval(0., 0.);
		} catch (IllegalArgumentException e) {
			fail("Could not create singleton Interval.");
		}
	}
	
	@Test
	public void testInvalidInterval() {
		try {
			new Interval(1., 0.);
			fail("Invalid Interval was created.");
		} catch (IllegalArgumentException e) {}
	}

	@Test
	public void testNeg() {
		RealSet interval = new Interval(-1., 2.);
		
		assertEquals(new Interval(-2., 1.), interval.neg());
	}

	@Test
	public void testAdd() {
		RealSet interval = new Interval(0., 10.);
		RealSet offset = new Interval(-2., 3.);
		
		assertEquals(new Interval(-2., 13.), interval.add(offset));
	}
	
	@Test
	public void testSub() {
		RealSet interval = new Interval(1., 5.);
		RealSet offset = new Interval(-1., 2.);
		
		assertEquals(new Interval(-1., 6.), interval.sub(offset));
	}

	@Test
	public void testIntersectTrueIntervalResult() {
		RealSet interval1 = new Interval(0., 2.);
		RealSet interval2 = new Interval(1., 3.);
		
		assertEquals(new Interval(1., 2.), interval1.intersect(interval2));
	}
	
	@Test
	public void testIntersectSingletonResult() {
		RealSet interval1 = new Interval(0., 1.);
		RealSet interval2 = new Interval(1., 2.);
		
		assertEquals(new Singleton(1.), interval1.intersect(interval2));
	}
	
	@Test
	public void testIntersectEmptyRealSetResult() {
		RealSet interval1 = new Interval(0., 1.);
		RealSet interval2 = new Interval(2., 3.);
		
		assertEquals(emptyRealSet(), interval1.intersect(interval2));
	}

	@Test
	public void testContains() {
		RealSet interval = new Interval(0., 1.);
		
		assertTrue(interval.contains(0.));
		assertTrue(interval.contains(.5));
		assertTrue(interval.contains(1.));
		
		assertFalse(interval.contains(2));
	}
	
	@Test
	public void testEquivalentSingleton() {
		RealSet interval = new Interval(1., 1.);
		
		assertTrue(interval.equivalent(new Singleton(1.)));
		assertFalse(interval.equivalent(new Singleton(2.)));
	}
	
	@Test
	public void testEquivalentInterval() {
		RealSet interval = new Interval(0., 1.);
		
		assertTrue(interval.equivalent(new Interval(0., 1.)));
		assertFalse(interval.equivalent(new Singleton(0.)));
	}

}
