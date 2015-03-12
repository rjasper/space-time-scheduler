package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;


public class SimpleMinimumTimePathfinderTest extends AbstractMinimumTimePathfinderTest {

	@Override
	protected AbstractMinimumTimePathfinder createPathfinder() {
		return new SimpleMinimumTimePathfinder();
	}

}
