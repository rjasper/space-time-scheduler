package world.pathfinder;

public class FixTimeVelocityPathfinderImplTest extends FixTimeVelocityPathfinderTest {

	@Override
	protected FixTimeVelocityPathfinder createPathfinder() {
		return new FixTimeVelocityPathfinderImpl();
	}

}
