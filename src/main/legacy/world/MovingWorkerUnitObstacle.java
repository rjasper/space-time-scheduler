//package legacy.world;
//
//import static java.util.Collections.*;
//
//import java.util.HashSet;
//import java.util.Objects;
//import java.util.Set;
//
//import scheduler.Task;
//import scheduler.Node;
//import world.ArcTimePath;
//import world.DecomposedTrajectory;
//import world.SpatialPath;
//
//import com.vividsolutions.jts.geom.Point;
//
///**
// * A {@code MovingNodeObstacle} represents a non-stationary path segment
// * of a node. The goal of any moving node is always a task. The trajectory
// * is always decomposed.
// * 
// * @author Rico
// */
//public class MovingNodeObstacle extends NodeObstacle {
//
//	/**
//	 * The destination of this path segment.
//	 */
//	private final Task goal;
//	
//	/**
//	 * Stores the path sections which were evaded by this one.
//	 */
//	private final Set<NodeObstacle> evadees = new HashSet<>();
//	
//	/**
//	 * Stores an unmodifiable view on {@link #evadees}.
//	 */
//	private final Set<NodeObstacle> unmodifiableEvadees = unmodifiableSet(evadees);
//	
//	/**
//	 * Constructs a new {@code MovingNodeObstacle} of a node along a
//	 * trajectory leading to the task.
//	 * 
//	 * @param node
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
//	public MovingNodeObstacle(Node node, DecomposedTrajectory trajectory, Task goal) {
//		// throws NullPointerException and IllegalArgumentException
//		super(node, trajectory);
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
//	 * @return the goal which the node is moving to.
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
//	 * @return other node's path sections which were evaded by this obstacle.
//	 */
//	public Set<NodeObstacle> getEvadees() {
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
//	public void addEvadee(NodeObstacle evadee) {
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
//	public void removeEvadee(NodeObstacle evadee) {
//		Objects.requireNonNull(evadee, "evadee");
//
//		boolean status = evadees.remove(evadee);
//
//		if (!status)
//			throw new IllegalArgumentException("unknown evadee");
//	}
//
//	/* (non-Javadoc)
//	 * @see world.NodeObstacle#silence()
//	 */
//	@Override
//	public void clearEvasions() {
//		super.clearEvasions();
//		
//		evadees.clear();
//	}
//
//}
