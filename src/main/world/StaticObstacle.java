package world;

import static jts.geom.immutable.ImmutableGeometries.immutable;
import jts.geom.immutable.ImmutablePolygon;
import jts.geom.util.GeometriesRequire;

import com.vividsolutions.jts.geom.Polygon;

// TODO document
public class StaticObstacle {
	
	private final ImmutablePolygon shape;

	public StaticObstacle(ImmutablePolygon shape) {
		this.shape = GeometriesRequire.requireValidSimple2DPolygon(shape, "shape");
	}
	
	public StaticObstacle(Polygon shape) {
		this(immutable(shape));
	}

	public ImmutablePolygon getShape() {
		return shape;
	}

}
