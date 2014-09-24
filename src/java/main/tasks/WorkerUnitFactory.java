package tasks;

import java.time.LocalDateTime;

import world.LocalDateTimeFactory;
import jts.geom.factories.EnhancedGeometryBuilder;

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
	
	private EnhancedGeometryBuilder builder;

	private LocalDateTimeFactory timeFactory;
	
	private Polygon shape;
	
	private double maxSpeed;
	
	private long initialSeconds;
	
	public WorkerUnitFactory() {
		this(
			EnhancedGeometryBuilder.getInstance(),
			LocalDateTimeFactory.getInstance(),
			DEFAULT_SHAPE,
			DEFAULT_MAX_SPEED,
			DEFAULT_INITIAL_SECONDS
		);
	}

	public WorkerUnitFactory(EnhancedGeometryBuilder builder, LocalDateTimeFactory timeFact, Polygon shape, double maxSpeed, long initialSeconds) {
		this.builder = builder;
		this.timeFactory = timeFact;
		this.shape = shape;
		this.maxSpeed = maxSpeed;
		this.initialSeconds = initialSeconds;
	}
	
	public static WorkerUnitFactory getInstance() {
		if (instance == null)
			instance = new WorkerUnitFactory();
		
		return instance;
	}
	
	private EnhancedGeometryBuilder getBuilder() {
		return builder;
	}
	
	public void setBuilder(EnhancedGeometryBuilder builder) {
		this.builder = builder;
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

	public void setShape(Polygon shape) {
		this.shape = shape;
	}

	private double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	private long getInitialSeconds() {
		return initialSeconds;
	}

	public void setInitialSeconds(long initialSeconds) {
		this.initialSeconds = initialSeconds;
	}

	public WorkerUnit createWorkerUnit(double x, double y) {
		return createWorkerUnit(getShape(), getMaxSpeed(), x, y, getInitialSeconds());
	}

	public WorkerUnit createWorkerUnit(Polygon shape, double maxSpeed, double x, double y, long t) {
		EnhancedGeometryBuilder builder = getBuilder();
		LocalDateTimeFactory timeFact = getTimeFactory();

		Point initialLocation = builder.point(x, y);
		LocalDateTime initialTime = timeFact.second(t);
		
		return new WorkerUnit(shape, maxSpeed, initialLocation, initialTime);
	}

}
