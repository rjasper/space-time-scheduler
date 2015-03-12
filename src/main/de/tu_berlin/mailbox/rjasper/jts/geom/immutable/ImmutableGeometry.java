package de.tu_berlin.mailbox.rjasper.jts.geom.immutable;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Immutable geometries implement this interface.
 * 
 * @author Rico Jasper
 */
public interface ImmutableGeometry {
	
	/**
	 * @return an mutable version of this geometry.
	 */
	public abstract Geometry getMutable();
	
}
