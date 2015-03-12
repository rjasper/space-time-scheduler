package de.tu_berlin.kbs.swarmos.st_scheduler.world.util;

public interface SubPathOperation<P, Q> {
	
	public abstract P subPath(Q startPosition, Q finishPosition);

}
