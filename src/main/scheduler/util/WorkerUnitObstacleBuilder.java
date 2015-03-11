package scheduler.util;

import static jts.geom.immutable.ImmutableGeometries.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import jts.geom.immutable.ImmutablePolygon;
import scheduler.Schedule;
import scheduler.ScheduleAlternative;
import scheduler.WorkerUnit;
import world.DynamicObstacle;
import world.Trajectory;

public class WorkerUnitObstacleBuilder {
	
	private WorkerUnit worker;
	
	private LocalDateTime startTime;
	
	private LocalDateTime finishTime;
	
	private Schedule schedule = null;
	
	private ScheduleAlternative alternative = null;

	private Map<WorkerUnit, ImmutablePolygon> shapeLookUp;

	public void setWorker(WorkerUnit worker) {
		this.worker = Objects.requireNonNull(worker, "worker");
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = Objects.requireNonNull(startTime, "startTime");
	}

	public void setFinishTime(LocalDateTime finishTime) {
		this.finishTime = Objects.requireNonNull(finishTime, "finishTime");
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = Objects.requireNonNull(schedule, "schedule");
	}

	public void setAlternative(ScheduleAlternative alternative) {
		this.alternative = Objects.requireNonNull(alternative, "alternative");
	}

	public Collection<DynamicObstacle> build() {
		checkParameters();
		init();
		
		Collection<DynamicObstacle> workerObstacles = new LinkedList<>();
		
		LocalDateTime from = startTime;
		LocalDateTime to = finishTime;
		
		// TODO make code fancier (a little repetitive right now)
	
		// original trajectories
		for (WorkerUnit w : schedule.getWorkers()) {
			if (w == worker)
				continue;
			
			for (Trajectory t : w.getTrajectories(from, to))
				workerObstacles.add(makeWorkerObstacle(w, t));
		}
		
		// alternative trajectories added to schedule
		for (ScheduleAlternative a : schedule.getAlternatives()) {
			for (WorkerUnit w : a.getWorkers()) {
				if (w == worker)
					continue;
				
				for (Trajectory t : a.getTrajectoryUpdates(w))
					workerObstacles.add(makeWorkerObstacle(w, t));
			}
		}
	
		// alternative trajectories of current alternative
		for (WorkerUnit w : alternative.getWorkers()) {
			if (w == worker)
				continue;
			
			for (Trajectory t : alternative.getTrajectoryUpdates(w))
				workerObstacles.add(makeWorkerObstacle(w, t));
		}
		
		cleanUp();
		
		return workerObstacles;
	}

	private void checkParameters() {
		Objects.requireNonNull(worker, "worker");
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(finishTime, "finishTime");
		Objects.requireNonNull(schedule, "schedule");
		Objects.requireNonNull(alternative, "alternative");
		
		if (!startTime.isBefore(finishTime))
			throw new IllegalStateException("startTime is not before finishTime");
	}

	private void init() {
		shapeLookUp = new HashMap<>();
	}
	
	private void cleanUp() {
		shapeLookUp = null;
	}

	private DynamicObstacle makeWorkerObstacle(WorkerUnit worker, Trajectory trajectory) {
		double radius = this.worker.getRadius();
		
		ImmutablePolygon shape = shapeLookUp.computeIfAbsent(worker, w ->
			(ImmutablePolygon) immutable(w.getShape().buffer(radius)));
		
		return new DynamicObstacle(shape, trajectory);
	}
	
}
