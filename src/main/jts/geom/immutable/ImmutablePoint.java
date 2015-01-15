package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.alwaysTrue;
import static jts.geom.immutable.ImmutableGeometries.guard;
import static jts.geom.immutable.ImmutableGeometries.immutableNonNull;
import static jts.geom.immutable.ImmutableGeometries.mutable;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class ImmutablePoint extends Point implements ImmutableGeometry {

	private static final long serialVersionUID = 8473118084765349243L;

	public ImmutablePoint(Point point) {
		this(point.getCoordinateSequence(), point.getFactory());
	}

	public ImmutablePoint(CoordinateSequence coordinates, GeometryFactory factory) {
		super(immutableNonNull(coordinates), factory);
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

	@Override
	public void apply(CoordinateFilter filter) {
		assert alwaysTrue(filter = guard(filter));
		super.apply(filter);
	}

	@Override
	public void apply(CoordinateSequenceFilter filter) {
		assert alwaysTrue(filter = guard(filter));
		super.apply(filter);
	}

}
