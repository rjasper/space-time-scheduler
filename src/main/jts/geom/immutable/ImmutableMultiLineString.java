package jts.geom.immutable;

import static jts.geom.immutable.ImmutableGeometries.immutable;
import static jts.geom.immutable.ImmutableGeometries.mutable;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class ImmutableMultiLineString extends MultiLineString implements ImmutableGeometry {

	private static final long serialVersionUID = 5998127758585768022L;

	protected ImmutableMultiLineString(LineString[] lineStrings, GeometryFactory factory) {
		super(immutable(lineStrings), factory);
	}

	protected ImmutableMultiLineString(MultiLineString multiLineString) {
		this(retrieveLineStrings(multiLineString), multiLineString.getFactory());
	}

	private static LineString[] retrieveLineStrings(MultiLineString multiLineString) {
		int n = multiLineString.getNumGeometries();
		
		LineString[] lineStrings = new LineString[n];
		for (int i = 0; i < n; ++i)
			lineStrings[i] = (LineString) multiLineString.getGeometryN(i);
		
		return lineStrings;
	}

	@Override
	public MultiLineString getMutable() {
		LineString[] lineStrings = (LineString[]) geometries;
		
		return new MultiLineString(mutable(lineStrings), factory);
	}

	@Override
	public void normalize() {
		throw new UnsupportedOperationException("MultiLineString immutable");
	}

	@Override
	public MultiLineString norm() {
		MultiLineString mutable = getMutable();
		
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
	public ImmutableMultiLineString clone() {
		return this;
	}
	
	// TODO override guarded apply methods

}
