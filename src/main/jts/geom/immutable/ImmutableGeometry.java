package jts.geom.immutable;

import com.vividsolutions.jts.geom.Geometry;

public interface ImmutableGeometry {
	
	public abstract Geometry getMutable();
	
}
