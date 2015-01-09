package world;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import tasks.WorkerUnit;
import world.pathfinder.SpatialPathfinder;

/**
 * The {@code RadiusBasedWorldPerspectiveCache} is an implementation of the
 * {@link WorldPerspectiveCache}. The perspective on the original world is
 * determined by the radius of the individual worker units. Worker units of
 * the same radius will perceive the same view on the world.
 * 
 * @author Rico
 */
public class RadiusBasedWorldPerspectiveCache extends WorldPerspectiveCache {

	/**
	 * The perspective references of each known worker.
	 */
	private final Map<WorkerUnit, WorldPerspectiveReference> perceiverReferences =
		new IdentityHashMap<>();

	/**
	 * The perspective references of each known radius.
	 */
	private final Map<Double, WorldPerspectiveReference> radiusReferences =
		new HashMap<>();

	/**
	 * A helper class to track the amount of references per world perspective.
	 */
	private static class WorldPerspectiveReference {
		/**
		 * The referenced perspective.
		 */
		private final WorldPerspective perspective;
		
		/**
		 * The radius.
		 */
		private final double radius;
		
		/**
		 * The amount of perceivers of the perspective.
		 */
		private int refCount = 0;

		/**
		 * Constructs a new reference of a world perspective with the given radius.
		 * 
		 * @param radius
		 * @param perspective
		 */
		public WorldPerspectiveReference(double radius, WorldPerspective perspective) {
			this.perspective = perspective;
			this.radius = radius;
		}

		/**
		 * @return the perspective.
		 */
		public WorldPerspective getPerspective() {
			return perspective;
		}
		
		/**
		 * @return {@code true} if the perspective is referenced.
		 */
		public boolean isReferenced() {
			return refCount > 0;
		}

		/**
		 * @return radius of the perspective.
		 */
		public double getRadius() {
			return radius;
		}

		/**
		 * Increments the reference counter.
		 */
		public void incrementRef() {
			++refCount;
		}

		/**
		 * Decrements the reference counter.
		 */
		public void decrementRef() {
			--refCount;
		}
	}

	/**
	 * Constructs a new cache of the given world.
	 * The given class is used to create new pathfinders for the world's views.
	 * 
	 * @param world
	 * @param spatialPathfinderClass
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	public RadiusBasedWorldPerspectiveCache(
		World world,
		Class<? extends SpatialPathfinder> spatialPathfinderClass)
	{
		super(world, spatialPathfinderClass);
	}

	/**
	 * Constructs a new cache of the given world.
	 * The given class is used to create new pathfinders for the world's
	 * views.
	 * 
	 * @param world
	 * @param spatialPathfinderClass
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	public RadiusBasedWorldPerspectiveCache(
		World world, Supplier<? extends SpatialPathfinder> spatialPathfinderSupplier)
	{
		super(world, spatialPathfinderSupplier);
	}

	/**
	 * Adds a new reference for the given radius.
	 * 
	 * @param radius
	 * @param reference
	 */
	private void addRadiusReference(double radius, WorldPerspectiveReference reference) {
		Objects.requireNonNull(reference, "reference");
		
		if (Double.isNaN(radius) || radius < 0)
			throw new IllegalArgumentException("illegal radius");
		
		radiusReferences.put(radius, reference);
	}

	/**
	 * Removes a reference of the given radius.
	 * 
	 * @param radius
	 */
	private void removeRadiusReference(double radius) {
		radiusReferences.remove(radius);
	}

	/**
	 * Looks up a reference of the given radius.
	 * 
	 * @param radius
	 * @return the reference.
	 */
	private WorldPerspectiveReference lookUpByRadius(double radius) {
		return radiusReferences.get(radius);
	}

	/**
	 * Adds a new reference for the given receiver.
	 * 
	 * @param perceiver
	 * @param reference
	 */
	private void addPerceiverReference(WorkerUnit perceiver, WorldPerspectiveReference reference) {
		perceiverReferences.put(perceiver, reference);
		reference.incrementRef();
	}

	/**
	 * Removes a reference of the given perceiver.
	 * 
	 * @param perceiver
	 */
	private void removePerceiverReference(WorkerUnit perceiver) {
		WorldPerspectiveReference reference = perceiverReferences.remove(perceiver);
		reference.decrementRef();
	}

	/**
	 * Looks up a reference of the given perceiver.
	 * 
	 * @param perceiver
	 * @return the reference.
	 */
	private WorldPerspectiveReference lookUpByPerceiver(WorkerUnit perceiver) {
		return perceiverReferences.get(perceiver);
	}

	/**
	 * Creates a new perspective for the given radius.
	 * 
	 * @param radius
	 * @return the perspective.
	 */
	private WorldPerspectiveReference createPerspective(double radius) {
		World world = getWorld().buffer(radius);
	
		SpatialPathfinder spatialPathfinder = createSpatialPathfinder();
		spatialPathfinder.setStaticObstacles(world.getStaticObstacles());
	
		WorldPerspective perspective = new WorldPerspective(world, spatialPathfinder);
		WorldPerspectiveReference reference = new WorldPerspectiveReference(radius, perspective);
	
		return reference;
	}

	@Override
	public WorldPerspective getPerspectiveFor(WorkerUnit perceiver) {
		Objects.requireNonNull(perceiver, "perceiver");
		
		WorldPerspectiveReference reference;

		// perceiver might already be known
		reference = lookUpByPerceiver(perceiver);

		// if perceiver is unknown
		if (reference == null) {
			double radius = perceiver.getRadius();
			
			// perspective for radius might already exist
			reference = lookUpByRadius(radius);

			// perspective for radius does not exist yet
			if (reference == null) {
				reference = createPerspective(radius);
				addRadiusReference(radius, reference);
			}

			addPerceiverReference(perceiver, reference);
		}

		return reference.getPerspective();
	}

	@Override
	public void removePerceiver(WorkerUnit perceiver) {
		Objects.requireNonNull(perceiver, "perceiver");
		
		WorldPerspectiveReference reference = lookUpByPerceiver(perceiver);

		if (reference == null)
			throw new IllegalArgumentException("perceiver unknown");

		removePerceiverReference(perceiver);

		if (!reference.isReferenced())
			removeRadiusReference(reference.getRadius());
	}

}
