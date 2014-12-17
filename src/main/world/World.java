package world;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import util.CollectionsRequire;
import jts.geom.factories.EnhancedGeometryBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.GeometryCombiner;

public class World {

	private final Collection<Polygon> staticObstacles;
	
	private final Collection<DynamicObstacle> dynamicObstacles;

	private final Geometry map;

	public World() {
		this(emptyList(), emptyList());
	}

	/**
	 * Constructs a new {@code World} with static and dynamic obstacles.
	 * 
	 * @param staticObstacles
	 * @param dynamicObstacles
	 * @throws NullPointerException
	 *             if any collection is {@code null} or contains {@code null}.
	 */
	public World(Collection<Polygon> staticObstacles, Collection<DynamicObstacle> dynamicObstacles) {
		CollectionsRequire.requireContainsNonNull(staticObstacles, "staticObstacles");
		CollectionsRequire.requireContainsNonNull(dynamicObstacles, "dynamicObstacles");
		
		this.staticObstacles = immutable(staticObstacles);
		this.dynamicObstacles = new ArrayList<>(dynamicObstacles);
		this.map = immutable(makeMap(staticObstacles));
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

	public Collection<Polygon> getStaticObstacles() {
		return Collections.unmodifiableCollection( _getStaticObstacles() );
	}
	
	private Collection<Polygon> _getStaticObstacles() {
		return staticObstacles;
	}

	public Collection<DynamicObstacle> getDynamicObstacles() {
		return Collections.unmodifiableCollection( _getDynamicObstacles() );
	}
	
	private Collection<DynamicObstacle> _getDynamicObstacles() {
		return dynamicObstacles;
	}

	public Geometry space(Geometry mask) {
		Geometry map = getMap();
		Geometry space = mask.difference(map);

		return space;
	}

	public World buffer(double distance) {
		Collection<Polygon> staticObstacles = _getStaticObstacles().stream()
			.map(o -> (Polygon) o.buffer(distance)) // buffer always returns a polygon
			.collect(toList());
		Collection<DynamicObstacle> dynamicObstacles = _getDynamicObstacles().stream()
			.map(o -> o.buffer(distance))
			.collect(toList());

		return new World(staticObstacles, dynamicObstacles);
	}

}
