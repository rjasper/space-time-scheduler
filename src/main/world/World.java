package world;

import static common.collect.Immutables.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static jts.geom.immutable.ImmutableGeometries.immutable;

import java.util.Collection;

import jts.geom.factories.EnhancedGeometryBuilder;
import jts.geom.util.GeometriesRequire;
import util.CollectionsRequire;

import com.google.common.collect.ImmutableCollection;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.GeometryCombiner;

import common.collect.Immutables;

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
	private final ImmutableCollection<Polygon> staticObstacles;
	
	/**
	 * The moving obstacles of this world.
	 */
	private final ImmutableCollection<DynamicObstacle> dynamicObstacles;

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
		
		this.staticObstacles = Immutables.immutable( immutable(staticObstacles) );
		this.dynamicObstacles = immutable(dynamicObstacles);
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
	public ImmutableCollection<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	/**
	 * @return the moving obstacles of this world.
	 */
	public ImmutableCollection<DynamicObstacle> getDynamicObstacles() {
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
	 * @param mask
	 *            the area of interest
	 * @return the free space within the mask.
	 * @throws NullPointerException
	 *             if {@code mask} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code mask} is empty, invalid, non-simple, or not 2D.
	 */
	public Geometry space(Geometry mask) {
		GeometriesRequire.requireValidSimple2DGeometry(mask, "mask");
		
		Geometry map = getMap();
		Geometry space = mask.difference(map);

		return space;
	}

	/**
	 * Calculates a new World with all obstacles buffer by a given amount.
	 * 
	 * @param distance of the buffer
	 * @return the buffered world.
	 * @throws IllegalArgumentException if {@code distance} is not finite.
	 */
	public World buffer(double distance) {
		if (!Double.isFinite(distance))
			throw new IllegalArgumentException("distance is not finite");
		
		Collection<Polygon> staticObstacles = getStaticObstacles().stream()
			.map(o -> (Polygon) o.buffer(distance)) // buffer always returns a polygon
			.collect(toList());
		Collection<DynamicObstacle> dynamicObstacles = getDynamicObstacles().stream()
			.map(o -> o.buffer(distance))
			.collect(toList());

		return new World(staticObstacles, dynamicObstacles);
	}

}
