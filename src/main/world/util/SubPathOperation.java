package world.util;

public interface SubPathOperation<P, Q> {
	
	public abstract P subPath(Q startPosition, Q finishPosition);

}
