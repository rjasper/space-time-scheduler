package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import java.util.Objects;
import java.util.function.Supplier;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.AbstractSpatialPathfinder;

/**
 * Caches the perspectives of multiple node units and takes advantage of
 * similar perspective by reusing existing ones.
 * 
 * @author Rico Jasper
 */
public abstract class WorldPerspectiveCache {

	/**
	 * The original world.
	 */
	private final World world;

	/**
	 * The spatial pathfinder supplier. Supplies pathfinders for world views.
	 */
	private final Supplier<? extends AbstractSpatialPathfinder> spatialPathfinderSupplier;

	/**
	 * Constructs a new {@code WorldPerspectiveCache} of the given world.
	 * The given supplier is used to create new pathfinders for the world's
	 * views.
	 * 
	 * @param world
	 * @param spatialPathfinderSupplier
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	public WorldPerspectiveCache(World world, Supplier<? extends AbstractSpatialPathfinder> spatialPathfinderSupplier) {
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
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	public WorldPerspectiveCache(World world, Class<? extends AbstractSpatialPathfinder> spatialPathfinderClass) {
		this(world, makeSpatialPathfinderSupplier(spatialPathfinderClass));
	}

	/**
	 * Makes a supplier using the given spatial pathfinder class.
	 * 
	 * @param spatialPathfinderClass
	 * @return the supplier.
	 */
	private static Supplier<? extends AbstractSpatialPathfinder> makeSpatialPathfinderSupplier(
		Class<? extends AbstractSpatialPathfinder> spatialPathfinderClass)
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
	protected AbstractSpatialPathfinder createSpatialPathfinder() {
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
	 * @throws NullPointerException if {@code perceiver} is {@code null}.
	 */
	public abstract WorldPerspective getPerspectiveFor(Node perceiver);

	/**
	 * Removes the perceiver from the known perceivers. Might triggers to
	 * remove a cached world perspective.
	 * 
	 * @param perceiver
	 * @throws NullPointerException if {@code perceiver} is {@code null}.
	 */
	public abstract void removePerceiver(Node perceiver);

}
