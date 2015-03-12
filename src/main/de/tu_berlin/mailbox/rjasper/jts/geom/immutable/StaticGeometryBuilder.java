package de.tu_berlin.mailbox.rjasper.jts.geom.immutable;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometrySequencer.*;

import java.util.Collection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
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
 * Provides static methods to construct geometries.
 * 
 * @author Rico Jasper
 */
public final class StaticGeometryBuilder {
	
	/**
	 * The geometry factory used to provide JTS-like geometry construction.
	 */
	private static final MutableImmutableGeometryFactory FACTORY =
		new MutableImmutableGeometryFactory();

	/**
	 * The geometry builder used to provide Geotools-like geometry construciton.
	 */
	private static final MutableImmutableGeometryBuilder BUILDER =
		new MutableImmutableGeometryBuilder(FACTORY);

	private StaticGeometryBuilder() {}
	
	/**
	 * @return the geometry factory instance.
	 */
	public static MutableImmutableGeometryBuilder getBuilderInstance() {
		return BUILDER;
	}

	/**
	 * @return the geometry builder instance.
	 */
	public static MutableImmutableGeometryFactory getFactoryInstance() {
		return FACTORY;
	}

	public static Geometry geometry(Envelope envelope) {
		return FACTORY.toGeometry(envelope);
	}

	public static Geometry geometry(Geometry g) {
		return FACTORY.createGeometry(g);
	}

	public static Geometry geometry(Collection<? extends Geometry> geomList) {
		return FACTORY.buildGeometry(geomList);
	}

	public static Point point() {
		return BUILDER.point();
	}

	public static Point point(Coordinate coordinate) {
		return FACTORY.createPoint(coordinate);
	}

	public static Point point(CoordinateSequence coordinates) {
		return FACTORY.createPoint(coordinates);
	}

	public static Point point(double x) {
		return BUILDER.point(x);
	}

	public static Point point(double x, double y) {
		return BUILDER.point(x, y);
	}

	public static Point pointZ() {
		return BUILDER.pointZ();
	}

	public static Point pointZ(double x, double y, double z) {
		return BUILDER.pointZ(x, y, z);
	}

	public static LineString lineString() {
		return BUILDER.lineString();
	}

	public static LineString lineString(Coordinate... coordinates) {
		return FACTORY.createLineString(coordinates);
	}

	public static LineString lineString(CoordinateSequence coordinates) {
		return FACTORY.createLineString(coordinates);
	}
	
	public static LineString lineString(Geometry... geometries) {
		return lineString(sequence(geometries));
	}

	public static LineString lineString(double... ord) {
		return BUILDER.lineString(ord);
	}

	public static LineString lineStringZ() {
		return BUILDER.lineStringZ();
	}

	public static LineString lineStringZ(double... ord) {
		return BUILDER.lineStringZ(ord);
	}

	public static LinearRing linearRing() {
		return BUILDER.linearRing();
	}

	public static LinearRing linearRing(Coordinate... coordinates) {
		return FACTORY.createLinearRing(coordinates);
	}

	public static LinearRing linearRing(CoordinateSequence coordinates) {
		return FACTORY.createLinearRing(coordinates);
	}
	
	public static LinearRing linearRing(Geometry... geometries) {
		return linearRing(sequence(geometries));
	}

	public static LinearRing linearRing(double... ord) {
		return BUILDER.linearRing(ord);
	}

	public static LinearRing linearRingZ() {
		return BUILDER.linearRingZ();
	}

	public static LinearRing linearRingZ(double... ord) {
		return BUILDER.linearRingZ(ord);
	}

	public static Polygon polygon() {
		return BUILDER.polygon();
	}

	public static Polygon polygon(Coordinate... coordinates) {
		return FACTORY.createPolygon(coordinates);
	}

	public static Polygon polygon(CoordinateSequence coordinates) {
		return FACTORY.createPolygon(coordinates);
	}
	
	public static Polygon polygon(Geometry... geometries) {
		return polygon(sequence(geometries));
	}

	public static Polygon polygon(double... ord) {
		return BUILDER.polygon(ord);
	}

	public static Polygon polygon(LinearRing shell) {
		return FACTORY.createPolygon(shell);
	}

	public static Polygon polygon(LinearRing shell, LinearRing... holes) {
		return FACTORY.createPolygon(shell, holes);
	}

	public static Polygon polygon(Polygon shell, Polygon hole) {
		return BUILDER.polygon(shell, hole);
	}

	public static Polygon polygonZ() {
		return BUILDER.polygonZ();
	}

	public static Polygon polygonZ(double... ord) {
		return BUILDER.polygonZ(ord);
	}

	public static Polygon box(double x1, double y1, double x2, double y2) {
		return BUILDER.box(x1, y1, x2, y2);
	}
	
	public static Polygon boxZ(double x1, double y1, double x2, double y2, double z) {
		return BUILDER.boxZ(x1, y1, x2, y2, z);
	}

	public static Polygon circle(double x, double y, double radius, int nsides) {
		return BUILDER.circle(x, y, radius, nsides);
	}

	public static Polygon ellipse(
		double x1, double y1, double x2, double y2, int nsides)
	{
		return BUILDER.ellipse(x1, y1, x2, y2, nsides);
	}

	public static GeometryCollection geometryCollection(Geometry... geometries) {
		return FACTORY.createGeometryCollection(geometries);
	}

	public static MultiPoint multiPoint(Coordinate... coordinates) {
		return FACTORY.createMultiPoint(coordinates);
	}

	public static MultiPoint multiPoint(CoordinateSequence coordinates) {
		return FACTORY.createMultiPoint(coordinates);
	}

	public static MultiPoint multiPoint(double x1, double y1, double x2, double y2) {
		return BUILDER.multiPoint(x1, y1, x2, y2);
	}

	public static MultiPoint multiPoint(Point... point) {
		return FACTORY.createMultiPoint(point);
	}

	public static MultiPoint multiPointZ(double x1, double y1, double z1, double x2,
		double y2, double z2) {
		return BUILDER.multiPointZ(x1, y1, z1, x2, y2, z2);
	}

	public static MultiLineString multiLineString(LineString... lineStrings) {
		return FACTORY.createMultiLineString(lineStrings);
	}

	public static MultiPolygon multiPolygon(Polygon... polygons) {
		return FACTORY.createMultiPolygon(polygons);
	}

	public static Geometry immutableGeometry(Collection<? extends Geometry> geomList) {
		return FACTORY.buildImmutableGeometry(geomList);
	}

	public static Geometry immutableGeometry(Envelope envelope) {
		return FACTORY.toImmutableGeometry(envelope);
	}

	public static Geometry immutableGeometry(Geometry g) {
		return FACTORY.createImmutableGeometry(g);
	}
	
	private static final ImmutablePoint EMPTY_POINT = BUILDER.immutablePoint();

	public static ImmutablePoint immutablePoint() {
		return EMPTY_POINT;
	}

	public static ImmutablePoint immutablePoint(Coordinate coordinate) {
		return FACTORY.createImmutablePoint(coordinate);
	}

	public static ImmutablePoint immutablePoint(CoordinateSequence coordinates) {
		return FACTORY.createImmutablePoint(coordinates);
	}

	public static ImmutablePoint immutablePoint(double x) {
		return BUILDER.immutablePoint(x);
	}

	public static ImmutablePoint immutablePoint(double x, double y) {
		return BUILDER.immutablePoint(x, y);
	}
	
	private static final ImmutablePoint EMPTY_POINTZ = BUILDER.immutablePointZ();

	public static ImmutablePoint immutablePointZ() {
		return EMPTY_POINTZ;
	}

	public static ImmutablePoint immutablePointZ(double x, double y, double z) {
		return BUILDER.immutablePointZ(x, y, z);
	}
	
	private static final ImmutableLineString EMPTY_LINESTRING = BUILDER.immutableLineString();

	public static ImmutableLineString immutableLineString() {
		return EMPTY_LINESTRING;
	}

	public static ImmutableLineString immutableLineString(Coordinate... coordinates) {
		return FACTORY.createImmutableLineString(coordinates);
	}

	public static ImmutableLineString immutableLineString(CoordinateSequence coordinates) {
		return FACTORY.createImmutableLineString(coordinates);
	}
	
	public static ImmutableLineString immutableLineString(Geometry... geometries) {
		return immutableLineString(immutableNoCopy(sequence(geometries)));
	}

	public static ImmutableLineString immutableLineString(double... ord) {
		return BUILDER.immutableLineString(ord);
	}
	
	private static final ImmutableLineString EMPTY_LINESTRINGZ = BUILDER.immutableLineStringZ();

	public static ImmutableLineString immutableLineStringZ() {
		return EMPTY_LINESTRINGZ;
	}

	public static ImmutableLineString immutableLineStringZ(double... ord) {
		return BUILDER.immutableLineStringZ(ord);
	}
	
	private static final ImmutableLinearRing EMPTY_LINEARRING = BUILDER.immutableLinearRing();

	public static ImmutableLinearRing immutableLinearRing() {
		return EMPTY_LINEARRING;
	}

	public static ImmutableLinearRing immutableLinearRing(Coordinate... coordinates) {
		return FACTORY.createImmutableLinearRing(coordinates);
	}

	public static ImmutableLinearRing immutableLinearRing(CoordinateSequence coordinates) {
		return FACTORY.createImmutableLinearRing(coordinates);
	}
	
	public static ImmutableLinearRing immutableLinearRing(Geometry... geometries) {
		return immutableLinearRing(immutableNoCopy(sequence(geometries)));
	}

	public static ImmutableLinearRing immutableLinearRing(double... ord) {
		return BUILDER.immutableLinearRing(ord);
	}
	
	private static final ImmutableLinearRing EMPTY_LINEARRINGZ = BUILDER.immutableLinearRingZ();

	public static ImmutableLinearRing immutableLinearRingZ() {
		return EMPTY_LINEARRINGZ;
	}

	public static ImmutableLinearRing immutableLinearRingZ(double... ord) {
		return BUILDER.immutableLinearRingZ(ord);
	}
	
	private static final ImmutablePolygon EMPTY_POLYGON = BUILDER.immutablePolygon();

	public static ImmutablePolygon immutablePolygon() {
		return EMPTY_POLYGON;
	}

	public static ImmutablePolygon immutablePolygon(Coordinate... coordinates) {
		return FACTORY.createImmutablePolygon(coordinates);
	}

	public static ImmutablePolygon immutablePolygon(CoordinateSequence coordinates) {
		return FACTORY.createImmutablePolygon(coordinates);
	}
	
	public static ImmutablePolygon immutablePolygon(Geometry... geometries) {
		return immutablePolygon(immutableNoCopy(sequence(geometries)));
	}

	public static ImmutablePolygon immutablePolygon(double... ord) {
		return BUILDER.immutablePolygon(ord);
	}

	public static ImmutablePolygon immutablePolygon(LinearRing shell) {
		return FACTORY.createImmutablePolygon(shell);
	}

	public static ImmutablePolygon immutablePolygon(LinearRing shell, LinearRing... holes) {
		return FACTORY.createImmutablePolygon(shell, holes);
	}

	public static ImmutablePolygon immutablePolygon(Polygon shell, Polygon hole) {
		return BUILDER.immutablePolygon(shell, hole);
	}
	
	private static final ImmutablePolygon EMPTY_POLYGONZ = BUILDER.immutablePolygonZ();

	public static ImmutablePolygon immutablePolygonZ() {
		return EMPTY_POLYGONZ;
	}

	public static ImmutablePolygon immutablePolygonZ(double... ord) {
		return BUILDER.immutablePolygonZ(ord);
	}

	public static ImmutablePolygon immutableBox(
		double x1, double y1, double x2, double y2)
	{
		return BUILDER.immutableBox(x1, y1, x2, y2);
	}

	public static ImmutablePolygon immutableBoxZ(
		double x1, double y1, double x2, double y2, double z)
	{
		return BUILDER.immutableBoxZ(x1, y1, x2, y2, z);
	}

	public static ImmutablePolygon immutableCircle(
		double x, double y, double radius, int nsides)
	{
		return BUILDER.immutableCircle(x, y, radius, nsides);
	}

	public static ImmutablePolygon immutableEllipse(
		double x1, double y1, double x2, double y2, int nsides)
	{
		return BUILDER.immutableEllipse(x1, y1, x2, y2, nsides);
	}

	public static ImmutableGeometryCollection immutableGeometryCollection(Geometry... geometries) {
		return FACTORY.createImmutableGeometryCollection(geometries);
	}

	public static ImmutableMultiPoint immutableMultiPoint(Coordinate... coordinates) {
		return FACTORY.createImmutableMultiPoint(coordinates);
	}

	public static ImmutableMultiPoint immutableMultiPoint(CoordinateSequence coordinates) {
		return FACTORY.createImmutableMultiPoint(coordinates);
	}

	public static ImmutableMultiPoint immutableMultiPoint(
		double x1, double y1, double x2, double y2)
	{
		return BUILDER.immutableMultiPoint(x1, y1, x2, y2);
	}

	public static ImmutableMultiPoint immutableMultiPoint(Point... points) {
		return FACTORY.createImmutableMultiPoint(points);
	}

	public static ImmutableMultiPoint immutableMultiPointZ(
		double x1, double y1, double z1, double x2, double y2, double z2)
	{
		return BUILDER.immutableMultiPointZ(x1, y1, z1, x2, y2, z2);
	}

	public static ImmutableMultiLineString immutableMultiLineString(LineString... lineStrings) {
		return FACTORY.createImmutableMultiLineString(lineStrings);
	}

	public static ImmutableMultiPolygon immutableMultiPolygon(Polygon... polygons) {
		return FACTORY.createImmutableMultiPolygon(polygons);
	}
	
}
