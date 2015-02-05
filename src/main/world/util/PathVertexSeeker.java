package world.util;

// TODO document
/**
 * 
 * @author Rico
 *
 * @param <V> Vertex Type
 */
public interface PathVertexSeeker<V>
{

	public abstract V seekFloor(double position);
	
	public abstract V seekCeiling(double position);
	
	public abstract double position(V vertex);

}