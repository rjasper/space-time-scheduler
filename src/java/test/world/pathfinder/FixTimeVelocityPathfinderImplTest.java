package world.pathfinder;

public class FixTimeVelocityPathfinderImplTest extends FixTimeVelocityPathfinderTest {

	@Override
	protected FixTimeVelocityPathfinder getInstance() {
		return new FixTimeVelocityPathfinderImpl();
	}

}
