package tasks;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import util.NameProvider;
import world.DynamicObstacle;
import world.IdlingWorkerUnitObstacle;
import world.MovingWorkerUnitObstacle;
import world.Trajectory;
import world.WorkerUnitObstacle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
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
 * {@link #addObstacleSegment(WorkerUnitObstacle)} or
 * {@link #removeObstacleSegment(WorkerUnitObstacle)} which don't check for
 * overall consistency.</p>
 *
 * @author Rico Jasper
 */
public class WorkerUnit {

	/**
	 * The physical shape of this worker.
	 */
	private final Polygon shape;

	/**
	 * The cached radius of the shape.
	 */
	private transient double radius = Double.NaN;

	/**
	 * The maximum velocity of this worker.
	 */
	private final double maxSpeed;

	/**
	 * The initial location of the worker where it begins to 'exist'.
	 */
	private final Point initialLocation;

	/**
	 * The initial time of the worker when it begins to 'exist'.
	 */
	private final LocalDateTime initialTime;

	/**
	 * All tasks which were assigned to this worker.
	 */
	private TreeMap<LocalDateTime, Task> tasks = new TreeMap<>();

	/**
	 * All obstacle segments of this worker.
	 */
	private TreeMap<LocalDateTime, WorkerUnitObstacle> obstacleSegments = new TreeMap<>();

	/**
	 * Constructs a worker defining its shape, maximum velocity, initial
	 * location and initial time.
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
	public WorkerUnit(Polygon shape, double maxSpeed, Point initialLocation, LocalDateTime initialTime) {
		Objects.requireNonNull(shape, "shape");
		Objects.requireNonNull(initialLocation, "initialLocation");
		Objects.requireNonNull(initialTime, "initialTime");

		if (shape.isEmpty() || !shape.isSimple() || !shape.isValid())
			throw new IllegalArgumentException("illegal shape");
		if (initialLocation.isEmpty() || !initialLocation.isValid())
			throw new IllegalArgumentException("illegal initialLocation");
		if (!Double.isFinite(maxSpeed) || maxSpeed <= 0)
			throw new IllegalArgumentException("maximum speed is not a positive finite number");

		this.shape = shape;
		this.maxSpeed = maxSpeed;
		this.initialLocation = initialLocation;
		this.initialTime = initialTime;

		putInitialObstacleSegment();
	}

	/**
	 * Initializes the {@link #obstacleSegments} with an
	 * {@link IdlingWorkerUnitObstacle} at its initial location and initial
	 * time.
	 */
	private void putInitialObstacleSegment() {
		Point initialLocation = getInitialLocation();
		LocalDateTime initialTime = getInitialTime();

		WorkerUnitObstacle segment = new IdlingWorkerUnitObstacle(this, initialLocation, initialTime);

		putObstacleSegment(segment);
	}

	/**
	 * @return the physical shape of this worker.
	 */
	public Polygon getShape() {
		return shape;
	}

	/**
	 * Calculates and caches the radius of this worker.
	 *
	 * @return the radius of this worker's shape.
	 */
	public double getRadius() {
		// TODO caching might not be reasonable
		//      (is essentially always calculated)

		if (Double.isNaN(radius))
			radius = calcRadius();

		return radius;
	}

	/**
	 * Calculates the radius.
	 *
	 * @return the radius.
	 */
	private double calcRadius() {
		Coordinate[] coords = getShape().getCoordinates();

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
	public Point getInitialLocation() {
		return initialLocation;
	}

	/**
	 * @return the initial time of the worker when it begins to 'exist'.
	 */
	public LocalDateTime getInitialTime() {
		return initialTime;
	}

	/**
	 * @return an unmodifiable view on the time ordered map of tasks assigned to
	 * this worker.
	 */
	public Map<LocalDateTime, Task> getTasks() {
		// TODO might be better to store
		return unmodifiableMap(tasks);
	}

	/**
	 * Assigns a new task to this worker.
	 *
	 * @param task
	 * @throws NullPointerException if task is null
	 */
	public void addTask(Task task) {
		Objects.requireNonNull(task, "task");

		tasks.put(task.getStartTime(), task);
	}

	/**
	 * @return an unmodifiable view on the obstacle segments of this worker.
	 */
	public Collection<WorkerUnitObstacle> getObstacleSegments() {
		return unmodifiableCollection(obstacleSegments.values());
	}

	/**
	 * @return the actual obstacle segments.
	 */
	private TreeMap<LocalDateTime, WorkerUnitObstacle> _getObstacleSegments() {
		return obstacleSegments;
	}

	/**
	 * Returns the obstacle segment of the given time. The time interval of
	 * returned segment will include the given time. If no such obstacle exists
	 * (e.g., before the worker was initialized) then {@code null} is returned.
	 *
	 * @param time
	 * @return the obstacle segment or {@code null} if no such segment exists.
	 */
	public WorkerUnitObstacle getObstacleSegment(LocalDateTime time) {
		Objects.requireNonNull(time, "time");

		Entry<LocalDateTime, WorkerUnitObstacle> entry = _getObstacleSegments()
			.floorEntry(time);

		return entry == null ? null : entry.getValue();
	}

	/**
	 * Adds an obstacle segment.
	 *
	 * @param segment
	 * @throws NullPointerException if segment is null
	 */
	public void addObstacleSegment(WorkerUnitObstacle segment) {
		Objects.requireNonNull(segment, "segment");

		putObstacleSegment(segment);
	}

	/**
	 * Puts an obstacle segment into the map of {@link #obstacleSegments}.
	 *
	 * @param segment
	 */
	private void putObstacleSegment(WorkerUnitObstacle segment) {
		Map<LocalDateTime, WorkerUnitObstacle> segments = _getObstacleSegments();

		segments.put(segment.getStartTime(), segment);
	}

	/**
	 * Removes an obstacle segment.
	 *
	 * @param segment
	 * @throws NullPointerException
	 *             if segment is null
	 * @throws IllegalArgumentException
	 *             if segment is unknown (e.g. not a segment of this worker)
	 */
	public void removeObstacleSegment(WorkerUnitObstacle segment) {
		Objects.requireNonNull(segment, "segment");

		boolean status = _getObstacleSegments().remove(segment.getStartTime(), segment);

		if (!status)
			throw new IllegalArgumentException("unknown obstacle segment");
	}

	/**
	 * Creates a set of idle slots which represent segments of the worker
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

		TreeMap<LocalDateTime, WorkerUnitObstacle> segments = _getObstacleSegments();
		Map.Entry<LocalDateTime, WorkerUnitObstacle> firstEntry = segments.lowerEntry(from);
		Collection<WorkerUnitObstacle> segmentsSubSet = segments.subMap(from, to).values();

		// first segment might not exist
		Stream<WorkerUnitObstacle> first =
			firstEntry == null ? Stream.empty() : Stream.of(firstEntry.getValue());
		Stream<WorkerUnitObstacle> tail = segmentsSubSet.stream();

		return Stream.concat(first, tail)
			// filter 'idle' (non-occupied) segments
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
	 * Calculates a trajectory from all obstacle segments merged together.
	 *
	 * @return the merged trajectory
	 */
	public Trajectory calcMergedTrajectory() {
		return _getObstacleSegments().values().stream()
			.filter(o -> !(o instanceof IdlingWorkerUnitObstacle))
			.map(DynamicObstacle::getTrajectory)
			.reduce((u, v) -> u.merge(v))
			.get();
	}

	@Override
	public String toString() {
		return NameProvider.nameFor(this);
	}

}