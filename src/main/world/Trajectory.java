package world;

import java.time.Duration;
import java.time.LocalDateTime;

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
public interface Trajectory extends Path<Trajectory.Vertex, Trajectory.Segment> {

	/**
	 * The vertex of a {@code Trajectory}. Stores additional information about
	 * the vertex in context to the path.
	 */
	public static class Vertex implements Path.Vertex {
		
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
		public Vertex(SpatialPath.Vertex spatialVertex, LocalDateTime time) {
			this.spatialVertex = spatialVertex;
			this.time = time;
		}
	
		@Override
		public boolean isFirst() {
			return spatialVertex.isFirst();
		}
	
		@Override
		public boolean isLast() {
			return spatialVertex.isLast();
		}
	
		@Override
		public int getIndex() {
			return spatialVertex.getIndex();
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
	 * The segment of a {@code Trajectory}. Stores additional information about
	 * the segment in context to the path.
	 */
	public static class Segment implements Path.Segment<Vertex> {
		
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
		public Segment(Vertex start, Vertex finish, SpatialPath.Segment spatialSegment) {
			this.start = start;
			this.finish = finish;
			this.spatialSegment = spatialSegment;
		}
		
		/**
		 * @return whether this segment is the first one.
		 */
		@Override
		public boolean isFirst() {
			return spatialSegment.isFirst();
		}
	
		/**
		 * @return whether this segment is the last one.
		 */
		@Override
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
		@Override
		public Vertex getStartVertex() {
			return start;
		}
	
		/**
		 * @return the finish vertex.
		 */
		@Override
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
	 * Interpolates the location at the given time.
	 * 
	 * @param time
	 * @return the location.
	 * @throws NullPointerException
	 *             if {@code time} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code time} is not covered by the trajectory.
	 */
	public abstract ImmutablePoint interpolateLocation(LocalDateTime time);

	/**
	 * @return the euclidean length of the spatial path.
	 */
	public abstract double length();

	/**
	 * @return the trace which is a geometry only including all points of the
	 *         spatial path.
	 */
	public abstract Geometry trace();
	
	/**
	 * Calculates the sub trajectory given by a time interval.
	 * 
	 * @param startTime
	 * @param finishTime
	 * @return the sub trajectory. The trajectory will be empty if
	 *         {@code startTime} &lt;= {@code finishTime}
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 */
	public abstract Trajectory subPath(LocalDateTime startTime, LocalDateTime finishTime);

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
	@Override
	public abstract Trajectory concat(Path<? extends Vertex, ? extends Segment> other);

	/**
	 * Calculates the arc time path (s-t) of this trajectory in relation to the
	 * given base time.
	 * 
	 * @param baseTime
	 * @return the arc time path.
	 * @throws NullPointerException if {@code baseTime} is {@code null}.
	 */
	public abstract ArcTimePath calcArcTimePath(LocalDateTime baseTime);

}