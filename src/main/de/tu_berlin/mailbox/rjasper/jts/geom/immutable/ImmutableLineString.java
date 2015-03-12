package de.tu_berlin.mailbox.rjasper.jts.geom.immutable;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * <p>
 * Extends the {@code LineString} to be immutable. Any Attempts to alter
 * the geometry trigger an {@link UnsupportedOperationException}.
 * </p>
 * 
 * <p>
 * Note that {@code ImmutableLineString} is not a super type of
 * {@link ImmutableLinearRing}.
 * </p>
 * 
 * @author Rico Jasper
 */
public class ImmutableLineString extends LineString implements ImmutableGeometry {

	private static final long serialVersionUID = -2967661517374395217L;

	/**
	 * Constructs a new {@code ImmutableLineString} from the given
	 * line string.
	 * 
	 * @param lineString
	 */
	public ImmutableLineString(LineString lineString) {
		this(lineString.getCoordinateSequence(), lineString.getFactory());
	}

	/**
	 * Constructs a new {@code ImmutableLineString} from the given
	 * {@code CoordinateSequence}.
	 * 
	 * @param points
	 * @param factory
	 */
	public ImmutableLineString(CoordinateSequence points, GeometryFactory factory) {
		super(immutableNonNull(points), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.LineString#getCoordinateSequence()
	 */
	@Override
	public CoordinateSequence getCoordinateSequence() {
		return (CoordinateSequence) super.getCoordinateSequence().clone();
	}

	/*
	 * (non-Javadoc)
	 * @see jts.geom.immutable.ImmutableGeometry#getMutable()
	 */
	@Override
	public LineString getMutable() {
		return new LineString(mutable(points), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#geometryChanged()
	 */
	@Override
	public void geometryChanged() {
		throw new UnsupportedOperationException("LineString immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.LineString#normalize()
	 */
	@Override
	public void normalize() {
		throw new UnsupportedOperationException("LineString immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#norm()
	 */
	@Override
	public LineString norm() {
		LineString mutable = getMutable();
		
		mutable.normalize();
		
		return mutable;
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.LineString#reverse()
	 */
	@Override
	public Geometry reverse() {
		CoordinateSequence mutable = mutable(getCoordinateSequence());
		CoordinateSequences.reverse(mutable);
		
		return new LineString(mutable, factory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.LineString#apply(com.vividsolutions.jts.geom.CoordinateFilter)
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
	 * @see com.vividsolutions.jts.geom.LineString#apply(com.vividsolutions.jts.geom.CoordinateSequenceFilter)
	 */
	@Override
	public void apply(CoordinateSequenceFilter filter) {
		// since the guard has a performance impact, only apply it when
		// assertions are enabled
		assert alwaysTrue(filter = guard(filter));
		super.apply(filter);
	}

}
