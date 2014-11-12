package world;

import tasks.Task;
import tasks.WorkerUnit;

public class MovingWorkerUnitObstacle extends WorkerUnitObstacle {

	private final Task goal;
	
	public MovingWorkerUnitObstacle(WorkerUnit worker, Trajectory trajectory, Task goal) {
		super(worker, trajectory);
		
		this.goal = goal;
	}

	public Task getGoal() {
		return goal;
	}

}
