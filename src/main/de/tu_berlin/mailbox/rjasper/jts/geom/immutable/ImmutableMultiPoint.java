package de.tu_berlin.mailbox.rjasper.jts.geom.immutable;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Extends the {@code MultiPoint} to be immutable. Any Attempts to alter
 * the geometry trigger an {@link UnsupportedOperationException}.
 * </p>
 *
 * <p>
 * Note that the {@link ImmutableGeometryCollection} is not the super type of
 * {@code ImmutableMultiPoint}.
 * </p>
 *
 * @author Rico Jasper
 */
public class ImmutableMultiPoint extends MultiPoint implements ImmutableGeometry {

	private static final long serialVersionUID = 7632840480221183791L;

	/**
	 * Constructs a new {@code ImmutableMultiPoint} from the given
	 * multi point.
	 *
	 * @param multiPoint
	 */
	public ImmutableMultiPoint(MultiPoint multiPoint) {
		this(retrievePoints(multiPoint), multiPoint.getFactory(), true);
	}

	/**
	 * Constructs a new {@code ImmutableMultiPoint} from the given points.
	 *
	 * @param points
	 * @param factory
	 */
	public ImmutableMultiPoint(Point[] points, GeometryFactory factory) {
		super(immutable(points), factory);
	}

	/**
	 * Constructs a new {@code ImmutableMultiPoint} from the given points. Does
	 * not make a copy of the given array.
	 *
	 * @param points
	 * @param factory
	 * @param shared
	 *            has to be {@code true}
	 */
	ImmutableMultiPoint(ImmutablePoint[] points, GeometryFactory factory, boolean shared) {
		super(points, factory);
		assert shared;
	}

	/**
	 * Retrieves the points from the given multi point.
	 *
	 * @param multiPoint
	 * @return the points.
	 */
	private static ImmutablePoint[] retrievePoints(MultiPoint multiPoint) {
		if (multiPoint instanceof ImmutableMultiPoint)
			return (ImmutablePoint[]) ((ImmutableMultiPoint) multiPoint).geometries;

		int n = multiPoint.getNumGeometries();

		ImmutablePoint[] points = new ImmutablePoint[n];
		for (int i = 0; i < n; ++i)
			points[i] = immutable((Point) multiPoint.getGeometryN(i));

		return points;
	}

	/*
	 * (non-Javadoc)
	 * @see jts.geom.immutable.ImmutableGeometry#getMutable()
	 */
	@Override
	public MultiPoint getMutable() {
		Point[] points = (Point[]) geometries;

		return new MultiPoint(mutable(points), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#geometryChanged()
	 */
	@Override
	public void geometryChanged() {
		throw new UnsupportedOperationException("MultiPoint immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.GeometryCollection#normalize()
	 */
	@Override
	public void normalize() {
		throw new UnsupportedOperationException("MultiPoint immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#norm()
	 */
	@Override
	public MultiPoint norm() {
		MultiPoint mutable = getMutable();

		mutable.normalize();

		return mutable;
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#union()
	 */
	@Override
	public Geometry union() {
		return getMutable().union();
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.GeometryCollection#apply(com.vividsolutions.jts.geom.CoordinateFilter)
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
	 * @see com.vividsolutions.jts.geom.GeometryCollection#apply(com.vividsolutions.jts.geom.CoordinateSequenceFilter)
	 */
	@Override
	public void apply(CoordinateSequenceFilter filter) {
		// since the guard has a performance impact, only apply it when
		// assertions are enabled
		assert alwaysTrue(filter = guard(filter));
		super.apply(filter);
	}

}
