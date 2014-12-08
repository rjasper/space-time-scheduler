package world;

import static java.util.Collections.emptyList;
import static org.junit.Assert.fail;

import org.junit.Test;

import world.pathfinder.StraightEdgePathfinder;

public class RadiusBasedWorldPerspectiveCacheTest {

	@Test
	public void testCreatePerspective() {
		fail("Not yet implemented"); // TODO

		WorldPerspectiveCache cache = makeEmptyCache();

		// XXX last edition
//		cache.getPerspectiveFor(perceiver);
	}

	@Test
	public void testRecallPerspective() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testReusePerspective() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testRemovePerspective() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testBufferPerspective() {
		fail("Not yet implemented"); // TODO
	}

	private WorldPerspectiveCache makeEmptyCache() {
		World world = new World(emptyList());
		RadiusBasedWorldPerspectiveCache cache =
			new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);

		return cache;
	}

}
