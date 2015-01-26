package world;

import static common.collect.ImmutablesCollectors.*;
import static java.util.Spliterator.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;
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
public class SimpleTrajectory implements Trajectory {

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

		this.spatialPath = spatialPath;
		this.times = times;
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
	 * @see world.Trajectory#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
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

		return getSpatialPath().get(0);
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

		return getSpatialPath().get(size() - 1);
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

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getLength()
	 */
	@Override
	public double getLength() {
		return spatialPath.length();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getTrace()
	 */
	@Override
	public Geometry getTrace() {
		return spatialPath.trace();
	}

	/**
	 * @return the number of vertices.
	 */
	public int size() {
		return spatialPath.size();
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#calcArcTimePath(java.time.LocalDateTime)
	 */
	@Override
	public ArcTimePath calcArcTimePath(LocalDateTime baseTime) {
		Spliterator<Vertex> spliterator = Spliterators.spliterator(
			vertexIterator(), size(), IMMUTABLE | ORDERED);

		ImmutableList<ImmutablePoint> vertices = StreamSupport
			.stream(spliterator, false)
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
		return String.format("(%s, %s)", getSpatialPath(), getTimes());
	}

}
