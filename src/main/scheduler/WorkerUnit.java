package scheduler;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import jts.geom.immutable.ImmutablePoint;
import jts.geom.immutable.ImmutablePolygon;
import scheduler.util.IntervalSet.Interval;
import scheduler.util.MappedIntervalSet;
import world.DynamicObstacle;
import world.IdlingWorkerUnitObstacle;
import world.MovingWorkerUnitObstacle;
import world.SimpleTrajectory;
import world.Trajectory;
import world.WorkerUnitObstacle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

/**
 * <p>The representation of a physical worker unit in the real world which is
 * managed by a scheduler. This class abstracts the physical abilities of
 * the real worker, such as its shape and maximum velocity. It stores a
 * list of takes it is assigned to and its routes which it travels in the real
 * world.</p>
 *
 * <p>An object of this class is not meant to be used by any scheduler unrelated
 * class since it would expose critical methods, such as
 * {@link #addObstacleSection(WorkerUnitObstacle)} or
 * {@link #removeObstacleSection(WorkerUnitObstacle)} which don't check for
 * overall consistency.</p>
 *
 * @author Rico Jasper
 */
public class WorkerUnit {
	
	/**
	 * The worker's ID.
	 */
	private final String id;
	
	/**
	 * The reference to this worker.
	 */
	private final WorkerUnitReference reference;

	/**
	 * The physical shape of this worker.
	 */
	private final ImmutablePolygon shape;

	/**
	 * The radius of the shape.
	 */
	private final double radius;

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
	 * All tasks which were assigned to this worker.
	 */
	private TreeMap<LocalDateTime, Task> tasks = new TreeMap<>();

	/**
	 * An unmodifiable view on {@link #tasks}.
	 */
	private NavigableMap<LocalDateTime, Task> unmodifiableTasks
		= unmodifiableNavigableMap(tasks);

	/**
	 * All obstacle sections of this worker.
	 */
	private NavigableMap<LocalDateTime, WorkerUnitObstacle> obstacleSections = new TreeMap<>();

	/**
	 * An unmodifiable view on {@link #obstacleSections}.
	 */
	private NavigableMap<LocalDateTime, WorkerUnitObstacle> unmodifiableObstacleSegments =
		unmodifiableNavigableMap(obstacleSections);

	/**
	 * Constructs a worker defining its shape, maximum velocity, initial
	 * location and initial time.
	 * 
	 * @param spec
	 *            the specification used to define configure the worker.
	 */
	public WorkerUnit(WorkerUnitSpecification spec) {
		this.id = spec.getWorkerId();
		this.reference = new WorkerUnitReference(this);
		this.shape = spec.getShape();
		this.maxSpeed = spec.getMaxSpeed();
		this.initialLocation = spec.getInitialLocation();
		this.initialTime = spec.getInitialTime();
		this.radius = calcRadius(shape);

		putInitialObstacleSegment();
	}

	/**
	 * Initializes the {@link #obstacleSections} with an
	 * {@link IdlingWorkerUnitObstacle} at its initial location and initial
	 * time.
	 */
	private void putInitialObstacleSegment() {
		ImmutablePoint initialLocation = getInitialLocation();
		LocalDateTime initialTime = getInitialTime();

		WorkerUnitObstacle section = new IdlingWorkerUnitObstacle(this, initialLocation, initialTime);

		putObstacleSection(section);
	}

	/**
	 * @return the ID.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the reference to this worker.
	 */
	public WorkerUnitReference getReference() {
		return reference;
	}

	/**
	 * @return the physical shape of this worker.
	 */
	public ImmutablePolygon getShape() {
		return shape;
	}

	/**
	 * @return the radius of this worker's shape.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Calculates the radius.
	 *
	 * @param shape of the worker
	 * @return the radius.
	 */
	private static double calcRadius(Polygon shape) {
		Coordinate[] coords = shape.getCoordinates();

		// determine the maximum square-distance to the origin
		double sqRadius = Arrays.stream(coords)
			.mapToDouble(c -> c.x*c.x + c.y*c.y)
			.max()
			.getAsDouble();

		return Math.sqrt(sqRadius);
	}

	/**
	 * @return the maximum velocity.
	 */
	public double getMaxSpeed() {
		return maxSpeed;
	}

	/**
	 * @return the initial location of the worker where it begins to 'exist'.
	 */
	public ImmutablePoint getInitialLocation() {
		return initialLocation;
	}

	/**
	 * @return the initial time of the worker when it begins to 'exist'.
	 */
	public LocalDateTime getInitialTime() {
		return initialTime;
	}
	
	/**
	 * @return all tasks this unit is assigned to.
	 */
	public Collection<Task> getTasks() {
		return unmodifiableTasks.values();
	}
	
	public boolean hasTask(Task task) {
		Task retrieval = tasks.get(task.getStartTime());
		
		return retrieval != null && retrieval.equals(task);
	}

	/**
	 * @return an unmodifiable view on the time ordered map of tasks assigned to
	 * this worker.
	 */
	public NavigableMap<LocalDateTime, Task> getNavigableTasks() {
		return unmodifiableTasks;
	}
	
	/**
	 * @return a view on the tasks as a time interval set.
	 */
	public MappedIntervalSet<LocalDateTime, Task> getTaskIntervals() {
		return new MappedIntervalSet<LocalDateTime, Task>(tasks,
			t -> new Interval<LocalDateTime>(t.getStartTime(), t.getFinishTime()));
	}

	/**
	 * Assigns a new task to this worker.
	 *
	 * @param task
	 * @throws NullPointerException
	 *             if {@code task} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code task} is not assigned to this worker.
	 */
	public void addTask(Task task) {
		Objects.requireNonNull(task, "task");
		
		if (task.getAssignedWorker().getActual() != this)
			throw new IllegalArgumentException("task not assigned to this worker");

		tasks.put(task.getStartTime(), task);
	}
	
	/**
	 * Removes a task from this worker.
	 * 
	 * @param task
	 * @throws NullPointerException
	 *             if {@code task} is {@code null}.
	 * @throws IllegalArgumentException
	 *             if {@code task} is not assigned to this worker.
	 */
	public void removeTask(Task task) {
		Objects.requireNonNull(task, "task");
		
		boolean status = tasks.remove(task.getStartTime(), task);
		
		if (!status)
			throw new IllegalArgumentException("unknown task");
	}

	/**
	 * Updates the given trajectory.
	 * 
	 * @param trajectory
	 * @throws NullPointerException if {@code trajectory} is {@code null}.
	 */
	public void updateTrajectory(Trajectory trajectory) {
		// TODO implement
		
		throw new RuntimeException("nyi");
	}

	/**
	 * @return the obstacle sections of this worker.
	 */
	public Collection<WorkerUnitObstacle> getObstacleSections() {
		return unmodifiableObstacleSegments.values();
	}
	
	/**
	 * @return an unmodifiable view on the time ordered map of obstacle sections.
	 */
	public NavigableMap<LocalDateTime, WorkerUnitObstacle> getNavigableObstacleSegments() {
		return unmodifiableObstacleSegments;
	}
	
	/**
	 * Returns the obstacle section of the given time. The time interval of
	 * returned section will include the given time. If no such obstacle exists
	 * (e.g., before the worker was initialized) then {@code null} is returned.
	 *
	 * @param time
	 * @return the obstacle section or {@code null} if no such section exists.
	 */
	public WorkerUnitObstacle getObstacleSection(LocalDateTime time) {
		Objects.requireNonNull(time, "time");

		Entry<LocalDateTime, WorkerUnitObstacle> entry = obstacleSections.floorEntry(time);

		return entry == null ? null : entry.getValue();
	}

	/**
	 * Adds an obstacle section.
	 *
	 * @param section
	 * @throws NullPointerException if section is null
	 */
	public void addObstacleSection(WorkerUnitObstacle section) {
		Objects.requireNonNull(section, "section");

		putObstacleSection(section);
	}

	/**
	 * Puts an obstacle section into the map of {@link #obstacleSections}.
	 *
	 * @param section
	 */
	private void putObstacleSection(WorkerUnitObstacle section) {
		obstacleSections.put(section.getStartTime(), section);
	}

	/**
	 * Removes an obstacle section.
	 *
	 * @param section
	 * @throws NullPointerException
	 *             if section is null
	 * @throws IllegalArgumentException
	 *             if section is unknown (e.g. not a section of this worker)
	 */
	public void removeObstacleSection(WorkerUnitObstacle section) {
		Objects.requireNonNull(section, "section");

		boolean status = obstacleSections.remove(section.getStartTime(), section);

		if (!status)
			throw new IllegalArgumentException("unknown obstacle section");
	}

	/**
	 * Creates a set of idle slots which represent sections of the worker
	 * while idling during a given time period.
	 *
	 * @param from the beginning of the time period
	 * @param to the ending of the time period
	 * @return the idle slots
	 * @throws NullPointerException if any argument is null
	 * @throws IllegalArgumentException if from is after to
	 */
	public Collection<IdleSlot> idleSlots(LocalDateTime from, LocalDateTime to) {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");

		if (from.compareTo(to) > 0)
			throw new IllegalArgumentException("from is after to");

		Map.Entry<LocalDateTime, WorkerUnitObstacle> firstEntry = obstacleSections.lowerEntry(from);
		Collection<WorkerUnitObstacle> sectionsSubSet =
			obstacleSections.subMap(from, true, to, true).values();

		// first section might not exist
		Stream<WorkerUnitObstacle> first =
			firstEntry == null ? Stream.empty() : Stream.of(firstEntry.getValue());
		Stream<WorkerUnitObstacle> tail = sectionsSubSet.stream();

		return Stream.concat(first, tail)
			// filter 'idle' (non-occupied) sections
			.filter(s -> s instanceof MovingWorkerUnitObstacle
				|| s instanceof IdlingWorkerUnitObstacle)
			// filter non-zero
			.filter(s -> !s.getDuration().isZero())
			// make IdleSlots
			.map(s -> new IdleSlot(
				s.getStartLocation(),
				s.getFinishLocation(),
				s.getStartTime(),
				s.getFinishTime()))
			.collect(toList());
	}

	/**
	 * Calculates a trajectory from all obstacle sections concatenated together.
	 *
	 * @return the merged trajectory.
	 */
	public Trajectory calcTrajectory() {
		return obstacleSections.values().stream()
			.map(DynamicObstacle::getTrajectory)
			.reduce((u, v) -> u.concat(v))
			.orElse(SimpleTrajectory.empty());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return id;
	}

}
