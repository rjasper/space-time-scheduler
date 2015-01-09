package world;

import java.util.Objects;
import java.util.function.Supplier;

import tasks.WorkerUnit;
import world.pathfinder.SpatialPathfinder;

/**
 * Caches the perspectives of multiple worker units and takes advantage of
 * similar perspective by reusing existing ones.
 * 
 * @author Rico
 */
public abstract class WorldPerspectiveCache {

	/**
	 * The original world.
	 */
	private final World world;

	/**
	 * The spatial pathfinder supplier. Supplies pathfinders for world views.
	 */
	private final Supplier<? extends SpatialPathfinder> spatialPathfinderSupplier;

	/**
	 * Constructs a new {@code WorldPerspectiveCache} of the given world.
	 * The given supplier is used to create new pathfinders for the world's
	 * views.
	 * 
	 * @param world
	 * @param spatialPathfinderSupplier
	 */
	public WorldPerspectiveCache(World world, Supplier<? extends SpatialPathfinder> spatialPathfinderSupplier) {
		this.world = Objects.requireNonNull(world, "world");
		this.spatialPathfinderSupplier =
			Objects.requireNonNull(spatialPathfinderSupplier, "spatialPathfinderSupplier");
	}

	/**
	 * Constructs a new {@code WorldPerspectiveCache} of the given world.
	 * The given class is used to create new pathfinders for the world's
	 * views.
	 * 
	 * @param world
	 * @param spatialPathfinderClass
	 */
	public WorldPerspectiveCache(World world, Class<? extends SpatialPathfinder> spatialPathfinderClass) {
		this(world, makeSpatialPathfinderSupplier(spatialPathfinderClass));
	}

	/**
	 * Makes a supplier using the given spatial pathfinder class.
	 * 
	 * @param spatialPathfinderClass
	 * @return the supplier.
	 */
	private static Supplier<? extends SpatialPathfinder> makeSpatialPathfinderSupplier(
		Class<? extends SpatialPathfinder> spatialPathfinderClass)
	{
		Objects.requireNonNull(spatialPathfinderClass, "spatialPathfinderClass");

		return () -> {
			try {
				return spatialPathfinderClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		};
	}

	/**
	 * @return creates a new spatial pathfinder.
	 */
	protected SpatialPathfinder createSpatialPathfinder() {
		return spatialPathfinderSupplier.get();
	}

	/**
	 * @return the original world.
	 */
	protected World getWorld() {
		return world;
	}

	/**
	 * Returns the perspective of the world for the given perceiver
	 * 
	 * @param perceiver
	 * @return the perspective.
	 */
	public abstract WorldPerspective getPerspectiveFor(WorkerUnit perceiver);

	/**
	 * Removes the perceiver from the known perceivers. Might triggers to
	 * remove a cached world perspective.
	 * 
	 * @param perceiver
	 */
	public abstract void removePerceiver(WorkerUnit perceiver);

}
