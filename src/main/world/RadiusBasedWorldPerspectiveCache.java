package world;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import tasks.WorkerUnit;
import world.pathfinder.SpatialPathfinder;

public class RadiusBasedWorldPerspectiveCache extends WorldPerspectiveCache {

	private final Map<WorkerUnit, WorldPerspectiveReference> perceiverReferences =
		new IdentityHashMap<>();

	private final Map<Double, WorldPerspectiveReference> radiusReferences =
		new HashMap<>();

	private static class WorldPerspectiveReference {
		private final WorldPerspective perspective;
		private final double radius;
		private int refCount = 0;

		public WorldPerspectiveReference(double radius, WorldPerspective perspective) {
			this.perspective = perspective;
			this.radius = radius;
		}

		public WorldPerspective getPerspective() {
			return perspective;
		}

		public int getRefCount() {
			return refCount;
		}

		public double getRadius() {
			return radius;
		}

		public void incrementRef() {
			++refCount;
		}

		public void decrementRef() {
			--refCount;
		}
	}

	public RadiusBasedWorldPerspectiveCache(World world, Class<? extends SpatialPathfinder> spatialPathfinderClass) {
		super(world, spatialPathfinderClass);
	}

	public RadiusBasedWorldPerspectiveCache(World world, Supplier<? extends SpatialPathfinder> spatialPathfinderSupplier) {
		super(world, spatialPathfinderSupplier);
	}

	private void addRadiusReference(double radius, WorldPerspectiveReference reference) {
		radiusReferences.put(radius, reference);
	}

	private void removeRadiusReference(double radius) {
		radiusReferences.remove(radius);
	}

	private WorldPerspectiveReference lookUpByRadius(double radius) {
		return radiusReferences.get(radius);
	}

	private void addPerceiverReference(WorkerUnit perceiver, WorldPerspectiveReference reference) {
		perceiverReferences.put(perceiver, reference);
		reference.incrementRef();
	}

	private void removePerceiverReference(WorkerUnit perceiver) {
		perceiverReferences.remove(perceiver);
	}

	private WorldPerspectiveReference lookUpByPerceiver(WorkerUnit perceiver) {
		return perceiverReferences.get(perceiver);
	}

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
		WorldPerspectiveReference reference;

		reference = lookUpByPerceiver(perceiver);

		if (reference == null) {
			double radius = perceiver.getRadius();
			reference = lookUpByRadius(radius);

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
		WorldPerspectiveReference reference = lookUpByPerceiver(perceiver);

		if (reference == null)
			throw new IllegalArgumentException("perceiver unknown");

		removePerceiverReference(perceiver);
		reference.decrementRef();

		if (reference.getRefCount() == 0)
			removeRadiusReference(reference.getRadius());
	}

}
