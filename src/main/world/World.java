package world;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import jts.geom.factories.EnhancedGeometryBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.GeometryCombiner;

public class World {

	private final Collection<Polygon> staticObstacles;

	private final Geometry map;

	public World() {
		this(emptyList());
	}

	public World(Collection<Polygon> staticObstacles) {
		this.staticObstacles = new ArrayList<>(staticObstacles);

		this.map = makeMap(staticObstacles);
	}

	private Geometry makeMap(Collection<Polygon> staticObstacles) {
		// for some reason the geometry combinder returns null when receiving
		// an empty list instead of some empty geometry
		if (staticObstacles.size() == 0) {
			EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

			return geomBuilder.polygon();
		}

		GeometryCombiner combinder = new GeometryCombiner(staticObstacles);
		Geometry map = combinder.combine();

		return map;
	}

	public Geometry getMap() {
		return map;
	}

	public Collection<Polygon> getPolygonMap() {
		return Collections.unmodifiableCollection( getStaticObstacles() );
	}

	private Collection<Polygon> getStaticObstacles() {
		return this.staticObstacles;
	}

	public Geometry space(Geometry mask) {
		Geometry map = getMap();
		Geometry space = mask.difference(map);

		return space;
	}

	public World buffer(double distance) {
		Collection<Polygon> staticObstacles = getStaticObstacles().stream()
			.map(o -> (Polygon) o.buffer(distance)) // buffer always returns a polygon
			.collect(toList());

		return new World(staticObstacles);
	}

}
