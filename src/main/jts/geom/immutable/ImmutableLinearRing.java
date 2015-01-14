package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.immutable;
import static jts.geom.immutable.ImmutableGeometries.mutable;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

public class ImmutableLinearRing extends LinearRing implements ImmutableGeometry {

	private static final long serialVersionUID = -1840083284048029664L;

	public ImmutableLinearRing(LinearRing linearRing) {
		this(linearRing.getCoordinateSequence(), linearRing.getFactory());
	}

	public ImmutableLinearRing(CoordinateSequence points, GeometryFactory factory) {
		super(immutable(points), factory);
	}

	@Override
	public LinearRing getMutable() {
		ImmutableCoordinateSequence points = (ImmutableCoordinateSequence) this.points;
		
		return new LinearRing(points.getMutable(), factory);
	}

	@Override
	public LinearRing norm() {
		LinearRing mutable = getMutable();
		
		mutable.normalize();
		
		return mutable;
	}

	@Override
	public Geometry reverse() {
		CoordinateSequence mutable = mutable(getCoordinateSequence());
		CoordinateSequences.reverse(mutable);
		
		return new LinearRing(mutable, factory);
	}

	@Override
	public void normalize() {
		throw new UnsupportedOperationException("LinearRing immutable");
	}

	@Override
	public ImmutableLinearRing clone() {
		return this;
	}
	
	// TODO override guarded apply methods

}
