package de.tu_berlin.kbs.swarmos.st_scheduler.matchers;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryContains extends TypeSafeMatcher<Geometry> {

	@Factory
	public static Matcher<Geometry> contains(Geometry operand) {
		return new GeometryContains(operand);
	}
	
	private final Geometry geometry;

	public GeometryContains(Geometry geometry) {
		this.geometry = geometry;
	}

	@Override
	public void describeTo(Description description) {
		description
			.appendText("contains ")
			.appendValue(geometry);
	}

	@Override
	protected void describeMismatchSafely(Geometry item, Description mismatchDescription) {
		mismatchDescription
			.appendValue(item)
			.appendText(" does not contain ")
			.appendValue(geometry);
	}

	@Override
	protected boolean matchesSafely(Geometry item) {
		return item.contains(geometry);
	}

}
