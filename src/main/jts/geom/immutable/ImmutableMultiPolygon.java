package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.immutable;
import static jts.geom.immutable.ImmutableGeometries.mutable;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

class ImmutableMultiPolygon extends MultiPolygon implements ImmutableGeometry {

	private static final long serialVersionUID = 2982709814063389648L;

	protected ImmutableMultiPolygon(Polygon[] polygons, GeometryFactory factory) {
		super(immutable(polygons), factory);
	}

	protected ImmutableMultiPolygon(MultiPolygon multiPolygon) {
		this(retrievePolygons(multiPolygon), multiPolygon.getFactory());
	}
	
	private static Polygon[] retrievePolygons(MultiPolygon multiPolygon) {
		int n = multiPolygon.getNumGeometries();
		
		Polygon[] polygons = new Polygon[n];
		for (int i = 0; i < n; ++i)
			polygons[i] = (Polygon) multiPolygon.getGeometryN(i);
		
		return polygons;
	}

	@Override
	public MultiPolygon getMutable() {
		Polygon[] polygon = (Polygon[]) geometries;
		
		return new MultiPolygon(mutable(polygon), factory);
	}

	@Override
	public void normalize() {
		throw new UnsupportedOperationException("MultiPolygon immutable");
	}

	@Override
	public MultiPolygon norm() {
		MultiPolygon mutable = getMutable();
		
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
	public ImmutableMultiPolygon clone() {
		return this;
	}
	
	// TODO override guarded apply methods

}
