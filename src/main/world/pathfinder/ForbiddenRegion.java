package world.pathfinder;

import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.util.Objects;

import jts.geom.util.GeometriesRequire;
import world.DynamicObstacle;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Represents a forbidden region in the arc-time plane. A {@link Geometry}
 * represents an area of arc-time pairs on a curve. The arc results in a
 * position of a trajectory which is unavailable at certain times due to dynamic
 * obstacles. Such arc-time pairs belong therefore to a forbidden region.
 * 
 * @author Rico
 */
public class ForbiddenRegion { 
	
	/**
	 * The geometry representing the forbidden region.
	 */
	private final Geometry region;
	
	/**
	 * The dynamic obstacle responsible for this forbidden region.
	 */
	private final DynamicObstacle dynamicObstacle;

	/**
	 * Constructs a new forbidden region.
	 * 
	 * @param region
	 *            representing the area of the forbidden region.
	 * @param dynamicObstacle
	 *            causing the forbidden region.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if region is empty, invalid, non-simple or not 2D.
	 */
	public ForbiddenRegion(Geometry region, DynamicObstacle dynamicObstacle) {
		Objects.requireNonNull(dynamicObstacle, "dynamicObstacle");
		GeometriesRequire.requireValidSimple2DGeometry(region, "region");
		
		this.region = immutable(region);
		this.dynamicObstacle = dynamicObstacle;
	}

	/**
	 * @return the geometry representing the forbidden region.
	 */
	public Geometry getRegion() {
		return region;
	}

	/**
	 * @return the dynamic obstacle responsible for this forbidden region.
	 */
	public DynamicObstacle getDynamicObstacle() {
		return dynamicObstacle;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return region.toString();
	}

}
