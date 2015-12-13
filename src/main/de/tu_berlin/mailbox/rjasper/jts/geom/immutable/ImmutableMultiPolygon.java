package de.tu_berlin.mailbox.rjasper.jts.geom.immutable;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.alwaysTrue;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.guard;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.immutable;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.mutable;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * <p>
 * Extends the {@code MultiPolygon} to be immutable. Any Attempts to alter
 * the geometry trigger an {@link UnsupportedOperationException}.
 * </p>
 *
 * <p>
 * Note that the {@link ImmutableGeometryCollection} is not the super type of
 * {@code ImmutableMultiPolygon}.
 * </p>
 *
 * @author Rico Jasper
 */
public class ImmutableMultiPolygon extends MultiPolygon implements ImmutableGeometry {

	private static final long serialVersionUID = 2982709814063389648L;

	/**
	 * Constructs a new {@code ImmutableMultiPolygon} from the given
	 * multi polygon.
	 *
	 * @param multiPolygon
	 */
	public ImmutableMultiPolygon(MultiPolygon multiPolygon) {
		this(retrievePolygons(multiPolygon), multiPolygon.getFactory(), true);
	}

	/**
	 * Constructs a new {@code ImmutableMultiLineString} from the given
	 * polygons.
	 *
	 * @param polygons
	 * @param factory
	 */
	public ImmutableMultiPolygon(Polygon[] polygons, GeometryFactory factory) {
		super(immutable(polygons), factory);
	}

	/**
	 * Constructs a new {@code ImmutableMultiPolygon} from the given
	 * polygons. Does not make a copy of the given array.
	 *
	 * @param polygons
	 * @param factory
	 * @param shared
	 *            has to be {@code true}
	 */
	ImmutableMultiPolygon(ImmutablePolygon[] polygons, GeometryFactory factory, boolean shared) {
		super(polygons, factory);
		assert shared;
	}

	/**
	 * Retrieves the polygons from the given multi polygon.
	 *
	 * @param multiPolygon
	 * @return the polygons.
	 */
	private static ImmutablePolygon[] retrievePolygons(MultiPolygon multiPolygon) {
		if (multiPolygon instanceof ImmutableMultiPolygon)
			return (ImmutablePolygon[]) ((ImmutableMultiPolygon) multiPolygon).geometries;

		int n = multiPolygon.getNumGeometries();

		ImmutablePolygon[] polygons = new ImmutablePolygon[n];
		for (int i = 0; i < n; ++i)
			polygons[i] = immutable((Polygon) multiPolygon.getGeometryN(i));

		return polygons;
	}

	/*
	 * (non-Javadoc)
	 * @see jts.geom.immutable.ImmutableGeometry#getMutable()
	 */
	@Override
	public MultiPolygon getMutable() {
		Polygon[] polygon = (Polygon[]) geometries;

		return new MultiPolygon(mutable(polygon), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#geometryChanged()
	 */
	@Override
	public void geometryChanged() {
		throw new UnsupportedOperationException("MultiPolygon immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.GeometryCollection#normalize()
	 */
	@Override
	public void normalize() {
		throw new UnsupportedOperationException("MultiPolygon immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#norm()
	 */
	@Override
	public MultiPolygon norm() {
		MultiPolygon mutable = getMutable();

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
