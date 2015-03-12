package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.GeometryMatchers.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.PerspectiveCacheFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.function.Supplier;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Polygon;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.factories.NodeFactory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.RadiusBasedWorldPerspectiveCache;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspective;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.WorldPerspectiveCache;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.AbstractSpatialPathfinder;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.StraightEdgePathfinder;

public class RadiusBasedWorldPerspectiveCacheTest {
	
	private static final NodeFactory wFact = NodeFactory.getInstance();

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
		Node perceiver = wFact.createNode("perceiver", 0.0, 0.0);
		WorldPerspectiveCache cache = emptyPerspectiveCache();

		WorldPerspective perspective = cache.getPerspectiveFor(perceiver);

		assertThat("did not create a perspective",
			perspective, is(notNullValue(WorldPerspective.class)));
	}

	@Test
	public void testRecallPerspective() {
		Node perceiver = wFact.createNode("perceiver", 0.0, 0.0);
		WorldPerspectiveCache cache = emptyPerspectiveCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver);

		assertThat("did not recall perspective",
			perspective1, is(sameInstance(perspective2)));
	}

	@Test
	public void testReusePerspective() {
		// Both perceivers have the same shape and therefore the same perspective.
		Node perceiver1 = wFact.createNode("perceiver1",  0.0,  0.0);
		Node perceiver2 = wFact.createNode("perceiver2", 50.0, 50.0);
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
		Node perceiver1 = wFact.createNode("perceiver1", shape1, 1.0,  0.0,  0.0, 0.0);
		Node perceiver2 = wFact.createNode("perceiver2", shape2, 1.0, 50.0, 50.0, 0.0);
		WorldPerspectiveCache cache = emptyPerspectiveCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver1);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver2);

		assertThat("did not create different perspectives",
			perspective1, is(not(sameInstance(perspective2))));
	}

	@Test
	public void testRemovePerspective() {
		Node perceiver = wFact.createNode("perceiver", 0.0, 0.0);
		WorldPerspectiveCache cache = emptyPerspectiveCache();

		WorldPerspective perspective1 = cache.getPerspectiveFor(perceiver);
		cache.removePerceiver(perceiver);
		WorldPerspective perspective2 = cache.getPerspectiveFor(perceiver);

		assertThat("did not remove perspective",
			perspective1, is(not(sameInstance(perspective2))));
	}

	@Test
	public void testBufferPerspective() {
		Node perceiver = wFact.createNode("perceiver", 0.0, 0.0);
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
