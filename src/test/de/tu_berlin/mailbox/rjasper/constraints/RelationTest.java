package de.tu_berlin.mailbox.rjasper.constraints;

import static de.tu_berlin.mailbox.rjasper.constraints.VariableFactory.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class RelationTest {
	
	@Test
	public void testNeg() {
		// effectively [2., 4.]
		RealSet relation = new Relation(intervalVariable(0., 1.), new Interval(2., 3.));
		
		assertEquals(new Interval(-4., -2.), relation.neg());
	}

	@Test
	public void testAdd() {
		// effectively [6, 7]
		RealSet relation = new Relation(intervalVariable(2., 3.), new Singleton(4.));
		RealSet other = new Interval(1., 2.);
		
		assertEquals(new Interval(7., 9.), relation.add(other));
	}

	@Test
	public void testSub() {
		// effectively [6, 7]
		RealSet relation = new Relation(intervalVariable(2., 3.), new Singleton(4.));
		RealSet other = new Interval(1., 2.);
		
		assertEquals(new Interval(4., 6.), relation.sub(other));
	}

	@Test
	public void testIntersect() {
		// effectively [3, 5]
		RealSet relation = new Relation(intervalVariable(1., 3.), new Singleton(2.));
		RealSet other = new Interval(2., 4.);
		
		assertEquals(new Interval(3., 4.), relation.intersect(other));
	}

	@Test
	public void testNormalize() {
		// effectively [2, 6]
		RealSet relation = new Relation(intervalVariable(3., 5.), new Interval(-1., 1.));
		
		assertEquals(new Interval(2., 6.), relation.normalize());
	}

	@Test
	public void testContains() {
		// effectively [-1, 8]
		RealSet relation = new Relation(intervalVariable(1., 5.), new Interval(-2., 3.));
		
		assertTrue(relation.contains(5.));
		assertFalse(relation.contains(-2.));
		assertFalse(relation.contains(9.));
	}

	@Test
	public void testEquivalent() {
		// effectively [-2, 5]
		RealSet relation = new Relation(intervalVariable(-2., 3.), new Interval(0., 2.));
		
		assertTrue(relation.equivalent(new Interval(-2., 5.)));
	}
	
}
