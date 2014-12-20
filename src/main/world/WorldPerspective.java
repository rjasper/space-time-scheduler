package world;

import java.util.Objects;

import world.pathfinder.SpatialPathfinder;

public class WorldPerspective {

	private final World world;

	private final SpatialPathfinder spatialPathfinder;

	public WorldPerspective(World world, SpatialPathfinder spatialPathfinder) {
		Objects.requireNonNull(world, "world");
		Objects.requireNonNull(spatialPathfinder, "spatialPathfinder");

		spatialPathfinder.setStaticObstacles(world.getStaticObstacles());

		this.world = world;
		this.spatialPathfinder = spatialPathfinder;
	}

	public World getWorld() {
		return world;
	}

	public SpatialPathfinder getSpatialPathfinder() {
		return spatialPathfinder;
	}

}
