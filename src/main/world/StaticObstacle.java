package world;

import static jts.geom.immutable.ImmutableGeometries.*;
import jts.geom.immutable.ImmutablePolygon;
import jts.geom.util.GeometriesRequire;

import com.vividsolutions.jts.geom.Polygon;

// TODO document
public class StaticObstacle {
	
	private final ImmutablePolygon shape;

	public StaticObstacle(Polygon shape) {
		this.shape = immutable(GeometriesRequire.requireValidSimple2DPolygon(shape, "shape"));
	}

	public ImmutablePolygon getShape() {
		return shape;
	}

	public StaticObstacle buffer(double distance) {
		if (!Double.isFinite(distance) || distance < 0.0)
			throw new IllegalArgumentException("invalid distance");
		
		return new StaticObstacle((Polygon) shape.buffer(distance));
	}

}
