package de.tu_berlin.mailbox.rjasper.constraints;

import static de.tu_berlin.mailbox.rjasper.constraints.RealSets.add;
import static de.tu_berlin.mailbox.rjasper.constraints.RealSets.emptyRealSet;
import static de.tu_berlin.mailbox.rjasper.constraints.RealSets.intersect;
import static de.tu_berlin.mailbox.rjasper.constraints.VariableFactory.intervalVariable;
import static de.tu_berlin.mailbox.rjasper.constraints.VariableFactory.singletonVariable;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RealSetsTest {
	
	@Test
	public void testIntersectSingletonSingletonEmpty() {
		Singleton s1 = new Singleton(2.);
		Singleton s2 = new Singleton(3.);
		
		assertEquals(emptyRealSet(), intersect(s1, s2));
	}

	@Test
	public void testIntersectSingletonSingletonSingleton() {
		Singleton s1 = new Singleton(2.);
		Singleton s2 = new Singleton(2.);

		assertEquals(new Singleton(2.), intersect(s1, s2));
	}

	@Test
	public void testIntersectSingletonIntervalEmpty() {
		Singleton s1 = new Singleton(2.);
		Interval s2 = new Interval(0., 1.);

		assertEquals(emptyRealSet(), intersect(s1, s2));
	}

	@Test
	public void testIntersectSingletonIntervalSingleton() {
		Singleton s1 = new Singleton(2.);
		Interval s2 = new Interval(1., 3.);

		assertEquals(new Singleton(2.), intersect(s1, s2));
	}
	
	@Test
	public void testIntersectSingletonRelation() {
		Singleton s1 = new Singleton(5.);
		// effectively s2 = [0, 10]
		Relation s2 = new Relation(intervalVariable(0., 10.), new Singleton(0.));
		
		assertEquals(new Singleton(5.), intersect(s2, s1));
	}

	@Test
	public void testIntersectIntervalIntervalEmpty() {
		Interval s1 = new Interval(0., 1.);
		Interval s2 = new Interval(2., 3.);
		
		assertEquals(emptyRealSet(), intersect(s1, s2));
	}

	@Test
	public void testIntersectIntervalIntervalSingleton() {
		Interval s1 = new Interval(0., 1.);
		Interval s2 = new Interval(1., 2.);

		assertEquals(new Singleton(1.), intersect(s1, s2));
	}

	@Test
	public void testIntersectIntervalRelation() {
		Interval s1 = new Interval(4., 6.);
		// effectively s2 = [5.5, 7.5]
		Relation s2 = new Relation(intervalVariable(5., 7.), new Singleton(.5));
		
		assertEquals(new Interval(5.5, 6.), intersect(s2, s1));
	}
	
	@Test
	public void testIntersectRelationRelation() {
		// effectively s1 = [1, 3] and s2 = [3, 5]
		Relation s1 = new Relation(intervalVariable(0., 2.), new Singleton(1.));
		Relation s2 = new Relation(intervalVariable(4., 6.), new Singleton(-1.));
		
		assertEquals(new Singleton(3.), intersect(s1, s2));
	}

	@Test
	public void testAddSingletonSingleton() {
		Singleton s1 = new Singleton(1.);
		Singleton s2 = new Singleton(2.);
		
		assertEquals(new Singleton(3.), add(s1, s2));
	}
	
	@Test
	public void testAddSingletonInterval() {
		Singleton s1 = new Singleton(1.);
		Interval s2 = new Interval(1., 2.);
		
		assertEquals(new Interval(2., 3.), add(s1, s2));
	}
	
	@Test
	public void testAddSingletonRelation() {
		Singleton s1 = new Singleton(2.);
		// effectively s2 = [2, 4]
		Relation s2 = new Relation(intervalVariable(3., 5.), new Singleton(-1.));
		
		assertEquals(new Interval(4., 6.), add(s2, s1));
	}

	@Test
	public void testAddIntervalInterval() {
		Interval s1 = new Interval(1., 2.);
		Interval s2 = new Interval(2., 4.);
		
		assertEquals(new Interval(3., 6.), add(s1, s2));
	}

	@Test
	public void testAddIntervalRelation() {
		Interval s1 = new Interval(2., 3.);
		// effectively s2 = {5}
		Relation s2 = new Relation(singletonVariable(0.), new Singleton(5.));
		
		assertEquals(new Interval(7., 8.), add(s2, s1));
	}
	
	@Test
	public void testAddRelationRelation() {
		// effectively s1 = [1, 2] and s2 = {5} 
		Relation s1 = new Relation(intervalVariable(0., 1.), new Singleton(1.));
		Relation s2 = new Relation(singletonVariable(2.), new Singleton(3.));
		
		assertEquals(new Interval(6., 7.), add(s1, s2));
	}

}
