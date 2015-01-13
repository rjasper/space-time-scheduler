package world;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import jts.geom.immutable.ImmutablePoint;

import org.apache.commons.collections4.iterators.IteratorIterable;

import util.DurationConv;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.vividsolutions.jts.geom.Point;

// TODO document
public class ArcTimePath extends Path {
	
	private transient Duration duration = null;

	public ArcTimePath() {}

	public ArcTimePath(List<? extends Point> vertices) {
		super(vertices);
	}

	public ArcTimePath(ImmutableList<ImmutablePoint> vertices) {
		super(vertices);
	}

	@Override
	protected ArcTimePath create(ImmutableList<ImmutablePoint> vertices) {
		return new ArcTimePath(vertices);
	}
	
	public double length() {
		if (isEmpty())
			return 0.0;
		else
			return get(size()-1).getX();
	}
	
	public Duration duration() {
		if (duration == null) {
			double seconds = get(size()-1).getY();
			
			duration = DurationConv.ofSeconds(seconds);
		}
		
		return duration;
	}

	@Override
	public ArcTimePath concat(Path other) {
		if (!(other instanceof ArcTimePath))
			throw new IllegalArgumentException("incompatible path");
		
		return (ArcTimePath) super.concat(other);
	}

	@Override
	protected void checkVertices(List<? extends Point> vertices) {
		super.checkVertices(vertices);
		
		// check arc ordinates
		// arcs have to be equal or greater than 0
		
		boolean nonNegativeArcs = vertices.stream()
			.map(Point::getX) // arc ordinate
			.allMatch(s -> s >= 0);
		
		if (!nonNegativeArcs) // e.g. negative arcs
			throw new IllegalArgumentException("path has negative arc values");
		
		// check time ordinates
		// times have to be strictly increasing
		
		Iterator<Double> it = vertices.stream()
			.map(Point::getY) // time ordinate
			.iterator();
		
		boolean isOrdered = Ordering.natural()
			.isOrdered(new IteratorIterable<>(it));
		
		if (!isOrdered)
			throw new IllegalArgumentException("path is not causal");
	}
	
	@Override
	public Iterator<Vertex> vertexIterator() {
		return new VertexIterator();
	}

	public static class Vertex extends Path.Vertex {

		private Vertex(ImmutablePoint point, boolean first, boolean last) {
			super(point, first, last);
		}
		
	}
	
	private class VertexIterator extends AbstractVertexIterator<Vertex> {

		@Override
		protected Vertex nextVertex(ImmutablePoint point) {
			return new Vertex(point, isFirst(), isLast());
		}
		
	}

	@Override
	public Iterator<Segment> segmentIterator() {
		return new SegmentIterator();
	}
	
	public static class Segment extends Path.Segment<Vertex> {
		
		private Segment(Vertex start, Vertex finish) {
			super(start, finish);
		}
		
		public double length() {
			return getFinishVertex().getX() - getStartVertex().getX();
		}
		
		public double durationInSeconds() {
			return getFinishVertex().getY() - getStartVertex().getY();
		}
		
		public Duration duration() {
			return DurationConv.ofSeconds(durationInSeconds());
		}
	}
	
	private class SegmentIterator extends AbstractSegmentIterator<Vertex, Segment> {
		@Override
		protected Iterator<Vertex> supplyVertexIterator() {
			return new VertexIterator();
		}

		@Override
		protected Segment nextSegment(Vertex start, Vertex finish) {
			return new Segment(start, finish);
		}
	}

}
