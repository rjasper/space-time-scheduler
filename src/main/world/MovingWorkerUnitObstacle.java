package world;

import java.util.List;

import tasks.Task;
import tasks.WorkerUnit;

import com.vividsolutions.jts.geom.Point;

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
	
	public List<Point> getSpatialPathComponent() {
		return getTrajectory().getSpatialPathComponent();
	}
	
	public List<Point> getArcTimePathComponent() {
		return getTrajectory().getArcTimePathComponent();
	}
	
	public Task getGoal() {
		return goal;
	}

}
