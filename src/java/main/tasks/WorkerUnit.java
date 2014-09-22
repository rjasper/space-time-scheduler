package tasks;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Point;

public class WorkerUnit {
	
	private final double maxSpeed;
	
	private final Point initialLocation;
	
	private final LocalDateTime initialTime;
	
	private TreeMap<LocalDateTime, Task> tasks = new TreeMap<>();
	
	public WorkerUnit(double maxSpeed, Point initialLocation, LocalDateTime initialTime) {
		if (maxSpeed <= 0)
			throw new IllegalArgumentException("maximum speed must be positive");
		
		this.maxSpeed = maxSpeed;
		this.initialLocation = initialLocation;
		this.initialTime = initialTime;
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
	
	private TreeMap<LocalDateTime, Task> getTasks() {
		return tasks;
	}

	public boolean addTask(Task task) {
		// TODO: proper checks (e.g. path planning)
		TreeMap<LocalDateTime, Task> tasks = getTasks();
		LocalDateTime time = task.getStartTime();
		
		tasks.put(time, task);
		
		return true;
	}

	public Collection<IdleSlot> idleSubSet(LocalDateTime from, LocalDateTime to) {
		TreeMap<LocalDateTime, Task> tasks = getTasks();
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
