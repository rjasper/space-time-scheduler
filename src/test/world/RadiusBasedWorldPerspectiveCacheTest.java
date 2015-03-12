package world;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static matchers.GeometryMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static world.factories.PerspectiveCacheFactory.*;

import java.util.Collection;
import java.util.function.Supplier;

import jts.geom.immutable.ImmutablePolygon;

import org.junit.Test;

import scheduler.WorkerUnit;
import scheduler.factories.WorkerUnitFactory;
import world.pathfinder.AbstractSpatialPathfinder;
import world.pathfinder.StraightEdgePathfinder;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Polygon;

public class RadiusBasedWorldPerspectiveCacheTest {
	
	private static final WorkerUnitFactory wFact = WorkerUnitFactory.getInstance();

	@Test(expected = NullPointerException.class)
	public void testNullWorldInitialization() {
		new RadiusBasedWorldPerspectiveCache(null, StraightEdgePathfinder.class);
	}

	@Test(expected = NullPointerException.class)
	public void testNullPathfinderClassInitialization() {
		new RadiusBasedWorldPerspectiveCache(new World(), (Class<AbstractSpatialPathfinder>) null);
	}

	@Test(expected = NullPointerException.class)
	public void testNullPathfinderSupplierInitialization() {
		new RadiusBasedWorldPerspectiveCache(new World(), (Supplier<AbstractSpatialPathfinder>) null);
	}

	@Test
	public void testCreatePerspective() {
		WorkerUnit perceiver = wFact.createWorkerUnit("perceiver", 0.0, 0.0);
		WorldPerspectiveCache cache = emptyPerspectiveCache();

		WorldPerspective perspective = cache.getPerspectiveFor(perceiver);

		assertThat("did not create a perspective",
			perspective, is(notNullValue(WorldPerspective.class)));
	}

	@Test
	public void testRecallPerspective() {
		WorkerUnit perceiver = wFact.createWorkerUnit("perceiver", 0.0, 0.0);
		WorldPerspectiveCache cache = emptyPerspectiveCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver);

		assertThat("did not recall perspective",
			perspective1, is(sameInstance(perspective2)));
	}

	@Test
	public void testReusePerspective() {
		// Both perceivers have the same shape and therefore the same perspective.
		WorkerUnit perceiver1 = wFact.createWorkerUnit("perceiver1",  0.0,  0.0);
		WorkerUnit perceiver2 = wFact.createWorkerUnit("perceiver2", 50.0, 50.0);
		WorldPerspectiveCache cache = emptyPerspectiveCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver1);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver2);

		assertThat("did not reuse perspective",
			perspective1, is(sameInstance(perspective2)));
	}

	@Test
	public void testUsesDifferentPerspectives() {
		ImmutablePolygon shape1 = immutableBox(-1.0, -1.0, 1.0, 1.0);
		ImmutablePolygon shape2 = immutableBox(-2.0, -2.0, 2.0, 2.0);
		WorkerUnit perceiver1 = wFact.createWorkerUnit("perceiver1", shape1, 1.0,  0.0,  0.0, 0.0);
		WorkerUnit perceiver2 = wFact.createWorkerUnit("perceiver2", shape2, 1.0, 50.0, 50.0, 0.0);
		WorldPerspectiveCache cache = emptyPerspectiveCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver1);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver2);

		assertThat("did not create different perspectives",
			perspective1, is(not(sameInstance(perspective2))));
	}

	@Test
	public void testRemovePerspective() {
		WorkerUnit perceiver = wFact.createWorkerUnit("perceiver", 0.0, 0.0);
		WorldPerspectiveCache cache = emptyPerspectiveCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver);
		cache.removePerceiver(perceiver);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver);

		assertThat("did not remove perspective",
			perspective1, is(not(sameInstance(perspective2))));
	}

	@Test
	public void testBufferPerspective() {
		WorkerUnit perceiver = wFact.createWorkerUnit("perceiver", 0.0, 0.0);
		StaticObstacle obstacle = new StaticObstacle(
			immutableBox(10.0, 10.0, 20.0, 20.0));
		WorldPerspectiveCache cache = perspectiveCache(ImmutableList.of(obstacle));

		WorldPerspective perspective = cache.getPerspectiveFor(perceiver);

		double radius = perceiver.getRadius();
		Collection<StaticObstacle> bufferedObstacles = perspective.getView().getStaticObstacles();
		StaticObstacle bufferedObstacle = bufferedObstacles.iterator().next();
		Polygon bufferedShape = bufferedObstacle.getShape();

		assertThat("did not buffer perspective correctly",
			bufferedShape, is(topologicallyEqualTo(obstacle.getShape().buffer(radius))));
	}

}
