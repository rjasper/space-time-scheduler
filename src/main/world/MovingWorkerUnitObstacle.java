package world;

import java.util.List;
import java.util.Objects;

import tasks.Task;
import tasks.WorkerUnit;

import com.vividsolutions.jts.geom.Point;

/**
 * A {@code MovingWorkerUnitObstacle} represents a non-stationary path segment
 * of a worker. The goal of any moving worker is always a task. The trajectory
 * is always decomposed.
 * 
 * @author Rico
 */
public class MovingWorkerUnitObstacle extends WorkerUnitObstacle {

	/**
	 * The destination of this path segment.
	 */
	private final Task goal;
	
	/**
	 * Constructs a new {@code MovingWorkerUnitObstacle} of a worker along a
	 * trajectory leading to the task.
	 * 
	 * @param worker
	 * @param trajectory
	 * @param goal
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if any of the following is true:
	 *             <ul>
	 *             <li>The trajectory is empty.</li>
	 *             <li>The trajectory does not lead to the goal.</li>
	 *             </ul>
	 */
	public MovingWorkerUnitObstacle(WorkerUnit worker, DecomposedTrajectory trajectory, Task goal) {
		// throws NullPointerException and IllegalArgumentException
		super(worker, trajectory);
		
		Objects.requireNonNull(goal, "goal");
		
		this.goal = goal;
		
		if (!checkDestination())
			throw new IllegalArgumentException("trajectory does not lead to the goal");
	}
	
	/**
	 * Checks if the destination of this object is consistent. The trajectory
	 * must lead to the goal.
	 * 
	 * @return {@code true} if the destination is consistent.
	 */
	private boolean checkDestination() {
		Point trajDestination = getFinishLocation();
		Point goalLocation = getGoal().getLocation();
		
		return trajDestination.equals(goalLocation);
	}

	/*
	 * (non-Javadoc)
	 * @see world.DynamicObstacle#getTrajectory()
	 */
	@Override
	public DecomposedTrajectory getTrajectory() {
		return (DecomposedTrajectory) super.getTrajectory();
	}
	
	/**
	 * @return the goal which the worker is moving to.
	 */
	public Task getGoal() {
		return goal;
	}

	/**
	 * @return the spatial path component of the decomposed trajectory.
	 * @see {@link DecomposedTrajectory#getSpatialPathComponent()}
	 */
	public List<Point> getSpatialPathComponent() {
		return getTrajectory().getSpatialPathComponent();
	}
	
	/**
	 * @return the arc time path component of the decomposed trajectory.
	 * @see {@link DecomposedTrajectory#getArcTimePathComponent()}
	 */
	public List<Point> getArcTimePathComponent() {
		return getTrajectory().getArcTimePathComponent();
	}

}
