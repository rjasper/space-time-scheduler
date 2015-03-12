package de.tu_berlin.mailbox.rjasper.st_scheduler.world.util;

public interface SubPathOperation<P, Q> {
	
	public abstract P subPath(Q startPosition, Q finishPosition);

}
