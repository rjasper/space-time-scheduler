package jts.geom.immutable;

import util.ArraysClone;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * Implements an immutable version of a {@code CoordinateSequence}. It denies
 * any coordinate changing operations. Also does not return the actual
 * coordinates of this sequence. Uses an underlying {@code CoordinateSequence}
 * to store the coordinates.
 * 
 * @author Rico
 */
class ImmutableCoordinateSequence implements CoordinateSequence {
	
	/**
	 * An empty sequence without any coordinates.
	 */
	private static final CoordinateSequence EMPTY_INTERNAL =
		new CoordinateArraySequence(0);
	
	/**
	 * The underlying {@code CoordinateSequence} which stores the coordintes.
	 */
	private final CoordinateSequence internal;
	
	/**
	 * Constructs an empty sequence without coordinates.
	 */
	public ImmutableCoordinateSequence() {
		this.internal = EMPTY_INTERNAL;
	}

	/**
	 * Constructs a immutable sequence using the given {@code CoordinateSequence}
	 * as underlying storage of the coordinates. Will make a copy of the given
	 * sequence to ensure that the coordinates are not exposed.
	 * 
	 * @param coordinateSequence the underlying sequence.
	 */
	public ImmutableCoordinateSequence(CoordinateSequence coordinateSequence) {
		if (coordinateSequence == null)
			this.internal = EMPTY_INTERNAL;
		else if (coordinateSequence instanceof ImmutableCoordinateSequence)
			this.internal = ((ImmutableCoordinateSequence) coordinateSequence)
				.getInternal();
		else
			this.internal = (CoordinateSequence) coordinateSequence.clone();
	}
	
	/**
	 * A packet private constructor which never makes a clone of the given
	 * coordinate sequence.
	 * 
	 * @param coordinateSequence
	 *            the underlying sequence.
	 * @param shared
	 *            always true
	 */
	ImmutableCoordinateSequence(CoordinateSequence coordinateSequence, boolean shared) {
		assert shared;
		this.internal = coordinateSequence == null
			? EMPTY_INTERNAL
			: coordinateSequence;
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#getDimension()
	 */
	@Override
	public int getDimension() {
		return internal.getDimension();
	}
	
	/**
	 * @return the internal coordinate sequence.
	 */
	private CoordinateSequence getInternal() {
		return internal;
	}

	/**
	 * Returns the i-th coordinate actually return by the underlying sequence.
	 * 
	 * @param i the position of the coordinate 
	 * @return the actual coordinate.
	 */
	Coordinate getCoordinateActual(int i) {
		return internal.getCoordinate(i);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int)
	 */
	@Override
	public Coordinate getCoordinate(int i) {
		// note that a copy is returned
		return internal.getCoordinateCopy(i);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinateCopy(int)
	 */
	@Override
	public Coordinate getCoordinateCopy(int i) {
		return internal.getCoordinateCopy(i);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int, com.vividsolutions.jts.geom.Coordinate)
	 */
	@Override
	public void getCoordinate(int index, Coordinate coord) {
		internal.getCoordinate(index, coord);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#getX(int)
	 */
	@Override
	public double getX(int index) {
		return internal.getX(index);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#getY(int)
	 */
	@Override
	public double getY(int index) {
		return internal.getY(index);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#getOrdinate(int, int)
	 */
	@Override
	public double getOrdinate(int index, int ordinateIndex) {
		return internal.getOrdinate(index, ordinateIndex);
	}
	
	/**
	 * @return a mutable version of this sequence.
	 */
	public CoordinateSequence getMutable() {
		return (CoordinateSequence) internal.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#size()
	 */
	@Override
	public int size() {
		return internal.size();
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#setOrdinate(int, int, double)
	 */
	@Override
	public void setOrdinate(int index, int ordinateIndex, double value) {
		throw new UnsupportedOperationException("immutable CoordinateSequence");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#toCoordinateArray()
	 */
	@Override
	public Coordinate[] toCoordinateArray() {
		return ArraysClone.deepCloneCopy( internal.toCoordinateArray() );
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.CoordinateSequence#expandEnvelope(com.vividsolutions.jts.geom.Envelope)
	 */
	@Override
	public Envelope expandEnvelope(Envelope env) {
		return internal.expandEnvelope(env);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ImmutableCoordinateSequence clone() {
		return this;
	}

}
