package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;


public class StraightEdgePathfinderTest extends AbstractSpatialPathfinderTest {

	@Override
	protected AbstractSpatialPathfinder createPathfinder() {
		return new StraightEdgePathfinder();
	}

}
