package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.*;

import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * <p>
 * Extends the {@code GeometryCollection} to be immutable. Any Attempts to alter
 * the geometry trigger an {@link UnsupportedOperationException}.
 * </p>
 * 
 * <p>
 * Note that the {@code ImmutableGeometryCollection} is not the super type of
 * {@link ImmutableMultiPoint}, {@link ImmutableMultiLineString}, or
 * {@link ImmutableMultiPolygon}.
 * </p>
 * 
 * @author Rico
 */
public class ImmutableGeometryCollection extends GeometryCollection implements ImmutableGeometry {

	private static final long serialVersionUID = 6115804066466833485L;

	/**
	 * Constructs a new {@code ImmutableGeometryCollection} from the given
	 * geometry collection.
	 * 
	 * @param geometryCollection
	 */
	public ImmutableGeometryCollection(GeometryCollection geometryCollection) {
		this(retrieveGeometries(geometryCollection), geometryCollection.getFactory(), true);
	}

	/**
	 * Constructs a new {@code ImmutableGeometryCollection} from the given
	 * geometries.
	 * 
	 * @param geometries
	 * @param factory
	 */
	public ImmutableGeometryCollection(Geometry[] geometries, GeometryFactory factory) {
		super(immutable(geometries), factory);
	}
	
	/**
	 * Constructs a new {@code ImmutableGeometryCollection} from the given
	 * geometries. Does not make a copy of the given array.
	 * 
	 * @param geometries
	 * @param factory
	 * @param shared
	 *            has to be {@code true}
	 */
	ImmutableGeometryCollection(Geometry[] geometries, GeometryFactory factory, boolean shared) {
		super(geometries, factory);
		assert shared;
	}

	/**
	 * Retrieves the geometries from the given geometry collection.
	 * 
	 * @param geometryCollection
	 * @return the geometries.
	 */
	private static Geometry[] retrieveGeometries(GeometryCollection geometryCollection) {
		if (geometryCollection instanceof ImmutableGeometryCollection)
			return ((ImmutableGeometryCollection) geometryCollection).geometries;
		
		int n = geometryCollection.getNumGeometries();
		
		Geometry[] geometries = new Geometry[n];
		for (int i = 0; i < n; ++i)
			geometries[i] = geometryCollection.getGeometryN(i);
		
		return geometries;
	}

	/*
	 * (non-Javadoc)
	 * @see jts.geom.immutable.ImmutableGeometry#getMutable()
	 */
	@Override
	public GeometryCollection getMutable() {
		return new GeometryCollection(mutable(geometries), factory);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#geometryChanged()
	 */
	@Override
	public void geometryChanged() {
		throw new UnsupportedOperationException("GeometryCollection immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.GeometryCollection#normalize()
	 */
	@Override
	public void normalize() {
		throw new UnsupportedOperationException("GeometryCollection immutable");
	}

	/*
	 * (non-Javadoc)
	 * @see com.vividsolutions.jts.geom.Geometry#norm()
	 */
	@Override
	public Geometry norm() {
		GeometryCollection mutable = getMutable();
		
		mutable.normalize();
		
		return mutable;
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
