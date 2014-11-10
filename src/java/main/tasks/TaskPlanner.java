package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import world.DecomposedTrajectory;
import world.DynamicObstacle;
import world.DynamicWorldBuilder;
import world.pathfinder.JavaFixTimePathfinder;
import world.pathfinder.JavaMinimumTimePathfinder;
import world.pathfinder.MinimumTimePathfinder;
import world.pathfinder.FixTimePathfinder;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class TaskPlanner {
	
	private WorkerUnit workerUnit = null;
	
	private Collection<WorkerUnit> workerPool = null;
	
	private Collection<Polygon> staticObstacles = null;
	
	private Point location = null;
	
	private LocalDateTime earliestStartTime = null;
	
	private LocalDateTime latestStartTime = null;
	
	private Duration duration = null;
	
	private Task resultTask = null;
	
	private DecomposedTrajectory resultToTask;
	
	private DecomposedTrajectory resultFromTask;
	
	public boolean isReady() {
		return workerUnit != null
			&& workerPool != null
			&& staticObstacles != null
			&& location != null
			&& earliestStartTime != null
			&& latestStartTime != null
			&& duration != null;
	}
	
	// TODO check setter args

	private WorkerUnit getWorkerUnit() {
		return workerUnit;
	}

	public void setWorkerUnit(WorkerUnit worker) {
		this.workerUnit = worker;
	}

	private Collection<WorkerUnit> getWorkerPool() {
		return workerPool;
	}

	public void setWorkerPool(Collection<WorkerUnit> workerPool) {
		this.workerPool = new ArrayList<>(workerPool);
	}

	private Collection<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	public void setStaticObstacles(Collection<Polygon> staticObstacles) {
		this.staticObstacles = new ArrayList<>(staticObstacles);
	}

	private Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	private LocalDateTime getEarliestStartTime() {
		return earliestStartTime;
	}

	public void setEarliestStartTime(LocalDateTime earliestStartTime) {
		this.earliestStartTime = earliestStartTime;
	}

	private LocalDateTime getLatestStartTime() {
		return latestStartTime;
	}

	public void setLatestStartTime(LocalDateTime latestStartTime) {
		this.latestStartTime = latestStartTime;
	}

	private Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public Task getResultTask() {
		return resultTask;
	}

	private void setResultTask(Task resultTask) {
		this.resultTask = resultTask;
	}

	public DecomposedTrajectory getResultToTask() {
		return resultToTask;
	}

	private void setResultToTask(DecomposedTrajectory resultToTask) {
		this.resultToTask = resultToTask;
	}

	public DecomposedTrajectory getResultFromTask() {
		return resultFromTask;
	}

	private void setResultFromTask(DecomposedTrajectory resultFromTask) {
		this.resultFromTask = resultFromTask;
	}

//	public boolean plan() {
//		if (!isReady())
//			throw new IllegalStateException("not ready yet");
//		
//		WorkerUnit worker = getWorkerUnit();
//		double maxSpeed = worker.getMaxSpeed();
//		Collection<Polygon> staticObstacles = getStaticObstacles();
//		Collection<DynamicObstacle> dynamicObstacles = buildDynamicObstacles();
//		LocalDateTime earliestStartTime = getEarliestStartTime();
//		LocalDateTime latestStartTime = getLatestStartTime();
//		Duration duration = getDuration();
//		Point location = getLocation();
//		
//		MinimumTimePathfinder mtpf = new JavaMinimumTimePathfinder();
//		
//		mtpf.setStaticObstacles(staticObstacles);
//		mtpf.setDynamicObstacles(dynamicObstacles);
//		
//		Task pred = worker.getFloorTask(earliestStartTime);
//		Task succ = worker.getCeilingTask(earliestStartTime);
//		
//		// trajectory to new task
//		
//		LocalDateTime startTime;
//		Point startLocation;
//		
//		// if there is no predecessor use initial position and time
//		if (pred == null) {
//			startTime = worker.getInitialTime();
//			startLocation = worker.getInitialLocation();
//		} else {
//			startTime = pred.getFinishTime();
//			startLocation = pred.getLocation();
//		}
//		
//		mtpf.setStartPoint(startLocation);
//		mtpf.setFinishPoint(location);
//		mtpf.setStartTime(startTime);
//		mtpf.setEarliestFinishTime(earliestStartTime);
//		mtpf.setLatestFinishTime(latestStartTime);
//		mtpf.setBufferDuration(duration);
//		mtpf.setMaxSpeed(maxSpeed);
//		
//		boolean status = mtpf.calculatePath();
//		
//		if (!status)
//			return false;
//		
//		Trajectory toTask = mtpf.getResultTrajectory();
//		
//		LocalDateTime taskStartTime = toTask.getLastTime();
//		LocalDateTime taskFinishTime = taskStartTime.plus(duration);
//		
//		// trajectory to following task
//
//		Trajectory fromTask;
//		if (succ != null) {
//			FixTimePathfinder stpf = new JavaFixTimePathfinder();
//			
//			stpf.setStartPoint(location);
//			stpf.setFinishPoint(succ.getLocation());
//			stpf.setStartTime(taskFinishTime);
//			stpf.setFinishTime(succ.getStartTime());
//			stpf.setMaxSpeed(maxSpeed);
//			
//			status = stpf.calculatePath();
//			
//			if (!status)
//				return false;
//			
//			fromTask = stpf.getResultTrajectory();
//		} else {
//			fromTask = null;
//		}
//		
//		Task task = new Task(location, taskStartTime, taskFinishTime);
//		
//		setResultTask(task);
//		setResultToTask(toTask);
//		setResultFromTask(fromTask);
//		
//		return true;
//	}
	
	public boolean plan() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		WorkerUnit worker = getWorkerUnit();
		double maxSpeed = worker.getMaxSpeed();
		Collection<Polygon> staticObstacles = getStaticObstacles();
		Collection<DynamicObstacle> dynamicObstacles = buildDynamicObstacles();
		LocalDateTime earliestStartTime = getEarliestStartTime();
		LocalDateTime latestStartTime = getLatestStartTime();
		Duration duration = getDuration();
		Point location = getLocation();
		
		MinimumTimePathfinder mtpf = new JavaMinimumTimePathfinder();
		
		mtpf.setStaticObstacles(staticObstacles);
		mtpf.setDynamicObstacles(dynamicObstacles);
		
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
		
		mtpf.setStartPoint(startLocation);
		mtpf.setFinishPoint(location);
		mtpf.setStartTime(startTime);
		mtpf.setEarliestFinishTime(earliestStartTime);
		mtpf.setLatestFinishTime(latestStartTime);
		mtpf.setBufferDuration(duration);
		mtpf.setMaxSpeed(maxSpeed);
		
		boolean status = mtpf.calculatePath();
		
		if (!status)
			return false;
		
		DecomposedTrajectory toTask = mtpf.getResultTrajectory();
		
		LocalDateTime taskStartTime = toTask.getFinishTime();
		LocalDateTime taskFinishTime = taskStartTime.plus(duration);
		
		// trajectory to following task

		DecomposedTrajectory fromTask;
		if (succ != null) {
			FixTimePathfinder stpf = new JavaFixTimePathfinder();
			
			stpf.setStartPoint(location);
			stpf.setFinishPoint(succ.getLocation());
			stpf.setStartTime(taskFinishTime);
			stpf.setFinishTime(succ.getStartTime());
			stpf.setMaxSpeed(maxSpeed);
			
			status = stpf.calculatePath();
			
			if (!status)
				return false;
			
			fromTask = stpf.getResultTrajectory();
		} else {
			fromTask = null;
		}
		
		Task task = new Task(location, taskStartTime, taskFinishTime);
		
		setResultTask(task);
		setResultToTask(toTask);
		setResultFromTask(fromTask);
		
		return true;
	}
	
	private Collection<DynamicObstacle> buildDynamicObstacles() {
		WorkerUnit worker = getWorkerUnit();
		Collection<WorkerUnit> pool = getWorkerPool();
		LocalDateTime latestStartTime = getLatestStartTime();
		Duration duration = getDuration();
		
		DynamicWorldBuilder builder = new DynamicWorldBuilder();
		
		Collection<WorkerUnit> others = new ArrayList<>(pool);
		others.remove(worker);
		
		builder.setWorkers(others);
		builder.setEndTime(latestStartTime.plus(duration));
		
		builder.build();
		
		return builder.getObstacles();
	}
	
}
