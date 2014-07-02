package matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryIsEmpty extends BaseMatcher<Geometry> {

	public GeometryIsEmpty() {}

	@Override
	public boolean matches(Object object) {
		if (object instanceof Geometry) {
			Geometry geometry = (Geometry) object;
			
			return geometry.isEmpty();
		} else {
			return false;
		}
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("EMPTY");
	}
	
	@Factory
	public static Matcher<Geometry> isEmpty() {
		return new GeometryIsEmpty();
	}

}
