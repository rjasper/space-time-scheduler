package jts.geom.immutable;

import org.geotools.geometry.jts.GeometryBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Extends the {@code GeometryBuilder} by new methods to build immutable
 * geometries also.
 * 
 * @author Rico
 */
public class MutableImmutableGeometryBuilder extends GeometryBuilder {
	
	/**
	 * The immutable builder used to build immutable geometries.
	 */
	private final ImmutableGeometryBuilder immutableBuilder;

	/**
	 * Constructs a new {@code ImmutableGeometryBuilder} using a default
	 * {@link GeometryFactory} to assemble the geometries.
	 */
	public MutableImmutableGeometryBuilder() {
		this(new GeometryFactory());
	}

	/**
	 * Constructs a new {@code ImmutableGeometryBuilder} using the given
	 * {@link GeometryFactory} to assemble the geometries.
	 * 
	 * @param geomFact
	 */
	public MutableImmutableGeometryBuilder(GeometryFactory geomFact) {
		super(geomFact);
		this.immutableBuilder = new ImmutableGeometryBuilder(geomFact);
	}
	
	public ImmutablePoint immutablePoint() {
		return immutableBuilder.point();
	}

	public ImmutablePoint immutablePointZ() {
		return immutableBuilder.pointZ();
	}

	public ImmutablePoint immutablePoint(double x) {
		return immutableBuilder.point(x);
	}

	public ImmutablePoint immutablePoint(double x, double y) {
		return immutableBuilder.point(x, y);
	}

	public ImmutablePoint immutablePointZ(double x, double y, double z) {
		return immutableBuilder.pointZ(x, y, z);
	}

	public ImmutableLineString immutableLineString() {
		return immutableBuilder.lineString();
	}

	public ImmutableLineString immutableLineStringZ() {
		return immutableBuilder.lineStringZ();
	}

	public ImmutableLineString immutableLineString(double... ord) {
		return immutableBuilder.lineString(ord);
	}

	public ImmutableLineString immutableLineStringZ(double... ord) {
		return immutableBuilder.lineStringZ(ord);
	}

	public ImmutableLinearRing immutableLinearRing() {
		return immutableBuilder.linearRing();
	}

	public ImmutableLinearRing immutableLinearRingZ() {
		return immutableBuilder.linearRingZ();
	}

	public ImmutableLinearRing immutableLinearRing(double... ord) {
		return immutableBuilder.linearRing(ord);
	}

	public ImmutableLinearRing immutableLinearRingZ(double... ord) {
		return immutableBuilder.linearRingZ(ord);
	}

	public ImmutablePolygon immutablePolygon() {
		return immutableBuilder.polygon();
	}

	public ImmutablePolygon immutablePolygonZ() {
		return immutableBuilder.polygonZ();
	}

	public ImmutablePolygon immutablePolygon(double... ord) {
		return immutableBuilder.polygon(ord);
	}

	public ImmutablePolygon immutablePolygonZ(double... ord) {
		return immutableBuilder.polygonZ(ord);
	}

	public ImmutablePolygon immutablePolygon(LinearRing shell) {
		return immutableBuilder.polygon(shell);
	}

	public ImmutablePolygon immutablePolygon(LinearRing shell, LinearRing hole) {
		return immutableBuilder.polygon(shell, hole);
	}

	public ImmutablePolygon immutablePolygon(Polygon shell, Polygon hole) {
		return immutableBuilder.polygon(shell, hole);
	}

	public ImmutablePolygon immutableBox(double x1, double y1, double x2, double y2) {
		return immutableBuilder.box(x1, y1, x2, y2);
	}

	public ImmutablePolygon immutableBoxZ(double x1, double y1, double x2, double y2,
		double z) {
		return immutableBuilder.boxZ(x1, y1, x2, y2, z);
	}

	public ImmutablePolygon immutableEllipse(double x1, double y1, double x2, double y2,
		int nsides) {
		return immutableBuilder.ellipse(x1, y1, x2, y2, nsides);
	}

	public ImmutablePolygon immutableCircle(double x, double y, double radius, int nsides) {
		return immutableBuilder.circle(x, y, radius, nsides);
	}

	public ImmutableMultiPoint immutableMultiPoint(double x1, double y1, double x2,
		double y2) {
		return immutableBuilder.multiPoint(x1, y1, x2, y2);
	}

	public ImmutableMultiPoint immutableMultiPointZ(double x1, double y1, double z1,
		double x2, double y2, double z2) {
		return immutableBuilder.multiPointZ(x1, y1, z1, x2, y2, z2);
	}

	public ImmutableMultiLineString immutableMultiLineString(LineString... lines) {
		return immutableBuilder.multiLineString(lines);
	}

	public ImmutableMultiPolygon immutableMultiPolygon(Polygon... polys) {
		return immutableBuilder.multiPolygon(polys);
	}

	public ImmutableGeometryCollection immutableGeometryCollection(Geometry... geoms) {
		return immutableBuilder.geometryCollection(geoms);
	}

}
