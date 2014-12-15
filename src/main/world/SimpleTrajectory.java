package world;

import static java.util.Collections.unmodifiableList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import util.PathOperations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * A {@code SimpleTrajectory} is a immutable trajectory which stores
 * the 3-dimensional vertices directly.
 * </p>
 *
 * <p>
 * The ordinates of the vertices are split as a point list and time list.
 * The i-th element of the point list corresponds to the i-th element of the
 * time list as they represent the i-th vertex of the trajectory together.
 * Therefore, both list always have the same size.
 * </p>
 *
 * @author Rico Jasper
 */
public class SimpleTrajectory extends CachedTrajectory {

	/**
	 * The spatial (x-y) ordinates.
	 */
	private final List<Point> spatialPath;

	/**
	 * The time (t) ordinates.
	 */
	private final List<LocalDateTime> times;

	/**
	 * Constructs a new SimpleTrajectory using the provided ordinates.
	 *
	 * @param spatialPath the x-y-ordinates
	 * @param times the t-ordinates.
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if any of the following is true:
	 * <ul>
	 * <li>{@code spatialPath} and {@code times} do not have the same size.</li>
	 * <li>{@code times} is not causal (increasing in time).</li>
	 * </ul>
	 */
	public SimpleTrajectory(List<Point> spatialPath, List<LocalDateTime> times) {
		Objects.requireNonNull(spatialPath, "spatialPath");
		Objects.requireNonNull(times, "times");

		if (spatialPath.size() != times.size())
			throw new IllegalArgumentException("spatialPath and times do not have the same size");

		// TODO check if times is causal (sorted)

		this.spatialPath = spatialPath;
		this.times = Collections.unmodifiableList(new ArrayList<>(times));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((spatialPath == null) ? 0 : spatialPath.hashCode());
		result = prime * result + ((times == null) ? 0 : times.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
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
	 * @see world.Trajectory#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getSpatialPath()
	 */
	@Override
	public List<Point> getSpatialPath() {
		return unmodifiableList(spatialPath);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getTimes()
	 */
	@Override
	public List<LocalDateTime> getTimes() {
		return unmodifiableList(times);
	}

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getStartLocation()
	 */
	@Override
	public Point getStartLocation() {
		if (isEmpty())
			return null;

		List<Point> spatialPath = getSpatialPath();

		return spatialPath.get(0);
	};

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getFinishLocation()
	 */
	@Override
	public Point getFinishLocation() {
		if (isEmpty())
			return null;

		List<Point> spatialPath = getSpatialPath();
		int n = spatialPath.size();

		return spatialPath.get(n-1);
	};

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getStartTime()
	 */
	@Override
	public LocalDateTime getStartTime() {
		if (isEmpty())
			return null;

		List<LocalDateTime> times = getTimes();

		return times.get(0);
	};

	/*
	 * (non-Javadoc)
	 * @see world.Trajectory#getFinishTime()
	 */
	@Override
	public LocalDateTime getFinishTime() {
		if (isEmpty())
			return null;

		List<LocalDateTime> times = getTimes();
		int n = times.size();

		return times.get(n-1);
	};

	/*
	 * (non-Javadoc)
	 * @see world.CachedTrajectory#calcLength()
	 */
	@Override
	protected double calcLength() {
		return PathOperations.length( getSpatialPath() );
	}

	/*
	 * (non-Javadoc)
	 * @see world.CachedTrajectory#calcTrace()
	 */
	@Override
	protected Geometry calcTrace() {
		return PathOperations.calcTrace( getSpatialPath() );
	}

	/**
	 * @return the number of vertices.
	 */
	public int size() {
		return spatialPath.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("(%s, %s)",
			getSpatialPath(), getTimes());
	}

}
