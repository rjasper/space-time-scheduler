package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.*;

import com.vividsolutions.jts.geom.Polygon;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;

/**
 * A {@code StaticObstacle} represents a physical object in the real world. It
 * describes the shape of such an object.
 * 
 * @author Rico Jasper
 */
public class StaticObstacle {
	
	/**
	 * The physical shape.
	 */
	private final ImmutablePolygon shape;

	/**
	 * Creates a new {@code StaticObstacle} with the given physical shape.
	 * 
	 * @param shape
	 * @throws NullPointerException
	 *             if {@code shape} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the shape is empty, non-simple, or invalid.
	 */
	public StaticObstacle(ImmutablePolygon shape) {
		this.shape = GeometriesRequire.requireValidSimple2DPolygon(shape, "shape");
	}

	/**
	 * @return the shape.
	 */
	public ImmutablePolygon getShape() {
		return shape;
	}

	/**
	 * Creates a new {@code StaticObstacle} with a buffered version of this
	 * one's shape.
	 * 
	 * @param distance
	 *            of the buffer
	 * @return the buffered version.
	 * @throws IllegalArgumentException
	 *             if {@code distance} is not a positive finite number.
	 */
	public StaticObstacle buffer(double distance) {
		if (!Double.isFinite(distance) || distance < 0.0)
			throw new IllegalArgumentException("invalid distance");
		
		Polygon buffered = (Polygon) shape.buffer(distance);
		
		return new StaticObstacle(immutable(buffered));
	}

}
