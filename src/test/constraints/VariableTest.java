package constraints;

import static constraints.RealSets.*;
import static constraints.VariableFactory.*;
import static constraints.VariableFixtures.*;
import static org.junit.Assert.*;

import org.junit.Test;

import util.NameProvider;

public class VariableTest {
	
	@Test
	public void testEmpty() {
		Variable v = emptyVariable();
		
		assertEquals(emptyRealSet(), v.evaluate());
	}
	
	@Test
	public void testReal() {
		Variable v = realVariable();
		
		assertEquals(fullRealSet(), v.evaluate());
	}

	@Test
	public void testSingleton() {
		Variable v = singletonVariable(1.);
		
		assertEquals(new Singleton(1.), v.evaluate());
	}
	
	@Test
	public void testInterval() {
		Variable v = intervalVariable(0., 1.);
		
		assertEquals(new Interval(0., 1.), v.evaluate());
	}
	
	@Test
	public void testSingletonInterval() {
		Variable v = customVariable(
			new Singleton(1.),
			new Interval(0., 2.)
		);
		
		assertEquals(new Singleton(1.), v.evaluate());
	}
	
	@Test
	public void testIntervalInterval() {
		Variable v = customVariable(
			new Interval(0., 2.),
			new Interval(1., 3.)
		);
		
		assertEquals(new Interval(1., 2.), v.evaluate());
	}
	
	@Test
	public void testRelation() {
		Variable v = relativeVariable(intervalVariable(0., 10.), new Interval(-1., 2.));
		
		assertEquals(new Interval(-1., 12.), v.evaluate());
	}
	
	@Test
	public void testMutualDependency() {
		Variable[] vars = mutualDependentVariables();
		Variable v1 = vars[0];
		Variable v2 = vars[1];
		
		assertEquals(new Interval(5., 10.), v1.evaluate());
		assertEquals(new Interval(10., 15.), v2.evaluate());
	}
	
	@Test
	public void testMutualDependencyTriple() {
		Variable a = new Variable();
		Variable b = new Variable();
		Variable c = new Variable();
		NameProvider.setNameFor(a, "A");
		NameProvider.setNameFor(b, "B");
		NameProvider.setNameFor(c, "C");
		
		a.constrain(
				new Interval(0., 5.),
				new Relation(b, new Singleton(-7.)),
				new Relation(c, new Singleton(-2.)));
		b.constrain(
				new Interval(10., 20.),
				new Relation(a, new Singleton(+7.)),
				new Relation(c, new Singleton(+5.)));
		c.constrain(
				new Relation(a, new Singleton(+2.)),
				new Relation(b, new Singleton(-5.)));
		
		a.ready();
		b.ready();
		c.ready();
		
		assertEquals(new Interval(3., 5.), a.evaluate());
		assertEquals(new Interval(10., 12.), b.evaluate());
		assertEquals(new Interval(5., 7.), c.evaluate());
	}
	
	@Test
	public void testMutualDependenyInterval() {
		Variable a = new Variable();
		Variable b = new Variable();
		NameProvider.setNameFor(a, "A");
		NameProvider.setNameFor(b, "B");
		
		a.constrain(
				new Interval(0., 10.),
				new Relation(b, new Interval(-18., -12.)));
		b.constrain(
				new Interval(20., 30.),
				new Relation(a, new Interval(12., 18.)));
		
		a.ready();
		b.ready();

		assertEquals(new Interval(2., 10.), a.evaluate());
		assertEquals(new Interval(20., 28.), b.evaluate());
	}

}
