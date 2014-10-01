package tasks;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import world.Trajectory;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class WorkerUnit {
	
	private final Polygon shape;
	
	private final double maxSpeed;
	
	private final Point initialLocation;
	
	private final LocalDateTime initialTime;
	
	private TreeMap<LocalDateTime, Task> tasks = new TreeMap<>();
	
	private Map<Task, Trajectory> trajectories = new HashMap<>();
	
	public WorkerUnit(Polygon shape, double maxSpeed, Point initialLocation, LocalDateTime initialTime) {
		if (maxSpeed <= 0)
			throw new IllegalArgumentException("maximum speed must be positive");
		
		this.shape = shape;
		this.maxSpeed = maxSpeed;
		this.initialLocation = initialLocation;
		this.initialTime = initialTime;
	}
	
	public Polygon getShape() {
		return shape;
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
		return Collections.unmodifiableMap(tasks);
	}
	
	public Task getFloorTask(LocalDateTime time) {
		Entry<LocalDateTime, Task> entry = _getTasks().floorEntry(time);
		
		return entry == null ? null : entry.getValue();
	}
	
	public Task getCeilingTask(LocalDateTime time) {
		Entry<LocalDateTime, Task> entry = _getTasks().ceilingEntry(time);
		
		return entry == null ? null : entry.getValue();
	}
	
	public Task getLastTask() {
		Entry<LocalDateTime, Task> entry = _getTasks().lastEntry();
		
		return entry == null ? null : entry.getValue();
	}
	
	public Map<Task, Trajectory> getTrajectories() {
		return Collections.unmodifiableMap(trajectories);
	}
	
	private TreeMap<LocalDateTime, Task> _getTasks() {
		return tasks;
	}

	private Map<Task, Trajectory> _getTrajectories() {
		return trajectories;
	}

	public void addTask(Task task, Trajectory toTask, Trajectory fromTask) {
		// TODO check validity of trajectories
		
		Map<Task, Trajectory> trajectories = _getTrajectories();
		
		LocalDateTime startTime = task.getStartTime();
		Task succ = getCeilingTask(startTime);
		
		_getTasks().put(startTime, task);
		trajectories.put(task, toTask);
		
		if ((succ == null) != (fromTask == null))
			throw new IllegalStateException("fromTask trajectory is invalid");
		
		if (succ != null)
			trajectories.put(succ, fromTask);
	}
	
	public Collection<IdleSlot> idleSubSet(LocalDateTime from, LocalDateTime to) {
		TreeMap<LocalDateTime, Task> tasks = _getTasks();
		Map.Entry<LocalDateTime, Task> first = tasks.lowerEntry(from);
		Map.Entry<LocalDateTime, Task> last = tasks.higherEntry(to);
		NavigableMap<LocalDateTime, Task> taskSubSet = tasks.subMap(from, true, to, true);
		
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
				slots.add(new IdleSlot(startLocation, finishLocation, startTime, finishTime));
			
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
			slots.add(new IdleSlot(startLocation, finishLocation, startTime, finishTime));
		
		return slots;
	}

}
