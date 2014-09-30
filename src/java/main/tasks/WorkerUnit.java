package tasks;

import static util.Comparables.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import jts.geom.factories.EnhancedGeometryBuilder;
import world.Pathfinder;
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

//	public boolean addTask(Task task) {
//		LocalDateTime time = task.getStartTime();
//		
//		boolean status = planTrajectoryToTask(task);
//		
//		if (status)
//			_getTasks().put(time, task);
//		
//		return status;
//	}
	
	public void addTask(Task task, Trajectory toTask, Trajectory fromTask) {
		Map<Task, Trajectory> trajectories = _getTrajectories();
		
		LocalDateTime startTime = task.getStartTime();
		Task pred = getFloorTask(startTime);
		Task succ = getCeilingTask(startTime);
		
		_getTasks().put(startTime, task);
		trajectories.put(pred, toTask);
		
		if (succ != null)
			trajectories.put(succ, fromTask);
	}
	
//	private boolean planTrajectoryToTask(Point location, LocalDateTime earliest, LocalDateTime latest, Duration duration) {
//		EnhancedGeometryBuilder fact = EnhancedGeometryBuilder.getInstance();
//		TreeMap<LocalDateTime, Task> tasks = _getTasks();
//		Map<Task, Trajectory> trajectories = _getTrajectories();
//		double maxSpeed = getMaxSpeed();
//		Pathfinder pf = Pathfinder.getInstance();
//		
////		Duration duration = task.getDuration();
////		Point location = task.getLocation();
////		Entry<LocalDateTime, Task> predEntry = tasks.floorEntry(task.getStartTime());
////		Entry<LocalDateTime, Task> succEntry   = tasks.ceilingEntry(task.getFinishTime());
//		Entry<LocalDateTime, Task> predEntry = tasks.floorEntry(earliest);
//		Entry<LocalDateTime, Task> succEntry = tasks.ceilingEntry(earliest);
//		Task pred = predEntry == null ? null : predEntry.getValue();
//		Task succ = succEntry == null ? null : succEntry.getValue();
//		
//		// TODO implement proper path planning
//		// TODO reminder: when building dynamic world, end time must include work time
//		
//		// trajectory to new task
//		
//		LocalDateTime startTime, finishTime;
//		Point startLocation, finishLocation;
//		
//		// if there is no predecessor use initial position and time
//		if (pred == null) {
//			startTime = getInitialTime();
//			startLocation = getInitialLocation();
//		} else {
//			startTime = pred.getFinishTime();
//			startLocation = pred.getLocation();
//		}
//		
////		finishTime = task.getStartTime();
////		finishLocation = task.getLocation();
//		
//		// TODO calculate latest finish time
//		LocalDateTime latestFinishTime = min(latest.plus(duration), succ.getStartTime());
//		
//		pf.useMinimumFinishTime();
//		
//		pf.setStartingPoint(startLocation);
//		pf.setFinishPoint(location);
//		pf.setStartingTime(startTime);
//		pf.setEarliestFinishTime(earliest);
//		pf.setLatestFinishTime(latestFinishTime);
//		pf.setSpareTime(duration);
//		pf.setMaxSpeed(maxSpeed);
//		
//		pf.calculatePath();
//		
//		if (!pf.isPathFound())
//			return false;
//		
//		Trajectory toTask = pf.getTrajectory();
//		
////		Trajectory toTask = new Trajectory(
////			fact.lineString(startLocation, finishLocation),
////			Arrays.asList(startTime, finishTime));
//		
//		// TODO extract time
//		LocalDateTime taskStartTime = toTask.getLastTime();
//		
//		// trajectory to following task
//		
//		if (succ != null) {
////			Trajectory toSucc = new Trajectory(
////				fact.lineString(task.getLocation(), succ.getLocation()),
////				Arrays.asList(task.getFinishTime(), succ.getStartTime())
////			);
//			
//			pf.useSpecifiedFinishTime();
//			
//			pf.setStartingPoint(location);
//			pf.setFinishPoint(succ.getLocation());
//			pf.setStartingTime(taskStartTime.plus(duration));
//			pf.setLatestFinishTime(succ.getFinishTime());
//			pf.setMaxSpeed(maxSpeed); // here actually redundant
//			
//			pf.calculatePath();
//			
//			if (!pf.isPathFound())
//				return false;
//			
//			Trajectory toSucc = pf.getTrajectory();
//			
//			trajectories.put(succ, toSucc);
//		}
//		
//		// TODO don't put yet
//		trajectories.put(task, toTask);
//		
//		return true;
//	}

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
