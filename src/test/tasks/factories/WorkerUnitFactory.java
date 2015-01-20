package tasks.factories;

import static util.TimeFactory.*;
import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.Duration;
import java.time.LocalDateTime;

import tasks.TaskPlanner;
import tasks.WorkerUnit;
import world.RadiusBasedWorldPerspectiveCache;
import world.World;
import world.WorldPerspectiveCache;
import world.pathfinder.StraightEdgePathfinder;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class WorkerUnitFactory {

	private static final Polygon DEFAULT_SHAPE = createDefaultShape();

	private static Polygon createDefaultShape() {
		return polygon(-5., 5., 5., 5., 5., -5., -5., -5., -5., 5.);
	}

	private static final double DEFAULT_MAX_SPEED = 1.0;

	private static final long DEFAULT_INITIAL_SECONDS = 0L;

	private static WorkerUnitFactory instance = null;

	private TaskPlanner taskPlanner = new TaskPlanner();

	private Polygon shape;

	private double maxSpeed;

	private double initialSeconds;

	public WorkerUnitFactory() {
		this(
			DEFAULT_SHAPE,
			DEFAULT_MAX_SPEED,
			DEFAULT_INITIAL_SECONDS
		);
	}

	public WorkerUnitFactory(Polygon shape, double maxSpeed, long initialSeconds) {
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

	private Polygon getShape() {
		return shape;
	}

	private TaskPlanner getTaskPlanner() {
		return taskPlanner;
	}

	public void setShape(Polygon shape) {
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

	public WorkerUnit createWorkerUnit(Polygon shape, double maxSpeed, double x, double y, double t) {
		Point initialLocation = point(x, y);
		LocalDateTime initialTime = atSecond(t);

		return new WorkerUnit(shape, maxSpeed, initialLocation, initialTime);
	}

	public boolean addTask(WorkerUnit worker, double x, double y, long tStart, long tEnd) {
		return addTaskWithDuration(worker, x, y, tStart, tEnd - tStart);
	}

	public boolean addTaskWithDuration(WorkerUnit worker, double x, double y, long t, long d) {
		TaskPlanner tp = getTaskPlanner();

		Point location = point(x, y);
		LocalDateTime time = atSecond(t);
		Duration duration = Duration.ofSeconds(d);

		tp.setWorkerUnit(worker);
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
