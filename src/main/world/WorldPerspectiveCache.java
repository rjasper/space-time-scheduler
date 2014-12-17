package world;

import java.util.function.Supplier;

import tasks.WorkerUnit;
import world.pathfinder.SpatialPathfinder;

public abstract class WorldPerspectiveCache {

	private final World world;

	private final Supplier<? extends SpatialPathfinder> spatialPathfinderSupplier;

	public WorldPerspectiveCache(World world, Supplier<? extends SpatialPathfinder> spatialPathfinderSupplier) {
		if (world == null)
			throw new NullPointerException("world is null");
		if (spatialPathfinderSupplier == null)
			throw new NullPointerException("spatialPathfinderSupplier is null");

		this.world = world;
		this.spatialPathfinderSupplier = spatialPathfinderSupplier;
	}

	public WorldPerspectiveCache(World world, Class<? extends SpatialPathfinder> spatialPathfinderClass) {
		this(world, makeSpatialPathfinderSupplier(spatialPathfinderClass));
	}

	private static Supplier<? extends SpatialPathfinder> makeSpatialPathfinderSupplier(
		Class<? extends SpatialPathfinder> spatialPathfinderClass)
	{
		if (spatialPathfinderClass == null)
			throw new NullPointerException("spatialPathfinderClass is null");

		return () -> {
			try {
				return spatialPathfinderClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		};
	}

	protected SpatialPathfinder createSpatialPathfinder() {
		return spatialPathfinderSupplier.get();
	}

	protected World getWorld() {
		return world;
	}

	public abstract WorldPerspective getPerspectiveFor(WorkerUnit perceiver);

	public abstract void removePerceiver(WorkerUnit perceiver);

}
