package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.AbstractFixTimePathfinder;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.SimpleFixTimePathfinder;

public class SimpleFixTimePathfinderTest extends AbstractFixTimePathfinderTest {

	@Override
	protected AbstractFixTimePathfinder createPathfinder() {
		return new SimpleFixTimePathfinder();
	}

}
