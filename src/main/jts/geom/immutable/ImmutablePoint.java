package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.immutable;
import static jts.geom.immutable.ImmutableGeometries.mutable;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

class ImmutablePoint extends Point implements ImmutableGeometry {

	private static final long serialVersionUID = 8473118084765349243L;

	protected ImmutablePoint(CoordinateSequence coordinates, GeometryFactory factory) {
		super(immutable(coordinates), factory);
	}

	protected ImmutablePoint(Point point) {
		this(point.getCoordinateSequence(), point.getFactory());
	}

	@Override
	public Point getMutable() {
		ImmutableCoordinateSequence coords = (ImmutableCoordinateSequence) getCoordinateSequence();
		
		return new Point(mutable(coords), factory);
	}

	@Override
	public void normalize() {
		throw new UnsupportedOperationException("Point immutable");
	}

	@Override
	public Point norm() {
		Point mutable = getMutable();
		
		mutable.normalize();
		
		return mutable;
	}

	@Override
	public Geometry reverse() {
		return getMutable();
	}

	@Override
	public ImmutablePoint clone() {
		return this;
	}

}
