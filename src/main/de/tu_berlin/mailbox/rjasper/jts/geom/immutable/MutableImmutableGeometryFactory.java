package de.tu_berlin.mailbox.rjasper.jts.geom.immutable;

import java.util.Collection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

/**
 * Extends the {@code GeometryFactory} by new methods to build immutable
 * geometries also.
 * 
 * @author Rico
 */
public class MutableImmutableGeometryFactory extends GeometryFactory {
	
	private static final long serialVersionUID = 7603376747306617405L;
	
	/**
	 * The immutable factory used to build immutable geometries.
	 */
	private final ImmutableGeometryFactory immutableFactory;

	/**
	 * Constructs a factory that generates Geometries having a floating
	 * PrecisionModel and a spatial-reference ID of 0.
	 */
	public MutableImmutableGeometryFactory() {
		this(new PrecisionModel());
	}

	/**
	 * Constructs a factory that generates Geometries having the given
	 * CoordinateSequence implementation, a double-precision floating
	 * PrecisionModel and a spatial-reference ID of 0.
	 */
	public MutableImmutableGeometryFactory(CoordinateSequenceFactory coordinateSequenceFactory) {
		this(new PrecisionModel(), 0, coordinateSequenceFactory);
	}

	/**
	 * Constructs a factory that generates Geometries having the given
	 * PrecisionModel and the default CoordinateSequence implementation.
	 *
	 * @param precisionModel
	 *            the PrecisionModel to use
	 */
	public MutableImmutableGeometryFactory(PrecisionModel precisionModel) {
		this(precisionModel, 0);
	}

	/**
	 * Constructs a factory that generates Geometries having the given
	 * PrecisionModel and spatial-reference ID, and the default
	 * CoordinateSequence implementation.
	 *
	 * @param precisionModel
	 *            the PrecisionModel to use
	 * @param SRID
	 *            the SRID to use
	 */
	public MutableImmutableGeometryFactory(PrecisionModel precisionModel, int SRID) {
		this(precisionModel, SRID, CoordinateArraySequenceFactory.instance());
	}

	/**
	 * Constructs a factory that generates Geometries having the given
	 * PrecisionModel, spatial-reference ID, and CoordinateSequence
	 * implementation.
	 */
	public MutableImmutableGeometryFactory(
		PrecisionModel precisionModel,
		int SRID,
		CoordinateSequenceFactory coordinateSequenceFactory)
	{
		super(precisionModel, SRID, coordinateSequenceFactory);
		this.immutableFactory = new ImmutableGeometryFactory(this);
	}

	/**
	 * @return the immutable geometry factory.
	 */
	public ImmutableGeometryFactory getImmutableFactory() {
		return immutableFactory;
	}

	@SuppressWarnings("rawtypes") // cannot override otherwise
	public Geometry buildImmutableGeometry(Collection geomList) {
		return immutableFactory.buildGeometry(geomList);
	}

	public Geometry createImmutableGeometry(Geometry g) {
		return immutableFactory.createGeometry(g);
	}

	public ImmutablePoint createImmutablePoint(Coordinate coordinate) {
		return (ImmutablePoint) super.createPoint(coordinate);
	}

	public ImmutablePoint createImmutablePoint(CoordinateSequence coordinates) {
		return immutableFactory.createPoint(coordinates);
	}

	public ImmutableLineString createImmutableLineString(Coordinate[] coordinates) {
		return immutableFactory.createLineString(coordinates);
	}

	public ImmutableLineString createImmutableLineString(CoordinateSequence coordinates) {
		return immutableFactory.createLineString(coordinates);
	}

	public ImmutableLinearRing createImmutableLinearRing(Coordinate[] coordinates) {
		return immutableFactory.createLinearRing(coordinates);
	}
	
	public ImmutableLinearRing createImmutableLinearRing(CoordinateSequence coordinates) {
		return immutableFactory.createLinearRing(coordinates);
	}

	public ImmutablePolygon createImmutablePolygon(Coordinate[] coordinates) {
		return immutableFactory.createPolygon(coordinates);
	}

	public ImmutablePolygon createImmutablePolygon(CoordinateSequence coordinates) {
		return immutableFactory.createPolygon(coordinates);
	}

	public ImmutablePolygon createImmutablePolygon(LinearRing shell) {
		return immutableFactory.createPolygon(shell);
	}

	public ImmutablePolygon createImmutablePolygon(LinearRing shell, LinearRing[] holes) {
		return immutableFactory.createPolygon(shell, holes);
	}

	public ImmutableGeometryCollection createImmutableGeometryCollection(
		Geometry[] geometries) {
		return immutableFactory.createGeometryCollection(geometries);
	}

	public ImmutableMultiPoint createImmutableMultiPoint(Coordinate[] coordinates) {
		return immutableFactory.createMultiPoint(coordinates);
	}

	public ImmutableMultiPoint createImmutableMultiPoint(CoordinateSequence coordinates) {
		return immutableFactory.createMultiPoint(coordinates);
	}

	public ImmutableMultiPoint createImmutableMultiPoint(Point[] points) {
		return immutableFactory.createMultiPoint(points);
	}

	public ImmutableMultiLineString createImmutableMultiLineString(
		LineString[] lineStrings) {
		return immutableFactory.createMultiLineString(lineStrings);
	}

	public ImmutableMultiPolygon createImmutableMultiPolygon(Polygon[] polygons) {
		return immutableFactory.createMultiPolygon(polygons);
	}

	public Geometry toImmutableGeometry(Envelope envelope) {
		return immutableFactory.toGeometry(envelope);
	}

}
