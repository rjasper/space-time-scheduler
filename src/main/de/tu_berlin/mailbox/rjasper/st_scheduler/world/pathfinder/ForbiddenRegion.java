package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.immutable;

import java.util.Objects;

import com.vividsolutions.jts.geom.Geometry;

import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.DynamicObstacle;

/**
 * Represents a forbidden region in the arc-time plane. A {@link Geometry}
 * represents an area of arc-time pairs on a curve. The arc results in a
 * position of a trajectory which is unavailable at certain times due to dynamic
 * obstacles. Such arc-time pairs belong therefore to a forbidden region.
 *
 * @author Rico Jasper
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
	 *             if region is empty, invalid, non-simple.
	 */
	public ForbiddenRegion(Geometry region, DynamicObstacle dynamicObstacle) {
		Objects.requireNonNull(dynamicObstacle, "dynamicObstacle");
		GeometriesRequire.requireValidSimple2DGeometry(region, "region");

		// allow region of any dimension
//		if (!(region instanceof Polygon || region instanceof MultiPolygon))
//			throw new IllegalArgumentException("illegal region");

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
