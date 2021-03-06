package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static de.tu_berlin.mailbox.rjasper.collect.Immutables.immutable;
import static de.tu_berlin.mailbox.rjasper.collect.ImmutablesCollectors.toImmutableList;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableMultiPolygon;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.GeometryExtracter;

import de.tu_berlin.mailbox.rjasper.collect.CollectionsRequire;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;

/**
 * The {@code World} represents the physical outside world containing any
 * independent static or dynamic obstacles. Once created the world cannot be
 * changed.
 *
 * @author Rico Jasper
 */
public class World {

	/**
	 * The stationary obstacles of this world.
	 */
	private final ImmutableCollection<StaticObstacle> staticObstacles;

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
		this(ImmutableList.of(), ImmutableList.of());
	}

	/**
	 * Constructs a new {@code World} with static and dynamic obstacles.
	 *
	 * @param staticObstacles
	 * @param dynamicObstacles
	 * @throws NullPointerException
	 *             if any collection is {@code null} or contains {@code null}.
	 */
	public World(
		ImmutableCollection<StaticObstacle> staticObstacles,
		ImmutableCollection<DynamicObstacle> dynamicObstacles)
	{
		CollectionsRequire.requireNonNull(staticObstacles, "staticObstacles");
		CollectionsRequire.requireNonNull(dynamicObstacles, "dynamicObstacles");

		this.staticObstacles = immutable(staticObstacles);
		this.dynamicObstacles = immutable(dynamicObstacles);
		this.map = makeMap(staticObstacles);
	}

	/**
	 * Creates the map geometry from static obstacles.
	 *
	 * @param staticObstacles
	 * @return the map.
	 */
	private static Geometry makeMap(Collection<StaticObstacle> staticObstacles) {
		ImmutablePolygon[] shapes = staticObstacles.stream()
			.map(StaticObstacle::getShape)
			.toArray(n -> new ImmutablePolygon[n]);

		Geometry map = immutableMultiPolygon(shapes);

		// multipolygon might be self-intersecting
		if (!map.isValid()) {
			Geometry union = map.union(); // produces GeometryCollection

			@SuppressWarnings("unchecked")
			List<Polygon> polygons = (List<Polygon>) GeometryExtracter.extract(union, Polygon.class);

			map = immutableMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
		}

		return map;
	}

	/**
	 * @return the stationary obstacles of this world.
	 */
	public ImmutableCollection<StaticObstacle> getStaticObstacles() {
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

		ImmutableList<StaticObstacle> staticObstacles = getStaticObstacles().stream()
			.map(o -> o.buffer(distance)) // buffer always returns a polygon
			.collect(toImmutableList());
		ImmutableList<DynamicObstacle> dynamicObstacles = getDynamicObstacles().stream()
			.map(o -> o.buffer(distance))
			.collect(toImmutableList());

		return new World(staticObstacles, dynamicObstacles);
	}

}
