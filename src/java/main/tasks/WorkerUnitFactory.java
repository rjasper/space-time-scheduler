package tasks;

import static java.util.Collections.emptyList;

import java.time.Duration;
import java.time.LocalDateTime;

import jts.geom.factories.EnhancedGeometryBuilder;
import world.LocalDateTimeFactory;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class WorkerUnitFactory {

	private static final Polygon DEFAULT_SHAPE =
		createDefaultShape(EnhancedGeometryBuilder.getInstance());

	private static Polygon createDefaultShape(EnhancedGeometryBuilder builder) {
		return builder.polygon(-5., 5., 5., 5., 5., -5., -5., -5., -5., 5.);
	}

	private static final double DEFAULT_MAX_SPEED = 1.0;

	private static final long DEFAULT_INITIAL_SECONDS = 0L;

	private static WorkerUnitFactory instance = null;

	private EnhancedGeometryBuilder geometryBuilder;

	private LocalDateTimeFactory timeFactory;

	private TaskPlanner taskPlanner = new TaskPlanner();

	private Polygon shape;

	private double maxSpeed;

	private double initialSeconds;

	public WorkerUnitFactory() {
		this(
			EnhancedGeometryBuilder.getInstance(),
			LocalDateTimeFactory.getInstance(),
			DEFAULT_SHAPE,
			DEFAULT_MAX_SPEED,
			DEFAULT_INITIAL_SECONDS
		);
	}

	public WorkerUnitFactory(EnhancedGeometryBuilder geomBuilder, LocalDateTimeFactory timeFact, Polygon shape, double maxSpeed, long initialSeconds) {
		this.geometryBuilder = geomBuilder;
		this.timeFactory = timeFact;
		this.shape = shape;
		this.maxSpeed = maxSpeed;
		this.initialSeconds = initialSeconds;

		taskPlanner.setStaticObstacles(emptyList());
		taskPlanner.setDynamicObstacles(emptyList());
		taskPlanner.setWorkerPool(emptyList());
	}

	public static WorkerUnitFactory getInstance() {
		if (instance == null)
			instance = new WorkerUnitFactory();

		return instance;
	}

	private EnhancedGeometryBuilder getGeometryBuilder() {
		return geometryBuilder;
	}

	public void setGeometryBuilder(EnhancedGeometryBuilder geomBuilder) {
		this.geometryBuilder = geomBuilder;
	}

	private Polygon getShape() {
		return shape;
	}

	private LocalDateTimeFactory getTimeFactory() {
		return timeFactory;
	}

	public void setTimeFactory(LocalDateTimeFactory timeFact) {
		this.timeFactory = timeFact;
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
		EnhancedGeometryBuilder geomFact = getGeometryBuilder();
		LocalDateTimeFactory timeFact = getTimeFactory();

		Point initialLocation = geomFact.point(x, y);
		LocalDateTime initialTime = timeFact.seconds(t);

		return new WorkerUnit(shape, maxSpeed, initialLocation, initialTime);
	}

	public boolean addTask(WorkerUnit worker, double x, double y, long tStart, long tEnd) {
		return addTaskWithDuration(worker, x, y, tStart, tEnd - tStart);
	}

	public boolean addTaskWithDuration(WorkerUnit worker, double x, double y, long t, long d) {
		EnhancedGeometryBuilder geomFact = getGeometryBuilder();
		LocalDateTimeFactory timeFactory = getTimeFactory();
		TaskPlanner tp = getTaskPlanner();

		Point location = geomFact.point(x, y);
		LocalDateTime time = timeFactory.second(t);
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
