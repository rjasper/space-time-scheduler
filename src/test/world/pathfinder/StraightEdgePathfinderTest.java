package world.pathfinder;

public class StraightEdgePathfinderTest extends AbstractSpatialPathfinderTest {

	@Override
	protected AbstractSpatialPathfinder createPathfinder() {
		return new StraightEdgePathfinder();
	}

}
