package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.AbstractMinimumTimePathfinder;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.SimpleMinimumTimePathfinder;

public class SimpleMinimumTimePathfinderTest extends AbstractMinimumTimePathfinderTest {

	@Override
	protected AbstractMinimumTimePathfinder createPathfinder() {
		return new SimpleMinimumTimePathfinder();
	}

}
