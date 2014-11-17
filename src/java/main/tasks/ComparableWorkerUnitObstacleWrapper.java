package tasks;

import world.IdlingWorkerUnitObstacle;
import world.MovingWorkerUnitObstacle;
import world.OccupiedWorkerUnitObstacle;
import world.WorkerUnitObstacle;

public class ComparableWorkerUnitObstacleWrapper {
	
	private final WorkerUnitObstacle workerUnitObstacle;

	public ComparableWorkerUnitObstacleWrapper(
		WorkerUnitObstacle workerUnitObstacle)
	{
		this.workerUnitObstacle = workerUnitObstacle;
	}
	
	@Override
	public int hashCode() {
		if (workerUnitObstacle instanceof OccupiedWorkerUnitObstacle)
			return hashCodeFor((OccupiedWorkerUnitObstacle) workerUnitObstacle);
		else if (workerUnitObstacle instanceof MovingWorkerUnitObstacle)
			return hashCodeFor((MovingWorkerUnitObstacle) workerUnitObstacle);
		else if (workerUnitObstacle instanceof IdlingWorkerUnitObstacle)
			return hashCodeFor((IdlingWorkerUnitObstacle) workerUnitObstacle);
		else
			throw new IllegalArgumentException("unknown WorkerUnitObstacle type");
	}
	
	private static int hashCodeFor(OccupiedWorkerUnitObstacle obstacle) {
		int prime = 31;
		int result = 1;
		
		result = prime * result + obstacle.getClass().hashCode();
		result = prime * result + obstacle.getOccupation().hashCode();
		
		return result;
	}
	
	private static int hashCodeFor(MovingWorkerUnitObstacle obstacle) {
		int prime = 31;
		int result = 1;
		
		result = prime * result + obstacle.getClass().hashCode();
		result = prime * result + obstacle.getGoal().hashCode();
		
		return result;
	}
	
	private static int hashCodeFor(IdlingWorkerUnitObstacle obstacle) {
		int prime = 31;
		int result = 1;
		
		result = prime * result + obstacle.getClass().hashCode();
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComparableWorkerUnitObstacleWrapper other = (ComparableWorkerUnitObstacleWrapper) obj;
		if (workerUnitObstacle == null) {
			if (other.workerUnitObstacle != null)
				return false;
		} else if (!equal(workerUnitObstacle, other.workerUnitObstacle))
			return false;
		return true;
	}

	private static boolean equal(WorkerUnitObstacle lhs, WorkerUnitObstacle rhs) {
		if (!lhs.getClass().equals(rhs.getClass()))
			return false;
		if (!lhs.getWorkerUnit().equals(rhs.getWorkerUnit()))
			return false;
		
		if (lhs.getClass().equals(OccupiedWorkerUnitObstacle.class))
			return equal((OccupiedWorkerUnitObstacle) lhs, (OccupiedWorkerUnitObstacle) rhs);
		if (lhs.getClass().equals(MovingWorkerUnitObstacle.class))
			return equal((MovingWorkerUnitObstacle) lhs, (MovingWorkerUnitObstacle) rhs);
		if (lhs.getClass().equals(IdlingWorkerUnitObstacle.class))
			return equal((IdlingWorkerUnitObstacle) lhs, (IdlingWorkerUnitObstacle) rhs);
		
		throw new IllegalArgumentException("unknown WorkerUnitObstacle type");
	}

	private static boolean equal(OccupiedWorkerUnitObstacle lhs, OccupiedWorkerUnitObstacle rhs) {
		return lhs.getOccupation().equals(rhs.getOccupation());
	}

	private static boolean equal(MovingWorkerUnitObstacle lhs, MovingWorkerUnitObstacle rhs) {
		return lhs.getGoal().equals(rhs.getGoal());
	}

	private static boolean equal(IdlingWorkerUnitObstacle lhs, IdlingWorkerUnitObstacle rhs) {
		return true;
	}

}
