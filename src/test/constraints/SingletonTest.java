package constraints;

import static constraints.RealSets.emptyRealSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SingletonTest {

	@Test
	public void testNeg() {
		RealSet singleton = new Singleton(1.);
		
		assertEquals(new Singleton(-1.), singleton.neg());
	}

	@Test
	public void testAdd() {
		RealSet singleton = new Singleton(1.);
		RealSet other = new Singleton(2.);
		
		assertEquals(new Singleton(3.), singleton.add(other));
	}

	@Test
	public void testSub() {
		RealSet singleton = new Singleton(1.);
		RealSet other = new Singleton(2.);
		
		assertEquals(new Singleton(-1.), singleton.sub(other));
	}

	@Test
	public void testIntersectSingletonResult() {
		RealSet singleton1 = new Singleton(1.);
		RealSet singleton2 = new Singleton(1.);
		
		assertEquals(new Singleton(1.), singleton1.intersect(singleton2));
	}

	@Test
	public void testIntersectEmptyRealSetResult() {
		RealSet singleton1 = new Singleton(1.);
		RealSet singleton2 = new Singleton(2.);
		
		assertEquals(emptyRealSet(), singleton1.intersect(singleton2));
	}

	@Test
	public void testContains() {
		RealSet singleton = new Singleton(1.);
		
		assertTrue(singleton.contains(1.));
		assertFalse(singleton.contains(0.));
	}
	
	@Test
	public void testEquivalent() {
		RealSet singleton = new Singleton(1.);
		
		assertTrue(singleton.equivalent(new Singleton(1.)));
	}

}
