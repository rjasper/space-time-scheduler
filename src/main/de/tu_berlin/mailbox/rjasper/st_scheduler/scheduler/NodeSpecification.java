package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import java.time.LocalDateTime;
import java.util.Objects;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;

/**
 * Specifies the representation of a physical node unit in the real world.
 * This class abstracts the physical abilities of the real node, such as its
 * shape and maximum velocity.</p>
 *
 * @author Rico Jasper
 */
public final class NodeSpecification {

	/**
	 * The node's ID.
	 */
	private final String nodeId;

	/**
	 * The physical shape of this node.
	 */
	private final ImmutablePolygon shape;

	/**
	 * The maximum velocity of this node.
	 */
	private final double maxSpeed;

	/**
	 * The initial location of the node where it begins to 'exist'.
	 */
	private final ImmutablePoint initialLocation;

	/**
	 * The initial time of the node when it begins to 'exist'.
	 */
	private final LocalDateTime initialTime;

	/**
	 * Constructs a node specification defining its shape, maximum velocity,
	 * initial location and initial time.
	 *
	 * @param nodeId
	 *            the node id.
	 * @param shape
	 *            the physical shape
	 * @param maxSpeed
	 *            the maximum velocity
	 * @param initialLocation
	 *            the location where the node begins to 'exist'
	 * @param initialTime
	 *            the time when the node begins to 'exist'
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
	public NodeSpecification(
		String nodeId,
		ImmutablePolygon shape,
		double maxSpeed,
		ImmutablePoint initialLocation,
		LocalDateTime initialTime)
	{
		Objects.requireNonNull(nodeId, "nodeId");
		Objects.requireNonNull(initialLocation, "initialLocation");
		Objects.requireNonNull(initialTime, "initialTime");
		GeometriesRequire.requireValidSimple2DPolygon(shape, "shape");
		GeometriesRequire.requireValid2DPoint(initialLocation, "initialLocation");

		if (!Double.isFinite(maxSpeed) || maxSpeed <= 0)
			throw new IllegalArgumentException("maximum speed is not a positive finite number");

		this.nodeId = nodeId;
		this.shape = shape;
		this.maxSpeed = maxSpeed;
		this.initialLocation = initialLocation;
		this.initialTime = initialTime;
	}

	/**
	 * @return the node's ID.
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @return the physical shape of this node.
	 */
	public ImmutablePolygon getShape() {
		return shape;
	}

	/**
	 * @return the maximum velocity.
	 */
	public double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * @return the initial location of the node where it begins to 'exist'.
	 */
	public ImmutablePoint getInitialLocation() {
		return initialLocation;
	}

	/**
	 * @return the initial time of the node when it begins to 'exist'.
	 */
	public LocalDateTime getInitialTime() {
		return initialTime;
	}

}
