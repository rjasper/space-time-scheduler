package jts.geom.immutable;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public final class ImmutableGeometries {
	
	private ImmutableGeometries() {}
	
	public static boolean isImmutable(Geometry geometry) {
		return geometry instanceof ImmutableGeometry;
	}
	
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
	
	public static ImmutablePoint immutable(Point point) {
		if (point == null || point instanceof ImmutablePoint)
			return (ImmutablePoint) point;
		else
			return new ImmutablePoint(point);
	}
	
	public static ImmutableLinearRing immutable(LinearRing linearRing) {
		if (linearRing == null || linearRing instanceof ImmutableLinearRing)
			return (ImmutableLinearRing) linearRing;
		else
			return new ImmutableLinearRing(linearRing);
	}
	
	public static ImmutableLineString immutable(LineString lineString) {
		if (lineString == null || lineString instanceof ImmutableLineString)
			return (ImmutableLineString) lineString;
		else
			return new ImmutableLineString(lineString);
	}
	
	public static ImmutablePolygon immutable(Polygon polygon) {
		if (polygon == null || polygon instanceof ImmutablePolygon)
			return (ImmutablePolygon) polygon;
		else
			return new ImmutablePolygon(polygon);
	}
	
	public static ImmutableGeometryCollection immutable(GeometryCollection multiPolygon) {
		if (multiPolygon == null || multiPolygon instanceof ImmutableGeometryCollection)
			return (ImmutableGeometryCollection) multiPolygon;
		else
			return new ImmutableGeometryCollection(multiPolygon);
	}
	
	public static ImmutableMultiPoint immutable(MultiPoint multiPoint) {
		if (multiPoint == null || multiPoint instanceof ImmutableMultiPoint)
			return (ImmutableMultiPoint) multiPoint;
		else
			return new ImmutableMultiPoint(multiPoint);
	}
	
	public static ImmutableMultiLineString immutable(MultiLineString multiLineString) {
		if (multiLineString == null || multiLineString instanceof ImmutableMultiLineString)
			return (ImmutableMultiLineString) multiLineString;
		else
			return new ImmutableMultiLineString(multiLineString);
	}
	
	public static ImmutableMultiPolygon immutable(MultiPolygon multiPolygon) {
		if (multiPolygon == null || multiPolygon instanceof ImmutableMultiPolygon)
			return (ImmutableMultiPolygon) multiPolygon;
		else
			return new ImmutableMultiPolygon(multiPolygon);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Geometry> T[] immutable(T[] geometries) {
		if (geometries == null)
			return null;

		Class<?> componentType = geometries.getClass().getComponentType();
		
		return Arrays.stream(geometries)
			.map(ImmutableGeometries::immutable)
			.toArray(n -> (T[]) Array.newInstance(componentType, n));
	}
	
//	public static MutableImmutableGeometryFactory immutable(GeometryFactory factory) {
//		// TODO cache factories
//		
//		if (factory == null || factory instanceof MutableImmutableGeometryFactory)
//			return (MutableImmutableGeometryFactory) factory;
//		else
//			return new MutableImmutableGeometryFactory(
//				factory.getPrecisionModel(),
//				factory.getSRID(),
//				factory.getCoordinateSequenceFactory());
//	}

	public static ImmutableCoordinateSequence immutable(CoordinateSequence coords) {
		if (coords == null || coords instanceof ImmutableCoordinateSequence)
			return (ImmutableCoordinateSequence) coords;
		else
			return new ImmutableCoordinateSequence(coords);
	}
	
	public static CoordinateSequence immutableNonNull(CoordinateSequence sequence) {
		if (sequence == null)
			return new ImmutableCoordinateSequence();
		else
			return immutable(sequence);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Geometry> T mutable(T geometry) {
		if (geometry instanceof ImmutableGeometry) {
			ImmutableGeometry immutable = (ImmutableGeometry) geometry;
			
			return (T) immutable.getMutable();
		} else {
			return geometry;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Geometry> T[] mutable(T[] geometries) {
		if (geometries == null)
			return null;
		
		Class<?> componentType = geometries.getClass().getComponentType();
		
		return Arrays.stream(geometries)
			.map(ImmutableGeometries::mutable)
			.toArray(n -> (T[]) Array.newInstance(componentType, n));
	}
	
	public static <T extends Geometry> List<T> mutable(Collection<T> collection) {
		return collection.stream()
			.map(ImmutableGeometries::mutable)
			.collect(toList());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Geometry> T mutableOrClone(T geometry) {
		if (geometry == null || geometry instanceof ImmutableGeometry)
			return mutable(geometry);
		else
			return (T) geometry.clone();
	}
	
	public static CoordinateSequence mutable(CoordinateSequence coords) {
		if (coords instanceof ImmutableCoordinateSequence)
			return ((ImmutableCoordinateSequence) coords).getMutable();
		else
			return coords;
	}

}
