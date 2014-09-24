package world;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jts.geom.factories.EnhancedGeometryBuilder;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import tasks.Task;
import tasks.WorkerUnit;

public class DynamicWorldBuilder {
	
	private List<WorkerUnit> workers = Collections.emptyList();
	
	private LocalDateTime endTime;
	
	private List<DynamicObstacle> obstacles;
	
	public boolean isReady() {
		return endTime != null;
	}
	
	public boolean isDone() {
		return obstacles != null;
	}
	
	public List<WorkerUnit> getWorkers() {
		return workers;
	}

	public void setWorkers(Collection<WorkerUnit> workers) {
		if (workers == null)
			throw new NullPointerException("workers cannot be null");
		
		this.workers = new ArrayList<>(workers);
	}

	public List<DynamicObstacle> getObstacles() {
		return obstacles;
	}

	private void setObstacles(List<DynamicObstacle> obstacles) {
		this.obstacles = obstacles;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		if (endTime == null)
			throw new NullPointerException("endTime cannot be null");
		
		this.endTime = endTime;
	}
	
	public void build() {
		if (!isReady())
			throw new IllegalStateException("builder not ready");
		
		List<WorkerUnit> workers = getWorkers();
		List<DynamicObstacle> obstacles = new LinkedList<>();
		
		for (WorkerUnit w : workers) {
			Polygon shape = w.getShape();
			Map<Task, Trajectory> trajectories = w.getTrajectories();
			
			for (Task t : w.getTasks().values()) {
				Trajectory toTask = trajectories.get(t);
				Trajectory atTask = createStationaryTrajectoryFromTask(t);
				
				obstacles.add( new DynamicObstacle(shape, toTask) );
				obstacles.add( new DynamicObstacle(shape, atTask) );
			}
			
			Trajectory toEnd = createStationaryTrajectoryToEndTime(w);
			
			if (toEnd != null)
				obstacles.add( new DynamicObstacle(shape, toEnd));
		}
		
		setObstacles(obstacles);
	}
	
	private Trajectory createStationaryTrajectoryFromTask(Task task) {
		EnhancedGeometryBuilder fact = EnhancedGeometryBuilder.getInstance();
		
		Point location = task.getLocation();
		
		return new Trajectory(
			fact.lineString(location, location),
			Arrays.asList(task.getStartTime(), task.getFinishTime())
		);
	}
	
	private Trajectory createStationaryTrajectoryToEndTime(WorkerUnit worker) {
		Task lastTask = worker.getLastTask();
		LocalDateTime endTime = getEndTime();
		
		Point lastLocation;
		LocalDateTime lastTime;
		if (lastTask == null) {
			lastLocation = worker.getInitialLocation();
			lastTime = worker.getInitialTime();
		} else {
			lastLocation = lastTask.getLocation();
			lastTime = lastTask.getFinishTime();
		}
		
		// if the final position of the worker is after the end time
		// then no final stationary trajectory is needed (or possible)
		if (lastTime.compareTo(getEndTime()) >= 0)
			return null;

		EnhancedGeometryBuilder fact = EnhancedGeometryBuilder.getInstance();
		
		return new Trajectory(
			fact.lineString(lastLocation, lastLocation),
			Arrays.asList(lastTime, endTime)
		);
	}

}
