package world.pathfinder;

public class SimpleMinimumTimePathfinderTest extends AbstractMinimumTimePathfinderTest {

	@Override
	protected AbstractMinimumTimePathfinder createPathfinder() {
		return new SimpleMinimumTimePathfinder();
	}

}
