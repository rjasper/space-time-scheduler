package world;

import world.pathfinder.SpatialPathfinder;

public class WorldPerspective {

	private final World world;

	private final SpatialPathfinder spatialPathfinder;

	public WorldPerspective(World world, SpatialPathfinder spatialPathfinder) {
		if (world == null)
			throw new NullPointerException("world is null");
		if (spatialPathfinder == null)
			throw new NullPointerException("spatialPathfinder is null");

		spatialPathfinder.setStaticObstacles(world.getPolygonMap());

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
