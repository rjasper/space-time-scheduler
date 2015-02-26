//package legacy.world;
//
//import static java.util.Collections.*;
//
//import java.util.HashSet;
//import java.util.Objects;
//import java.util.Set;
//
//import scheduler.Task;
//import scheduler.WorkerUnit;
//import world.ArcTimePath;
//import world.DecomposedTrajectory;
//import world.SpatialPath;
//
//import com.vividsolutions.jts.geom.Point;
//
///**
// * A {@code MovingWorkerUnitObstacle} represents a non-stationary path segment
// * of a worker. The goal of any moving worker is always a task. The trajectory
// * is always decomposed.
// * 
// * @author Rico
// */
//public class MovingWorkerUnitObstacle extends WorkerUnitObstacle {
//
//	/**
//	 * The destination of this path segment.
//	 */
//	private final Task goal;
//	
//	/**
//	 * Stores the path sections which were evaded by this one.
//	 */
//	private final Set<WorkerUnitObstacle> evadees = new HashSet<>();
//	
//	/**
//	 * Stores an unmodifiable view on {@link #evadees}.
//	 */
//	private final Set<WorkerUnitObstacle> unmodifiableEvadees = unmodifiableSet(evadees);
//	
//	/**
//	 * Constructs a new {@code MovingWorkerUnitObstacle} of a worker along a
//	 * trajectory leading to the task.
//	 * 
//	 * @param worker
//	 * @param trajectory
//	 * @param goal
//	 * @throws NullPointerException
//	 *             if any argument is {@code null}.
//	 * @throws IllegalArgumentException
//	 *             if any of the following is true:
//	 *             <ul>
//	 *             <li>The trajectory is empty.</li>
//	 *             <li>The trajectory does not lead to the goal.</li>
//	 *             </ul>
//	 */
//	public MovingWorkerUnitObstacle(WorkerUnit worker, DecomposedTrajectory trajectory, Task goal) {
//		// throws NullPointerException and IllegalArgumentException
//		super(worker, trajectory);
//		
//		this.goal = Objects.requireNonNull(goal, "goal");
//		
//		if (!checkDestination())
//			throw new IllegalArgumentException("trajectory does not lead to the goal");
//	}
//	
//	/**
//	 * Checks if the destination of this object is consistent. The trajectory
//	 * must lead to the goal.
//	 * 
//	 * @return {@code true} if the destination is consistent.
//	 */
//	private boolean checkDestination() {
//		Point trajDestination = getFinishLocation();
//		Point goalLocation = getGoal().getLocation();
//		
//		return trajDestination.equals(goalLocation);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see world.DynamicObstacle#getTrajectory()
//	 */
//	@Override
//	public DecomposedTrajectory getTrajectory() {
//		return (DecomposedTrajectory) super.getTrajectory();
//	}
//	
//	/**
//	 * @return the goal which the worker is moving to.
//	 */
//	public Task getGoal() {
//		return goal;
//	}
//
//	/**
//	 * @return the spatial path component of the decomposed trajectory.
//	 * @see {@link DecomposedTrajectory#getSpatialPathComponent()}
//	 */
//	public SpatialPath getSpatialPathComponent() {
//		return getTrajectory().getSpatialPathComponent();
//	}
//	
//	/**
//	 * @return the arc time path component of the decomposed trajectory.
//	 * @see {@link DecomposedTrajectory#getArcTimePathComponent()}
//	 */
//	public ArcTimePath getArcTimePathComponent() {
//		return getTrajectory().getArcTimePathComponent();
//	}
//
//	/**
//	 * @return other worker's path sections which were evaded by this obstacle.
//	 */
//	public Set<WorkerUnitObstacle> getEvadees() {
//		return unmodifiableEvadees;
//	}
//
//	/**
//	 * Registers a path section which were evaded by this one.
//	 *
//	 * @param evadee the section which were evaded.
//	 * @throws NullPointerException if the {@code evadee} is {@code null}.
//	 * @throws IllegalArgumentException if the {@code evadee} was already registered.
//	 */
//	public void addEvadee(WorkerUnitObstacle evadee) {
//		Objects.requireNonNull(evadee, "evadee");
//
//		boolean status = evadees.add(evadee);
//
//		if (!status)
//			throw new IllegalArgumentException("evadee already present");
//	}
//
//	/**
//	 * Removes a registered {@code evadee} which evaded by this one.
//	 *
//	 * @param evader to be unregistered
//	 * @throws NullPointerException if the {@code evadee} is {@code null}.
//	 * @throws IllegalArgumentException if the {@code evadee} was not registered.
//	 */
//	public void removeEvadee(WorkerUnitObstacle evadee) {
//		Objects.requireNonNull(evadee, "evadee");
//
//		boolean status = evadees.remove(evadee);
//
//		if (!status)
//			throw new IllegalArgumentException("unknown evadee");
//	}
//
//	/* (non-Javadoc)
//	 * @see world.WorkerUnitObstacle#silence()
//	 */
//	@Override
//	public void clearEvasions() {
//		super.clearEvasions();
//		
//		evadees.clear();
//	}
//
//}
