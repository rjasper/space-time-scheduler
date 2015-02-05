package world.util;

import java.util.Objects;
import java.util.function.Function;

import world.Path;

// TODO document
public abstract class AbstractPathVertexSeeker<
	V extends Path.Vertex,
	S extends Path.Segment<? extends V>,
	P extends Path<V, S>>
implements PathVertexSeeker<V, S, P>
{
	
	private final P path;
	
	private final Function<V, Double> positionMapper;
	
	public AbstractPathVertexSeeker(P path, Function<V, Double> positionMapper) {
		this.path = Objects.requireNonNull(path, "path");
		this.positionMapper = Objects.requireNonNull(positionMapper, "positionMapper");
	}
	
	protected P getPath() {
		return path;
	}
	
	@Override
	public double position(V vertex) {
		return positionMapper.apply(vertex);
	}
	
	@Override
	public abstract V seekFloor(double position);

}
