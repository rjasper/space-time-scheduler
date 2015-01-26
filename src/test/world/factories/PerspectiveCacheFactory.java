package world.factories;

import world.RadiusBasedWorldPerspectiveCache;
import world.StaticObstacle;
import world.World;
import world.WorldPerspectiveCache;
import world.pathfinder.StraightEdgePathfinder;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

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
		return perspectiveCache(ImmutableList.of());
	}

	/**
	 * Constructs a new {@code WorldPerspectiveCache} for a world containing
	 * the given static obstacles.
	 * 
	 * @param staticObstacles
	 * @return the cache.
	 */
	public static WorldPerspectiveCache perspectiveCache(ImmutableCollection<StaticObstacle> staticObstacles) {
		World world = new World(staticObstacles, ImmutableList.of());
		RadiusBasedWorldPerspectiveCache cache =
			new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);

		return cache;
	}

}
