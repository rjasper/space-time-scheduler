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
	
	@SuppressWarnings("unchecked")
	public static <T extends Geometry> T immutable(T geometry) {
		if (geometry == null)
			return null;
		if (isImmutable(geometry))
			return geometry;
		
		if (geometry instanceof Point)
			return (T) new ImmutablePoint((Point) geometry);
		if (geometry instanceof LinearRing)
			return (T) new ImmutableLinearRing((LinearRing) geometry);
		if (geometry instanceof LineString)
			return (T) new ImmutableLineString((LineString) geometry);
		if (geometry instanceof Polygon)
			return (T) new ImmutablePolygon((Polygon) geometry);
		if (geometry instanceof MultiPoint)
			return (T) new ImmutableMultiPoint((MultiPoint) geometry);
		if (geometry instanceof MultiLineString)
			return (T) new ImmutableMultiLineString((MultiLineString) geometry);
		if (geometry instanceof MultiPolygon)
			return (T) new ImmutableMultiPolygon((MultiPolygon) geometry);
		if (geometry instanceof GeometryCollection)
			return (T) new ImmutableGeometryCollection((GeometryCollection) geometry);
		
		throw new IllegalArgumentException("unknown geometry");
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
	
	public static <T extends Geometry> List<T> immutable(Collection<T> collection) {
		return collection.stream()
			.map(ImmutableGeometries::immutable)
			.collect(toList());
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
	
	public static CoordinateSequence immutable(CoordinateSequence coords) {
		if (coords instanceof ImmutableCoordinateSequence)
			return coords;
		else
			return new ImmutableCoordinateSequence(coords);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Geometry> T mutableOrClone(T geometry) {
		if (geometry instanceof ImmutableGeometry)
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
