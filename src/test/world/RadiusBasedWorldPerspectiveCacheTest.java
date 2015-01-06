package world;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static matchers.GeometryMatchers.topologicallyEqualTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.function.Supplier;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import tasks.WorkerUnit;
import tasks.factories.WorkerUnitFactory;
import world.pathfinder.SpatialPathfinder;
import world.pathfinder.StraightEdgePathfinder;

import com.vividsolutions.jts.geom.Polygon;

public class RadiusBasedWorldPerspectiveCacheTest {

	private static final WorkerUnitFactory wFact = WorkerUnitFactory.getInstance();
	private static final EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

	@Test(expected = NullPointerException.class)
	public void testNullWorldInitialization() {
		new RadiusBasedWorldPerspectiveCache(null, StraightEdgePathfinder.class);
	}

	@Test(expected = NullPointerException.class)
	public void testNullPathfinderClassInitialization() {
		new RadiusBasedWorldPerspectiveCache(new World(), (Class<SpatialPathfinder>) null);
	}

	@Test(expected = NullPointerException.class)
	public void testNullPathfinderSupplierInitialization() {
		new RadiusBasedWorldPerspectiveCache(new World(), (Supplier<SpatialPathfinder>) null);
	}

	@Test
	public void testCreatePerspective() {
		WorkerUnit perceiver = wFact.createWorkerUnit(0.0, 0.0);
		WorldPerspectiveCache cache = makeEmptyCache();

		WorldPerspective perspective = cache.getPerspectiveFor(perceiver);

		assertThat("did not create a perspective",
			perspective, is(notNullValue(WorldPerspective.class)));
	}

	@Test
	public void testRecallPerspective() {
		WorkerUnit perceiver = wFact.createWorkerUnit(0.0, 0.0);
		WorldPerspectiveCache cache = makeEmptyCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver);

		assertThat("did not recall perspective",
			perspective1, is(sameInstance(perspective2)));
	}

	@Test
	public void testReusePerspective() {
		// Both perceivers have the same shape and therefore the same perspective.
		WorkerUnit perceiver1 = wFact.createWorkerUnit( 0.0,  0.0);
		WorkerUnit perceiver2 = wFact.createWorkerUnit(50.0, 50.0);
		WorldPerspectiveCache cache = makeEmptyCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver1);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver2);

		assertThat("did not reuse perspective",
			perspective1, is(sameInstance(perspective2)));
	}

	@Test
	public void testUsesDifferentPerspectives() {
		Polygon shape1 = geomBuilder.box(-1.0, -1.0, 1.0, 1.0);
		Polygon shape2 = geomBuilder.box(-2.0, -2.0, 2.0, 2.0);
		WorkerUnit perceiver1 = wFact.createWorkerUnit(shape1, 1.0,  0.0,  0.0, 0.0);
		WorkerUnit perceiver2 = wFact.createWorkerUnit(shape2, 1.0, 50.0, 50.0, 0.0);
		WorldPerspectiveCache cache = makeEmptyCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver1);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver2);

		assertThat("did not create different perspectives",
			perspective1, is(not(sameInstance(perspective2))));
	}

	@Test
	public void testRemovePerspective() {
		WorkerUnit perceiver = wFact.createWorkerUnit(0.0, 0.0);
		WorldPerspectiveCache cache = makeEmptyCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver);
		cache.removePerceiver(perceiver);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver);

		assertThat("did not remove perspective",
			perspective1, is(not(sameInstance(perspective2))));
	}

	@Test
	public void testBufferPerspective() {
		WorkerUnit perceiver = wFact.createWorkerUnit(0.0, 0.0);
		Polygon obstacle = geomBuilder.box(10.0, 10.0, 20.0, 20.0);
		WorldPerspectiveCache cache = makeCacheFrom(singleton(obstacle));

		WorldPerspective perspective = cache.getPerspectiveFor(perceiver);

		double radius = perceiver.getRadius();
		Collection<Polygon> bufferedObstacles = perspective.getView().getStaticObstacles();
		Polygon bufferedObstacle = bufferedObstacles.iterator().next();

		assertThat("did not buffer perspective correctly",
			bufferedObstacle, is(topologicallyEqualTo(obstacle.buffer(radius))));
	}

	private WorldPerspectiveCache makeEmptyCache() {
		return makeCacheFrom(emptyList());
	}

	private WorldPerspectiveCache makeCacheFrom(Collection<Polygon> staticObstacles) {
		World world = new World(staticObstacles, emptyList());
		RadiusBasedWorldPerspectiveCache cache =
			new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);

		return cache;
	}

}
