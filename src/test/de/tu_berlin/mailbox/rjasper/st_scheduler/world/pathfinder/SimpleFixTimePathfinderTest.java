package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;


public class SimpleFixTimePathfinderTest extends AbstractFixTimePathfinderTest {

	@Override
	protected AbstractFixTimePathfinder createPathfinder() {
		return new SimpleFixTimePathfinder();
	}

}
