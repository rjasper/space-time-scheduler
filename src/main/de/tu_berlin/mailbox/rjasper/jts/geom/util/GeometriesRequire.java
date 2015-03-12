package de.tu_berlin.mailbox.rjasper.jts.geom.util;

import java.util.Objects;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Provides checks for geometries to ensure preconditions like validity,
 * simplicity or dimensionality.
 * 
 * @author Rico Jasper
 */
public class GeometriesRequire {

	/**
	 * Checks if the given geometry is a non-empty, valid, and simple. The
	 * dimension of the coordinate sequence must be at least 2.
	 * 
	 * @param geometry
	 * @param name
	 *            of the geometry
	 * @return the geometry
	 * @throws IllegalArgumentException
	 *             if the geometry is empty, invalid, or not 2D
	 */
	public static <G extends Geometry> G requireValidSimple2DGeometry(G geometry, String name) {
		Objects.requireNonNull(geometry, "geometry");

		if (geometry.isEmpty())
			throw new IllegalArgumentException(name + " is empty");
		if (!geometry.isValid())
			throw new IllegalArgumentException(name + " is invalid");
		if (!geometry.isSimple())
			throw new IllegalArgumentException(name + " is non-simple");
		
		require2DGeometry(geometry, name);
		
		return geometry;
	}
	
	/**
	 * Checks if the given geometry is 2D. The dimension of the
	 * coordinate sequence must be at least 2.
	 * 
	 * @param geometry
	 * @param name
	 *            of the geometry
	 * @return the geometry
	 * @throws IllegalArgumentException
	 *             if the geometry is not 2D
	 */
	public static <G extends Geometry> G require2DGeometry(G geometry, String name) {
		GeometrySplitter<CoordinateSequence> splitter = new GeometrySplitter<CoordinateSequence>() {
			@Override
			protected CoordinateSequence take(Point point) {
				return point.getCoordinateSequence();
			}
			@Override
			protected CoordinateSequence take(LineString lineString) {
				return lineString.getCoordinateSequence();
			}
		};
		
		geometry.apply(new GeometryComponentFilter() {
			@Override
			public void filter(Geometry g) {
				// only the geometry's components are of interest
				if (g instanceof Polygon || g instanceof GeometryCollection)
					return;
				if (splitter.give(g).getDimension() < 2)
					throw new IllegalArgumentException(name + " is not 2D");
			}
		});
		
		return geometry;
	}
	
	/**
	 * Checks if the given point is a valid 2D point. The dimension of the
	 * coordinate sequence must be at least 2.
	 * 
	 * @param point
	 * @param name
	 *            of the point
	 * @return the point
	 * @throws IllegalArgumentException
	 *             if the point is empty, invalid, or not 2D
	 */
	public static <P extends Point> P requireValid2DPoint(P point, String name) {
		Objects.requireNonNull(point, name);
		
		if (point.isEmpty())
			throw new IllegalArgumentException(name + " is empty");
		if (!point.isValid())
			throw new IllegalArgumentException(name + " is invalid");
		if (point.getCoordinateSequence().getDimension() < 2)
			throw new IllegalArgumentException(name + " is not 2D");
		
		return point;
	}

	/**
	 * Checks if the given polygon is a valid simple 2D point. The dimension of
	 * the shell and holes must be at least 2.
	 * 
	 * @param polygon
	 * @param name
	 *            of the polygon
	 * @return the polygon
	 * @throws IllegalArgumentException
	 *             if the polygon is empty, invalid, non-simple, or not 2D
	 */
	public static <P extends Polygon> P requireValidSimple2DPolygon(P polygon, String name) {
		Objects.requireNonNull(polygon, name);
		
		if (polygon.isEmpty())
			throw new IllegalArgumentException(name + " is empty");
		if (!polygon.isValid())
			throw new IllegalArgumentException(name + " is invalid");
		if (!polygon.isSimple())
			throw new IllegalArgumentException(name + " is non-simple");
		
		require2DLineString(polygon.getExteriorRing(), name);
		
		int n = polygon.getNumInteriorRing();
		for (int i = 0; i < n; ++i)
			require2DLineString(polygon.getInteriorRingN(i), name);
		
		return polygon;
	}
	
	/**
	 * Checks if the given line string is 2D. The dimension of the
	 * coordinate sequence must be at least 2.
	 * 
	 * @param lineString
	 * @param name
	 *            of the line string
	 * @return the line string.
	 * @throws IllegalArgumentException
	 *             if the line string is not 2D.
	 */
	public static <L extends LineString> L require2DLineString(L lineString, String name) {
		Objects.requireNonNull(lineString, "lineString");
	
		if (lineString.getCoordinateSequence().getDimension() < 2)
			throw new IllegalArgumentException(name + " is not 2D");
		
		return lineString;
	}

	/**
	 * Checks if the given line string is valid, simple and 2D. The dimension of
	 * the coordinate sequence must be at least 2.
	 * 
	 * @param lineString
	 * @param name
	 * @return the line string.
	 * @throws IllegalArgumentException
	 *             if the line string is empty, invalid, non-simple, or not 2D.
	 */
	public static <L extends LineString> L requireValidSimple2DLineString(L lineString, String name) {
		Objects.requireNonNull(lineString, "lineString");

		if (lineString.isEmpty())
			throw new IllegalArgumentException(name + " is empty");
		if (!lineString.isValid())
			throw new IllegalArgumentException(name + " is invalid");
		if (!lineString.isSimple())
			throw new IllegalArgumentException(name + " is non-simple");
		if (lineString.getCoordinateSequence().getDimension() < 2)
			throw new IllegalArgumentException(name + " is not 2D");
		
		return lineString;
	}

}
