package de.tu_berlin.kbs.swarmos.st_scheduler.matchers;

import org.hamcrest.Matcher;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryMatchers {
	
	public static Matcher<Geometry> topologicallyEqualTo(Geometry operand) {
		return GeometryIsEqual.isEqual(operand);
	}
	
	public static Matcher<Geometry> isEmpty() {
		return GeometryIsEmpty.isEmpty();
	}
	
	public static Matcher<Geometry> contains(Geometry operand) {
		return GeometryContains.contains(operand);
	}

}
