package jts.geom.immutable;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Immutable geometries implement this interface.
 * 
 * @author Rico
 */
public interface ImmutableGeometry {
	
	/**
	 * @return an mutable version of this geometry.
	 */
	public abstract Geometry getMutable();
	
}
