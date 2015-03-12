package de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.RadiusBasedWorldPerspectiveCache;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspectiveCache;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.StraightEdgePathfinder;

/**
 * Provides static methods to construct {@link PerspectiveCache}s.
 * 
 * @author Rico Jasper
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
