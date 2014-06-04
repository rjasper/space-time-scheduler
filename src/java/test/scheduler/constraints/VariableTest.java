package scheduler.constraints;

import static org.junit.Assert.*;

import org.junit.Test;

import static scheduler.constraints.VariableFactory.*;
import static scheduler.constraints.VariableFixtures.*;
import static scheduler.constraints.RealSets.emptyRealSet;
import static scheduler.constraints.RealSets.fullRealSet;

public class VariableTest {
	
	@Test
	public void testCompleteEmpty() {
		Variable v = emptyVariable();
		
		assertEquals(emptyRealSet(), v.getDomain());
	}
	
	@Test
	public void testCompleteReal() {
		Variable v = realVariable();
		
		assertEquals(fullRealSet(), v.getDomain());
	} 

	@Test
	public void testCompleteSingleton() {
		Variable v = singletonVariable(1.);
		
		assertEquals(new Singleton(1.), v.getDomain());
	}
	
	@Test
	public void testCompleteInterval() {
		Variable v = intervalVariable(0., 1.);
		
		assertEquals(new Interval(0., 1.), v.getDomain());
	}
	
	@Test
	public void testCompleteSingletonInterval() {
		Variable v = customVariable(
			new Singleton(1.),
			new Interval(0., 2.)
		);
		
		assertEquals(new Singleton(1.), v.getDomain());
	}
	
	@Test
	public void testCompleteIntervalInterval() {
		Variable v = customVariable(
			new Interval(0., 2.),
			new Interval(1., 3.)
		);
		
		assertEquals(new Interval(1., 2.), v.getDomain());
	}
	
	@Test
	public void testCompleteRelation() {
		Variable v = relativeVariable(intervalVariable(0., 10.), new Interval(-1., 2.));
		
		assertEquals(new Interval(-1., 12.), v.getDomain());
	}
	
	@Test
	public void testCompleteMutualDependency() {
		Variable[] vars = mutualDependentVariables();
		Variable v1 = vars[0];
		Variable v2 = vars[1];
		
		v1.complete();
		v2.complete();
		
		assertEquals(new Interval(5., 10.), v1.getDomain());
		assertEquals(new Interval(10., 15.), v2.getDomain());
	}

}
