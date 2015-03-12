package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometrySequencer.*;
import static java.lang.Double.*;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.DoubleSubPointPathOperation;
import de.tu_berlin.mailbox.rjasper.util.SmartArrayCache;

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
	protected final V makeVertex(int index, boolean first, boolean last) {
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

	/**
	 * Caches the point path's vertices.
	 */
	private transient SmartArrayCache<V> verticesCache = null;

	/* (non-Javadoc)
	 * @see world.AbstractPath#getVertex(int)
	 */
	@Override
	public V getVertex(int index) {
		if (verticesCache == null)
			verticesCache = new SmartArrayCache<>(super::getVertex, size());
		
		return verticesCache.get(index);
	}

	/**
	 * Caches the point path's segments.
	 */
	private transient SmartArrayCache<S> segmentsCache = null;

	/* (non-Javadoc)
	 * @see world.AbstractPath#getSegment(int)
	 */
	@Override
	public S getSegment(int index) {
		if (segmentsCache == null)
			segmentsCache = new SmartArrayCache<>(super::getSegment, size()-1);
		
		return segmentsCache.get(index);
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
	
	/**
	 * Caches the point path's envelope.
	 */
	private SoftReference<Envelope> envelopeCache = null;

	@Override
	public Envelope getEnvelope() {
		if (isEmpty())
			throw new IllegalStateException("path is empty");
		
		Envelope envelope = envelopeCache == null ? null : envelopeCache.get();
		
		if (envelope == null) {
			double
				minX = POSITIVE_INFINITY,
				maxX = NEGATIVE_INFINITY,
				minY = POSITIVE_INFINITY,
				maxY = NEGATIVE_INFINITY;
			
			for (Point p : points) {
				double x = p.getX(), y = p.getY();
				
				minX = min(minX, x);
				maxX = max(maxX, x);
				minY = min(minY, y);
				maxY = max(maxY, y);
			}
			
			envelope = new Envelope(minX, maxX, minY, maxY);
			
			envelopeCache = new SoftReference<>(envelope);
		}
		
		return envelope;
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
	
	/**
	 * Caches the trace of the path.
	 */
	private transient SoftReference<Geometry> traceCache = null;

	/* (non-Javadoc)
	 * @see world.Path#trace()
	 */
	@Override
	public Geometry trace() {
		if (isEmpty())
			return immutableLineString();
		
		Geometry trace = null;
		
		if (traceCache != null)
			trace = traceCache.get();
		
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
			
			if (points.size() == 1)
				trace = points.get(0);
			else
				trace = immutableLineString(sequence(points));
	
			traceCache = new SoftReference<>(trace);
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
	public PointPath<V, S> subPath(double fromSubIndex, double toSubIndex) {
		return DoubleSubPointPathOperation.subPath(this,
			v -> (double) v.getIndex(),
			points -> create(points),
			fromSubIndex, toSubIndex);
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
