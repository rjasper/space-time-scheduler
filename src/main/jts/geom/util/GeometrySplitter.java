package jts.geom.util;

import jts.geom.immutable.ImmutableGeometryCollection;
import jts.geom.immutable.ImmutableLineString;
import jts.geom.immutable.ImmutableLinearRing;
import jts.geom.immutable.ImmutableMultiLineString;
import jts.geom.immutable.ImmutableMultiPoint;
import jts.geom.immutable.ImmutableMultiPolygon;
import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public abstract class GeometrySplitter<T> {
	
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
	
	protected T take(Geometry geometry) {
		throw new UnsupportedOperationException("undefined operation");
	}
	
	protected T take(Point point) {
		return take((Geometry) point);
	}
	
	protected T take(LineString lineString) {
		return take((Geometry) lineString);
	}
	
	protected T take(LinearRing linearRing) {
		return take((LineString) linearRing);
	}
	
	protected T take(Polygon polygon) {
		return take((Geometry) polygon);
	}
	
	protected T take(GeometryCollection geometryCollection) {
		return take((Geometry) geometryCollection);
	}
	
	protected T take(MultiPoint multiPoint) {
		return take((GeometryCollection) multiPoint);
	}
	
	protected T take(MultiLineString multiLineString) {
		return take((GeometryCollection) multiLineString);
	}
	
	protected T take(MultiPolygon multiPolygon) {
		return take((GeometryCollection) multiPolygon);
	}
	
	protected T take(ImmutablePoint point) {
		return take((Point) point);
	}
	
	protected T take(ImmutableLineString lineString) {
		return take((LineString) lineString);
	}
	
	protected T take(ImmutableLinearRing linearRing) {
		return take((LinearRing) linearRing);
	}
	
	protected T take(ImmutablePolygon polygon) {
		return take((Polygon) polygon);
	}
	
	protected T take(ImmutableGeometryCollection geometryCollection) {
		return take((GeometryCollection) geometryCollection);
	}
	
	protected T take(ImmutableMultiPoint multiPoint) {
		return take((MultiPoint) multiPoint);
	}
	
	protected T take(ImmutableMultiLineString multiLineString) {
		return take((MultiLineString) multiLineString);
	}
	
	protected T take(ImmutableMultiPolygon multiPolygon) {
		return take((MultiPolygon) multiPolygon);
	}

}
