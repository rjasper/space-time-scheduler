package de.tu_berlin.mailbox.rjasper.jts.geom.immutable;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.alwaysTrue;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.guard;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.immutableNonNull;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.mutable;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Extends the {@code Point} to be immutable. Any Attempts to alter the geometry
 * trigger an {@link UnsupportedOperationException}.
 * </p>
 * 
 * @author Rico Jasper
 */
public class ImmutablePoint extends Point implements ImmutableGeometry {

	private static final long serialVersionUID = 8473118084765349243L;
	
	/**
	 * Constructs a new {@code ImmutablePoint} from the given point.
	 * 
	 * @param point
	 */
	public ImmutablePoint(Point point) {
		this(point.getCoordinateSequence(), point.getFactory());
	}

	/**
	 * Constructs a new {@code ImmutableGeometryCollection} from the given
	 * coordinate sequence.
	 * 
	 * @param coordinates
	 * @param factory
	 */
	public ImmutablePoint(CoordinateSequence coordinates, GeometryFactory factory) {
		super(immutableNonNull(coordinates), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see jts.geom.immutable.ImmutableGeometry#getMutable()
	 */
	@Override
	public Point getMutable() {
		ImmutableCoordinateSequence coords = (ImmutableCoordinateSequence) getCoordinateSequence();
		
		return new Point(mutable(coords), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#geometryChanged()
	 */
	@Override
	public void geometryChanged() {
		throw new UnsupportedOperationException("Point immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Point#normalize()
	 */
	@Override
	public void normalize() {
		throw new UnsupportedOperationException("Point immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#norm()
	 */
	@Override
	public Point norm() {
		Point mutable = getMutable();
		
		mutable.normalize();
		
		return mutable;
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Point#reverse()
	 */
	@Override
	public Geometry reverse() {
		return getMutable();
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Point#apply(com.vividsolutions.jts.geom.CoordinateFilter)
	 */
	@Override
	public void apply(CoordinateFilter filter) {
		// since the guard has a performance impact, only apply it when
		// assertions are enabled
		assert alwaysTrue(filter = guard(filter));
		super.apply(filter);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Point#apply(com.vividsolutions.jts.geom.CoordinateSequenceFilter)
	 */
	@Override
	public void apply(CoordinateSequenceFilter filter) {
		// since the guard has a performance impact, only apply it when
		// assertions are enabled
		assert alwaysTrue(filter = guard(filter));
		super.apply(filter);
	}

}
