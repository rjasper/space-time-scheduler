package world.pathfinder;

public class MinimumTimeVelocityPathfinderImplTest extends MinimumTimeVelocityPathfinderTest {

	@Override
	protected MinimumTimeVelocityPathfinder createPathfinder() {
		return new MinimumTimeVelocityPathfinderImpl();
	}

}
