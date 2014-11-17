package world;

import tasks.Task;
import tasks.WorkerUnit;

public class MovingWorkerUnitObstacle extends WorkerUnitObstacle {

	private final Task goal;
	
	public MovingWorkerUnitObstacle(WorkerUnit worker, DecomposedTrajectory trajectory, Task goal) {
		super(worker, trajectory);
		
		this.goal = goal;
	}

	@Override
	public DecomposedTrajectory getTrajectory() {
		return (DecomposedTrajectory) super.getTrajectory();
	}
	
	public Task getGoal() {
		return goal;
	}

}
