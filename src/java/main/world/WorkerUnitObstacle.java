package world;

import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

import tasks.WorkerUnit;

import com.vividsolutions.jts.geom.Polygon;

public abstract class WorkerUnitObstacle extends DynamicObstacle {

	private final WorkerUnit workerUnit;

	private final Set<MovingWorkerUnitObstacle> evasions = new HashSet<>();

	public WorkerUnitObstacle(WorkerUnit worker, Trajectory trajectory) {
		this(worker, worker.getShape(), trajectory);
	}

	private WorkerUnitObstacle(WorkerUnit worker, Polygon shape, Trajectory trajectory) {
		super(shape, trajectory);

		this.workerUnit = worker;
	}

	public WorkerUnit getWorkerUnit() {
		return workerUnit;
	}

	public Set<MovingWorkerUnitObstacle> getEvasions() {
		return unmodifiableSet(evasions);
	}

	public void addEvasion(MovingWorkerUnitObstacle evasion) {
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
