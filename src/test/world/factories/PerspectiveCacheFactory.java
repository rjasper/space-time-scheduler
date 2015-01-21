package world.factories;

import static java.util.Collections.*;

import java.util.Collection;

import world.RadiusBasedWorldPerspectiveCache;
import world.StaticObstacle;
import world.World;
import world.WorldPerspectiveCache;
import world.pathfinder.StraightEdgePathfinder;

/**
 * Provides static methods to construct {@link PerspectiveCache}s.
 * 
 * @author Rico
 */
public final class PerspectiveCacheFactory {
	
	private PerspectiveCacheFactory() {}

	/**
	 * Constructs a new {@code WorldPerspectiveCache} for an empty world.
	 * 
	 * @return the cache.
	 */
	public static WorldPerspectiveCache emptyPerspectiveCache() {
		return perspectiveCache(emptyList());
	}

	/**
	 * Constructs a new {@code WorldPerspectiveCache} for a world containing
	 * the given static obstacles.
	 * 
	 * @param staticObstacles
	 * @return the cache.
	 */
	public static WorldPerspectiveCache perspectiveCache(Collection<StaticObstacle> staticObstacles) {
		World world = new World(staticObstacles, emptyList());
		RadiusBasedWorldPerspectiveCache cache =
			new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);

		return cache;
	}

}
