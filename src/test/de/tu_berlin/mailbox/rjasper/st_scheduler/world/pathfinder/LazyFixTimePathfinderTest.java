package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import java.time.Duration;


public class LazyFixTimePathfinderTest extends AbstractFixTimePathfinderTest {

	@Override
	protected AbstractFixTimePathfinder createPathfinder() {
		LazyFixTimePathfinder pf = new LazyFixTimePathfinder();

		pf.setMinStopDuration( Duration.ZERO );

		return pf;
	}

}
