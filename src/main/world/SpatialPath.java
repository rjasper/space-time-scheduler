package world;

import java.util.Iterator;

import world.util.SpatialPathInterpolator;
import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.operation.distance.DistanceOp;

/**
 * Implements an spatial path.
 * 
 * @author Rico
 */
public class SpatialPath extends AbstractPath<SpatialPath.Vertex, SpatialPath.Segment> {
	
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
	 * Caches the length of the path.
	 */
	private transient double length = Double.NaN;
	
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
	 * An interpolator for this spatial path.
	 */
	private SpatialPathInterpolator interpolator =
		new SpatialPathInterpolator(this, false);
	
	/**
	 * Interpolates the location of the given arc.
	 * 
	 * @param arc
	 * @return the location.
	 */
	public ImmutablePoint interpolateLocation(double arc) {
		return interpolator.interpolate(arc);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#concat(world.Path)
	 */
	@Override
	public SpatialPath concat(AbstractPath<?, ?> other) {
		if (!(other instanceof SpatialPath))
			throw new IllegalArgumentException("incompatible path");
		
		return (SpatialPath) super.concat(other);
	}
	
	/* (non-Javadoc)
	 * @see world.Path#subPath(int, double, int, double)
	 */
	@Override
	public SpatialPath subPath(
		int startIndexInclusive,
		double startAlpha,
		int finishIndexExclusive,
		double finishAlpha)
	{
		return (SpatialPath) super.subPath(
			startIndexInclusive,
			startAlpha,
			finishIndexExclusive,
			finishAlpha);
	}

	/**
	 * @return the length of the path.
	 */
	public double length() {
		if (Double.isNaN(length)) {
			Iterator<? extends Vertex> it = vertexIterator();
			Vertex last = null;
			while (it.hasNext())
				last = it.next();
			
			length = last == null ? 0.0 : last.getArc();
		}
		
		return length;
	}
	
	/*
	 * (non-Javadoc)
	 * @see world.Path#vertexIterator()
	 */
	@Override
	public Iterator<Vertex> vertexIterator() {
		return new VertexIterator();
	}

	/**
	 * The vertex of a {@code SpatialPath}. Stores additional information about
	 * the vertex in context to the path.
	 */
	public static class Vertex extends AbstractPath.Vertex {
		
		/**
		 * The arc value.
		 */
		private final double arc;
		
		/**
		 * Constructs a new {@code Vertex}.
		 * 
		 * @param point
		 * @param arc
		 *            value
		 * @param first
		 *            whether the vertex is the first one
		 * @param last
		 *            whether the vertex is the last one
		 */
		private Vertex(ImmutablePoint point, double arc, boolean first, boolean last) {
			super(point, first, last);
			
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
	 * The {@code VertexIterator} of a {@code SpatialPath}.
	 */
	private class VertexIterator extends AbstractVertexIterator {
		
		/**
		 * The accumulated arc value.
		 */
		private double arc = 0.0;
		
		/*
		 * (non-Javadoc)
		 * @see world.Path.AbstractVertexIterator#nextVertex(jts.geom.immutable.ImmutablePoint)
		 */
		@Override
		protected Vertex createNextVertex(ImmutablePoint point) {
			if (!isFirst())
				arc += DistanceOp.distance(getLastVertex().getPoint(), point);
			
			return new Vertex(point, arc, isFirst(), isLast());
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#segmentIterator()
	 */
	@Override
	public Iterator<Segment> segmentIterator() {
		return new SegmentIterator();
	}
	
	/**
	 * The segment of a {@code SpatialPath}. Stores additional information about
	 * the segment in context to the path.
	 */
	public static class Segment extends AbstractPath.Segment<Vertex> {
		
		/**
		 * Constructs a new {@code Segment} connecting the given vertices.
		 * 
		 * @param start
		 *            start vertex
		 * @param finish
		 *            finish vertex
		 */
		private Segment(Vertex start, Vertex finish) {
			super(start, finish);
		}
		
		/**
		 * @return the length of this segment.
		 */
		public double length() {
			return getFinishVertex().getArc() - getStartVertex().getArc();
		}
	}
	
	/**
	 * The {@code SegmentIterator} of a {@code SpatialPath}.
	 */
	private class SegmentIterator extends AbstractSegmentIterator {
		
		/*
		 * (non-Javadoc)
		 * @see world.Path.AbstractSegmentIterator#supplyVertexIterator()
		 */
		@Override
		protected Iterator<Vertex> supplyVertexIterator() {
			return new VertexIterator();
		}

		/*
		 * (non-Javadoc)
		 * @see world.Path.AbstractSegmentIterator#nextSegment(world.Path.Vertex, world.Path.Vertex)
		 */
		@Override
		protected Segment createNextSegment(Vertex start, Vertex finish) {
			return new Segment(start, finish);
		}
		
	}

}
