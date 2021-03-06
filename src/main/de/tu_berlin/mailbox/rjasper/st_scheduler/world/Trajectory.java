package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.time.TimeConv;

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
 * @author Rico Jasper
 */
public interface Trajectory
extends Path<Trajectory.Vertex, Trajectory.Segment>
{

	/**
	 * The vertex of a {@code Trajectory}. Stores additional information about
	 * the vertex in context to the path.
	 */
	public static class Vertex extends AbstractPath.Vertex {
		
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
		public Vertex(Trajectory trajectory, SpatialPath.Vertex spatialVertex, LocalDateTime time) {
			super(trajectory, spatialVertex.getIndex());
			
			this.spatialVertex = spatialVertex;
			this.time = time;
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
			return TimeConv.durationToSeconds(Duration.between(baseTime, time));
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("<%.2f, %.2f, %s>", getX(), getY(), getTime());
		}
		
	}

	/**
	 * The segment of a {@code Trajectory}. Stores additional information about
	 * the segment in context to the path.
	 */
	public static class Segment extends AbstractPath.Segment<Vertex> {
		
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
			super(start, finish);
			
			this.spatialSegment = spatialSegment;
		}
	
		/**
		 * @return whether the segment is stationary (i.e., does not change the
		 *         location).
		 */
		public boolean isStationary() {
			return getStartLocation().equals(getFinishLocation());
		}
	
		/**
		 * @return the spatial segment.
		 */
		public SpatialPath.Segment getSpatialSegment() {
			return spatialSegment;
		}
		
		/**
		 * @return the start location.
		 */
		public ImmutablePoint getStartLocation() {
			return startVertex.getLocation();
		}
		
		/**
		 * @return the finish location.
		 */
		public ImmutablePoint getFinishLocation() {
			return finishVertex.getLocation();
		}
		
		/**
		 * @return the start time.
		 */
		public LocalDateTime getStartTime() {
			return startVertex.getTime();
		}
		
		/**
		 * @return the finish time.
		 */
		public LocalDateTime getFinishTime() {
			return finishVertex.getTime();
		}
		
		/**
		 * @param baseTime
		 *            the base time to relate to.
		 * @return the start time in seconds.
		 */
		public double getStartTimeInSeconds(LocalDateTime baseTime) {
			return startVertex.getTimeInSeconds(baseTime);
		}
	
		/**
		 * @param baseTime
		 *            the base time to relate to.
		 * @return the finish time in seconds.
		 */
		public double getFinishTimeInSeconds(LocalDateTime baseTime) {
			return finishVertex.getTimeInSeconds(baseTime);
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
				duration = Duration.between(startVertex.getTime(), finishVertex.getTime());
			
			return duration;
		}
	
		/**
		 * @return the duration in seconds.
		 */
		public double durationInSeconds() {
			if (Double.isNaN(seconds))
				seconds = TimeConv.durationToSeconds(duration());
			
			return seconds;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("(%s, %s)", getStartVertex(), getFinishVertex());
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
	 * @throws NoSuchElementException
	 *             if the trajectory is empty.
	 */
	public abstract ImmutablePoint getStartLocation();

	/**
	 * @return the location of the last vertex. {@code null} if trajectory is
	 *         empty.
	 * @throws NoSuchElementException
	 *             if the trajectory is empty.
	 */
	public abstract ImmutablePoint getFinishLocation();

	/**
	 * @return the time of the first vertex. {@code null} if trajectory is
	 *         empty.
	 * @throws NoSuchElementException
	 *             if the trajectory is empty.
	 */
	public abstract LocalDateTime getStartTime();

	/**
	 * @return the time of the last vertex. {@code null} if trajectory is empty.
	 * @throws NoSuchElementException
	 *             if the trajectory is empty.
	 */
	public abstract LocalDateTime getFinishTime();

	/**
	 * Determines whether this trajectory is stationary in space during the
	 * given time interval [{@code from}, {@code to}].
	 * 
	 * @param from
	 * @param to
	 * @return {@code true} if the trajectory is stationary.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalStateException
	 *             if the trajectory is empty.
	 * @throws IllegalArgumentException
	 *             if {@code from} &gt;= {@code to} or [{@code from},
	 *             {@code to}] is not included within [{@link #getStartTime()},
	 *             {@link #getFinishTime()}].
	 */
	public abstract boolean isStationary(LocalDateTime from, LocalDateTime to);

	/**
	 * Interpolates the location at the given time.
	 * 
	 * @param time
	 * @return the location.
	 * @throws NullPointerException
	 *             if {@code time} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code time} is not covered by the trajectory.
	 * @throws NoSuchElementException
	 *             if the trajectory is empty.
	 */
	public abstract ImmutablePoint interpolateLocation(LocalDateTime time);

	/**
	 * @return the euclidean length of the spatial path.
	 */
	public abstract double length();

	/**
	 * @return the time difference between the first and last vertex.
	 */
	public abstract Duration duration();

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
