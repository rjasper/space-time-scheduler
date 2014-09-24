package tasks;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import jts.geom.factories.EnhancedGeometryBuilder;
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

	public boolean addTask(Task task) {
		LocalDateTime time = task.getStartTime();
		
		boolean status = planTrajectoryToTask(task);
		
		if (status)
			_getTasks().put(time, task);
		
		return status;
	}
	
	private boolean planTrajectoryToTask(Task task) {
		EnhancedGeometryBuilder fact = EnhancedGeometryBuilder.getInstance();
		TreeMap<LocalDateTime, Task> tasks = _getTasks();
		Map<Task, Trajectory> trajectories = _getTrajectories();
		
		Entry<LocalDateTime, Task> predEntry = tasks.floorEntry(task.getStartTime());
		Entry<LocalDateTime, Task> succEntry   = tasks.ceilingEntry(task.getFinishTime());
		Task pred = predEntry == null ? null : predEntry.getValue();
		Task succ = succEntry == null ? null : succEntry.getValue();
		
		// TODO implement proper path planning
		
		// trajectory to new task
		
		LocalDateTime startTime, finishTime;
		Point startLocation, finishLocation;
		
		// if there is no predecessor use initial position and time
		if (pred == null) {
			startTime = getInitialTime();
			startLocation = getInitialLocation();
		} else {
			startTime = pred.getFinishTime();
			startLocation = pred.getLocation();
		}
		
		finishTime = task.getStartTime();
		finishLocation = task.getLocation();
		
		Trajectory toTask = new Trajectory(
			fact.lineString(startLocation, finishLocation),
			Arrays.asList(startTime, finishTime));
		
		trajectories.put(task, toTask);
		
		// trajectory to following task
		
		if (succ != null) {
			Trajectory toSucc = new Trajectory(
				fact.lineString(task.getLocation(), succ.getLocation()),
				Arrays.asList(task.getFinishTime(), succ.getStartTime())
			);
			
			trajectories.put(succ, toSucc);
		}
		
		return true;
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
