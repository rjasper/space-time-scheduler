package tasks;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
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

public class WorkerUnit {

	private final Polygon shape;

	private transient double radius = Double.NaN;

	private final double maxSpeed;

	private final Point initialLocation;

	private final LocalDateTime initialTime;

	private TreeMap<LocalDateTime, Task> tasks = new TreeMap<>();

	private TreeMap<LocalDateTime, WorkerUnitObstacle> obstacleSegments = new TreeMap<>();

	public WorkerUnit(Polygon shape, double maxSpeed, Point initialLocation, LocalDateTime initialTime) {
		if (!Double.isFinite(maxSpeed) || maxSpeed <= 0)
			throw new IllegalArgumentException("maximum speed is not a positive real number");
		// TODO check initialLocation

		this.shape = shape;
		this.maxSpeed = maxSpeed;
		this.initialLocation = initialLocation;
		this.initialTime = initialTime;

		putInitialObstacleSegment();
	}

	private void putInitialObstacleSegment() {
		Point initialLocation = getInitialLocation();
		LocalDateTime initialTime = getInitialTime();

		WorkerUnitObstacle segment = new IdlingWorkerUnitObstacle(this, initialLocation, initialTime);

		putObstacleSegment(segment);
	}

	public Polygon getShape() {
		return shape;
	}

	public double getRadius() {
		if (Double.isNaN(radius))
			radius = calcRadius();

		return radius;
	}

	private double calcRadius() {
		Coordinate[] coords = getShape().getCoordinates();

		double sqRadius = Arrays.stream( coords )
			.mapToDouble(c -> c.x*c.x + c.y*c.y)
			.max()
			.getAsDouble();

		return Math.sqrt(sqRadius);
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public Point getInitialLocation() {
		return initialLocation;
	}

	public LocalDateTime getInitialTime() {
		return initialTime;
	}

	public Map<LocalDateTime, Task> getTasks() {
		return unmodifiableMap(tasks);
	}

	private TreeMap<LocalDateTime, Task> _getTasks() {
		return tasks;
	}

	public void addTask(Task task) {
		_getTasks().put(task.getStartTime(), task);
	}

	public Collection<WorkerUnitObstacle> getObstacleSegments() {
		return unmodifiableCollection(obstacleSegments.values());
	}

	private TreeMap<LocalDateTime, WorkerUnitObstacle> _getObstacleSegments() {
		return obstacleSegments;
	}

	public WorkerUnitObstacle getObstacleSegment(LocalDateTime time) {
		Entry<LocalDateTime, WorkerUnitObstacle> entry = _getObstacleSegments()
			.floorEntry(time);

		return entry == null ? null : entry.getValue();
	}

	public void addObstacleSegment(WorkerUnitObstacle segment) {
		putObstacleSegment(segment);
	}

	private WorkerUnitObstacle putObstacleSegment(WorkerUnitObstacle segment) {
		Map<LocalDateTime, WorkerUnitObstacle> segments = _getObstacleSegments();

		return segments.put(segment.getStartTime(), segment);
	}

	public void removeObstacleSegment(WorkerUnitObstacle segment) {
		boolean status = _getObstacleSegments().remove(segment.getStartTime(), segment);

		if (!status)
			throw new IllegalArgumentException("unknown obstacle segment");
	}

	public Collection<IdleSlot> idleSubSet(LocalDateTime from, LocalDateTime to) {
		TreeMap<LocalDateTime, WorkerUnitObstacle> segments = _getObstacleSegments();
		Map.Entry<LocalDateTime, WorkerUnitObstacle> firstEntry = segments.lowerEntry(from);
		Collection<WorkerUnitObstacle> segmentsSubSet = segments.subMap(from, to).values();

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
