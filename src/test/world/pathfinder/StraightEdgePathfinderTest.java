package world.pathfinder;

public class StraightEdgePathfinderTest extends SpatialPathfinderTest {

	@Override
	protected SpatialPathfinder createPathfinder() {
		return new StraightEdgePathfinder();
	}

}
