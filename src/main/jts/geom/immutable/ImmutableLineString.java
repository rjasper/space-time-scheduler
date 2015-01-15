package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.*;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class ImmutableLineString extends LineString implements ImmutableGeometry {

	private static final long serialVersionUID = -2967661517374395217L;

	public ImmutableLineString(LineString lineString) {
		this(lineString.getCoordinateSequence(), lineString.getFactory());
	}

	public ImmutableLineString(CoordinateSequence points, GeometryFactory factory) {
		super(immutableNonNull(points), factory);
	}

	@Override
	public CoordinateSequence getCoordinateSequence() {
		return (CoordinateSequence) super.getCoordinateSequence().clone();
	}

	@Override
	public LineString getMutable() {
		return new LineString(mutable(points), factory);
	}

	@Override
	public void normalize() {
		throw new UnsupportedOperationException("LineString immutable");
	}

	@Override
	public LineString norm() {
		LineString mutable = getMutable();
		
		mutable.normalize();
		
		return mutable;
	}

	@Override
	public Geometry reverse() {
		CoordinateSequence mutable = mutable(getCoordinateSequence());
		CoordinateSequences.reverse(mutable);
		
		return new LineString(mutable, factory);
	}

	@Override
	public ImmutableLineString clone() {
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
