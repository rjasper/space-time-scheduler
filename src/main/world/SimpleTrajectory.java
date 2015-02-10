package world;

import static common.collect.ImmutablesCollectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import jts.geom.immutable.ImmutablePoint;
import world.util.BinarySearchSeeker;
import world.util.DoubleSubTrajectoryOperation;
import world.util.IndexSeeker;
import world.util.Interpolator;
import world.util.Interpolator.InterpolationResult;
import world.util.PointPathInterpolator;
import world.util.Seeker;
import world.util.TimeSubIndexInterpolator;
import world.util.TimeSubTrajectoryOperation;

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
	
	@Override
	public ImmutablePoint interpolateLocation(LocalDateTime time) {
		Objects.requireNonNull(time, "time");
		
		if (time.isBefore(getStartTime()) || time.isAfter(getFinishTime()))
			throw new IllegalArgumentException("time must be covered by this trajectory");
		
		// first step is to calculate the sub index of the given time
		
		Seeker<LocalDateTime, LocalDateTime> timeSeeker = new BinarySearchSeeker<>(
			this.getTimes()::get,
			Function.identity(),
			(t1, t2) -> t1.compareTo(t2),
			size());
		Interpolator<LocalDateTime, Double> indexInterpolator = new TimeSubIndexInterpolator(timeSeeker);
		
		InterpolationResult<Double> indexResult = indexInterpolator.interpolate(time);
		
		double index = indexResult.getInterpolation();
		
		// second step interpolates the location at the given sub index
		
		SpatialPath spatialPath = getSpatialPath();
		
		Seeker<Double, SpatialPath.Vertex> locationSeeker =
			new IndexSeeker<>(spatialPath::getVertex, size());
		Interpolator<Double, ImmutablePoint> locationInterpolator =
			new PointPathInterpolator<SpatialPath.Vertex>(locationSeeker);
		
		InterpolationResult<ImmutablePoint> locationResult = locationInterpolator.interpolate(index);
		
		return locationResult.getInterpolation();
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
	 * @see world.Path#subPath(double, double)
	 */
	@Override
	public Trajectory subPath(double startPosition, double finishPosition) {
		return DoubleSubTrajectoryOperation.subPath(this,
			v -> (double) v.getIndex(),
			startPosition, finishPosition);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#subPath(java.time.LocalDateTime, java.time.LocalDateTime)
	 */
	@Override
	public Trajectory subPath(LocalDateTime startTime, LocalDateTime finishTime) {
		return TimeSubTrajectoryOperation.subPath(this, startTime, finishTime);
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
