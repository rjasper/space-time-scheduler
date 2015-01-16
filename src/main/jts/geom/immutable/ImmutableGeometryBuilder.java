package jts.geom.immutable;

import org.geotools.geometry.jts.GeometryBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A immutable version of the {@code GeometryBuilder}. Builds only immutable
 * geometries. Uses a {@link ImmutableGeometryFactory} to assemble the
 * geometries.
 * 
 * @author Rico
 */
public class ImmutableGeometryBuilder extends GeometryBuilder {

	/**
	 * Constructs a new {@code ImmutableGeometryBuilder} using a new
	 * {@code MutableImmutableGeometryFactory}.
	 */
	public ImmutableGeometryBuilder() {
		this(new MutableImmutableGeometryFactory());
	}

	/**
	 * Constructs a new {@code ImmutableGeometryBuilder} using the given
	 * factory to make a {@code ImmutableGeometryFactory}.
	 */
	public ImmutableGeometryBuilder(GeometryFactory geomFact) {
		super(makeImmutableGeometryFactory(geomFact));
	}
	
	/**
	 * Makes a {@code ImmutableGeometryFactory} from the given factory.
	 * 
	 * @param factory
	 * @return the {@code MutableImmutableGeometryFactory}.
	 */
	private static ImmutableGeometryFactory makeImmutableGeometryFactory(GeometryFactory factory) {
		if (factory instanceof ImmutableGeometryFactory)
			return (ImmutableGeometryFactory) factory;
		else if (factory instanceof MutableImmutableGeometryFactory)
			return ((MutableImmutableGeometryFactory) factory).getImmutableFactory();
		else
			return new ImmutableGeometryFactory(factory);
	}

	@Override
	public ImmutablePoint point() {
		return (ImmutablePoint) super.point();
	}

	@Override
	public ImmutablePoint pointZ() {
		return (ImmutablePoint) super.pointZ();
	}

	@Override
	public ImmutablePoint point(double x) {
		return (ImmutablePoint) super.point(x);
	}

	@Override
	public ImmutablePoint point(double x, double y) {
		return (ImmutablePoint) super.point(x, y);
	}

	@Override
	public ImmutablePoint pointZ(double x, double y, double z) {
		return (ImmutablePoint) super.pointZ(x, y, z);
	}

	@Override
	public ImmutableLineString lineString() {
		return (ImmutableLineString) super.lineString();
	}

	@Override
	public ImmutableLineString lineStringZ() {
		return (ImmutableLineString) super.lineStringZ();
	}

	@Override
	public ImmutableLineString lineString(double... ord) {
		return (ImmutableLineString) super.lineString(ord);
	}

	@Override
	public ImmutableLineString lineStringZ(double... ord) {
		return (ImmutableLineString) super.lineStringZ(ord);
	}

	@Override
	public ImmutableLinearRing linearRing() {
		return (ImmutableLinearRing) super.linearRing();
	}

	@Override
	public ImmutableLinearRing linearRingZ() {
		return (ImmutableLinearRing) super.linearRingZ();
	}

	@Override
	public ImmutableLinearRing linearRing(double... ord) {
		return (ImmutableLinearRing) super.linearRing(ord);
	}

	@Override
	public ImmutableLinearRing linearRingZ(double... ord) {
		return (ImmutableLinearRing) super.linearRingZ(ord);
	}

	@Override
	public ImmutablePolygon polygon() {
		return (ImmutablePolygon) super.polygon();
	}

	@Override
	public ImmutablePolygon polygonZ() {
		return (ImmutablePolygon) super.polygonZ();
	}

	@Override
	public ImmutablePolygon polygon(double... ord) {
		return (ImmutablePolygon) super.polygon(ord);
	}

	@Override
	public ImmutablePolygon polygonZ(double... ord) {
		return (ImmutablePolygon) super.polygonZ(ord);
	}

	@Override
	public ImmutablePolygon polygon(LinearRing shell) {
		return(ImmutablePolygon) super.polygon(shell);
	}

	@Override
	public ImmutablePolygon polygon(LinearRing shell, LinearRing hole) {
		return (ImmutablePolygon) super.polygon(shell, hole);
	}

	@Override
	public ImmutablePolygon polygon(Polygon shell, Polygon hole) {
		return (ImmutablePolygon) super.polygon(shell, hole);
	}

	@Override
	public ImmutablePolygon box(double x1, double y1, double x2, double y2) {
		return (ImmutablePolygon) super.box(x1, y1, x2, y2);
	}

	@Override
	public ImmutablePolygon boxZ(double x1, double y1, double x2, double y2, double z) {
		return (ImmutablePolygon) super.boxZ(x1, y1, x2, y2, z);
	}

	@Override
	public ImmutablePolygon ellipse(double x1, double y1, double x2, double y2,
		int nsides) {
		return (ImmutablePolygon) super.ellipse(x1, y1, x2, y2, nsides);
	}

	@Override
	public ImmutablePolygon circle(double x, double y, double radius, int nsides) {
		return (ImmutablePolygon) super.circle(x, y, radius, nsides);
	}

	@Override
	public ImmutableMultiPoint multiPoint(double x1, double y1, double x2, double y2) {
		return (ImmutableMultiPoint) super.multiPoint(x1, y1, x2, y2);
	}

	@Override
	public ImmutableMultiPoint multiPointZ(double x1, double y1, double z1, double x2,
		double y2, double z2) {
		return (ImmutableMultiPoint) super.multiPointZ(x1, y1, z1, x2, y2, z2);
	}

	@Override
	public ImmutableMultiLineString multiLineString(LineString... lines) {
		return (ImmutableMultiLineString) super.multiLineString(lines);
	}

	@Override
	public ImmutableMultiPolygon multiPolygon(Polygon... polys) {
		return (ImmutableMultiPolygon) super.multiPolygon(polys);
	}

	@Override
	public ImmutableGeometryCollection geometryCollection(Geometry... geoms) {
		return (ImmutableGeometryCollection) super.geometryCollection(geoms);
	}

}
