package world;

import java.util.List;
import java.util.Objects;

import jts.geom.immutable.ImmutableGeometries;
import jts.geom.immutable.ImmutablePoint;
import jts.geom.util.GeometriesRequire;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.UnmodifiableIterator;
import com.vividsolutions.jts.geom.Point;

// TODO document
public class Path implements Iterable<ImmutablePoint> {
	
	private final ImmutableList<ImmutablePoint> vertices;
	
	public Path(List<Point> vertices) {
		checkVertices(vertices);
		
		this.vertices = makeImmutable(vertices);
	}
	
	public Path(ImmutableList<ImmutablePoint> vertices) {
		checkVertices(vertices);
		
		this.vertices = vertices;
	}
	
	protected void checkVertices(List<? extends Point> vertices) {
		Objects.requireNonNull(vertices, "vertices");
		
		if (vertices.size() == 1)
			throw new IllegalArgumentException("invalid size");
		
		vertices.forEach(p ->
			GeometriesRequire.requireValid2DPoint((Point) p, "vertices"));
	}
	
	@SuppressWarnings("unchecked")
	private ImmutableList<ImmutablePoint> makeImmutable(List<? extends Point> vertices) {
		if (vertices instanceof ImmutableList<?> &&
			vertices.stream().allMatch(ImmutableGeometries::isImmutable))
		{
			return (ImmutableList<ImmutablePoint>) vertices;
		} else {
			Builder<ImmutablePoint> builder = ImmutableList.builder();
			
			vertices.stream()
				.map(ImmutableGeometries::immutable)
				.forEach(builder::add);
			
			return builder.build();
		}
	}

	public int size() {
		return vertices.size();
	}

	public boolean isEmpty() {
		return vertices.isEmpty();
	}

	@Override
	public UnmodifiableIterator<ImmutablePoint> iterator() {
		return vertices.iterator();
	}

}
