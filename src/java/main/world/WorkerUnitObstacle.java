package world;

import static java.util.Collections.*;

import java.util.HashSet;
import java.util.Set;

import tasks.WorkerUnit;

public abstract class WorkerUnitObstacle extends DynamicObstacle {

	private final WorkerUnit workerUnit;
	
	private final Set<WorkerUnitObstacle> evasions = new HashSet<>();
	
	public WorkerUnitObstacle(WorkerUnit worker, Trajectory trajectory) {
		super(worker.getShape(), trajectory);
		
		this.workerUnit = worker;
	}

	public WorkerUnit getWorkerUnit() {
		return workerUnit;
	}

	public Set<WorkerUnitObstacle> getEvasions() {
		return unmodifiableSet(evasions);
	}
	
	public void addEvasion(WorkerUnitObstacle evasion) {
		boolean status = evasions.add(evasion);
		
		if (!status)
			throw new IllegalArgumentException("evasion already present");
	}
	
	public void removeEvasion(WorkerUnitObstacle evasion) {
		boolean status = evasions.remove(evasion);
		
		if (!status)
			throw new IllegalArgumentException("unknown evasion");
	}

}
