package world;

import tasks.Task;
import tasks.WorkerUnit;

public class OccupiedWorkerUnitObstacle extends WorkerUnitObstacle {

	private final Task occupation;
	
	public OccupiedWorkerUnitObstacle(WorkerUnit worker, Trajectory trajectory, Task occupation) {
		super(worker, trajectory);
		
		this.occupation = occupation;
	}

	public Task getOccupation() {
		return occupation;
	}

}
