package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.factories;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePolygon;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.atSecond;

import java.time.LocalDateTime;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePolygon;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.NodeSpecification;

public class NodeFactory {

	private static final ImmutablePolygon DEFAULT_SHAPE =
		immutablePolygon(-5., 5., 5., 5., 5., -5., -5., -5., -5., 5.);

	private static final double DEFAULT_MAX_SPEED = 1.0;

	private static final long DEFAULT_INITIAL_SECONDS = 0L;

	private static NodeFactory instance = null;

	private ImmutablePolygon shape;

	private double maxSpeed;

	private double initialSeconds;

	public NodeFactory() {
		this(
			DEFAULT_SHAPE,
			DEFAULT_MAX_SPEED,
			DEFAULT_INITIAL_SECONDS
		);
	}

	public NodeFactory(ImmutablePolygon shape, double maxSpeed, long initialSeconds) {
		this.shape = shape;
		this.maxSpeed = maxSpeed;
		this.initialSeconds = initialSeconds;
	}

	public static NodeFactory getInstance() {
		if (instance == null)
			instance = new NodeFactory();

		return instance;
	}

	private ImmutablePolygon getShape() {
		return shape;
	}

	public void setShape(ImmutablePolygon shape) {
		this.shape = shape;
	}

	private double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	private double getInitialSeconds() {
		return initialSeconds;
	}

	public void setInitialSeconds(double initialSeconds) {
		this.initialSeconds = initialSeconds;
	}

	public Node createNode(String id, double x, double y) {
		return createNode(id, getShape(), getMaxSpeed(), x, y, getInitialSeconds());
	}

	public Node createNode(String id, ImmutablePolygon shape, double maxSpeed, double x, double y, double t) {
		return new Node(createNodeSpecification(id, shape, maxSpeed, x, y, t));
	}

	public NodeSpecification createNodeSpecification(String id, double x, double y) {
		return createNodeSpecification(id, getShape(), getMaxSpeed(), x, y, getInitialSeconds());
	}

	public NodeSpecification createNodeSpecification(String id, ImmutablePolygon shape, double maxSpeed, double x, double y, double t) {
		ImmutablePoint initialLocation = immutablePoint(x, y);
		LocalDateTime initialTime = atSecond(t);

		return new NodeSpecification(id, shape, maxSpeed, initialLocation, initialTime);
	}

}
