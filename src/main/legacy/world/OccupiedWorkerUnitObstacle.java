//package legacy.world;
//
//import java.time.LocalDateTime;
//import java.util.Objects;
//
//import jts.geom.immutable.ImmutablePoint;
//import scheduler.Job;
//import scheduler.Node;
//import world.SimpleTrajectory;
//import world.SpatialPath;
//import world.Trajectory;
//
//import com.google.common.collect.ImmutableList;
//
///**
// * An {@code OccupiedNodeObstacle} represents the stationary path segment
// * of a node being occupied with a job.
// * 
// * @author Rico
// */
//public class OccupiedNodeObstacle extends NodeObstacle {
//
//	/**
//	 * The job the node is occupied with.
//	 */
//	private final Job occupation;
//
//	/**
//	 * Constructs a new {@code OccupiedNodeObstacle} of the node having
//	 * an occupation.
//	 * 
//	 * @param node
//	 * @param occupation
//	 * @throws NullPointerException if any argument is {@code null}.
//	 */
//	public OccupiedNodeObstacle(Node node, Job occupation) {
//		// throws NullPointerException
//		super(node, buildTrajectory(occupation));
//		
//		Objects.requireNonNull(occupation, "occupation");
//
//		this.occupation = occupation;
//	}
//
//	/**
//	 * Builds the stationary trajectory of the node during its occupation.
//	 * 
//	 * @param occupation
//	 * @return the trajectory.
//	 * @throws NullPointerException if the occupation is {@code null}.
//	 */
//	private static Trajectory buildTrajectory(Job occupation) {
//		Objects.requireNonNull(occupation, "occupation");
//		
//		ImmutablePoint location = occupation.getLocation();
//		LocalDateTime startTime = occupation.getStartTime();
//		LocalDateTime finishTime = occupation.getFinishTime();
//		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(location, location));
//		ImmutableList<LocalDateTime> times = ImmutableList.of(startTime, finishTime);
//	
//		return new SimpleTrajectory(spatialPath, times);
//	}
//
//	/**
//	 * @return the occupation of the node.
//	 */
//	public Job getOccupation() {
//		return occupation;
//	}
//
//}
