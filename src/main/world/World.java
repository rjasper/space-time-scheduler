package world;

import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.util.ArrayList;
import java.util.Collection;

import jts.geom.factories.EnhancedGeometryBuilder;
import util.CollectionsRequire;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.GeometryCombiner;

/**
 * The {@code World} represents the physical outside world containing any
 * independent static or dynamic obstacles. Once created the world cannot be
 * changed.
 * 
 * @author Rico
 */
public class World {

	/**
	 * The stationary obstacles of this world.
	 */
	private final Collection<Polygon> staticObstacles;
	
	/**
	 * The moving obstacles of this world.
	 */
	private final Collection<DynamicObstacle> dynamicObstacles;

	/**
	 * The union of all static obstacles.
	 */
	private final Geometry map;

	/**
	 * Creates an empty World without any obstacles.
	 */
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
		
		this.staticObstacles = unmodifiableCollection( immutable(staticObstacles) );
		this.dynamicObstacles = unmodifiableCollection( new ArrayList<>(dynamicObstacles) );
		this.map = immutable(makeMap(staticObstacles));
	}

	/**
	 * Creates the map geometry from static obstacles.
	 * 
	 * @param staticObstacles
	 * @return the map.
	 */
	private static Geometry makeMap(Collection<Polygon> staticObstacles) {
		// for some reason the geometry combiner returns null when receiving
		// an empty list instead of some empty geometry
		if (staticObstacles.size() == 0) {
			EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

			return geomBuilder.polygon();
		}

		GeometryCombiner combinder = new GeometryCombiner(staticObstacles);
		Geometry map = combinder.combine();

		return map;
	}

	/**
	 * @return the stationary obstacles of this world.
	 */
	public Collection<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	/**
	 * @return the moving obstacles of this world.
	 */
	public Collection<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}

	/**
	 * @return the map which is the union of all static obstacles.
	 */
	public Geometry getMap() {
		return map;
	}

	/**
	 * Calculates the free space of an area of the worlds map.
	 * 
	 * @param mask the area of interest
	 * @return the free space within the mask.
	 */
	public Geometry space(Geometry mask) {
		Geometry map = getMap();
		Geometry space = mask.difference(map);

		return space;
	}

	/**
	 * Calculates a new World with all obstacles buffer by a given amount.
	 * 
	 * @param distance of the buffer
	 * @return the buffered world.
	 */
	public World buffer(double distance) {
		Collection<Polygon> staticObstacles = getStaticObstacles().stream()
			.map(o -> (Polygon) o.buffer(distance)) // buffer always returns a polygon
			.collect(toList());
		Collection<DynamicObstacle> dynamicObstacles = getDynamicObstacles().stream()
			.map(o -> o.buffer(distance))
			.collect(toList());

		return new World(staticObstacles, dynamicObstacles);
	}

}
