package tasks;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import world.IdlingWorkerUnitObstacle;
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
			throw new IllegalArgumentException("maximum speed must be a positive real number");

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
		TreeMap<LocalDateTime, Task> tasks = _getTasks();
		Map.Entry<LocalDateTime, Task> first = tasks.lowerEntry(from);
		Map.Entry<LocalDateTime, Task> last = tasks.higherEntry(to);
		NavigableMap<LocalDateTime, Task> taskSubSet = tasks.subMap(from, true,
			to, true);

		Point startLocation, finishLocation;
		LocalDateTime startTime, finishTime;

		if (first == null) {
			startLocation = getInitialLocation();
			startTime = getInitialTime();
		} else {
			Task firstTask = first.getValue();

			startLocation = firstTask.getLocation();
			startTime = firstTask.getFinishTime();
		}

		Collection<IdleSlot> slots = new LinkedList<>();

		for (Task t : taskSubSet.values()) {
			finishLocation = t.getLocation();
			finishTime = t.getStartTime();

			// don't add idle slots without duration
			if (startTime.compareTo(finishTime) < 0)
				slots.add(new IdleSlot(startLocation, finishLocation,
					startTime, finishTime));

			startLocation = t.getLocation();
			startTime = t.getFinishTime();
		}

		if (last == null) {
			finishLocation = null;
			finishTime = null;
		} else {
			Task lastTask = last.getValue();

			finishLocation = lastTask.getLocation();
			finishTime = lastTask.getStartTime();
		}

		// don't add idle slots without duration
		if (last == null || startTime.compareTo(finishTime) < 0)
			slots.add(new IdleSlot(startLocation, finishLocation, startTime,
				finishTime));

		return slots;
	}

}
