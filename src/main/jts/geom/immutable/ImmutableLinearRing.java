package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.*;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * <p>
 * Extends the {@code LinearRing} to be immutable. Any Attempts to alter
 * the geometry trigger an {@link UnsupportedOperationException}.
 * </p>
 * 
 * <p>
 * Note that {@link ImmutableLineString} is not a super type of
 * {@code ImmutableLinearRing}.
 * </p>
 * 
 * @author Rico
 */
public class ImmutableLinearRing extends LinearRing implements ImmutableGeometry {

	private static final long serialVersionUID = -1840083284048029664L;

	/**
	 * Constructs a new {@code ImmutableLinearRing} from the given
	 * linear ring.
	 * 
	 * @param linearRing
	 */
	public ImmutableLinearRing(LinearRing linearRing) {
		this(linearRing.getCoordinateSequence(), linearRing.getFactory());
	}

	/**
	 * Constructs a new {@code ImmutableLinearRing} from the given
	 * {@code CoordinateSequence}.
	 * 
	 * @param points
	 * @param factory
	 */
	public ImmutableLinearRing(CoordinateSequence points, GeometryFactory factory) {
		super(immutableNonNull(points), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see jts.geom.immutable.ImmutableGeometry#getMutable()
	 */
	@Override
	public LinearRing getMutable() {
		ImmutableCoordinateSequence points = (ImmutableCoordinateSequence) this.points;
		
		return new LinearRing(points.getMutable(), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#geometryChanged()
	 */
	@Override
	public void geometryChanged() {
		throw new UnsupportedOperationException("LinearRing immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#norm()
	 */
	@Override
	public LinearRing norm() {
		LinearRing mutable = getMutable();
		
		mutable.normalize();
		
		return mutable;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.LinearRing#reverse()
	 */
	@Override
	public Geometry reverse() {
		CoordinateSequence mutable = mutable(getCoordinateSequence());
		CoordinateSequences.reverse(mutable);
		
		return new LinearRing(mutable, factory);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.LineString#normalize()
	 */
	@Override
	public void normalize() {
		throw new UnsupportedOperationException("LinearRing immutable");
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
