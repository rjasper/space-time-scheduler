package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.immutable;
import static jts.geom.immutable.ImmutableGeometries.mutable;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class ImmutablePolygon extends Polygon implements ImmutableGeometry {

	private static final long serialVersionUID = -1394928736389998201L;

	public ImmutablePolygon(Polygon polygon) {
		this(retrieveShell(polygon), retrieveHoles(polygon), polygon.getFactory());
	}

	public ImmutablePolygon(LinearRing shell, LinearRing[] holes, GeometryFactory factory) {
		super(immutable(shell), immutable(holes), factory);
	}

//	ImmutablePolygon(ImmutableLinearRing shell, ImmutableLinearRing[] holes, ImmutableGeometryFactory factory, boolean shared) {
//		super(shell, holes, factory);
//		assert shared;
//	}
	
	private static LinearRing retrieveShell(Polygon polygon) {
		return (LinearRing) polygon.getExteriorRing();
	}
	
	private static LinearRing[] retrieveHoles(Polygon polygon) {
		int n = polygon.getNumInteriorRing();
		
		LinearRing[] holes = new LinearRing[n];
		for (int i = 0; i < n; ++i)
			holes[i] = (LinearRing) polygon.getInteriorRingN(i);
		
		return holes;
	}

	@Override
	public Polygon getMutable() {
		return new Polygon(mutable(shell), mutable(holes), factory);
	}

	@Override
	public void normalize() {
		throw new UnsupportedOperationException("Polygon immutable");
	}

	@Override
	public Polygon norm() {
		Polygon mutable = getMutable();
		
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
	public ImmutablePolygon clone() {
		return this;
	}
	
	// TODO override guarded apply methods

}
