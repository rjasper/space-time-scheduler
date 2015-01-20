package world;

import static jts.geom.immutable.ImmutableGeometries.*;
import jts.geom.immutable.ImmutablePolygon;
import jts.geom.util.GeometriesRequire;

import com.vividsolutions.jts.geom.Polygon;

/**
 * A {@code StaticObstacle} represents a physical object in the real world. It
 * describes the shape of such an object.
 * 
 * @author Rico
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
	public StaticObstacle(Polygon shape) {
		this.shape = immutable(GeometriesRequire.requireValidSimple2DPolygon(shape, "shape"));
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
		
		return new StaticObstacle((Polygon) shape.buffer(distance));
	}

}
