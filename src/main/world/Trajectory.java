package world;

import java.time.Duration;
import java.time.LocalDateTime;
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

		SpatialPath spatialPath = lhsSpatialPath.concat(rhsSpatialPath);
		ImmutableList<LocalDateTime> times = ImmutableList.<LocalDateTime>builder()
			.addAll(lhsTimes)
			.addAll(rhsTimes)
			.build();

		return new SimpleTrajectory(spatialPath, times);
	}
	
	/**
	 * @return a {@code VertexIterator}
	 */
	public default Iterator<Vertex> vertexIterator() {
		return new VertexIterator(this);
	}

	/**
	 * The vertex of a {@code Trajectory}. Stores additional information about
	 * the vertex in context to the path.
	 */
	public static class Vertex {
		
		/**
		 * The spatial vertex.
		 */
		private final SpatialPath.Vertex spatialVertex;
		
		/**
		 * The time ordinate.
		 */
		private final LocalDateTime time;
		
		/**
		 * Constructs a new {@code Vertex} of the given spatial vertex and time.
		 * 
		 * @param spatialVertex
		 * @param time
		 */
		private Vertex(SpatialPath.Vertex spatialVertex, LocalDateTime time) {
			this.spatialVertex = spatialVertex;
			this.time = time;
		}
	
		/**
		 * @return whether this vertex the first one.
		 */
		public boolean isFirst() {
			return spatialVertex.isFirst();
		}
	
		/**
		 * @return whether this vertex the first one.
		 */
		public boolean isLast() {
			return spatialVertex.isLast();
		}
	
		/**
		 * @return the spatial vertex.
		 */
		public SpatialPath.Vertex getSpatialVertex() {
			return spatialVertex;
		}
	
		/**
		 * @return the location.
		 */
		public ImmutablePoint getLocation() {
			return spatialVertex.getPoint();
		}
	
		/**
		 * @return the x-ordinate.
		 */
		public double getX() {
			return spatialVertex.getX();
		}
	
		/**
		 * @return the y-ordinate.
		 */
		public double getY() {
			return spatialVertex.getY();
		}
	
		/**
		 * @return the arc value.
		 */
		public double getArc() {
			return spatialVertex.getArc();
		}
		
		/**
		 * @return the time ordinate.
		 */
		public LocalDateTime getTime() {
			return time;
		}
	
		/**
		 * @param baseTime
		 *            the base time to relate to.
		 * @return the time in seconds.
		 */
		public double getTimeInSeconds(LocalDateTime baseTime) {
			return DurationConv.inSeconds(Duration.between(baseTime, time));
		}
		
	}

	/**
	 * The {@code VertexIterator} of a {@code Trajectory}.
	 */
	public static class VertexIterator implements Iterator<Vertex> {
	
		/**
		 * The spatial vertex iterator.
		 */
		private final Iterator<SpatialPath.Vertex> spatialIterator;
		
		/**
		 * The time iterator.
		 */
		private final Iterator<LocalDateTime> timeIterator;
	
		/**
		 * Constructs a new {@code VertexIterator} for the given trajectory.
		 * 
		 * @param trajectory
		 */
		private VertexIterator(Trajectory trajectory) {
			this.spatialIterator = trajectory.getSpatialPath().vertexIterator();
			this.timeIterator = trajectory.getTimes().iterator();
		}
	
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			// equivalent to timeIterator.hasNext()
			return spatialIterator.hasNext();
		}
	
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Vertex next() {
			return new Vertex(spatialIterator.next(), timeIterator.next());
		}
	
	}

	/**
	 * @return a {@code SegmentIterator}.
	 */
	public default Iterator<Segment> segmentIterator() {
		return new SegmentIterator(this);
	}

	/**
	 * The segment of a {@code Trajectory}. Stores additional information about
	 * the segment in context to the path.
	 */
	public static class Segment {
		
		/**
		 * The start vertex.
		 */
		private final Vertex start;
		
		/**
		 * The finish vertex.
		 */
		private final Vertex finish;
		
		/**
		 * The spatial segment.
		 */
		private final SpatialPath.Segment spatialSegment;
		
		/**
		 * Caches the duration.
		 */
		private transient Duration duration = null;
		
		/**
		 * Caches the duration in seconds.
		 */
		private transient double seconds = Double.NaN;
		
		/**
		 * Constructs a new {@code Segment} connecting the given vertices.
		 * 
		 * @param start
		 *            start vertex
		 * @param finish
		 *            finish vertex
		 * @param spatialSegment
		 */
		private Segment(Vertex start, Vertex finish, SpatialPath.Segment spatialSegment) {
			this.start = start;
			this.finish = finish;
			this.spatialSegment = spatialSegment;
		}
		
		/**
		 * @return whether this segment is the first one.
		 */
		public boolean isFirst() {
			return spatialSegment.isFirst();
		}
	
		/**
		 * @return whether this segment is the last one.
		 */
		public boolean isLast() {
			return spatialSegment.isLast();
		}
	
		/**
		 * @return whether the segment is stationary (i.e., does not change the
		 *         location).
		 */
		public boolean isStationary() {
			return getStartLocation().equals(getFinishLocation());
		}
	
		/**
		 * @return the start vertex.
		 */
		public Vertex getStartVertex() {
			return start;
		}
	
		/**
		 * @return the finish vertex.
		 */
		public Vertex getFinishVertex() {
			return finish;
		}
		
		/**
		 * @return the start location.
		 */
		public ImmutablePoint getStartLocation() {
			return start.getLocation();
		}
		
		/**
		 * @return the finish location.
		 */
		public ImmutablePoint getFinishLocation() {
			return finish.getLocation();
		}
		
		/**
		 * @return the start time.
		 */
		public LocalDateTime getStartTime() {
			return start.getTime();
		}
		
		/**
		 * @return the finish time.
		 */
		public LocalDateTime getFinishTime() {
			return finish.getTime();
		}
		
		/**
		 * @param baseTime
		 *            the base time to relate to.
		 * @return the start time in seconds.
		 */
		public double getStartTimeInSeconds(LocalDateTime baseTime) {
			return start.getTimeInSeconds(baseTime);
		}
	
		/**
		 * @param baseTime
		 *            the base time to relate to.
		 * @return the finish time in seconds.
		 */
		public double getFinishTimeInSeconds(LocalDateTime baseTime) {
			return finish.getTimeInSeconds(baseTime);
		}
	
		/**
		 * @return the spatial segment.
		 */
		public SpatialPath.Segment getSpatialSegment() {
			return spatialSegment;
		}
	
		/**
		 * @return the length of this segment.
		 */
		public double length() {
			return spatialSegment.length();
		}
		
		/**
		 * @return the duration.
		 */
		public Duration duration() {
			if (duration == null)
				duration = Duration.between(start.getTime(), finish.getTime());
			
			return duration;
		}
	
		/**
		 * @return the duration in seconds.
		 */
		public double durationInSeconds() {
			if (Double.isNaN(seconds))
				seconds = DurationConv.inSeconds(duration());
			
			return seconds;
		}
		
	}

	/**
	 * The {@code SegmentIterator} of a {@code Trajectory}.
	 */
	public static class SegmentIterator implements Iterator<Segment> {
		
		/**
		 * The vertex iterator.
		 */
		private final Iterator<Trajectory.Vertex> vertexIterator;
		
		/**
		 * The spatial segment iterator.
		 */
		private final Iterator<SpatialPath.Segment> spatialSegmentIterator;
		
		/**
		 * The last yielded vertex.
		 */
		private Trajectory.Vertex lastVertex = null;
		
		/**
		 * Constructs a new {@code SegmentIterator} for the given trajectory.
		 * 
		 * @param trajectory
		 */
		private SegmentIterator(Trajectory trajectory) {
			this.vertexIterator = new VertexIterator(trajectory);
			this.spatialSegmentIterator = trajectory.getSpatialPath().segmentIterator();
			
			if (vertexIterator.hasNext())
				lastVertex = vertexIterator.next();
		}
	
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			// equivalent to spatialSegmentIterator.hasNext()
			return vertexIterator.hasNext();
		}
	
		/*
		 * (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
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

}