package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static com.vividsolutions.jts.geom.IntersectionMatrix.isTrue;
import static com.vividsolutions.jts.geom.Location.INTERIOR;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.lineString;
import static java.util.Objects.requireNonNull;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometryIterable;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometrySplitter;

/**
 * Checks if two points are visible to each other.
 *
 * @author Rico Jasper
 */
public class VisibilityEdgeChecker {

	private final Geometry forbiddenMap;

	public VisibilityEdgeChecker(Geometry forbiddenMap) {
		this.forbiddenMap = requireNonNull(forbiddenMap, "forbiddenMap");
	}

	/**
	 * Checks if two points have a clear line of sight to each other. Forbidden
	 * regions might block the view.
	 *
	 * @param from from-point
	 * @param to to-point
	 * @return {@code true} if no forbidden region blocks the view
	 */
	public boolean check(Point from, Point to) {
		if (from.equalsTopo(to))
			return !within(from);

		LineString line = lineString(from, to);

		return new GeometryIterable(forbiddenMap, true, false, false).stream()
			.allMatch(new GeometrySplitter<Boolean>() {
				// just to be sure, handle all primitives
				// only polygons block the line of sight
				@Override
				protected Boolean take(Point point) {
					return true;
				}
				@Override
				protected Boolean take(LineString lineString) {
					return true;
				}
				@Override
				protected Boolean take(Polygon polygon) {
					IntersectionMatrix matrix = line.relate(polygon);

					return !isTrue(matrix.get(INTERIOR, INTERIOR));
				}
			}::give);
	}

	private boolean within(Point point) {
		return new GeometryIterable(forbiddenMap, true, false, false).stream()
			.anyMatch(g -> point.within(g));
	}

}
