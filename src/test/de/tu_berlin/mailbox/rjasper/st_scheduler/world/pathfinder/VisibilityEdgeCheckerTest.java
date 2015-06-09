package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;

public class VisibilityEdgeCheckerTest {

	// TODO implement more tests

	@Test
	public void testSamePointGeometryCollection() {
		Geometry forbiddenMap = geometryCollection(box(-1, -1, 1, 1));

		VisibilityEdgeChecker checker = new VisibilityEdgeChecker(forbiddenMap);

		boolean res = checker.check(point(0, 0), point(0, 0));

		assertThat("points are visible",
			res, is(false));
	}

}
