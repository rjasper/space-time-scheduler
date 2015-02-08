package world;

import static common.collect.ImmutablesCollectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static util.DurationConv.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 * A {@code SimpleTrajectory} is a immutable trajectory which stores the
 * 3-dimensional vertices directly.
 * </p>
 *
 * <p>
 * The ordinates of the vertices are split as a point list and time list. The
 * i-th element of the point list corresponds to the i-th element of the time
 * list as they represent the i-th vertex of the trajectory together. Therefore,
 * both list always have the same size.
 * </p>
 *
 * @author Rico Jasper
 */
public class SimpleTrajectory extends AbstractPath<Trajectory.Vertex, Trajectory.Segment> implements Trajectory {
	
	/**
	 * An empty {@code SimpleTrajectory}.
	 */
	private static final SimpleTrajectory EMPTY =
		new SimpleTrajectory(SpatialPath.empty(), ImmutableList.of());
	
	/**
	 * @return an empty {@code SimpleTrajectory}.
	 */
	public static SimpleTrajectory empty() {
		return EMPTY;
	}

	/**
	 * The spatial (x-y) ordinates.
	 */
	private final SpatialPath spatialPath;

	/**
	 * The time (t) ordinates.
	 */
	private final ImmutableList<LocalDateTime> times;

	/**
	 * Caches the duration of this trajectory.
	 */
	private transient Duration duration = null;

	/**
	 * Constructs a new SimpleTrajectory using the provided ordinates.
	 *
	 * @param spatialPath
	 *            the x-y-ordinates
	 * @param times
	 *            the t-ordinates.
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if any of the following is true:
	 *             <ul>
	 *             <li>{@code spatialPath} and {@code times} do not have the
	 *             same size.</li>
	 *             <li>{@code times} is not causal (increasing in time).</li>
	 *             </ul>
	 */
	public SimpleTrajectory(SpatialPath spatialPath, ImmutableList<LocalDateTime> times) {
		Objects.requireNonNull(spatialPath, "spatialPath");
		Objects.requireNonNull(times, "times");

		if (spatialPath.size() != times.size())
			throw new IllegalArgumentException(
				"spatialPath and times do not have the same size");
		if (!Ordering.natural().isOrdered(times))
			throw new IllegalArgumentException("times is not causal");

		this.spatialPath = spatialPath;
		this.times = times;
	}

	/*
	 * (non-Javadoc)
	 * @see world.AbstractPath#makeVertex(int, boolean, boolean)
	 */
	@Override
	protected Trajectory.Vertex makeVertex(int index, boolean first, boolean last) {
		SpatialPath.Vertex spatialVertex = getSpatialPath().getVertex(index);
		LocalDateTime time = getTimes().get(index);
		
		return new Trajectory.Vertex(spatialVertex, time);
	}

	/*
	 * (non-Javadoc)
	 * @see world.AbstractPath#makeSegment(world.Path.Vertex, world.Path.Vertex)
	 */
	@Override
	protected Trajectory.Segment makeSegment(Trajectory.Vertex start, Trajectory.Vertex finish) {
		SpatialPath.Segment spatialSegment = new SpatialPath.Segment(
			start.getSpatialVertex(), finish.getSpatialVertex());
		
		return new Trajectory.Segment(start, finish, spatialSegment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((spatialPath == null) ? 0 : spatialPath.hashCode());
		result = prime * result + ((times == null) ? 0 : times.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		SimpleTrajectory other = (SimpleTrajectory) obj;
		if (spatialPath == null) {
			if (other.spatialPath != null)
				return false;
		} else if (!spatialPath.equals(other.spatialPath))
			return false;
		if (times == null) {
			if (other.times != null)
				return false;
		} else if (!times.equals(other.times))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see world.Trajectory#getSpatialPath()
	 */
	@Override
	public SpatialPath getSpatialPath() {
		return spatialPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see world.Trajectory#getTimes()
	 */
	@Override
	public ImmutableList<LocalDateTime> getTimes() {
		return times;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see world.Trajectory#getStartLocation()
	 */
	@Override
	public ImmutablePoint getStartLocation() {
		if (isEmpty())
			return null;

		return getSpatialPath().getPoint(0);
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see world.Trajectory#getFinishLocation()
	 */
	@Override
	public ImmutablePoint getFinishLocation() {
		if (isEmpty())
			return null;

		return getSpatialPath().getPoint(size() - 1);
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see world.Trajectory#getStartTime()
	 */
	@Override
	public LocalDateTime getStartTime() {
		if (isEmpty())
			return null;

		return getTimes().get(0);
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see world.Trajectory#getFinishTime()
	 */
	@Override
	public LocalDateTime getFinishTime() {
		if (isEmpty())
			return null;

		List<LocalDateTime> times = getTimes();
		int n = times.size();

		return times.get(n - 1);
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see world.Trajectory#getDuration()
	 */
	@Override
	public Duration getDuration() {
		if (duration == null) {
			duration = isEmpty()
				? Duration.ZERO
				: Duration.between(getStartTime(), getFinishTime());
		}

		return duration;
	}

	/**
	 * @return the number of vertices.
	 */
	@Override
	public int size() {
		return spatialPath.size();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getLength()
	 */
	@Override
	public double length() {
		return spatialPath.length();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getTrace()
	 */
	@Override
	public Geometry trace() {
		return spatialPath.trace();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#interpolateLocation(java.time.LocalDateTime)
	 */
	@Override
	public ImmutablePoint interpolateLocation(LocalDateTime time) {
		Objects.requireNonNull(time, "time");
		
		if (time.isBefore(getStartTime()) || time.isAfter(getFinishTime()))
			throw new IllegalArgumentException("time must be covered by this trajectory");
		
		int n = size();
		int segmentPos = seekSegment(time);
		
		// if after the last segment (or last point)
		if (segmentPos == n-1)
			return getFinishLocation();
		
		SpatialPath spatialPath = getSpatialPath();
		List<LocalDateTime> times = getTimes();
		LocalDateTime t1 = times.get(segmentPos);
		Duration d1 = Duration.between(t1, time);
		
		// if time is on spot of one point
		if (d1.isZero())
			return spatialPath.getPoint(segmentPos);
		
		LocalDateTime t2 = times.get(segmentPos+1);
		Duration d = Duration.between(t1, t2);
		
		double alpha = inSeconds(d1) / inSeconds(d);
		
		return spatialPath.interpolate(segmentPos, alpha);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#subTrajectory(java.time.LocalDateTime, java.time.LocalDateTime)
	 */
	@Override
	public SimpleTrajectory subTrajectory(LocalDateTime startTime, LocalDateTime finishTime) {
		// check empty cases
		
		// if trajectory is empty
		if (isEmpty())
			return this; // empty
		// if the interval is empty or does not intersect
		if (startTime.compareTo(finishTime) >= 0 ||
			startTime.compareTo(getFinishTime()) >= 0 ||
			finishTime.compareTo(getStartTime()) <= 0)
		{
			return empty();
		}
		
		// at this point it is is guaranteed that the trajectory intersects with the interval
		
		int t0CmpStartTime = getStartTime().compareTo(startTime);
		int tEndCmpFinishTime = getFinishTime().compareTo(finishTime);
		
		// if identical
		if (t0CmpStartTime >= 0 && tEndCmpFinishTime <= 0)
			return this;
		 
		int n = size();
		int t0CmpFinishTime = getStartTime().compareTo(finishTime);
		// start values will always be set in the for loop
		// finish values will not be set in the loop if finishCmp >= 0
		// start index is inclusive, finish index exclusive
		int startIndex = 0, finishIndex = n;
		double startAlpha = 0.0, finishAlpha = 0.0;
		
		ImmutableList.Builder<LocalDateTime> builder = ImmutableList.builder();
		
		List<LocalDateTime> times = getTimes();
		LocalDateTime t1 = times.get(0);
		int t1CmpStartTime = t0CmpStartTime;
		int t1CmpFinishTime = t0CmpFinishTime;
		
		// determine times of the sub trajectory and determine start and finish
		// indices to calculate the spatial path
		for (int i = 1; i < n; ++i) {
			LocalDateTime t2 = times.get(i);
			int t2CmpStartTime = t2.compareTo(startTime);
			int t2CmpFinishTime = t2.compareTo(finishTime);
			
			// add times to list

			// t1 < startTime < t2
			if (t1CmpStartTime < 0 && t2CmpStartTime > 0)
				builder.add(startTime);
			// startTime <= t1 <= finishTime
			if (t1CmpStartTime >= 0 && t1CmpFinishTime <= 0)
				builder.add(t1);
			// t1 < finishTime < t2
			if (t1CmpFinishTime < 0 && t2CmpFinishTime > 0)
				builder.add(finishTime);
			
			// determine indices and alpha values
			
			// t1 <= startTime < t2
			if (t1CmpStartTime <= 0 && t2CmpStartTime > 0) {
				startIndex = i-1;
				startAlpha = inSeconds(Duration.between(t1, startTime))
					/ inSeconds(Duration.between(t1, t2));
			}
			// t1 <= finishTime < t2
			if (t1CmpFinishTime <= 0 && t2CmpFinishTime > 0) {
				finishIndex = i;
				finishAlpha = inSeconds(Duration.between(t1, finishTime))
					/ inSeconds(Duration.between(t1, t2));
			}
			
			t1 = t2;
			t1CmpStartTime = t2CmpStartTime;
			t1CmpFinishTime = t2CmpFinishTime;
		}

		// startTime <= t1 <= finishTime
		if (t1CmpStartTime >= 0 && t1CmpFinishTime <= 0)
			builder.add(t1);
		
		ImmutableList<LocalDateTime> subTimes = builder.build();
		
		SpatialPath subSpatialPath = getSpatialPath().subPath(startIndex, startAlpha, finishIndex, finishAlpha);
		
		return new SimpleTrajectory(subSpatialPath, subTimes);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Path#concat(world.Path)
	 */
	@Override
	public Trajectory concat(Path<? extends Trajectory.Vertex, ? extends Trajectory.Segment> other) {
		Objects.requireNonNull(other, "other");
		
		if (!(other instanceof Trajectory))
			throw new IllegalArgumentException("incompatible path");
		
		Trajectory traj = (Trajectory) other;
		
		if (getFinishTime().compareTo(traj.getStartTime()) > 0)
			throw new IllegalArgumentException("other is before this one");
		
		SpatialPath lhsSpatialPath = getSpatialPath();
		SpatialPath rhsSpatialPath = traj.getSpatialPath();
		List<LocalDateTime> lhsTimes = getTimes();
		List<LocalDateTime> rhsTimes = traj.getTimes();
	
		SpatialPath spatialPath = lhsSpatialPath.concat(rhsSpatialPath);
		ImmutableList<LocalDateTime> times = ImmutableList.<LocalDateTime>builder()
			.addAll(lhsTimes)
			.addAll(rhsTimes)
			.build();
	
		return new SimpleTrajectory(spatialPath, times);
	}
	
	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#calcArcTimePath(java.time.LocalDateTime)
	 */
	@Override
	public ArcTimePath calcArcTimePath(LocalDateTime baseTime) {
		ImmutableList<ImmutablePoint> vertices = vertexStream()
			.map(v -> {
				double arc = v.getSpatialVertex().getArc();
				double seconds = v.getTimeInSeconds(baseTime);
				
				return immutablePoint(arc, seconds);
			})
			.collect(toImmutableList());
	
		return new ArcTimePath(vertices);
	}

	/**
	 * Seeks the position of the segment covering the given time. If two
	 * segments cover the time (i.e., the time is on spot of a vertex) the
	 * second one's position is returned. This method assumes that {@code time}
	 * is covered by this trajectory.
	 * 
	 * @param time
	 * @return the segment position.
	 */
	private int seekSegment(LocalDateTime time) {
		// d1 + d2 == getDuration()
		Duration d1 = Duration.between(getStartTime(), time);
		Duration d2 = Duration.between(time, getFinishTime());
		
		if (d1.compareTo(d2) <= 0)
			return seekSegmentForward(time);
		else
			return seekSegmentBackward(time);
	}
	
	/**
	 * Seeks the segment covering the given time using a forward loop. This
	 * method assumes that {@code time} is covered by this trajectory.
	 * 
	 * @param time
	 * @return the segment's position.
	 */
	private int seekSegmentForward(LocalDateTime time) {
		List<LocalDateTime> times = getTimes();
		int n = times.size();
		
		for (int i = 1; i < n; ++i) {
			if (times.get(i).compareTo(time) < 0)
				return i-1;
		}
		
		return n-1;
	}

	/**
	 * Seeks the segment covering the given time using a backward loop. This
	 * method assumes that {@code time} is covered by this trajectory.
	 * 
	 * @param time
	 * @return the segment's position.
	 */
	private int seekSegmentBackward(LocalDateTime time) {
		List<LocalDateTime> times = getTimes();
		int n = times.size();
		
		for (int i = n-1; i > 0; --i) {
			if (times.get(i).compareTo(time) >= 0)
				return i;
		}
		
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Iterator<ImmutablePoint> points = getSpatialPath().getPoints().iterator();
		Iterator<LocalDateTime> times = getTimes().iterator();
		
		StringBuffer buf = new StringBuffer();
		
		buf.append("(");
		
		while (points.hasNext()) { // equivalent to times.hasNext()
			buf.append(points.next());
			buf.append(' ');
			buf.append(times.next());
			
			if (points.hasNext())
				buf.append(", ");
		}
		
		buf.append(")");
		
		return buf.toString();
	}

}
