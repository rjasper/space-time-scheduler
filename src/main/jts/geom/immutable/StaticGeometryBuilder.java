package jts.geom.immutable;

import java.util.Collection;

import org.geotools.geometry.jts.GeometryBuilder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public final class StaticGeometryBuilder {
	
	private static final GeometryFactory MUTABLE_FACTORY = new GeometryFactory();
	
	private static final GeometryBuilder MUTABLE_BUILDER = new GeometryBuilder(MUTABLE_FACTORY);

	private static final ImmutableGeometryFactory IMMUTABLE_FACTORY = new ImmutableGeometryFactory(MUTABLE_FACTORY);
	
	private static final ImmutableGeometryBuilder IMMUTABLE_BUILDER = new ImmutableGeometryBuilder(IMMUTABLE_FACTORY);

	public static Geometry buildGeometry(Collection<Geometry> geomList) {
		return MUTABLE_FACTORY.buildGeometry(geomList);
	}

	public static Point createPoint(Coordinate coordinate) {
		return MUTABLE_FACTORY.createPoint(coordinate);
	}

	public static Point createPoint(CoordinateSequence coordinates) {
		return MUTABLE_FACTORY.createPoint(coordinates);
	}

	public static MultiLineString createMultiLineString(LineString[] lineStrings) {
		return MUTABLE_FACTORY.createMultiLineString(lineStrings);
	}

	public static GeometryCollection createGeometryCollection(Geometry[] geometries) {
		return MUTABLE_FACTORY.createGeometryCollection(geometries);
	}

	public static MultiPolygon createMultiPolygon(Polygon[] polygons) {
		return MUTABLE_FACTORY.createMultiPolygon(polygons);
	}

	public static LinearRing createLinearRing(Coordinate[] coordinates) {
		return MUTABLE_FACTORY.createLinearRing(coordinates);
	}

	public static LinearRing createLinearRing(CoordinateSequence coordinates) {
		return MUTABLE_FACTORY.createLinearRing(coordinates);
	}

	public static MultiPoint createMultiPoint(Point[] point) {
		return MUTABLE_FACTORY.createMultiPoint(point);
	}

	public static MultiPoint createMultiPoint(Coordinate[] coordinates) {
		return MUTABLE_FACTORY.createMultiPoint(coordinates);
	}

	public static MultiPoint createMultiPoint(CoordinateSequence coordinates) {
		return MUTABLE_FACTORY.createMultiPoint(coordinates);
	}

	public static Polygon createPolygon(LinearRing shell, LinearRing[] holes) {
		return MUTABLE_FACTORY.createPolygon(shell, holes);
	}

	public static Polygon createPolygon(CoordinateSequence coordinates) {
		return MUTABLE_FACTORY.createPolygon(coordinates);
	}

	public static Polygon createPolygon(Coordinate[] coordinates) {
		return MUTABLE_FACTORY.createPolygon(coordinates);
	}

	public static Polygon createPolygon(LinearRing shell) {
		return MUTABLE_FACTORY.createPolygon(shell);
	}

	public static LineString createLineString(Coordinate[] coordinates) {
		return MUTABLE_FACTORY.createLineString(coordinates);
	}

	public static LineString createLineString(CoordinateSequence coordinates) {
		return MUTABLE_FACTORY.createLineString(coordinates);
	}

	public static Geometry createGeometry(Geometry g) {
		return MUTABLE_FACTORY.createGeometry(g);
	}

	public static Geometry buildImmutableGeometry(Collection<Geometry> geomList) {
		return IMMUTABLE_FACTORY.buildGeometry(geomList);
	}

	public static Geometry createImmutableImmutableGeometry(Geometry g) {
		return IMMUTABLE_FACTORY.createGeometry(g);
	}

	public static ImmutablePoint createImmutablePoint(CoordinateSequence coordinates) {
		return IMMUTABLE_FACTORY.createPoint(coordinates);
	}

	public static ImmutablePoint createImmutablePoint(Coordinate coordinate) {
		return IMMUTABLE_FACTORY.createPoint(coordinate);
	}

	public static ImmutableLineString createImmutableLineString(Coordinate[] coordinates) {
		return IMMUTABLE_FACTORY.createLineString(coordinates);
	}

	public static ImmutableLineString createImmutableLineString(CoordinateSequence coordinates) {
		return IMMUTABLE_FACTORY.createLineString(coordinates);
	}

	public static ImmutableLinearRing createImmutableLinearRing(Coordinate[] coordinates) {
		return IMMUTABLE_FACTORY.createLinearRing(coordinates);
	}

	public static ImmutableLinearRing createImmutableLinearRing(CoordinateSequence coordinates) {
		return IMMUTABLE_FACTORY.createLinearRing(coordinates);
	}

	public static ImmutablePolygon createImmutablePolygon(Coordinate[] coordinates) {
		return IMMUTABLE_FACTORY.createPolygon(coordinates);
	}

	public static ImmutablePolygon createImmutablePolygon(CoordinateSequence coordinates) {
		return IMMUTABLE_FACTORY.createPolygon(coordinates);
	}

	public static ImmutablePolygon createImmutablePolygon(LinearRing shell) {
		return IMMUTABLE_FACTORY.createPolygon(shell);
	}

	public static ImmutablePolygon createImmutablePolygon(LinearRing shell, LinearRing[] holes) {
		return IMMUTABLE_FACTORY.createPolygon(shell, holes);
	}

	public static ImmutableGeometryCollection createImmutableGeometryCollection(
		Geometry[] geometries) {
		return IMMUTABLE_FACTORY.createGeometryCollection(geometries);
	}

	public static ImmutableMultiPoint createImmutableMultiPoint(Coordinate[] coordinates) {
		return IMMUTABLE_FACTORY.createMultiPoint(coordinates);
	}

	public static ImmutableMultiPoint createImmutableMultiPoint(CoordinateSequence coordinates) {
		return IMMUTABLE_FACTORY.createMultiPoint(coordinates);
	}

	public static ImmutableMultiPoint createImmutableMultiPoint(Point[] points) {
		return IMMUTABLE_FACTORY.createMultiPoint(points);
	}

	public static ImmutableMultiLineString createImmutableMultiLineString(
		LineString[] lineStrings) {
		return IMMUTABLE_FACTORY.createMultiLineString(lineStrings);
	}

	public static ImmutableMultiPolygon createImmutableMultiPolygon(Polygon[] polygons) {
		return IMMUTABLE_FACTORY.createMultiPolygon(polygons);
	}

	public static Point point() {
		return MUTABLE_BUILDER.point();
	}

	public static Point pointZ() {
		return MUTABLE_BUILDER.pointZ();
	}

	public static Point point(double x) {
		return MUTABLE_BUILDER.point(x);
	}

	public static Point point(double x, double y) {
		return MUTABLE_BUILDER.point(x, y);
	}

	public static Point pointZ(double x, double y, double z) {
		return MUTABLE_BUILDER.pointZ(x, y, z);
	}

	public static LineString lineString() {
		return MUTABLE_BUILDER.lineString();
	}

	public static LineString lineStringZ() {
		return MUTABLE_BUILDER.lineStringZ();
	}

	public static LineString lineString(double... ord) {
		return MUTABLE_BUILDER.lineString(ord);
	}

	public static LineString lineStringZ(double... ord) {
		return MUTABLE_BUILDER.lineStringZ(ord);
	}

	public static LinearRing linearRing() {
		return MUTABLE_BUILDER.linearRing();
	}

	public static LinearRing linearRingZ() {
		return MUTABLE_BUILDER.linearRingZ();
	}

	public static LinearRing linearRing(double... ord) {
		return MUTABLE_BUILDER.linearRing(ord);
	}

	public static LinearRing linearRingZ(double... ord) {
		return MUTABLE_BUILDER.linearRingZ(ord);
	}

	public static Polygon polygon() {
		return MUTABLE_BUILDER.polygon();
	}

	public static Polygon polygonZ() {
		return MUTABLE_BUILDER.polygonZ();
	}

	public static Polygon polygon(double... ord) {
		return MUTABLE_BUILDER.polygon(ord);
	}

	public static Polygon polygonZ(double... ord) {
		return MUTABLE_BUILDER.polygonZ(ord);
	}

	public static Polygon polygon(LinearRing shell) {
		return MUTABLE_BUILDER.polygon(shell);
	}

	public static Polygon polygon(LinearRing shell, LinearRing hole) {
		return MUTABLE_BUILDER.polygon(shell, hole);
	}

	public static Polygon polygon(Polygon shell, Polygon hole) {
		return MUTABLE_BUILDER.polygon(shell, hole);
	}

	public static Polygon box(double x1, double y1, double x2, double y2) {
		return MUTABLE_BUILDER.box(x1, y1, x2, y2);
	}

	public static Polygon boxZ(double x1, double y1, double x2, double y2, double z) {
		return MUTABLE_BUILDER.boxZ(x1, y1, x2, y2, z);
	}

	public static Polygon ellipse(double x1, double y1, double x2, double y2,
		int nsides) {
		return MUTABLE_BUILDER.ellipse(x1, y1, x2, y2, nsides);
	}

	public static Polygon circle(double x, double y, double radius, int nsides) {
		return MUTABLE_BUILDER.circle(x, y, radius, nsides);
	}

	public static MultiPoint multiPoint(double x1, double y1, double x2, double y2) {
		return MUTABLE_BUILDER.multiPoint(x1, y1, x2, y2);
	}

	public static MultiPoint multiPointZ(double x1, double y1, double z1, double x2,
		double y2, double z2) {
		return MUTABLE_BUILDER.multiPointZ(x1, y1, z1, x2, y2, z2);
	}

	public static MultiLineString multiLineString(LineString... lines) {
		return MUTABLE_BUILDER.multiLineString(lines);
	}

	public static MultiPolygon multiPolygon(Polygon... polys) {
		return MUTABLE_BUILDER.multiPolygon(polys);
	}

	public static GeometryCollection geometryCollection(Geometry... geoms) {
		return MUTABLE_BUILDER.geometryCollection(geoms);
	}

	public static ImmutablePoint immutablePoint() {
		return IMMUTABLE_BUILDER.point();
	}

	public static ImmutablePoint immutablePointZ() {
		return IMMUTABLE_BUILDER.pointZ();
	}

	public static ImmutablePoint immutablePoint(double x) {
		return IMMUTABLE_BUILDER.point(x);
	}

	public static ImmutablePoint immutablePoint(double x, double y) {
		return IMMUTABLE_BUILDER.point(x, y);
	}

	public static ImmutablePoint immutablePointZ(double x, double y, double z) {
		return IMMUTABLE_BUILDER.pointZ(x, y, z);
	}

	public static ImmutableLineString immutableLineString() {
		return IMMUTABLE_BUILDER.lineString();
	}

	public static ImmutableLineString immutableLineStringZ() {
		return IMMUTABLE_BUILDER.lineStringZ();
	}

	public static ImmutableLineString immutableLineString(double... ord) {
		return IMMUTABLE_BUILDER.lineString(ord);
	}

	public static ImmutableLineString immutableLineStringZ(double... ord) {
		return IMMUTABLE_BUILDER.lineStringZ(ord);
	}

	public static ImmutableLinearRing immutableLinearRing() {
		return IMMUTABLE_BUILDER.linearRing();
	}

	public static ImmutableLinearRing immutableLinearRingZ() {
		return IMMUTABLE_BUILDER.linearRingZ();
	}

	public static ImmutableLinearRing immutableLinearRing(double... ord) {
		return IMMUTABLE_BUILDER.linearRing(ord);
	}

	public static ImmutableLinearRing immutableLinearRingZ(double... ord) {
		return IMMUTABLE_BUILDER.linearRingZ(ord);
	}

	public static ImmutablePolygon immutablePolygon() {
		return IMMUTABLE_BUILDER.polygon();
	}

	public static ImmutablePolygon immutablePolygonZ() {
		return IMMUTABLE_BUILDER.polygonZ();
	}

	public static ImmutablePolygon immutablePolygon(double... ord) {
		return IMMUTABLE_BUILDER.polygon(ord);
	}

	public static ImmutablePolygon immutablePolygonZ(double... ord) {
		return IMMUTABLE_BUILDER.polygonZ(ord);
	}

	public static ImmutablePolygon immutablePolygon(LinearRing shell) {
		return IMMUTABLE_BUILDER.polygon(shell);
	}

	public static ImmutablePolygon immutablePolygon(LinearRing shell, LinearRing hole) {
		return IMMUTABLE_BUILDER.polygon(shell, hole);
	}

	public static ImmutablePolygon immutablePolygon(Polygon shell, Polygon hole) {
		return IMMUTABLE_BUILDER.polygon(shell, hole);
	}

	public static ImmutablePolygon immutableBox(double x1, double y1, double x2, double y2) {
		return IMMUTABLE_BUILDER.box(x1, y1, x2, y2);
	}

	public static ImmutablePolygon immutableBoxZ(double x1, double y1, double x2, double y2,
		double z) {
		return IMMUTABLE_BUILDER.boxZ(x1, y1, x2, y2, z);
	}

	public static ImmutablePolygon immutableEllipse(double x1, double y1, double x2, double y2,
		int nsides) {
		return IMMUTABLE_BUILDER.ellipse(x1, y1, x2, y2, nsides);
	}

	public static ImmutablePolygon immutableCircle(double x, double y, double radius, int nsides) {
		return IMMUTABLE_BUILDER.circle(x, y, radius, nsides);
	}

	public static ImmutableMultiPoint immutableMultiPoint(double x1, double y1, double x2,
		double y2) {
		return IMMUTABLE_BUILDER.multiPoint(x1, y1, x2, y2);
	}

	public static ImmutableMultiPoint immutableMultiPointZ(double x1, double y1, double z1,
		double x2, double y2, double z2) {
		return IMMUTABLE_BUILDER.multiPointZ(x1, y1, z1, x2, y2, z2);
	}

	public static ImmutableMultiLineString immutableMultiLineString(LineString... lines) {
		return IMMUTABLE_BUILDER.multiLineString(lines);
	}

	public static ImmutableMultiPolygon immutableMultiPolygon(Polygon... polys) {
		return IMMUTABLE_BUILDER.multiPolygon(polys);
	}

	public static ImmutableGeometryCollection immutableGeometryCollection(Geometry... geoms) {
		return IMMUTABLE_BUILDER.geometryCollection(geoms);
	}

}
