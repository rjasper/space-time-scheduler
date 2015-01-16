package jts.geom.immutable;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Provides functions to convert {@link Geometry geometries} and
 * {@link CoordinateSequence coordinate sequences} into their mutable or
 * immutable version. Optimizes the situation where a object is already of
 * the requested type which does not require to create a new version.
 * 
 * @author Rico
 */
public final class ImmutableGeometries {
	
	private ImmutableGeometries() {}
	
	/**
	 * Determines whether the given geometry is immutable.
	 * 
	 * @param geometry
	 * @return {@code true} if the geometry is immutable.
	 */
	public static boolean isImmutable(Geometry geometry) {
		return geometry instanceof ImmutableGeometry;
	}
	
	/**
	 * Returns an immutable version of the given geometry.
	 * 
	 * @param geometry
	 * @return the immutable geometry.
	 */
	public static Geometry immutable(Geometry geometry) {
		if (geometry == null || geometry instanceof ImmutableGeometry)
			return geometry;
		if (geometry instanceof Point)
			return new ImmutablePoint((Point) geometry);
		if (geometry instanceof LinearRing)
			return new ImmutableLinearRing((LinearRing) geometry);
		if (geometry instanceof LineString)
			return new ImmutableLineString((LineString) geometry);
		if (geometry instanceof Polygon)
			return new ImmutablePolygon((Polygon) geometry);
		if (geometry instanceof MultiPoint)
			return new ImmutableMultiPoint((MultiPoint) geometry);
		if (geometry instanceof MultiLineString)
			return new ImmutableMultiLineString((MultiLineString) geometry);
		if (geometry instanceof MultiPolygon)
			return new ImmutableMultiPolygon((MultiPolygon) geometry);
		if (geometry instanceof GeometryCollection)
			return new ImmutableGeometryCollection((GeometryCollection) geometry);
		
		throw new IllegalArgumentException("unknown geometry");
	}
	
	/**
	 * Returns an immutable version of the given point.
	 * 
	 * @param point
	 * @return the immutable point.
	 */
	public static ImmutablePoint immutable(Point point) {
		if (point == null || point instanceof ImmutablePoint)
			return (ImmutablePoint) point;
		else
			return new ImmutablePoint(point);
	}

	/**
	 * Returns an immutable version of the given linearRing.
	 * 
	 * @param linearRing
	 * @return the immutable linearRing.
	 */
	public static ImmutableLinearRing immutable(LinearRing linearRing) {
		if (linearRing == null || linearRing instanceof ImmutableLinearRing)
			return (ImmutableLinearRing) linearRing;
		else
			return new ImmutableLinearRing(linearRing);
	}

	/**
	 * Returns an immutable version of the given line string.
	 * 
	 * @param lineString
	 * @return the immutable line string.
	 */
	public static ImmutableLineString immutable(LineString lineString) {
		if (lineString == null || lineString instanceof ImmutableLineString)
			return (ImmutableLineString) lineString;
		else
			return new ImmutableLineString(lineString);
	}

	/**
	 * Returns an immutable version of the given polygon.
	 * 
	 * @param polygon
	 * @return the immutable polygon.
	 */
	public static ImmutablePolygon immutable(Polygon polygon) {
		if (polygon == null || polygon instanceof ImmutablePolygon)
			return (ImmutablePolygon) polygon;
		else
			return new ImmutablePolygon(polygon);
	}

	/**
	 * Returns an immutable version of the given geometry collection.
	 * 
	 * @param geometryCollection
	 * @return the immutable geometry collection.
	 */
	public static ImmutableGeometryCollection immutable(GeometryCollection geometryCollection) {
		if (geometryCollection == null || geometryCollection instanceof ImmutableGeometryCollection)
			return (ImmutableGeometryCollection) geometryCollection;
		else
			return new ImmutableGeometryCollection(geometryCollection);
	}

	/**
	 * Returns an immutable version of the given multi point.
	 * 
	 * @param multiPoint
	 * @return the immutable multi point.
	 */
	public static ImmutableMultiPoint immutable(MultiPoint multiPoint) {
		if (multiPoint == null || multiPoint instanceof ImmutableMultiPoint)
			return (ImmutableMultiPoint) multiPoint;
		else
			return new ImmutableMultiPoint(multiPoint);
	}

	/**
	 * Returns an immutable version of the given multi line string.
	 * 
	 * @param multiLineString
	 * @return the immutable multi line string.
	 */
	public static ImmutableMultiLineString immutable(MultiLineString multiLineString) {
		if (multiLineString == null || multiLineString instanceof ImmutableMultiLineString)
			return (ImmutableMultiLineString) multiLineString;
		else
			return new ImmutableMultiLineString(multiLineString);
	}

	/**
	 * Returns an immutable version of the given multi polygon.
	 * 
	 * @param multiPolygon
	 * @return the immutable multi polygon.
	 */
	public static ImmutableMultiPolygon immutable(MultiPolygon multiPolygon) {
		if (multiPolygon == null || multiPolygon instanceof ImmutableMultiPolygon)
			return (ImmutableMultiPolygon) multiPolygon;
		else
			return new ImmutableMultiPolygon(multiPolygon);
	}

	/**
	 * Returns an immutable version of the given array of geometries. Always
	 * creates a new array.
	 * 
	 * @param geometries
	 * @return the immutable geometries.
	 */
	@SuppressWarnings("unchecked") // cast to (T[])
	public static <T extends Geometry> T[] immutable(T[] geometries) {
		if (geometries == null)
			return null;

		Class<?> componentType = geometries.getClass().getComponentType();
		
		return Arrays.stream(geometries)
			.map(ImmutableGeometries::immutable)
			.toArray(n -> (T[]) Array.newInstance(componentType, n));
	}

	/**
	 * Returns an immutable version of the given coordinate sequence.
	 * 
	 * @param sequence
	 * @return the immutable sequence
	 */
	public static ImmutableCoordinateSequence immutable(CoordinateSequence sequence) {
		if (sequence == null || sequence instanceof ImmutableCoordinateSequence)
			return (ImmutableCoordinateSequence) sequence;
		else
			return new ImmutableCoordinateSequence(sequence);
	}
	
	/**
	 * Returns an immutable version of the given coordinate sequence without
	 * making a copy of it.
	 * 
	 * @param coords
	 * @return the immutable sequence
	 */
	static ImmutableCoordinateSequence immutableNoCopy(CoordinateSequence coords) {
		if (coords == null || coords instanceof ImmutableCoordinateSequence)
			return (ImmutableCoordinateSequence) coords;
		else
			return new ImmutableCoordinateSequence(coords, true); // no copy
	}
	
	/**
	 * Returns an immutable version of the given coordinate sequence. If the
	 * given sequence is {@code null} it returns an empty sequence.
	 * 
	 * @param sequence
	 * @return the immutable sequence
	 */
	public static CoordinateSequence immutableNonNull(CoordinateSequence sequence) {
		if (sequence == null)
			return new ImmutableCoordinateSequence();
		else
			return immutable(sequence);
	}
	
	/**
	 * Returns a mutable version of the given geometry.
	 * 
	 * @param geometry
	 * @return the mutable geometry.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Geometry> T mutable(T geometry) {
		if (geometry instanceof ImmutableGeometry) {
			ImmutableGeometry immutable = (ImmutableGeometry) geometry;
			
			return (T) immutable.getMutable();
		} else {
			return geometry;
		}
	}
	
	/**
	 * Returns an mutable version of the given array of geometries.
	 * 
	 * @param points
	 * @return the mutable points.
	 */
	public static Geometry[] mutable(Geometry[] geometries) {
		return mutable(geometries, Geometry.class);
	}

	/**
	 * Returns an mutable version of the given array of points.
	 * 
	 * @param points
	 * @return the mutable points.
	 */
	public static Point[] mutable(Point[] points) {
		return mutable(points, Point.class);
	}

	/**
	 * Returns an mutable version of the given array of line strings.
	 * 
	 * @param lineStrings
	 * @return the mutable line strings.
	 */
	public static LineString[] mutable(LineString[] lineStrings) {
		return mutable(lineStrings, LineString.class);
	}

	/**
	 * Returns an mutable version of the given array of linear rings.
	 * 
	 * @param linearRings
	 * @return the mutable linear rings.
	 */
	public static LinearRing[] mutable(LinearRing[] linearRings) {
		return mutable(linearRings, LinearRing.class);
	}

	/**
	 * Returns an mutable version of the given array of polygons.
	 * 
	 * @param polygons
	 * @return the mutable polygons.
	 */
	public static Polygon[] mutable(Polygon[] polygons) {
		return mutable(polygons, Polygon.class);
	}

	/**
	 * Returns an mutable version of the given array of geometry collections.
	 * 
	 * @param geometryCollections
	 * @return the mutable geometry collections.
	 */
	public static GeometryCollection[] mutable(GeometryCollection[] geometryCollections) {
		return mutable(geometryCollections, GeometryCollection.class);
	}

	/**
	 * Returns an mutable version of the given array of multi line strings.
	 * 
	 * @param multiLineStrings
	 * @return the mutable multi line strings.
	 */
	public static MultiLineString[] mutable(MultiLineString[] multiLineStrings) {
		return mutable(multiLineStrings, MultiLineString.class);
	}

	/**
	 * Returns an mutable version of the given array of multi points.
	 * 
	 * @param multiPoints
	 * @return the mutable multi points.
	 */
	public static MultiPoint[] mutable(MultiPoint[] multiPoints) {
		return mutable(multiPoints, MultiPoint.class);
	}

	/**
	 * Returns an mutable version of the given array of multi polygons.
	 * 
	 * @param multiPolygons
	 * @return the mutable multi polygons.
	 */
	public static MultiPolygon[] mutable(MultiPolygon[] multiPolygons) {
		return mutable(multiPolygons, MultiPolygon.class);
	}
	
	/**
	 * Returns an mutable version of the given array of geometries.
	 * 
	 * @param geometries
	 * @return the mutable geometries.
	 */
	@SuppressWarnings("unchecked") // cast to (T[])
	private static <T extends Geometry> T[] mutable(
		T[] geometries,
		Class<? extends Geometry> componentType)
	{
		if (geometries == null)
			return null;
		
		return Arrays.stream(geometries)
			.map(ImmutableGeometries::mutable)
			.toArray(n -> (T[]) Array.newInstance(componentType, n));
	}

	/**
	 * Returns a new mutable version of the given array of geometries.
	 * 
	 * @param geometries
	 * @return the mutable geometries.
	 */
	@SuppressWarnings("unchecked") // cast to (T)
	public static <T extends Geometry> T mutableOrClone(T geometry) {
		if (geometry == null || geometry instanceof ImmutableGeometry)
			return mutable(geometry);
		else
			return (T) geometry.clone();
	}

	/**
	 * Returns an mutable version of the given coordinate sequence.
	 * 
	 * @param sequence
	 * @return the mutable sequence
	 */
	public static CoordinateSequence mutable(CoordinateSequence coords) {
		if (coords instanceof ImmutableCoordinateSequence)
			return ((ImmutableCoordinateSequence) coords).getMutable();
		else
			return coords;
	}
	
	/**
	 * Returns always true.
	 * 
	 * @param anything a dummy argument
	 * @return {@code true}.
	 */
	static boolean alwaysTrue(Object anything) {
		return true;
	}
	
	/**
	 * Guards the given filter. Throws exception if filter tries to alter a
	 * coordinate.
	 * 
	 * @param filter
	 * @return the guarded filter.
	 */
	static CoordinateFilter guard(CoordinateFilter filter) {
		return new ImmutableCoordinateFilter(filter);
	}

	/**
	 * Guards the given filter. Throws exception if filter tries to alter a
	 * coordinate sequence.
	 * 
	 * @param filter
	 * @return the guarded filter.
	 */
	static CoordinateSequenceFilter guard(CoordinateSequenceFilter filter) {
		return new ImmutableCoordinateSequenceFilter(filter);
	}

}
