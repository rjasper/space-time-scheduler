package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import java.time.Duration;


public class LazyMinimumTimePathfinderTest extends AbstractMinimumTimePathfinderTest {

	@Override
	protected AbstractMinimumTimePathfinder createPathfinder() {
		LazyMinimumTimePathfinder pf = new LazyMinimumTimePathfinder();

		pf.setMinStopDuration( Duration.ZERO );

		return pf;
	}

}
