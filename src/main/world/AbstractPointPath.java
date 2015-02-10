package world;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static jts.geom.util.GeometrySequencer.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import jts.geom.immutable.ImmutableLineString;
import jts.geom.immutable.ImmutablePoint;
import jts.geom.util.GeometriesRequire;
import world.util.DoubleSubPointPathOperation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.vividsolutions.jts.geom.Point;

/**
 * A {@code Path} is an immutable list of immutable {@link Point}s. It ensures
 * validity of the path. All points have to be valid 2-dimensional points.
 * Singular paths of only one point are not allowed while empty paths are.
 * 
 * @author Rico
 */
public abstract class AbstractPointPath<
	V extends PointPath.Vertex,
	S extends PointPath.Segment<? extends V>>
extends AbstractPath<V, S>
implements PointPath<V, S>
{
	
	/**
	 * The points of the path.
	 */
	private final ImmutableList<ImmutablePoint> points;
	
	/**
	 * Caches the trace of the path.
	 */
	private transient ImmutableLineString trace = null;
	
	/**
	 * Constructs a path of the given points.
	 * 
	 * @param points
	 * @throws NullPointerException
	 *             if {@code points} are {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code points} contain invalid points.
	 * 
	 * @param points
	 */
	public AbstractPointPath(ImmutableList<ImmutablePoint> points) {
		checkVertices(points);
		
		this.points = points;
	}

	/**
	 * Creates a new path containing the given points.
	 * 
	 * @param points
	 * @return the new path.
	 */
	protected abstract PointPath<V, S> create(ImmutableList<ImmutablePoint> points);
	
	/**
	 * @return an empty path.
	 */
	protected abstract PointPath<V, S> getEmpty();
	
	/**
	 * Checks for validity of the given points.
	 * 
	 * @param points
	 * @throws NullPointerException
	 *             if the {@code points} are {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code points} contain invalid points.
	 */
	protected void checkVertices(List<? extends Point> points) {
		Objects.requireNonNull(points, "points");
		
		if (points.size() == 1)
			throw new IllegalArgumentException("invalid size");
		
		points.forEach(p ->
			GeometriesRequire.requireValid2DPoint((Point) p, "points"));
	}

	/* (non-Javadoc)
	 * @see world.AbstractPath#makeVertex(int, boolean, boolean)
	 */
	@Override
	protected V makeVertex(int index, boolean first, boolean last) {
		return makeVertex(index, getPoint(index), first, last);
	}
	
	/**
	 * Makes a vertex for the given index.
	 * 
	 * @param index
	 * @param point at the given index
	 * @param first whether it's the first vertex
	 * @param last whether it's the last vertex
	 * @return the vertex.
	 */
	protected abstract V makeVertex(int index, ImmutablePoint point, boolean first, boolean last);

	/* (non-Javadoc)
	 * @see world.Path#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return points.isEmpty();
	}

	/* (non-Javadoc)
	 * @see world.Path#size()
	 */
	@Override
	public int size() {
		return points.size();
	}

	/* (non-Javadoc)
	 * @see world.Path#get(int)
	 */
	@Override
	public ImmutablePoint getPoint(int index) {
		return points.get(index);
	}
	
	/* (non-Javadoc)
	 * @see world.Path#getPoints()
	 */
	@Override
	public ImmutableList<ImmutablePoint> getPoints() {
		return points;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPointPath<?, ?> other = (AbstractPointPath<?, ?>) obj;
		if (points == null) {
			if (other.points != null)
				return false;
		} else if (!points.equals(other.points))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see world.Path#trace()
	 */
	@Override
	public ImmutableLineString trace() {
		if (trace == null) {
			List<ImmutablePoint> points = new LinkedList<>(getPoints());
			Iterator<ImmutablePoint> it = points.iterator();
	
			// removes points which are identical to their predecessor
			Point last = null;
			while (it.hasNext()) {
				Point p = it.next();
	
				if (last != null && p.equals(last))
					it.remove();
	
				last = p;
			}
	
			// construct LineString
			
			if (points.size() == 1) {
				ImmutablePoint point = points.get(0);
				
				trace = immutableLineString(sequence(point, point));
			} else {
				trace = immutableLineString(sequence(points));
			}
	
		}
		
		return trace;
	}

	/* (non-Javadoc)
	 * @see world.Path#concat(world.AbstractPath)
	 */
	@Override
	public PointPath<V, S> concat(Path<? extends V, ? extends S> other) {
		if (!(other instanceof PointPath))
			throw new IllegalArgumentException("incompatible type");
		
		ImmutableList<ImmutablePoint> lhsVertices = this.getPoints();
		ImmutableList<ImmutablePoint> rhsVertices = ((PointPath<?, ?>) other).getPoints();
		
		Builder<ImmutablePoint> builder = ImmutableList.builder();
		
		ImmutableList<ImmutablePoint> points = builder
			.addAll(lhsVertices)
			.addAll(rhsVertices)
			.build();
		
		return create(points);
	}
	
	/* (non-Javadoc)
	 * @see world.Path#subPath(java.util.function.Function, double, double)
	 */
	@Override
	public PointPath<V, S> subPath(double startPosition, double finishPosition) {
		return DoubleSubPointPathOperation.subPath(this,
			v -> (double) v.getIndex(),
			points -> create(points),
			startPosition, finishPosition);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return points.toString();
	}

}
