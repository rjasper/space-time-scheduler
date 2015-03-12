package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.AbstractSpatialPathfinder;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder.StraightEdgePathfinder;

public class StraightEdgePathfinderTest extends AbstractSpatialPathfinderTest {

	@Override
	protected AbstractSpatialPathfinder createPathfinder() {
		return new StraightEdgePathfinder();
	}

}
