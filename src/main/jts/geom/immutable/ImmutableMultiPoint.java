package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.*;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class ImmutableMultiPoint extends MultiPoint implements ImmutableGeometry {

	private static final long serialVersionUID = 7632840480221183791L;

	public ImmutableMultiPoint(MultiPoint multiPoint) {
		this(retrievePoints(multiPoint), multiPoint.getFactory());
	}

	public ImmutableMultiPoint(Point[] points, GeometryFactory factory) {
		super(immutable(points), factory);
	}

//	ImmutableMultiPoint(ImmutablePoint[] points, ImmutableGeometryFactory factory, boolean shared) {
//		super(points, factory);
//		assert shared;
//	}

	private static Point[] retrievePoints(MultiPoint multiPoint) {
		int n = multiPoint.getNumGeometries();
		
		Point[] points = new Point[n];
		for (int i = 0; i < n; ++i)
			points[i] = (Point) multiPoint.getGeometryN(i);
		
		return points;
	}

	@Override
	public ImmutableMultiPoint getMutable() {
		Point[] points = (Point[]) geometries;
		
		return new ImmutableMultiPoint(mutable(points), factory);
	}

	@Override
	public void normalize() {
		throw new UnsupportedOperationException("MultiPoint immutable");
	}

	@Override
	public MultiPoint norm() {
		MultiPoint mutable = getMutable();
		
		mutable.normalize();
		
		return mutable;
	}

	@Override
	public Geometry reverse() {
		// TODO not efficient
		// reverse creates a copy of getMutable()
		return getMutable().reverse();
	}

	@Override
	public ImmutableMultiPoint clone() {
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
