package tasks.factories;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static util.TimeFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;
import tasks.TaskPlanner;
import tasks.WorkerUnit;
import tasks.WorkerUnitSpecification;
import world.RadiusBasedWorldPerspectiveCache;
import world.World;
import world.WorldPerspectiveCache;
import world.pathfinder.StraightEdgePathfinder;

import com.vividsolutions.jts.geom.Point;

public class WorkerUnitFactory {

	private static final ImmutablePolygon DEFAULT_SHAPE =
		immutablePolygon(-5., 5., 5., 5., 5., -5., -5., -5., -5., 5.);

	private static final double DEFAULT_MAX_SPEED = 1.0;

	private static final long DEFAULT_INITIAL_SECONDS = 0L;

	private static WorkerUnitFactory instance = null;

	private TaskPlanner taskPlanner = new TaskPlanner();

	private ImmutablePolygon shape;

	private double maxSpeed;

	private double initialSeconds;

	public WorkerUnitFactory() {
		this(
			DEFAULT_SHAPE,
			DEFAULT_MAX_SPEED,
			DEFAULT_INITIAL_SECONDS
		);
	}

	public WorkerUnitFactory(ImmutablePolygon shape, double maxSpeed, long initialSeconds) {
		this.shape = shape;
		this.maxSpeed = maxSpeed;
		this.initialSeconds = initialSeconds;

		World world = new World();
		WorldPerspectiveCache perspectiveCache =
			new RadiusBasedWorldPerspectiveCache(world, StraightEdgePathfinder.class);

		taskPlanner.setPerspectiveCache(perspectiveCache);
		taskPlanner.setWorkerPool(emptyList());
	}

	public static WorkerUnitFactory getInstance() {
		if (instance == null)
			instance = new WorkerUnitFactory();

		return instance;
	}

	private ImmutablePolygon getShape() {
		return shape;
	}

	private TaskPlanner getTaskPlanner() {
		return taskPlanner;
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

	public WorkerUnit createWorkerUnit(double x, double y) {
		return createWorkerUnit(getShape(), getMaxSpeed(), x, y, getInitialSeconds());
	}

	public WorkerUnit createWorkerUnit(ImmutablePolygon shape, double maxSpeed, double x, double y, double t) {
		return new WorkerUnit(createWorkerUnitSpecification(shape, maxSpeed, x, y, t));
	}

	public WorkerUnitSpecification createWorkerUnitSpecification(double x, double y) {
		return createWorkerUnitSpecification(getShape(), getMaxSpeed(), x, y, getInitialSeconds());
	}

	public WorkerUnitSpecification createWorkerUnitSpecification(ImmutablePolygon shape, double maxSpeed, double x, double y, double t) {
		ImmutablePoint initialLocation = immutablePoint(x, y);
		LocalDateTime initialTime = atSecond(t);

		return new WorkerUnitSpecification(shape, maxSpeed, initialLocation, initialTime);
	}

	public boolean addTask(WorkerUnit worker, UUID taskId, double x, double y, long tStart, long tEnd) {
		return addTaskWithDuration(worker, taskId, x, y, tStart, tEnd - tStart);
	}

	public boolean addTaskWithDuration(WorkerUnit worker, UUID taskId, double x, double y, long t, long d) {
		TaskPlanner tp = getTaskPlanner();

		Point location = point(x, y);
		LocalDateTime time = atSecond(t);
		Duration duration = Duration.ofSeconds(d);

		tp.setWorkerUnit(worker);
		tp.setTaskId(taskId);
		tp.setLocation(location);
		tp.setEarliestStartTime(time);
		tp.setLatestStartTime(time);
		tp.setDuration(duration);

		boolean status = tp.plan();

		if (!status)
			return false;

		return true;
	}

}
