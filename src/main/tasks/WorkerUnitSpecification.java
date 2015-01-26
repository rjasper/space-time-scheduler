package tasks;

import java.time.LocalDateTime;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;
import jts.geom.util.GeometriesRequire;

/**
 * Specifies the representation of a physical worker unit in the real world.
 * This class abstracts the physical abilities of the real worker, such as its
 * shape and maximum velocity.</p>
 * 
 * @author Rico
 */
public class WorkerUnitSpecification {

	/**
	 * The physical shape of this worker.
	 */
	private final ImmutablePolygon shape;

	/**
	 * The maximum velocity of this worker.
	 */
	private final double maxSpeed;

	/**
	 * The initial location of the worker where it begins to 'exist'.
	 */
	private final ImmutablePoint initialLocation;

	/**
	 * The initial time of the worker when it begins to 'exist'.
	 */
	private final LocalDateTime initialTime;

	/**
	 * Constructs a worker specification defining its shape, maximum velocity,
	 * initial location and initial time.
	 *
	 * @param shape
	 *            the physical shape
	 * @param maxSpeed
	 *            the maximum velocity
	 * @param initialLocation
	 *            the initial location where the worker begins to 'exist'
	 * @param initialTime
	 *            this initial time when the worker begins to 'exist'
	 * @throws NullPointerException
	 *             if any object argument is null
	 * @throws IllegalArgumentException
	 *             if any of the following is true:
	 *             <ul>
	 *             <li>the shape is empty, non-simple, or invalid</li>
	 *             <li>the initial location is empty or invalid</li>
	 *             <li>the maximum speed is non-finite or non-positive</li>
	 *             </ul>
	 */
	public WorkerUnitSpecification(
		ImmutablePolygon shape,
		double maxSpeed,
		ImmutablePoint initialLocation,
		LocalDateTime initialTime)
	{
		Objects.requireNonNull(initialLocation, "initialLocation");
		Objects.requireNonNull(initialTime, "initialTime");
		GeometriesRequire.requireValidSimple2DPolygon(shape, "shape");
		GeometriesRequire.requireValid2DPoint(initialLocation, "initialLocation");

		if (!Double.isFinite(maxSpeed) || maxSpeed <= 0)
			throw new IllegalArgumentException("maximum speed is not a positive finite number");

		this.shape = shape;
		this.maxSpeed = maxSpeed;
		this.initialLocation = initialLocation;
		this.initialTime = initialTime;
	}

	/**
	 * @return the physical shape of this worker.
	 */
	public final ImmutablePolygon getShape() {
		return shape;
	}

	/**
	 * @return the maximum velocity.
	 */
	public final double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * @return the initial location of the worker where it begins to 'exist'.
	 */
	public final ImmutablePoint getInitialLocation() {
		return initialLocation;
	}

	/**
	 * @return the initial time of the worker when it begins to 'exist'.
	 */
	public final LocalDateTime getInitialTime() {
		return initialTime;
	}

}
