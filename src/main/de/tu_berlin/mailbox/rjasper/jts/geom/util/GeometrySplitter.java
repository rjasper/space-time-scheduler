package de.tu_berlin.mailbox.rjasper.jts.geom.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometryCollection;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableLineString;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableLinearRing;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableMultiLineString;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableMultiPoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableMultiPolygon;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;

/**
 * Utility class to easily distinguish the different kinds of geometries. This
 * removes the need of manually distinguishing the type a geometry.
 * 
 * @author Rico
 *
 * @param <T>
 *            the return type of {@link #give(Geometry)} and
 *            {@link #take(Geometry)}.
 */
public abstract class GeometrySplitter<T> {
	
	/**
	 * Gives an arbitrary geometry to the most appropriate take method.
	 * 
	 * @param geometry
	 * @return the object returned by the chosen take method.
	 */
	public final T give(Geometry geometry) {
		if (geometry instanceof Point) {
			if (geometry instanceof ImmutablePoint)
				return take((ImmutablePoint) geometry);
			else
				return take((Point) geometry);
		} else if (geometry instanceof LineString) {
			if (geometry instanceof LinearRing) {
				if (geometry instanceof ImmutableLinearRing)
					return take((ImmutableLinearRing) geometry);
				else
					return take((LinearRing) geometry);
			} else if (geometry instanceof ImmutableLineString) {
				return take((ImmutableLineString) geometry);
			} else {
				return take((LineString) geometry);
			}
		} else if (geometry instanceof Polygon) {
			if (geometry instanceof ImmutablePolygon)
				return take((ImmutablePolygon) geometry);
			else
				return take((Polygon) geometry);
		} else if (geometry instanceof GeometryCollection) {
			if (geometry instanceof MultiPoint) {
				if (geometry instanceof ImmutableMultiPoint)
					return take((ImmutableMultiPoint) geometry);
				else
					return take((MultiPoint) geometry);
			} else if (geometry instanceof MultiLineString) {
				if (geometry instanceof ImmutableMultiLineString)
					return take((ImmutableMultiLineString) geometry);
				else
					return take((MultiLineString) geometry);
			} else if (geometry instanceof MultiPolygon) {
				if (geometry instanceof ImmutableMultiPolygon)
					return take((ImmutableMultiPolygon) geometry);
				else
					return take((MultiPolygon) geometry);
			} else if (geometry instanceof ImmutableGeometryCollection) {
				return take((ImmutableGeometryCollection) geometry);
			} else {
				return take((GeometryCollection) geometry);
			}
		} else {
			return take((Geometry) geometry);
		}
	}
	
	/**
	 * Takes an arbitrary {@code Geometry}.
	 * 
	 * @param geometry
	 * @return an object.
	 */
	protected T take(Geometry geometry) {
		throw new UnsupportedOperationException("undefined operation");
	}

	/**
	 * Takes a {@code Point}.
	 * 
	 * @param point
	 * @return an object.
	 */
	protected T take(Point point) {
		return take((Geometry) point);
	}

	/**
	 * Takes a {@code LineString}.
	 * 
	 * @param lineString
	 * @return an object.
	 */
	protected T take(LineString lineString) {
		return take((Geometry) lineString);
	}

	/**
	 * Takes a {@code LinearRing}.
	 * 
	 * @param linearRing
	 * @return an object.
	 */
	protected T take(LinearRing linearRing) {
		return take((LineString) linearRing);
	}

	/**
	 * Takes a {@code Polygon}.
	 * 
	 * @param polygon
	 * @return an object.
	 */
	protected T take(Polygon polygon) {
		return take((Geometry) polygon);
	}

	/**
	 * Takes a {@code GeometryCollection}.
	 * 
	 * @param geometryCollection
	 * @return an object.
	 */
	protected T take(GeometryCollection geometryCollection) {
		return take((Geometry) geometryCollection);
	}

	/**
	 * Takes a {@code MultiPoint}.
	 * 
	 * @param multiPoint
	 * @return an object.
	 */
	protected T take(MultiPoint multiPoint) {
		return take((GeometryCollection) multiPoint);
	}

	/**
	 * Takes a {@code MultiLineString}.
	 * 
	 * @param multiLineString
	 * @return an object.
	 */
	protected T take(MultiLineString multiLineString) {
		return take((GeometryCollection) multiLineString);
	}

	/**
	 * Takes a {@code MultiPolygon}.
	 * 
	 * @param multiPolygon
	 * @return an object.
	 */
	protected T take(MultiPolygon multiPolygon) {
		return take((GeometryCollection) multiPolygon);
	}

	/**
	 * Takes an {@code ImmutablePoint}.
	 * 
	 * @param point
	 * @return an object.
	 */
	protected T take(ImmutablePoint point) {
		return take((Point) point);
	}

	/**
	 * Takes an {@code ImmutableLineString}.
	 * 
	 * @param lineString
	 * @return an object.
	 */
	protected T take(ImmutableLineString lineString) {
		return take((LineString) lineString);
	}

	/**
	 * Takes an {@code ImmutableLinearRing}.
	 * 
	 * @param linearRing
	 * @return an object.
	 */
	protected T take(ImmutableLinearRing linearRing) {
		return take((LinearRing) linearRing);
	}

	/**
	 * Takes an {@code ImmutablePolygon}.
	 * 
	 * @param polygon
	 * @return an object.
	 */
	protected T take(ImmutablePolygon polygon) {
		return take((Polygon) polygon);
	}

	/**
	 * Takes an {@code ImmutableGeometryCollection}.
	 * 
	 * @param geometryCollection
	 * @return an object.
	 */
	protected T take(ImmutableGeometryCollection geometryCollection) {
		return take((GeometryCollection) geometryCollection);
	}

	/**
	 * Takes an {@code ImmutableMultiPoint}.
	 * 
	 * @param multiPoint
	 * @return an object.
	 */
	protected T take(ImmutableMultiPoint multiPoint) {
		return take((MultiPoint) multiPoint);
	}

	/**
	 * Takes an {@code ImmutableMultiLineString}.
	 * 
	 * @param multiLineString
	 * @return an object.
	 */
	protected T take(ImmutableMultiLineString multiLineString) {
		return take((MultiLineString) multiLineString);
	}

	/**
	 * Takes an {@code ImmutableMultiPolygon}.
	 * 
	 * @param multiPolygon
	 * @return an object.
	 */
	protected T take(ImmutableMultiPolygon multiPolygon) {
		return take((MultiPolygon) multiPolygon);
	}

}
