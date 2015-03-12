package scheduler.factories;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static util.TimeFactory.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;
import scheduler.IdleSlot;
import scheduler.Schedule;
import scheduler.ScheduleAlternative;
import scheduler.Scheduler;
import scheduler.TaskPlanner;
import scheduler.Node;
import scheduler.NodeSpecification;
import world.World;
import world.WorldPerspective;
import world.pathfinder.StraightEdgePathfinder;

import com.vividsolutions.jts.geom.Point;

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

//	private TaskPlanner getTaskPlanner() {
//		return taskPlanner;
//	}

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

	public boolean addTask(Node worker, UUID taskId, double x, double y, long tStart, long tEnd) {
		return addTaskWithDuration(worker, taskId, x, y, tStart, tEnd - tStart);
	}

	public boolean addTaskWithDuration(Node worker, UUID taskId, double x, double y, long t, long d) {
		TaskPlanner tp = new TaskPlanner();

		Point location = point(x, y);
		LocalDateTime time = atSecond(t);
		Duration duration = Duration.ofSeconds(d);
		LocalDateTime floorTime = worker.floorIdleTimeOrNull(time);
		LocalDateTime ceilTime = worker.ceilingIdleTimeOrNull(time);
		
		if (floorTime == null || ceilTime == null)
			return false;
		
		IdleSlot idleSlot = worker.idleSlots(floorTime, ceilTime).iterator().next();
		WorldPerspective perspective = new WorldPerspective(
			new World(), new StraightEdgePathfinder());
		Schedule schedule = new Schedule();
		ScheduleAlternative alternative = new ScheduleAlternative();
		
		schedule.addWorker(worker);

		tp.setTaskId(taskId);
		tp.setWorker(worker);
		tp.setLocation(location);
		tp.setEarliestStartTime(time);
		tp.setLatestStartTime(time);
		tp.setDuration(duration);
		tp.setIdleSlot(idleSlot);
		tp.setWorldPerspective(perspective);
		tp.setSchedule(schedule);
		tp.setScheduleAlternative(alternative);
		tp.setFixedEnd(!ceilTime.isEqual(Scheduler.END_OF_TIME));

		boolean status = tp.plan();

		if (!status)
			return false;
		
		alternative.seal();
		schedule.addAlternative(alternative);
		schedule.integrate(alternative);

		return true;
	}

}
