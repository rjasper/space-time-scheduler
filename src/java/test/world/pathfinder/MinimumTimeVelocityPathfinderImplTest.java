package world.pathfinder;

public class MinimumTimeVelocityPathfinderImplTest extends MinimumTimeVelocityPathfinderTest {

	@Override
	protected MinimumTimeVelocityPathfinder getPathfinder() {
		return new MinimumTimeVelocityPathfinderImpl();
	}

}
