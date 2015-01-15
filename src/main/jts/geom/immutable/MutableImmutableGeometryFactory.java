package jts.geom.immutable;

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

public class MutableImmutableGeometryFactory extends GeometryFactory {
	
	private static final long serialVersionUID = 7603376747306617405L;
	
	private final ImmutableGeometryFactory immutableGeomFact;

	public MutableImmutableGeometryFactory() {
		this(new PrecisionModel());
	}

	public MutableImmutableGeometryFactory(CoordinateSequenceFactory coordinateSequenceFactory) {
		this(new PrecisionModel(), 0, coordinateSequenceFactory);
	}

	public MutableImmutableGeometryFactory(
		PrecisionModel precisionModel,
		int SRID,
		CoordinateSequenceFactory coordinateSequenceFactory)
	{
		super(precisionModel, SRID, coordinateSequenceFactory);
		this.immutableGeomFact = new ImmutableGeometryFactory(this);
	}

	public MutableImmutableGeometryFactory(PrecisionModel precisionModel, int SRID) {
		this(precisionModel, SRID, getDefaultCoordinateSequenceFactory());
	}

	public MutableImmutableGeometryFactory(PrecisionModel precisionModel) {
		this(precisionModel, 0);
	}
	
	private static CoordinateSequenceFactory getDefaultCoordinateSequenceFactory() {
		return CoordinateArraySequenceFactory.instance();
	}
	
	public ImmutableGeometryFactory getImmutableFactory() {
		return immutableGeomFact;
	}

	@SuppressWarnings("rawtypes") // cannot override otherwise
	public Geometry buildImmutableGeometry(Collection geomList) {
		return immutableGeomFact.buildGeometry(geomList);
	}

	public Geometry createImmutableGeometry(Geometry g) {
		return immutableGeomFact.createGeometry(g);
	}

	public ImmutablePoint createImmutablePoint(Coordinate coordinate) {
		return (ImmutablePoint) super.createPoint(coordinate);
	}

	public ImmutablePoint createImmutablePoint(CoordinateSequence coordinates) {
		return immutableGeomFact.createPoint(coordinates);
	}

	public ImmutableLineString createImmutableLineString(Coordinate[] coordinates) {
		return immutableGeomFact.createLineString(coordinates);
	}

	public ImmutableLineString createImmutableLineString(CoordinateSequence coordinates) {
		return immutableGeomFact.createLineString(coordinates);
	}

	public ImmutableLinearRing createImmutableLinearRing(Coordinate[] coordinates) {
		return immutableGeomFact.createLinearRing(coordinates);
	}
	
	public ImmutableLinearRing createImmutableLinearRing(CoordinateSequence coordinates) {
		return immutableGeomFact.createLinearRing(coordinates);
	}

	public ImmutablePolygon createImmutablePolygon(Coordinate[] coordinates) {
		return immutableGeomFact.createPolygon(coordinates);
	}

	public ImmutablePolygon createImmutablePolygon(CoordinateSequence coordinates) {
		return immutableGeomFact.createPolygon(coordinates);
	}

	public ImmutablePolygon createImmutablePolygon(LinearRing shell) {
		return immutableGeomFact.createPolygon(shell);
	}

	public ImmutablePolygon createImmutablePolygon(LinearRing shell, LinearRing[] holes) {
		return immutableGeomFact.createPolygon(shell, holes);
	}

	public ImmutableGeometryCollection createImmutableGeometryCollection(
		Geometry[] geometries) {
		return immutableGeomFact.createGeometryCollection(geometries);
	}

	public ImmutableMultiPoint createImmutableMultiPoint(Coordinate[] coordinates) {
		return immutableGeomFact.createMultiPoint(coordinates);
	}

	public ImmutableMultiPoint createImmutableMultiPoint(CoordinateSequence coordinates) {
		return immutableGeomFact.createMultiPoint(coordinates);
	}

	public ImmutableMultiPoint createImmutableMultiPoint(Point[] points) {
		return immutableGeomFact.createMultiPoint(points);
	}

	public ImmutableMultiLineString createImmutableMultiLineString(
		LineString[] lineStrings) {
		return immutableGeomFact.createMultiLineString(lineStrings);
	}

	public ImmutableMultiPolygon createImmutableMultiPolygon(Polygon[] polygons) {
		return immutableGeomFact.createMultiPolygon(polygons);
	}

	public Geometry toImmutableGeometry(Envelope envelope) {
		return immutableGeomFact.toGeometry(envelope);
	}

}
