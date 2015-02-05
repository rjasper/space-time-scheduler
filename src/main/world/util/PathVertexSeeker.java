package world.util;

import world.Path;

// TODO document
public interface PathVertexSeeker<
	V extends Path.Vertex,
	S extends Path.Segment<? extends V>,
	P extends Path<V, S>>
{

	public abstract V seekFloor(double position);
	
	public abstract V seekCeiling(double position);
	
	public abstract double position(V vertex);

}