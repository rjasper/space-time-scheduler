package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.immutable;
import static jts.geom.immutable.ImmutableGeometries.mutable;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

public class ImmutableGeometryCollection extends GeometryCollection implements ImmutableGeometry {

	private static final long serialVersionUID = 6115804066466833485L;

	protected ImmutableGeometryCollection(Geometry[] geometries, GeometryFactory factory) {
		super(immutable(geometries), factory);
	}

	protected ImmutableGeometryCollection(GeometryCollection geometry) {
		this(retrieveGeometries(geometry), geometry.getFactory());
	}
	
	private static Geometry[] retrieveGeometries(GeometryCollection geometryCollection) {
		int n = geometryCollection.getNumGeometries();
		
		Geometry[] geometries = new Geometry[n];
		for (int i = 0; i < n; ++i)
			geometries[i] = geometryCollection.getGeometryN(i);
		
		return geometries;
	}

	@Override
	public GeometryCollection getMutable() {
		return new GeometryCollection(mutable(geometries), factory);
	}

	@Override
	public void normalize() {
		throw new UnsupportedOperationException("GeometryCollection immutable");
	}

	@Override
	public Geometry norm() {
		GeometryCollection mutable = getMutable();
		
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
	public ImmutableGeometryCollection clone() {
		return this;
	}
	
	// TODO override guarded apply methods

}
