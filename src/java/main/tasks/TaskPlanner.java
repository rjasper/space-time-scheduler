package tasks;

import static util.Comparables.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import world.DynamicObstacle;
import world.DynamicWorldBuilder;
import world.Pathfinder;
import world.Trajectory;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class TaskPlanner {
	
	private WorkerUnit worker = null;
	
	private Collection<WorkerUnit> workerPool = null;
	
	private Collection<Polygon> staticObstacles = null;
	
	private Point location = null;
	
	private LocalDateTime earliestStartTime = null;
	
	private LocalDateTime latestStartTime = null;
	
	private Duration duration = null;
	
	private Task task = null;
	
	private Trajectory toTask;
	
	private Trajectory fromTask;
	
	public boolean isReady() {
		// TODO implement
		return true;
	}
	
	// TODO check setter args

	public WorkerUnit getWorker() {
		return worker;
	}

	public void setWorker(WorkerUnit worker) {
		this.worker = worker;
	}

	public Collection<WorkerUnit> getWorkerPool() {
		return workerPool;
	}

	public void setWorkerPool(Collection<WorkerUnit> workerPool) {
		this.workerPool = new ArrayList<>(workerPool);
	}

	public Collection<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	public void setStaticObstacles(Collection<Polygon> staticObstacles) {
		this.staticObstacles = new ArrayList<>(staticObstacles);
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	public LocalDateTime getEarliestStartTime() {
		return earliestStartTime;
	}

	public void setEarliestStartTime(LocalDateTime earliestStartTime) {
		this.earliestStartTime = earliestStartTime;
	}

	public LocalDateTime getLatestStartTime() {
		return latestStartTime;
	}

	public void setLatestStartTime(LocalDateTime latestStartTime) {
		this.latestStartTime = latestStartTime;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public Task getTask() {
		return task;
	}

	private void setTask(Task task) {
		this.task = task;
	}

	public Trajectory getToTask() {
		return toTask;
	}

	private void setToTask(Trajectory toTask) {
		this.toTask = toTask;
	}

	public Trajectory getFromTask() {
		return fromTask;
	}

	private void setFromTask(Trajectory fromTask) {
		this.fromTask = fromTask;
	}

	public boolean plan() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		double maxSpeed = worker.getMaxSpeed();
		Pathfinder pf = Pathfinder.getInstance();
		
		Collection<DynamicObstacle> dynamicObstacles = buildDynamicObstacles();
		
		pf.addAllStaticObstacles(staticObstacles);
		pf.addAllDynamicObstacles(dynamicObstacles);
		
		Task pred = worker.getFloorTask(earliestStartTime);
		Task succ = worker.getCeilingTask(earliestStartTime);
		
		// trajectory to new task
		
		LocalDateTime startTime;
		Point startLocation;
		
		// if there is no predecessor use initial position and time
		if (pred == null) {
			startTime = worker.getInitialTime();
			startLocation = worker.getInitialLocation();
		} else {
			startTime = pred.getFinishTime();
			startLocation = pred.getLocation();
		}
		
		LocalDateTime earliestFinishTime = earliestStartTime.plus(duration);
		LocalDateTime latestFinishTime = latestStartTime.plus(duration);
		
		if (succ != null)
			latestFinishTime = min(latestFinishTime, succ.getStartTime());
		
		pf.useMinimumFinishTime();
		
		pf.setStartingPoint(startLocation);
		pf.setFinishPoint(location);
		pf.setStartingTime(startTime);
		pf.setEarliestFinishTime(earliestFinishTime);
		pf.setLatestFinishTime(latestFinishTime);
		pf.setSpareTime(duration);
		pf.setMaxSpeed(maxSpeed);
		
		pf.calculatePath();
		
		if (!pf.isPathFound())
			return false;
		
		Trajectory toTask = pf.getTrajectory();
		
		LocalDateTime taskStartTime = toTask.getLastTime();
		LocalDateTime taskFinishTime = taskStartTime.plus(duration);
		
		// trajectory to following task

		Trajectory fromTask;
		if (succ != null) {
			pf.useSpecifiedFinishTime();
			
			pf.setStartingPoint(location);
			pf.setFinishPoint(succ.getLocation());
			pf.setStartingTime(taskFinishTime);
			pf.setFinishTime(succ.getStartTime());
			pf.setMaxSpeed(maxSpeed); // here actually redundant
			
			pf.calculatePath();
			
			if (!pf.isPathFound())
				return false;
			
			fromTask = pf.getTrajectory();
		} else {
			fromTask = null;
		}
		
		Task task = new Task(location, taskStartTime, taskFinishTime);
		
		setTask(task);
		setToTask(toTask);
		setFromTask(fromTask);
		
		return true;
	}
	
	private Collection<DynamicObstacle> buildDynamicObstacles() {
		DynamicWorldBuilder builder = new DynamicWorldBuilder();
		
		Collection<WorkerUnit> otherWorkers = new ArrayList<>(workerPool);
		otherWorkers.remove(worker);
		
		builder.setWorkers(otherWorkers);
		builder.setEndTime(latestStartTime.plus(duration));
		
		builder.build();
		
		return builder.getObstacles();
	}
	
}
