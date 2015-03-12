//package legacy.world;
//
//import static java.util.Collections.*;
//
//import java.util.HashSet;
//import java.util.Objects;
//import java.util.Set;
//
//import jts.geom.immutable.ImmutablePolygon;
//import scheduler.Node;
//import world.DynamicObstacle;
//import world.Trajectory;
//
///**
// * <p>A {@code NodeObstacle} is a path section of a node represented as a
// * dynamic obstacle. A node typically has multiple path sections which form
// * the entire path the node follows. Ordinarily one section connects two
// * locations in time which a node has to visit. The path between those
// * locations is interchangeable. This enables to replace singular sections due
// * to job planning.</p>
// *
// * <p>Another property of a {@code NodeObstacle} are evasions. Nodes
// * might need to evade another node to avoid collisions. When a node changes
// * one of its sections due to job planning a previous evasion might become
// * obsolete. A {@code NodeObstacle} provides the functionality to register
// * the path section of the node which was evading this one. This enables to
// * detect any obsolete evasions for further actions.</p>
// *
// * @author Rico
// */
//public abstract class NodeObstacle extends DynamicObstacle {
//
//	/**
//	 * The node unit represented as an obstacle.
//	 */
//	private final Node node;
//
//	/**
//	 * Stores the path sections of node which where evading this one.
//	 */
//	private final Set<MovingNodeObstacle> evaders = new HashSet<>();
//
//	/**
//	 * Stores an unmodifiable view on {@link #evaders}.
//	 */
//	private final Set<MovingNodeObstacle> unmodifiableEvaders = unmodifiableSet(evaders);
//
//	/**
//	 * Creates a new {@code NodeObstacle} for the given node along the
//	 * trajectory.
//	 *
//	 * @param node
//	 * @param trajectory
//	 * @throws NullPointerException
//	 *             if any argument is {@code null}.
//	 * @throws IllegalArgumentException
//	 *             if the trajectory is empty.
//	 */
//	public NodeObstacle(Node node, Trajectory trajectory) {
//		// retrieveShape throws NullPointerException if node is null
//		super(retrieveShape(node), trajectory);
//
//		this.node = node;
//	}
//
//	/**
//	 * Returns the node's shape.
//	 *
//	 * @param node
//	 * @return the shape.
//	 * @throws NullPointerException if the node is {@code null}.
//	 */
//	private static ImmutablePolygon retrieveShape(Node node) {
//		Objects.requireNonNull(node, "node");
//
//		return node.getShape();
//	}
//
//	/**
//	 * @return the node unit represented by this obstacle.
//	 */
//	public Node getNode() {
//		return node;
//	}
//
//	/**
//	 * @return other node's path sections which had to evade this obstacle.
//	 */
//	public Set<MovingNodeObstacle> getEvaders() {
//		return unmodifiableEvaders;
//	}
//
//	/**
//	 * Registers a path section which had to evade this one.
//	 *
//	 * @param evader the section which had to evade.
//	 * @throws NullPointerException if the {@code evader} is {@code null}.
//	 * @throws IllegalArgumentException if the {@code evader} was already registered.
//	 */
//	public void addEvader(MovingNodeObstacle evader) {
//		Objects.requireNonNull(evader, "evader");
//
//		boolean status = evaders.add(evader);
//
//		if (!status)
//			throw new IllegalArgumentException("evader already present");
//	}
//
//	/**
//	 * Removes a registered {@code evader} which was evading this one.
//	 *
//	 * @param evader to be unregistered
//	 * @throws NullPointerException if the {@code evader} is {@code null}.
//	 * @throws IllegalArgumentException if the {@code evader} was not registered.
//	 */
//	public void removeEvader(MovingNodeObstacle evader) {
//		Objects.requireNonNull(evader, "evader");
//
//		boolean status = evaders.remove(evader);
//
//		if (!status)
//			throw new IllegalArgumentException("unknown evader");
//	}
//	
//	/**
//	 * Clears all evasion relations
//	 */
//	public void clearEvasions() {
//		evaders.clear();
//	}
//
//}
