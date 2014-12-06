package matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryIsEqual extends BaseMatcher<Geometry> {
	
	private Geometry expected;
	
	public GeometryIsEqual(Geometry expected) {
		this.expected = expected;
	}

	@Override
	public boolean matches(Object object) {
		if (object instanceof Geometry) {
			Geometry geometry = (Geometry) object;
			
			return expected.equals(geometry);
		} else {
			return false;
		}
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(expected);
	}
	
	@Factory
	public static Matcher<Geometry> isEqual(Geometry operand) {
		return new GeometryIsEqual(operand);
	}

}
