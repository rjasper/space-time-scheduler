package de.tu_berlin.mailbox.rjasper.jts.geom.immutable;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.immutable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A immutable version of the {@code GeometryFactory}. Builds only immutable
 * geometries. Uses a mutable factory to be assigned to the build geometries
 * to avoid unexpected appearances of immutable geometries.
 * 
 * @author Rico Jasper
 */
public class ImmutableGeometryFactory extends GeometryFactory {
	
	private static final long serialVersionUID = -7251529169493979058L;
	
	/**
	 * The mutable factory to be assigned to the build immutable geometries.
	 */
	private final GeometryFactory mutableFactory;

	/**
	 * Constructs a new {@code ImmutableGeometryFactory}. The given mutable
	 * factory will be assigned to the immutable geometries.
	 * 
	 * @param mutableFactory
	 */
	public ImmutableGeometryFactory(GeometryFactory mutableFactory) {
		super(
			mutableFactory.getPrecisionModel(),
			mutableFactory.getSRID(),
			mutableFactory.getCoordinateSequenceFactory());
		
		this.mutableFactory = mutableFactory;
	}

	@Override
	public Geometry createGeometry(Geometry g) {
		return immutable(g);
	}

	@Override
	public ImmutablePoint createPoint(CoordinateSequence coordinates) {
		return new ImmutablePoint(coordinates, mutableFactory);
	}

	@Override
	public ImmutablePoint createPoint(Coordinate coordinate) {
		return (ImmutablePoint) super.createPoint(coordinate);
	}

	@Override
	public ImmutableLineString createLineString(Coordinate[] coordinates) {
		return (ImmutableLineString) super.createLineString(coordinates);
	}

	@Override
	public ImmutableLineString createLineString(CoordinateSequence coordinates) {
		return new ImmutableLineString(coordinates, mutableFactory);
	}

	@Override
	public ImmutableLinearRing createLinearRing(Coordinate[] coordinates) {
		return (ImmutableLinearRing) super.createLinearRing(coordinates);
	}

	@Override
	public ImmutableLinearRing createLinearRing(CoordinateSequence coordinates) {
		return new ImmutableLinearRing(coordinates, mutableFactory);
	}

	@Override
	public ImmutablePolygon createPolygon(Coordinate[] coordinates) {
		return (ImmutablePolygon) super.createPolygon(coordinates);
	}

	@Override
	public ImmutablePolygon createPolygon(CoordinateSequence coordinates) {
		return (ImmutablePolygon) super.createPolygon(coordinates);
	}

	@Override
	public ImmutablePolygon createPolygon(LinearRing shell) {
		return (ImmutablePolygon) super.createPolygon(shell);
	}

	@Override
	public ImmutablePolygon createPolygon(LinearRing shell, LinearRing[] holes) {
		return new ImmutablePolygon(shell, holes, mutableFactory);
	}

	@Override
	public ImmutableGeometryCollection createGeometryCollection(Geometry[] geometries) {
		return new ImmutableGeometryCollection(geometries, mutableFactory);
	}

	@Override
	public ImmutableMultiPoint createMultiPoint(Coordinate[] coordinates) {
		return (ImmutableMultiPoint) super.createMultiPoint(coordinates);
	}

	@Override
	public ImmutableMultiPoint createMultiPoint(CoordinateSequence coordinates) {
		return (ImmutableMultiPoint) super.createMultiPoint(coordinates);
	}

	@Override
	public ImmutableMultiPoint createMultiPoint(Point[] points) {
		return new ImmutableMultiPoint(points, mutableFactory);
	}

	@Override
	public ImmutableMultiLineString createMultiLineString(LineString[] lineStrings) {
		return new ImmutableMultiLineString(lineStrings, mutableFactory);
	}

	@Override
	public ImmutableMultiPolygon createMultiPolygon(Polygon[] polygons) {
		return new ImmutableMultiPolygon(polygons, mutableFactory);
	}
	
}