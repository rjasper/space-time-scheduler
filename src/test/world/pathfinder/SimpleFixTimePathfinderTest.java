package world.pathfinder;

public class SimpleFixTimePathfinderTest extends AbstractFixTimePathfinderTest {

	@Override
	protected AbstractFixTimePathfinder createPathfinder() {
		return new SimpleFixTimePathfinder();
	}

}
