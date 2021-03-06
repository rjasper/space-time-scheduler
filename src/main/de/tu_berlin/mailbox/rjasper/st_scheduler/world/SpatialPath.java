package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.BinarySearchSeeker;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.Interpolator;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.PointPathInterpolator;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.Seeker;

/**
 * Implements the spatial path.
 *
 * @author Rico Jasper
 */
public class SpatialPath extends AbstractPointPath<SpatialPath.Vertex, SpatialPath.Segment> {

	/**
	 * An empty {@code SpatialPath}.
	 */
	private static final SpatialPath EMPTY = new SpatialPath(ImmutableList.of());

	/**
	 * @return an empty {@code SpatialPath}.
	 */
	public static SpatialPath empty() {
		return EMPTY;
	}

	/**
	 * Stores the arc values.
	 */
	private final double[] arcs;

	/**
	 * Constructs a spatial path of the given vertices.
	 *
	 * @param vertices
	 * @throws NullPointerException
	 *             if {@code vertices} are {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code vertices} contain invalid points.
	 */
	public SpatialPath(ImmutableList<ImmutablePoint> vertices) {
		super(vertices);

		this.arcs = calcArcs(vertices);
	}

	/**
	 * Calculates the arc values of the vertices from the given points.
	 *
	 * @param points
	 * @return the arc values.
	 */
	private static double[] calcArcs(List<? extends Point> points) {
		if (points.isEmpty())
			return new double[0];

		// if not empty there are at least two points

		int n = points.size();
		double[] arcs = new double[n];
		arcs[0] = 0;
		Point p1 = points.get(0);

		for (int i = 1; i < n; ++i) {
			Point p2 = points.get(i);

			arcs[i] = arcs[i-1] + DistanceOp.distance(p1, p2);
			p1 = p2;
		}

		return arcs;
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#create(com.google.common.collect.ImmutableList)
	 */
	@Override
	protected SpatialPath create(ImmutableList<ImmutablePoint> vertices) {
		return new SpatialPath(vertices);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#getEmpty()
	 */
	@Override
	protected SpatialPath getEmpty() {
		return empty();
	}

	/**
	 * The vertex of a {@code SpatialPath}. Stores additional information about
	 * the vertex in context to the path.
	 */
	public static class Vertex extends PointPath.Vertex {

		/**
		 * The arc value.
		 */
		private final double arc;

		/**
		 * Constructs a new {@code Vertex}.
		 *
		 * @param index
		 * @param point
		 * @param arc
		 *            value
		 */
		public Vertex(SpatialPath path, int index, ImmutablePoint point, double arc) {
			super(path, index, point);

			this.arc = arc;
		}

		/**
		 * @return the arc value.
		 */
		public double getArc() {
			return arc;
		}

	}

	/**
	 * The segment of a {@code SpatialPath}. Stores additional information about
	 * the segment in context to the path.
	 */
	public class Segment extends PointPath.Segment<Vertex> {

		/**
		 * Constructs a new {@code Segment} connecting the given vertices.
		 *
		 * @param start
		 *            start vertex
		 * @param finish
		 *            finish vertex
		 */
		public Segment(Vertex start, Vertex finish) {
			super(start, finish);
		}

		/**
		 * @return the length of this segment.
		 */
		public double length() {
			return getFinishVertex().getArc() - getStartVertex().getArc();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see world.AbstractPointPath#makeVertex(int, jts.geom.immutable.ImmutablePoint, boolean, boolean)
	 */
	@Override
	protected Vertex makeVertex(int index, ImmutablePoint point) {
		return new Vertex(this, index, point, arcs[index]);
	}

	/*
	 * (non-Javadoc)
	 * @see world.AbstractPath#makeSegment(world.Path.Vertex, world.Path.Vertex)
	 */
	@Override
	protected Segment makeSegment(Vertex start, Vertex finish) {
		return new Segment(start, finish);
	}

	/**
	 * @return the length of the path.
	 */
	public double length() {
		return isEmpty() ? 0.0 : arcs[size()-1];
	}

	/**
	 * Interpolates the location of the given arc.
	 *
	 * @param arc
	 * @return the location.
	 * @throws IllegalArgumentException
	 *             if arc is not finite or not within the interval
	 *             <i>[0.0,&nbsp;length()]</i>.
	 */
	public ImmutablePoint interpolateLocation(double arc) {
		if (!Double.isFinite(arc) || arc < 0.0 || arc > length())
			throw new IllegalArgumentException("invalid arc");
		if (isEmpty())
			throw new NoSuchElementException("path is empty");

		// short cut for first or last point
		if (arc == 0.0)
			return getFirstPoint();
		if (arc == length())
			return getLastPoint();

		Seeker<Double, Vertex> seeker = new BinarySearchSeeker<>(
			this::getVertex,
			Vertex::getArc,
			size());
		Interpolator<Double, ImmutablePoint> interpolator =
			new PointPathInterpolator<>(seeker);

		return interpolator.interpolate(arc).get();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#concat(world.Path)
	 */
	@Override
	public SpatialPath concat(Path<? extends Vertex, ? extends Segment> other) {
		if (!(other instanceof SpatialPath))
			throw new IllegalArgumentException("incompatible path");

		return (SpatialPath) super.concat(other);
	}

	/* (non-Javadoc)
	 * @see world.AbstractPointPath#subPath(java.util.function.Function, double, double)
	 */
	@Override
	public SpatialPath subPath(double startPosition, double finishPosition) {
		return (SpatialPath) super.subPath(startPosition, finishPosition);
	}

}
