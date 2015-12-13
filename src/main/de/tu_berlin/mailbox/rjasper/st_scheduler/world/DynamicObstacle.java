package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.immutable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;
import de.tu_berlin.mailbox.rjasper.util.NameProvider;

/**
 * <p>
 * A {@code DynamicObstacle} represents a physical object in the real world. It
 * describes the shape and the trajectory of such an object. The shape's origin
 * marks the center of the object. Offsetting the shape by a spatial point of
 * the trajectory gives the position of the shape to given time.
 * </p>
 * 
 * @author Rico Jasper
 */
public class DynamicObstacle implements Cloneable {

	/**
	 * The physical shape.
	 */
	private ImmutablePolygon shape;

	/**
	 * The trajectory.
	 */
	private Trajectory trajectory;

	/**
	 * Creates a new {@code DynamicObstacle} with the given physical shape and
	 * trajectory movement.
	 * 
	 * @param shape
	 * @param trajectory
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if any of the following is true:
	 *             <ul>
	 *             <li>The shape is empty, non-simple, or invalid.</li>
	 *             <li>The trajectory is empty or instantaneous.</li>
	 *             </ul>
	 */
	public DynamicObstacle(ImmutablePolygon shape, Trajectory trajectory) {
		Objects.requireNonNull(shape, "shape");
		Objects.requireNonNull(trajectory, "trajectory");
		GeometriesRequire.requireValidSimple2DPolygon(shape, "shape");
		
		if (trajectory.isEmpty() || trajectory.duration().isZero())
			throw new IllegalArgumentException("illegal trajectory");

		this.shape = immutable(shape);
		this.trajectory = trajectory;
	}

	/**
	 * @return the physical shape.
	 */
	public ImmutablePolygon getShape() {
		return shape;
	}

	/**
	 * @return the trajectory describing the movement.
	 */
	public Trajectory getTrajectory() {
		return trajectory;
	}

	/**
	 * @return the spatial path of the trajectory.
	 * @see Trajectory#getSpatialPath()
	 */
	public SpatialPath getSpatialPath() {
		return trajectory.getSpatialPath();
	}

	/**
	 * @return the start location of the trajectory.
	 * @see Trajectory#getStartLocation()
	 */
	public ImmutablePoint getStartLocation() {
		return trajectory.getStartLocation();
	}

	/**
	 * @return the finish location of the trajectory.
	 * @see Trajectory#getFinishLocation()
	 */
	public ImmutablePoint getFinishLocation() {
		return trajectory.getFinishLocation();
	}

	/**
	 * @return the time ordinates of the trajectory.
	 * @see Trajectory#getTimes()
	 */
	public List<LocalDateTime> getTimes() {
		return trajectory.getTimes();
	}

	/**
	 * @return the start time of the trajectory.
	 * @see Trajectory#getStartTime()
	 */
	public LocalDateTime getStartTime() {
		return trajectory.getStartTime();
	}

	/**
	 * @return the finish time of the trajectory
	 * @see Trajectory#getFinishTime()
	 */
	public LocalDateTime getFinishTime() {
		return trajectory.getFinishTime();
	}

	/**
	 * @return the duration of the trajectory.
	 * @see Trajectory#duration()
	 */
	public Duration getDuration() {
		return trajectory.duration();
	}

	/**
	 * Creates a new {@code DynamicObstacle} with a buffered version of this
	 * one's shape.
	 * 
	 * @param distance
	 *            of the buffer
	 * @return the buffered version.
	 * @throws IllegalArgumentException
	 *             if {@code distance} is not a positive finite number.
	 */
	public DynamicObstacle buffer(double distance) {
		if (!Double.isFinite(distance) || distance < 0.0)
			throw new IllegalArgumentException("invalid distance");

		DynamicObstacle clone = clone();

		clone.shape = immutable((Polygon) getShape().buffer(distance));

		return clone;
	}

	/**
	 * Calculates the radius.
	 *
	 * @return the radius.
	 */
	public double calcRadius() {
		Coordinate[] coords = shape.getCoordinates();

		// determine the maximum square-distance to the origin
		double sqRadius = Arrays.stream(coords)
			.mapToDouble(c -> c.x*c.x + c.y*c.y)
			.max()
			.getAsDouble();

		return Math.sqrt(sqRadius);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DynamicObstacle clone() {
		try {
			return (DynamicObstacle) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return NameProvider.nameForOrDefault(this, () ->
			String.format("(%s, %s)", getShape(), getTrajectory()));
	}

}
