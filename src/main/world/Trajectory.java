package world;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import util.DurationConv;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * A {@code Trajectory} represents a curve in 3-dimensional space with spatial
 * and temporal ordinates. It describes the position of a point for a given
 * time. The common use is to describe the movement of objects.
 * </p>
 * 
 * <p>
 * The curve is given by a list of multiple 3D points. Each point consists of
 * two spatial ordinates (x and y) and a temporal ordinate (t). The points
 * represent the vertices of the trajectory and are connected via lines. An
 * empty trajectory without any points is also possible. However, singular
 * trajectories consisting of only one vertex are forbidden.
 * </p>
 * 
 * <p>
 * The spatial ordinates are stored as a list of 2-dimensional {@link Point}s
 * (see {@link #getSpatialPath()}) while the temporal ordinates are a list of
 * {@link LocalDateTime}s (see {@link #getTimes()}). The i-th elements of each
 * list compose the i-th 3D vertex of the trajectory.
 * </p>
 * 
 * <p>
 * The temporal ordinates are constraint since objects can only move forward in
 * time. Therefore, the time ordinate must increase from point to point.
 * </p>
 * 
 * @author Rico
 */
public interface Trajectory {

	// TODO document
	
	public static class Vertex {
		
		private final SpatialPath.Vertex spatialVertex;
		private final LocalDateTime time;
		
		Vertex(SpatialPath.Vertex spatialVertex, LocalDateTime time) {
			this.spatialVertex = spatialVertex;
			this.time = time;
		}
	
		public boolean isFirst() {
			return spatialVertex.isFirst();
		}
	
		public boolean isLast() {
			return spatialVertex.isLast();
		}
	
		public SpatialPath.Vertex getSpatialVertex() {
			return spatialVertex;
		}
	
		public ImmutablePoint getLocation() {
			return spatialVertex.getPoint();
		}
	
		public double getX() {
			return spatialVertex.getX();
		}
	
		public double getY() {
			return spatialVertex.getY();
		}
	
		public double getArc() {
			return spatialVertex.getArc();
		}
		
		public double getTimeInSeconds(LocalDateTime baseTime) {
			return DurationConv.inSeconds(Duration.between(baseTime, time));
		}
	
		public LocalDateTime getTime() {
			return time;
		}
		
	}
	
	public static class VertexIterator implements Iterator<Vertex> {

		private final Iterator<SpatialPath.Vertex> spatialIterator;
		private final Iterator<LocalDateTime> timeIterator;
		
		private VertexIterator(Trajectory trajectory) {
			this.spatialIterator = trajectory.getSpatialPath().vertexIterator();
			this.timeIterator = trajectory.getTimes().iterator();
		}

		@Override
		public boolean hasNext() {
			// equivalent to timeIterator.hasNext()
			return spatialIterator.hasNext();
		}

		@Override
		public Vertex next() {
			return new Vertex(spatialIterator.next(), timeIterator.next());
		}

	}

	public static class Segment {
		
		private final Vertex start;
		private final Vertex finish;
		private final SpatialPath.Segment spatialSegment;
		
		private transient Duration duration = null;
		private transient double seconds = Double.NaN;
		
		Segment(Vertex start, Vertex finish, SpatialPath.Segment spatialSegment) {
			this.start = start;
			this.finish = finish;
			this.spatialSegment = spatialSegment;
		}
		
		public boolean isStationary() {
			return getStartLocation().equals(getFinishLocation());
		}
	
		public Vertex getStartVertex() {
			return start;
		}
	
		public Vertex getFinishVertex() {
			return finish;
		}
		
		public ImmutablePoint getStartLocation() {
			return start.getLocation();
		}
		
		public ImmutablePoint getFinishLocation() {
			return finish.getLocation();
		}
		
		public LocalDateTime getStartTime() {
			return start.getTime();
		}
		
		public LocalDateTime getFinishTime() {
			return finish.getTime();
		}
		
		public double getStartTimeInSeconds(LocalDateTime seconds) {
			return start.getTimeInSeconds(seconds);
		}
		
		public double getFinishTimeInSeconds(LocalDateTime seconds) {
			return finish.getTimeInSeconds(seconds);
		}
	
		public SpatialPath.Segment getSpatialSegment() {
			return spatialSegment;
		}
	
		public boolean isFirst() {
			return spatialSegment.isFirst();
		}
	
		public boolean isLast() {
			return spatialSegment.isLast();
		}
	
		public double length() {
			return spatialSegment.length();
		}
		
		public double durationInSeconds() {
			if (Double.isNaN(seconds))
				seconds = DurationConv.inSeconds(duration());
			
			return seconds;
		}
		
		public Duration duration() {
			if (duration == null)
				duration = Duration.between(start.getTime(), finish.getTime());
			
			return duration;
		}
		
	}
	
	public static class SegmentIterator implements Iterator<Segment> {
		
		private final Iterator<Trajectory.Vertex> vertexIterator;
		private final Iterator<SpatialPath.Segment> spatialSegmentIterator;
		
		private Trajectory.Vertex lastVertex = null;
		
		SegmentIterator(Trajectory trajectory) {
			this.vertexIterator = new VertexIterator(trajectory);
			this.spatialSegmentIterator = trajectory.getSpatialPath().segmentIterator();
			
			if (vertexIterator.hasNext())
				lastVertex = vertexIterator.next();
		}

		@Override
		public boolean hasNext() {
			// equivalent to spatialSegmentIterator.hasNext()
			return vertexIterator.hasNext();
		}

		@Override
		public Segment next() {
			Vertex start = lastVertex;
			Vertex finish = vertexIterator.next();
			SpatialPath.Segment spatialSegment = spatialSegmentIterator.next();
			
			Segment segment = new Segment(start, finish, spatialSegment);
			lastVertex = finish;
			
			return segment;
		}
		
	}
	
	/**
	 * @return {@code true} iff trajectory has no vertices.
	 */
	public abstract boolean isEmpty();

	/**
	 * @return the spatial ordinates (x-y).
	 */
	public abstract SpatialPath getSpatialPath();

	/**
	 * @return the temporal ordinates (t).
	 */
	public abstract ImmutableList<LocalDateTime> getTimes();

	/**
	 * @return the location of the first vertex. {@code null} if trajectory is
	 *         empty.
	 */
	public abstract ImmutablePoint getStartLocation();

	/**
	 * @return the location of the last vertex. {@code null} if trajectory is
	 *         empty.
	 */
	public abstract ImmutablePoint getFinishLocation();

	/**
	 * @return the time of the first vertex. {@code null} if trajectory is
	 *         empty.
	 */
	public abstract LocalDateTime getStartTime();

	/**
	 * @return the time of the last vertex. {@code null} if trajectory is empty.
	 */
	public abstract LocalDateTime getFinishTime();

	/**
	 * @return the time difference between the first and last vertex.
	 */
	public abstract Duration getDuration();

	/**
	 * @return the euclidean length of the spatial path.
	 */
	public abstract double getLength();

	/**
	 * @return the trace which is a geometry only including all points of the
	 *         spatial path.
	 */
	public abstract Geometry getTrace();
	
	/**
	 * Calculates the arc time path (s-t) of this trajectory in relation to the
	 * given base time.
	 * 
	 * @param baseTime
	 * @return the arc time path.
	 * @throws NullPointerException if {@code baseTime} is {@code null}.
	 */
	public abstract ArcTimePath calcArcTimePath(LocalDateTime baseTime);

	/**
	 * Calculates the merge of two trajectories. This trajectory serves as
	 * first section while the given one as the second one. This trajectory will
	 * not be modified. The given trajectory must have later time ordinates.
	 * The resulting trajectory connects the original ones by a straight line.
	 * 
	 * @param other the second trajectory section.
	 * @return the merged trajectory.
	 * @throws NullPointerException if other is {@code null}.
	 * @throws IllegalArgumentException if other's time is before this one's.
	 */
	public default Trajectory merge(Trajectory other) {
		Objects.requireNonNull(other, "other");
		
		if (getFinishTime().compareTo(other.getStartTime()) > 0)
			throw new IllegalArgumentException("other is before this one");
		
		SpatialPath lhsSpatialPath = getSpatialPath();
		SpatialPath rhsSpatialPath = other.getSpatialPath();
		List<LocalDateTime> lhsTimes = getTimes();
		List<LocalDateTime> rhsTimes = other.getTimes();

		int n = lhsSpatialPath.size() + rhsSpatialPath.size();

		SpatialPath spatialPath = lhsSpatialPath.concat(rhsSpatialPath);
		List<LocalDateTime> times = new ArrayList<>(n);

		times.addAll(lhsTimes);
		times.addAll(rhsTimes);

		return new SimpleTrajectory(spatialPath, times);
	}
	
	// TODO document
	
	public default Iterator<Vertex> vertexIterator() {
		return new VertexIterator(this);
	}
	
	public default Iterator<Segment> segmentIterator() {
		return new SegmentIterator(this);
	}

}