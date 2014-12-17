package world;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	public abstract List<Point> getSpatialPath();

	/**
	 * @return the temporal ordinates (t).
	 */
	public abstract List<LocalDateTime> getTimes();

	/**
	 * @return the location of the first vertex. {@code null} if trajectory is
	 *         empty.
	 */
	public abstract Point getStartLocation();

	/**
	 * @return the location of the last vertex. {@code null} if trajectory is
	 *         empty.
	 */
	public abstract Point getFinishLocation();

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
	 */
	public abstract List<Point> calcArcTimePath(LocalDateTime baseTime);

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
		
		List<Point> lhsSpatialPath = getSpatialPath();
		List<Point> rhsSpatialPath = other.getSpatialPath();
		List<LocalDateTime> lhsTimes = getTimes();
		List<LocalDateTime> rhsTimes = other.getTimes();

		int n = lhsSpatialPath.size() + rhsSpatialPath.size();

		List<Point> spatialPath = new ArrayList<>(n);
		List<LocalDateTime> times = new ArrayList<>(n);

		spatialPath.addAll(lhsSpatialPath);
		spatialPath.addAll(rhsSpatialPath);

		times.addAll(lhsTimes);
		times.addAll(rhsTimes);

		return new SimpleTrajectory(spatialPath, times);
	}

}