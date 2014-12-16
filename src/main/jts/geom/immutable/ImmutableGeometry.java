package jts.geom.immutable;

import com.vividsolutions.jts.geom.Geometry;

interface ImmutableGeometry {
	
	public abstract Geometry getMutable();
	
}
