package jts.geom.immutable;

import util.ArraysClone;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

class ImmutableCoordinateSequence implements CoordinateSequence {
	
	private static final CoordinateSequence EMPTY_INTERNAL =
		new CoordinateArraySequence(0);
	
	private final CoordinateSequence internal;
	
	public ImmutableCoordinateSequence() {
		this.internal = EMPTY_INTERNAL;
	}

	public ImmutableCoordinateSequence(CoordinateSequence coordinateSequence) {
		if (coordinateSequence == null)
			this.internal = EMPTY_INTERNAL;
		else if (coordinateSequence instanceof ImmutableCoordinateSequence)
			this.internal = coordinateSequence;
		else
			this.internal = (CoordinateSequence) coordinateSequence.clone();
	}
	
	ImmutableCoordinateSequence(CoordinateSequence coordinateSequence, boolean shared) {
		assert shared;
		this.internal = coordinateSequence;
	}

	@Override
	public int getDimension() {
		return internal.getDimension();
	}
	
	Coordinate getCoordinateActual(int i) {
		return internal.getCoordinate(i);
	}

	@Override
	public Coordinate getCoordinate(int i) {
		// note that a copy is returned
		return internal.getCoordinateCopy(i);
	}

	@Override
	public Coordinate getCoordinateCopy(int i) {
		return internal.getCoordinateCopy(i);
	}

	@Override
	public void getCoordinate(int index, Coordinate coord) {
		internal.getCoordinate(index, coord);
	}

	@Override
	public double getX(int index) {
		return internal.getX(index);
	}

	@Override
	public double getY(int index) {
		return internal.getY(index);
	}

	@Override
	public double getOrdinate(int index, int ordinateIndex) {
		return internal.getOrdinate(index, ordinateIndex);
	}

	@Override
	public int size() {
		return internal.size();
	}

	@Override
	public void setOrdinate(int index, int ordinateIndex, double value) {
		throw new UnsupportedOperationException("immutable CoordinateSequence");
	}

	@Override
	public Coordinate[] toCoordinateArray() {
		return ArraysClone.deepCloneCopy( internal.toCoordinateArray() );
	}

	@Override
	public Envelope expandEnvelope(Envelope env) {
		return internal.expandEnvelope(env);
	}

	@Override
	public ImmutableCoordinateSequence clone() {
		return this;
	}
	
	public CoordinateSequence getMutable() {
		return (CoordinateSequence) internal.clone();
	}

}
