package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import world.DynamicObstacle;
import world.DynamicWorldBuilder;
import world.Trajectory;
import world.pathfinder.MatlabMinimumTimePathfinder;
import world.pathfinder.MatlabSpecificTimePathfinder;
import world.pathfinder.MinimumTimePathfinder;
import world.pathfinder.SpecificTimePathfinder;

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
	
	private Task resultTask = null;
	
	private Trajectory resultToTask;
	
	private Trajectory resultFromTask;
	
	public boolean isReady() {
		return worker != null
			&& workerPool != null
			&& staticObstacles != null
			&& location != null
			&& earliestStartTime != null
			&& latestStartTime != null
			&& duration != null;
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

	public Task getResultTask() {
		return resultTask;
	}

	private void setResultTask(Task resultTask) {
		this.resultTask = resultTask;
	}

	public Trajectory getResultToTask() {
		return resultToTask;
	}

	private void setResultToTask(Trajectory resultToTask) {
		this.resultToTask = resultToTask;
	}

	public Trajectory getResultFromTask() {
		return resultFromTask;
	}

	private void setResultFromTask(Trajectory resultFromTask) {
		this.resultFromTask = resultFromTask;
	}

	public boolean plan() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");
		
		double maxSpeed = worker.getMaxSpeed();
//		MatlabPathfinder pf = new MatlabPathfinder();
		MinimumTimePathfinder mtpf = new MatlabMinimumTimePathfinder();
		
		Collection<DynamicObstacle> dynamicObstacles = buildDynamicObstacles();
		
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
		
//		pf.useMinimumFinishTime();
		
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
		
		Trajectory toTask = mtpf.getResultTrajectory();
		
		LocalDateTime taskStartTime = toTask.getLastTime();
		LocalDateTime taskFinishTime = taskStartTime.plus(duration);
		
		// trajectory to following task

		Trajectory fromTask;
		if (succ != null) {
			SpecificTimePathfinder stpf = new MatlabSpecificTimePathfinder();
			
//			stpf.useSpecifiedFinishTime();
			
			stpf.setStartPoint(location);
			stpf.setFinishPoint(succ.getLocation());
			stpf.setStartTime(taskFinishTime);
			stpf.setFinishTime(succ.getStartTime());
			stpf.setMaxSpeed(maxSpeed); // here actually redundant
			
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
		DynamicWorldBuilder builder = new DynamicWorldBuilder();
		
		Collection<WorkerUnit> otherWorkers = new ArrayList<>(workerPool);
		otherWorkers.remove(worker);
		
		builder.setWorkers(otherWorkers);
		builder.setEndTime(latestStartTime.plus(duration));
		
		builder.build();
		
		return builder.getObstacles();
	}
	
}
