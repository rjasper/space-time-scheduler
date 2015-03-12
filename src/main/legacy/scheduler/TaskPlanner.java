//package legacy.scheduler;
//
//import static java.util.Collections.*;
//import static java.util.stream.Collectors.*;
//import static jts.geom.immutable.ImmutableGeometries.*;
//import static util.Comparables.*;
//import static util.TimeConv.*;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.Collection;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Objects;
//import java.util.UUID;
//import java.util.stream.Stream;
//
//import jts.geom.immutable.ImmutablePoint;
//import jts.geom.util.GeometriesRequire;
//import scheduler.ScheduleResult.TrajectoryUpdate;
//import scheduler.Task;
//import scheduler.Node;
//import scheduler.NodeReference;
//import util.CollectionsRequire;
//import world.ArcTimePath;
//import world.DecomposedTrajectory;
//import world.DynamicObstacle;
//import legacy.world.IdlingNodeObstacle;
//import legacy.world.MovingNodeObstacle;
//import legacy.world.OccupiedNodeObstacle;
//import world.SpatialPath;
//import world.Trajectory;
//import legacy.world.NodeObstacle;
//import world.WorldPerspective;
//import world.WorldPerspectiveCache;
//import world.pathfinder.FixTimeVelocityPathfinder;
//import world.pathfinder.FixTimeVelocityPathfinderImpl;
//import world.pathfinder.MinimumTimeVelocityPathfinder;
//import world.pathfinder.MinimumTimeVelocityPathfinderImpl;
//import world.pathfinder.SpatialPathfinder;
//
//import com.vividsolutions.jts.geom.Point;
//
///**
// * <p>The TaskPlanner plans a new {@link Task} into an established set of tasks.
// * It requires multiple parameters which determine the {@link Node node}
// * to execute the new task, and the location, duration, and time interval of the
// * execution. It is also responsible for ensuring that the designated node is
// * able to reach the task's location with colliding with any other object; be it
// * stationary or another node.</p>
// *
// * <p>Should it be impossible to plan the new task then the TaskPlanner will
// * not change the current task set. This might be the case when the designated
// * node is unable to reach the location without violating any time
// * constraints.</p>
// *
// * <p>The planning involves the calculation of a spatial path from the previous
// * location of the node to the task's location and the successive path to
// * the next location the node is required to be. The next step is to calculate
// * a velocity profile to evade dynamic obstacles. Since the old path which
// * the node was previously planned to follow will be obsolete other nodes
// * might be affected. Nodes which were previously evading the now obsolete
// * path section should update their affected path sections with a new
// * velocity profile.</p>
// *
// * <p>The TaskPlanner creates a job queue to calculate the new velocity profile
// * for the new spatial paths of the designated node and all other affected
// * path sections of other nodes. The jobs are sorted by the
// * {@link Job#laxity()} to give priority to nodes in a hurry.</p>
// *
// * @author Rico Jasper
// */
//public class TaskPlanner {
//
//	/**
//	 * The FixTimeVelocityPathfinder to be used.
//	 */
//	private FixTimeVelocityPathfinder fixTimeVelocityPathfinder = new FixTimeVelocityPathfinderImpl();
//
//	/**
//	 * The MinimumTimeVelocityPathfinder to be used.
//	 */
//	private MinimumTimeVelocityPathfinder minimumTimeVelocityPathfinder = new MinimumTimeVelocityPathfinderImpl();
//
//	/**
//	 * The current node.
//	 */
//	private Node node = null;
//
//	/**
//	 * The whole node pool.
//	 */
//	private Collection<Node> nodePool = null;
//
//	/**
//	 * A cache of the {@link WorldPerspective perspectives} of the
//	 * {@link Node nodes}.
//	 */
//	private WorldPerspectiveCache perspectiveCache = null;
//
//	/**
//	 * A changing collection of dynamic obstacles of interest. During the planning of a
//	 * {@link Task} the list of DynamicObstacles might be extended multiple
//	 * times.
//	 */
//	private Collection<NodeObstacle> nodeObstacles = new LinkedList<>();
//	
//	/**
//	 * The id of the {@link Task task} to be planned.
//	 */
//	private UUID taskId = null;
//
//	/**
//	 * The location of the {@link Task task} to be planned.
//	 */
//	private ImmutablePoint location = null;
//
//	/**
//	 * The earliest start time of the {@link Task task} to be planned.
//	 */
//	private LocalDateTime earliestStartTime = null;
//
//	/**
//	 * The latest start time of the {@link Task task} to be planned.
//	 */
//	private LocalDateTime latestStartTime = null;
//
//	/**
//	 * The duration of the {@link Task task} to be planned.
//	 */
//	private Duration duration = null;
//	
//	/**
//	 * The planned task.
//	 */
//	private Task resultTask = null;
//	
//	/**
//	 * The trajectory updates.
//	 */
//	private List<TrajectoryUpdate> resultTrajectoryUpdates = null;
//
//	/**
//	 * @return the FixTimeVelocityPathfinder.
//	 */
//	private FixTimeVelocityPathfinder getFixTimeVelocityPathfinder() {
//		return fixTimeVelocityPathfinder;
//	}
//
//	/**
//	 * @return the MinimumTimeVelocityPathfinder.
//	 */
//	private MinimumTimeVelocityPathfinder getMinimumTimeVelocityPathfinder() {
//		return minimumTimeVelocityPathfinder;
//	}
//
//	/**
//	 * @return the current node.
//	 */
//	private Node getNode() {
//		return node;
//	}
//
//	/**
//	 * Sets the current node. The TaskPlanner uses this node to plan a
//	 * {@link Task} which is executed by this node.
//	 *
//	 * @param node to execute the task
//	 * @throws NullPointerException if node is null
//	 */
//	public void setNode(Node node) {
//		Objects.requireNonNull(node, "node");
//
//		this.node = node;
//	}
//
//	/**
//	 * @return the node pool
//	 */
//	private Collection<Node> getNodePool() {
//		return nodePool;
//	}
//
//	/**
//	 * Sets the node pool. Only nodes part of this pool will be regarded as
//	 * {@link DynamicObstacle dynamic obstacles}.
//	 *
//	 * @param nodePool
//	 * @throws NullPointerException
//	 *             if nodePool is null of contains {@code null}
//	 */
//	public void setNodePool(Collection<Node> nodePool) {
//		CollectionsRequire.requireNonNull(nodePool, "nodePool");
//		
//		this.nodePool = unmodifiableCollection( nodePool );
//	}
//
//	/**
//	 * @return the perspective cache
//	 */
//	private WorldPerspectiveCache getPerspectiveCache() {
//		return perspectiveCache;
//	}
//
//	/**
//	 * Sets the perspective cache.
//	 *
//	 * @param perspectiveCache
//	 * @throws NullPointerException if perspectiveCache is null
//	 */
//	public void setPerspectiveCache(WorldPerspectiveCache perspectiveCache) {
//		Objects.requireNonNull(perspectiveCache, "perspectiveCache");
//
//		this.perspectiveCache = perspectiveCache;
//	}
//
//	/**
//	 * @return the node obstacles currently of interest. This also includes
//	 *         already planned path sections of nodes.
//	 */
//	private Collection<NodeObstacle> getNodeObstacles() {
//		return nodeObstacles;
//	}
//
//	/**
//	 * Adds a path section of a node to the node obstacles of interests.
//	 *
//	 * @param section
//	 */
//	private void addNodeObstacle(NodeObstacle section) {
//		nodeObstacles.add(section);
//	}
//
//	/**
//	 * Adds multiple node obstacles to the ones of interest.
//	 *
//	 * @param sections
//	 */
//	private void addAllNodeObstacles(Collection<NodeObstacle> sections) {
//		nodeObstacles.addAll(sections);
//	}
//
//	/**
//	 * Clears the collection of dynamic obstacles of interests.
//	 */
//	private void clearCurrentDynamicObstacles() {
//		nodeObstacles.clear();
//	}
//
//	/**
//	 * @return the id of the {@link Task task} to be planned.
//	 */
//	private UUID getTaskId() {
//		return taskId;
//	}
//
//	/**
//	 * Sets the id of the {@link Task task} to be planned.
//	 * 
//	 * @param taskId
//	 */
//	public void setTaskId(UUID taskId) {
//		this.taskId = taskId;
//	}
//
//	/**
//	 * @return the location of the {@link Task task} to be planned.
//	 */
//	private ImmutablePoint getLocation() {
//		return location;
//	}
//
//	/**
//	 * Sets the location of the {@link Task task} to be planned.
//	 *
//	 * @param location
//	 */
//	public void setLocation(Point location) {
//		GeometriesRequire.requireValid2DPoint(location, "location");
//		
//		this.location = immutable(location);
//	}
//
//	/**
//	 * <p>Returns the earliest start time of the {@link Task task} to be
//	 * planned.<p>
//	 *
//	 * <p>Also consideres the {@link Node#getInitialTime() initial time} of
//	 * the {@link Node current node}.</p>
//	 *
//	 * @return the earliest start time of the task to be planned.
//	 */
//	private LocalDateTime getEarliestStartTime() {
//		LocalDateTime initialTime = getNode().getInitialTime();
//
//		return max(earliestStartTime, initialTime);
//	}
//
//	/**
//	 * Sets the earliest start time of the {@link Task task} to be planned.
//	 *
//	 * @param earliestStartTime
//	 */
//	public void setEarliestStartTime(LocalDateTime earliestStartTime) {
//		this.earliestStartTime = Objects.requireNonNull(earliestStartTime, "earliestStartTime");
//	}
//
//	/**
//	 * @return the latest start time of the {@link Task task} to be planned.
//	 */
//	private LocalDateTime getLatestStartTime() {
//		return latestStartTime;
//	}
//
//	/**
//	 * Sets the latest start time of the {@link Task task} to be planned.
//	 *
//	 * @param latestStartTime
//	 */
//	public void setLatestStartTime(LocalDateTime latestStartTime) {
//		this.latestStartTime = Objects.requireNonNull(latestStartTime, "latestStartTime");
//	}
//
//	/**
//	 * @return the duration of the {@link Task task} to be planned.
//	 */
//	private Duration getDuration() {
//		return duration;
//	}
//
//	/**
//	 * Sets the duration of the {@link Task task} to be planned.
//	 *
//	 * @param duration
//	 * @throws IllegalArgumentException
//	 *             if duration is negative or zero.
//	 */
//	public void setDuration(Duration duration) {
//		Objects.requireNonNull(duration, "duration");
//		
//		if (duration.isNegative() || duration.isZero())
//			throw new IllegalArgumentException("illegal duration");
//		
//		this.duration = duration;
//	}
//	
//	/**
//	 * @return a list of newly planned tasks.
//	 */
//	public List<Task> getResultTasks() {
//		if (resultTask == null)
//			return emptyList();
//		else
//			return singletonList(resultTask);
//	}
//	
//	/**
//	 * Resets the {@link #resultTask} to {@code null}.
//	 */
//	private void resetResultTask() {
//		resultTask = null;
//	}
//
//	/**
//	 * Sets the result task.
//	 * 
//	 * @param resultTask
//	 */
//	private void setResultTask(Task resultTask) {
//		this.resultTask = resultTask;
//	}
//
//	/**
//	 * @return the trajectory updates.
//	 */
//	public List<TrajectoryUpdate> getResultTrajectoryUpdates() {
//		if (resultTrajectoryUpdates == null)
//			return emptyList();
//		else
//			return resultTrajectoryUpdates;
//	}
//	
//	/**
//	 * Adds a new {@link TrajectoryUpdate} to {@link #resultTrajectoryUpdates}.
//	 * 
//	 * @param trajectory the updated trajectory
//	 * @param node whose trajectory was updated
//	 */
//	private void addTrajectoryUpdate(Trajectory trajectory, Node node) {
//		resultTrajectoryUpdates.add(
//			new TrajectoryUpdate(trajectory, node.getReference()));
//	}
//
//	/**
//	 * Resets the {@link #resultTrajectoryUpdates} to an empty
//	 * {@link LinkedList}.
//	 */
//	private void resetResultTrajectoryUpdates() {
//		resultTrajectoryUpdates = new LinkedList<>();
//	}
//
//	/**
//	 * @return the SpatialPathfinder.
//	 */
//	private SpatialPathfinder getSpatialPathfinder() {
//		Node node = getNode();
//		WorldPerspectiveCache cache = getPerspectiveCache();
//
//		WorldPerspective perspective = cache.getPerspectiveFor(node);
//
//		return perspective.getSpatialPathfinder();
//	}
//
//	/**
//	 * Checks if all parameters are properly set. Throws an exception otherwise.
//	 * 
//	 * <p>
//	 * The following parameters are to be set by their respective setters:
//	 * <ul>
//	 * <li>node</li>
//	 * <li>nodePool</li>
//	 * <li>perspectiveCache</li>
//	 * <li>location</li>
//	 * <li>earliestStartTime</li>
//	 * <li>latestStartTime</li>
//	 * <li>duration</li>
//	 * </ul>
//	 * </p>
//	 * 
//	 * @throws IllegalStateException
//	 *             if any parameter is not set or if {@code earliestStartTime}
//	 *             is after {@code latestStartTime}.
//	 */
//	private void checkParameters() {
//		// assert all parameters set
//		if (node        == null ||
//			nodePool        == null ||
//			perspectiveCache  == null ||
//			taskId            == null ||
//			location          == null ||
//			earliestStartTime == null ||
//			latestStartTime   == null ||
//			duration          == null)
//		{
//			throw new IllegalStateException("some parameters are not set");
//		}
//		
//		// assert earliest <= latest 
//		if (earliestStartTime.compareTo(latestStartTime) > 0)
//			throw new IllegalStateException("earliestStartTime is after latestStartTime");
//
//		// cannot plan with node which is not initialized yet
//		if (latestStartTime.compareTo(node.getInitialTime()) < 0)
//			throw new IllegalStateException("node not initialized yet");
//	}
//	
//	/**
//	 * <p>Plans new path sections of the current node to the new task and
//	 * the following one. The old section is replaced by the new ones.</p>
//	 *
//	 * <p>Other nodes might also be affected. If a node was previously
//	 * evading the current node while on a section which now has been removed,
//	 * then the evading node's velocity profile will be recalculated. Affected
//	 * nodes might also trigger the recalculation of sections of other
//	 * nodes recursively.</p>
//	 *
//	 * @return {@code true} if the task has been successfully planned.
//	 */
//	public boolean plan() {
//		checkParameters();
//		
//		resetResultTask();
//		resetResultTrajectoryUpdates();
//		
//		boolean status = planImpl();
//		
//		clearCurrentDynamicObstacles();
//
//		return status;
//	}
//
//	/**
//	 * <p>Helper class which implements the actual planning algorithm.</p>
//	 *
//	 * <p>The sole reason for splitting {@link #plan()} and {@link #planImpl()}
//	 * is the easier call to {@link #clearCurrentDynamicObstacles() clear}
//	 * the list of dynamic obstacles.</p>
//	 *
//	 * @return {@code true} if the task has been successfully planned.
//	 */
//	private boolean planImpl() {
//		Node node = getNode();
//
//		// the section to be replaced by two section to and form the new task
//		NodeObstacle section = node.getObstacleSection( getEarliestStartTime() );
//
//		Point taskLocation = getLocation();
//		Point sectionStartLocation = section.getStartLocation();
//		Point sectionFinishLocation = section instanceof IdlingNodeObstacle
//			? taskLocation
//			: section.getFinishLocation();
//
//		// calculate the path to the new task
//		SpatialPath toTask = calculateSpatialPath(sectionStartLocation, taskLocation);
//		if (toTask == null)
//			return false;
//		// calculate the path from the new task
//		SpatialPath fromTask = calculateSpatialPath(taskLocation, sectionFinishLocation);
//		if (fromTask == null)
//			return false;
//
//		// determine the path sections to be recalculated
//		Collection<MovingNodeObstacle> evasions = buildEvasions(section);
//
//		// prepare node obstacles
//		addAllNodeObstacles( buildNodePoolSegments(evasions, section) );
//
//		// make jobs
//		Stream<Job> createJob = Stream.of(new CreateJob(toTask, fromTask, section));
//		Stream<Job> updateJobs = evasions.stream().map(UpdateJob::new);
//
//		// sort jobs
//		List<Job> jobs = Stream.concat(createJob, updateJobs)
//			.sorted()
//			.collect(toList());
//
//		// calculate jobs or fail
//		for (Job j : jobs) {
//			boolean status = j.calculate();
//
//			if (!status)
//				return false;
//		}
//
//		// update obstacles
//		for (Job j : jobs)
//			j.commit();
//
//		return true;
//	}
//
//	/**
//	 * Calculates the path between to locations.
//	 *
//	 * @param startLocation
//	 * @param finishLocation
//	 * @return {@code true} if a path connecting both locations was found.
//	 */
//	private SpatialPath calculateSpatialPath(Point startLocation, Point finishLocation) {
//		SpatialPathfinder pf = getSpatialPathfinder();
//
//		pf.setStartLocation(startLocation);
//		pf.setFinishLocation(finishLocation);
//
//		boolean status = pf.calculate();
//
//		if (!status)
//			return null;
//
//		return pf.getResultSpatialPath();
//	}
//
//	/**
//	 * A abstract Job to first calculate a velocity profile and then to apply
//	 * it existing path section of nodes. It also has the property to
//	 * calculate a laxity to order jobs by importance.
//	 */
//	private static abstract class Job implements Comparable<Job> {
//
//		/**
//		 * The duration available to complete a path.
//		 */
//		private final Duration jobDuration;
//
//		/**
//		 * Cached laxity value.
//		 */
//		private double laxity;
//
//		/**
//		 * Constructs a Job with a duration.
//		 *
//		 * @param jobDuration
//		 */
//		public Job(Duration jobDuration) {
//			this.jobDuration = jobDuration;
//		}
//
//		/**
//		 * @return the maximum duration available to complete a path.
//		 */
//		public Duration getJobDuration() {
//			return jobDuration;
//		}
//
//		/**
//		 * Calculates and caches the laxity value. The laxity value is defined
//		 * by the time allowed to stop per length unit for a node and still
//		 * being able to reach the destination in time.
//		 *
//		 * @return the laxity
//		 */
//		public double laxity() {
//			if (Double.isNaN(laxity))
//				laxity = calcLaxity();
//
//			return laxity;
//		};
//
//		/**
//		 * The actual calculation of the laxity value. Should comply with the
//		 * definition of the laxity in {@link #laxity}.
//		 *
//		 * @return
//		 */
//		public abstract double calcLaxity();
//
//		/**
//		 * Calculates the new path sections but does not change any nodes
//		 * directly or indirectly. The calculation might be unsuccessful if
//		 * a node is unable to reach its destination in time.
//		 *
//		 * @return {@code true} if valid path sections could be calculated.
//		 */
//		public abstract boolean calculate();
//
//		/**
//		 * Commits the calculated path sections and replaces the old ones. One
//		 * can assume that calculate is always called before commit.
//		 */
//		public abstract void commit();
//
//		@Override
//		public int compareTo(Job o) {
//			// the more laxity the less favored
//			return Double.compare(this.laxity(), o.laxity());
//		}
//
//	}
//
//	/**
//	 * A CreateJob calculates the velocity profile for the current node
//	 * to the new task and to the following task. It calculates three entirely
//	 * new trajectories (toTask, atTask, fromTask) which will replace the
//	 * old section of the current node.
//	 */
//	private class CreateJob extends Job {
//
//		// Since I am using side-effects to be more concise here,
//		// I ordered the fields in the order they are assigned during
//		// the lifespan of this object to make life a bit easier for the reader.
//		//
//		// Conciseness was also the reason to omit any getter or setters.
//
//		// The first three fields are set by the constructor.
//
//		/**
//		 * The spatial path to the new task.
//		 */
//		private final SpatialPath toTask;
//
//		/**
//		 * The spatial path from the new task to the next one.
//		 */
//		private final SpatialPath fromTask;
//
//		/**
//		 * The path section to be replaced.
//		 */
//		private final NodeObstacle section;
//
//		// dynamicObstacles is set in the very beginning of calculate
//		// and is used by calculateTrajectoryToTask and
//		// calculateTrajectoryFromTask which are called by calculate.
//
//		/**
//		 * The view of the current node on the dynamic obstacles.
//		 */
//		private Collection<DynamicObstacle> dynamicObstacles;
//
//		// The next two fields are set by calculateTrajectoryToTask as its
//		// result. trajToTask is needed to create the new task.
//
//		/**
//		 * The evaded path sections to the new task.
//		 */
//		private Collection<NodeObstacle> evadedToTask;
//
//		/**
//		 * The trajectory to the new task.
//		 */
//		private DecomposedTrajectory trajToTask;
//
//		// The task is created after calculateTrajectoryToTask has finished.
//		// It is needed by calculateTrajectoryFromTask.
//
//		/**
//		 * The resulting task.
//		 */
//		private Task task;
//
//		// The next two fields are set by calculateTrajectoryFrom which is
//		// called after the task is created.
//
//		/**
//		 * The evaded path sections from the new task to the next one.
//		 */
//		private Collection<NodeObstacle> evadedFromTask;
//
//		/**
//		 * The trajectory from the new task to the next one.
//		 */
//		private DecomposedTrajectory trajFromTask;
//
//		// After both trajectories are calculated, the three sections which
//		// will replace the old section are created. They are needed by
//		// the commit operation which is called externally.
//
//		/**
//		 * The path section to the new task.
//		 */
//		private MovingNodeObstacle sectionToTask;
//
//		/**
//		 * The path section at the new task.
//		 */
//		private OccupiedNodeObstacle sectionAtTask;
//
//		/**
//		 * The path section form the new task to the next one.
//		 */
//		private NodeObstacle sectionFromTask;
//
//		/**
//		 * Constructs a CreateJob using the spatial path to the new task and to
//		 * the one after and the section to be replaced.
//		 *
//		 * @param toTask
//		 * @param fromTask
//		 * @param section
//		 */
//		public CreateJob(SpatialPath toTask, SpatialPath fromTask, NodeObstacle section) {
//			// doesn't check inputs since class is private
//
//			super(section.getDuration());
//
//			this.toTask = toTask;
//			this.fromTask = fromTask;
//			this.section = section;
//		}
//
//		/*
//		 * (non-Javadoc)
//		 *
//		 * calcLaxity need toTask and fromTask to be set. This is already done
//		 * by the constructor.
//		 *
//		 * @see tasks.TaskPlanner.Job#calcLaxity()
//		 */
//		@Override
//		public double calcLaxity() {
//			Node node = getNode();
//			double maxSpeed = node.getMaxSpeed();
//			double length = toTask.length() + fromTask.length();
//			double taskDuration = inSeconds( getDuration() );
//			double maxDuration = inSeconds( getJobDuration() );
//
//			return (maxDuration - taskDuration)/length - 1./maxSpeed;
//		}
//
//		@Override
//		public boolean calculate() {
//			boolean status;
//			Node node = getNode();
//			dynamicObstacles = buildDynamicObstaclesFor(node);
//
//			// calculate trajectory to task
//
//			// sets trajToTask and evadedToTask
//			status = calculateTrajectoryToTask();
//
//			if (!status)
//				return false;
//
//			// create task
//
//			UUID taskId = getTaskId();
//			NodeReference nodeRef = node.getReference();
//			ImmutablePoint taskLocation = getLocation();
//			Duration taskDuration = getDuration();
//			LocalDateTime taskStartTime = trajToTask.getFinishTime();
//
//			task = new Task(taskId, nodeRef, taskLocation, taskStartTime, taskDuration);
//
//			// calculate trajectory from task
//
//			// needs task; sets trajFromTask and evadedFromTask
//			status = calculateTrajectoryFromTask();
//
//			if (!status)
//				return false;
//
//			// create sections
//
//			// don't introduce trajectories without duration
//			sectionToTask = trajToTask.getDuration().isZero() ?
//				null : new MovingNodeObstacle(node, trajToTask, task);
//			sectionAtTask = new OccupiedNodeObstacle(node, task);
//
//			if (section instanceof MovingNodeObstacle) {
//				Task nextTask = ((MovingNodeObstacle) section).getGoal();
//				
//				// don't introduce trajectories without duration
//				sectionFromTask = trajFromTask.getDuration().isZero() ?
//					null : new MovingNodeObstacle(node, trajFromTask, nextTask);
//			} else if (section instanceof IdlingNodeObstacle) {
//				LocalDateTime taskFinishTime = task.getFinishTime();
//				sectionFromTask = new IdlingNodeObstacle(node, taskLocation, taskFinishTime);
//			} else {
//				throw new RuntimeException("unexpected NodeObstacle");
//			}
//
//			// add sections to current dynamic obstacles
//
//			if (sectionToTask != null)
//				addNodeObstacle(sectionToTask);
//			addNodeObstacle(sectionAtTask);
//			if (sectionFromTask != null)
//				addNodeObstacle(sectionFromTask);
//
//			return true;
//		}
//
//		/**
//		 * <p>Calculates the trajectory to the new task.</p>
//		 *
//		 * <p>This method makes use of side-effects where it isn't always easy
//		 * to follow what effects it has on the object. This was done to keep
//		 * the code more concise since this is only an inner helper class.</p>
//		 *
//		 * <p>This methods assumes that {@link #dynamicObstacles}, {@link #toTask},
//		 * and {@link #section} are already set. It sets {@link #evadedToTask}
//		 * and {@link #trajToTask} if the path calculation was successful.</p>
//		 *
//		 * @return {@code true} if a trajectory to the new task could be calculated.
//		 */
//		private boolean calculateTrajectoryToTask() {
//			MinimumTimeVelocityPathfinder pf = getMinimumTimeVelocityPathfinder();
//
//			pf.setDynamicObstacles  ( dynamicObstacles       );
//			pf.setSpatialPath       ( toTask                 );
//			pf.setStartArc          ( 0.0                    );
//			pf.setFinishArc         ( toTask.length()        );
//			pf.setMinArc            ( 0.0                    );
//			pf.setMaxArc            ( toTask.length()        );
//			pf.setMaxSpeed          ( getNode().getMaxSpeed() );
//			pf.setStartTime         ( section.getStartTime() );
//			pf.setEarliestFinishTime( getEarliestStartTime() );
//			pf.setLatestFinishTime  ( getLatestStartTime()   );
//			pf.setBufferDuration    ( getDuration()          );
//
//			boolean status = pf.calculate();
//
//			if (!status)
//				return false;
//
//			evadedToTask = onlyNodeObstacles( pf.getResultEvadedObstacles() );
//			trajToTask = pf.getResultTrajectory();
//
//			return true;
//		}
//
//		/**
//		 * <p>Calculates the trajectory from the new task to the next one.</p>
//		 *
//		 * <p>This method makes use of side-effects where it isn't always easy
//		 * to follow what effects it has on the object. This was done to keep
//		 * the code more concise since this is only an inner helper class.</p>
//		 *
//		 * <p>This methods assumes that {@link #dynamicObstacles}, {@link #toTask},
//		 * {@link #task}, and {@link #section} are already set. It sets
//		 * {@link #evadedFromTask} and {@link #trajFromTask} if the path
//		 * calculation was successful.</p>
//		 *
//		 * @return {@code true} if a trajectory from the new task to the next
//		 *         one could be calculated.
//		 */
//		private boolean calculateTrajectoryFromTask() {
//			FixTimeVelocityPathfinder pf = getFixTimeVelocityPathfinder();
//
//			pf.setDynamicObstacles( dynamicObstacles        );
//			pf.setSpatialPath     ( fromTask                );
//			pf.setStartArc        ( 0.0                     );
//			pf.setFinishArc       ( fromTask.length()       );
//			pf.setMinArc          ( 0.0                     );
//			pf.setMaxArc          ( fromTask.length()       );
//			pf.setMaxSpeed        ( getNode().getMaxSpeed() );
//			pf.setStartTime       ( task.getFinishTime()    );
//			pf.setFinishTime      ( section.getFinishTime() );
//
//			boolean status = pf.calculate();
//
//			if (!status)
//				return false;
//
//			evadedFromTask = onlyNodeObstacles( pf.getResultEvadedObstacles() );
//			trajFromTask = pf.getResultTrajectory();
//
//			return true;
//		}
//
//		/*
//		 * (non-Javadoc)
//		 *
//		 * commit needs section, evadedToTask, evadedFromTask, sectionToTask,
//		 * sectionAtTask, sectionFromTask, and task to be set. It expects
//		 * that calculate was called before.
//		 *
//		 * @see tasks.TaskPlanner.Job#commit()
//		 */
//		@Override
//		public void commit() {
//			// register evasions
//			if (sectionToTask != null) {
//				for (NodeObstacle e : evadedToTask)
//					registerEvasion(sectionToTask, e);
//			}
//
//			if (sectionFromTask instanceof MovingNodeObstacle) {
//				for (NodeObstacle e : evadedFromTask)
//					registerEvasion((MovingNodeObstacle) sectionFromTask, e);
//			}
//
//			// add obstacle sections and task
//			Node node = getNode();
//			silenceSection(section);
//			if (sectionToTask != null) {
//				node.addObstacleSection(sectionToTask);
//				addTrajectoryUpdate(trajToTask, node);
//			}
//			
//			node.addObstacleSection(sectionAtTask);
//			addTrajectoryUpdate(sectionAtTask.getTrajectory(), node);
//			
//			if (sectionFromTask != null) {
//				node.addObstacleSection(sectionFromTask);
//				addTrajectoryUpdate(trajFromTask, node);
//			}
//			
//			node.addTask(task);
//			setResultTask(task);
//		}
//
//	}
//
//	/**
//	 * An UpdateJob recalculates the existing velocity profile of a path
//	 * section of a node directly or indirectly affected by the new spatial
//	 * path of the current node.
//	 */
//	private class UpdateJob extends Job {
//
//		/**
//		 * The path section to be updated.
//		 */
//		private final MovingNodeObstacle section;
//
//		// The next two fields are set by calculate.
//
//		/**
//		 * The resulting updated path section.
//		 */
//		private MovingNodeObstacle updatedSegment;
//
//		/**
//		 * Evaded obstacles by the updated path section.
//		 */
//		private Collection<NodeObstacle> evaded;
//
//		/**
//		 * Constructs a UpdateJob which updates the velocity profile of the
//		 * given path section.
//		 *
//		 * @param section
//		 */
//		public UpdateJob(MovingNodeObstacle section) {
//			// doesn't check inputs since class is private
//
//			super(Duration.between(section.getStartTime(), section.getFinishTime()));
//
//			this.section = section;
//		}
//
//		@Override
//		public double calcLaxity() {
//			Node node = section.getNode();
//			double maxSpeed = node.getMaxSpeed();
//			double length = section.getTrajectory().length();
//			double maxDuration = inSeconds( getJobDuration() );
//
//			return maxDuration/length - 1./maxSpeed;
//		}
//
//		/*
//		 * (non-Javadoc)
//		 *
//		 * Sets evaded and updatedSegment if path could be found.
//		 *
//		 * @see tasks.TaskPlanner.Job#calculate()
//		 */
//		@Override
//		public boolean calculate() {
//			Node node = section.getNode();
//			FixTimeVelocityPathfinder pf = getFixTimeVelocityPathfinder();
//			
//			ArcTimePath st = section.getArcTimePathComponent();
//
//			pf.setDynamicObstacles( buildDynamicObstaclesFor(node)  );
//			pf.setSpatialPath     ( section.getSpatialPathComponent() );
//			pf.setMinArc          ( st.minArc()                       );
//			pf.setMaxArc          ( st.maxArc()                       );
//			pf.setStartArc        ( st.getStartPoint().getX()         );
//			pf.setFinishArc       ( st.getFinishPoint().getX()        );
//			pf.setMaxSpeed        ( node.getMaxSpeed()              );
//			pf.setStartTime       ( section.getStartTime()            );
//			pf.setFinishTime      ( section.getFinishTime()           );
//
//			boolean status = pf.calculate();
//
//			if (!status)
//				return false;
//
//			evaded = onlyNodeObstacles( pf.getResultEvadedObstacles() );
//			updatedSegment = new MovingNodeObstacle(
//				node, pf.getResultTrajectory(), section.getGoal());
//
//			addNodeObstacle(updatedSegment);
//
//			return true;
//		}
//
//		/*
//		 * (non-Javadoc)
//		 *
//		 * Uses all fields. Expects that calculate was already called and successful.
//		 *
//		 * @see tasks.TaskPlanner.Job#commit()
//		 */
//		@Override
//		public void commit() {
//			// register evasions
//			for (NodeObstacle e : evaded)
//				registerEvasion(updatedSegment, e);
//
//			// update obstacle section
//			Node node = section.getNode();
//			silenceSection(section);
//			node.addObstacleSection(updatedSegment);
//			addTrajectoryUpdate(updatedSegment.getTrajectory(), node);
//		}
//
//	}
//
//	/**
//	 * Filters a given list of dynamic obstacles. Only accepts
//	 * {@code NodeObstacle}s.
//	 *
//	 * @param obstacles
//	 * @return
//	 */
//	private static Collection<NodeObstacle> onlyNodeObstacles(Collection<DynamicObstacle> obstacles) {
//		return obstacles.stream()
//			.filter(o -> o instanceof NodeObstacle)
//			.map(o -> (NodeObstacle) o)
//			.collect(toList());
//	}
//
//	/**
//	 * Builds a collection of all potentially affected path sections when
//	 * removing the original path section of the current node.
//	 *
//	 * @param obstacleSegment the original path section to be removed
//	 * @return the potentially affected path sections
//	 */
//	private static Collection<MovingNodeObstacle> buildEvasions(NodeObstacle obstacleSegment) {
//		return obstacleSegment.getEvaders().stream()
//			.flatMap(TaskPlanner::buildEvasionsStream)
//			.collect(toList());
//	}
//
//	/**
//	 * Helps building a collection of affected evasions. Builds a stream
//	 * all ancestor evasions of the given section and the section itself.
//	 *
//	 * @param obstacleSegment
//	 * @return
//	 * @see #buildEvasions(NodeObstacle)
//	 */
//	private static Stream<MovingNodeObstacle> buildEvasionsStream(MovingNodeObstacle obstacleSegment) {
//		Stream<MovingNodeObstacle> self = Stream.of(obstacleSegment);
//		Stream<MovingNodeObstacle> ancestors = obstacleSegment.getEvaders().stream()
//			.flatMap(TaskPlanner::buildEvasionsStream);
//
//		return Stream.concat(self, ancestors);
//	}
//
//	/**
//	 * Builds a collection of node obstacles using the {@link #nodePool}.
//	 * Exculdes all path sections given by exclusions and the obsolete section
//	 * to be removed.
//	 *
//	 * @param exclusions
//	 * @param obsoleteSegment
//	 * @return
//	 */
//	private Collection<NodeObstacle> buildNodePoolSegments(
//		Collection<? extends NodeObstacle> exclusions,
//		NodeObstacle obsoleteSegment)
//	{
//		Collection<Node> pool = getNodePool();
//
//		// tODO only use relevant sections
//		return pool.stream()
//			.flatMap(w -> w.getObstacleSections().stream())
//			.filter(o -> !exclusions.contains(o) && !o.equals(obsoleteSegment))
//			.collect(toList());
//	}
//
//	/**
//	 * Builds the the given node's perspective on the dynamic obstacles.
//	 * It buffers all {@link #nodeObstacles obstacles of interests}
//	 * by the node's radius and excludes the path sections of the node
//	 * itself.
//	 *
//	 * @param node to build the perspective for
//	 * @return a collection of dynamic obstacles in the node's perspective.
//	 */
//	private Collection<DynamicObstacle> buildDynamicObstaclesFor(Node node) {
//		double bufferDistance = node.getRadius();
//
//		// an exact solution would be to calculate the minkowski sum
//		// of each obstacle and the node's shape
//		
//		Stream<DynamicObstacle> worldObstacles = getPerspectiveCache()
//			.getPerspectiveFor(node)
//			.getView()
//			.getDynamicObstacles()
//			.stream();
//		
//		Stream<DynamicObstacle> nodeObstacles = getNodeObstacles().stream()
//			.filter(o -> !(o instanceof NodeObstacle)
//				|| o.getNode() != node)
//			.map(o -> o.buffer(bufferDistance));
//
//		return Stream.concat(worldObstacles, nodeObstacles)
//			.collect(toList());
//	}
//	
//	/**
//	 * Registers an evasion between the given sections.
//	 * 
//	 * @param evader
//	 * @param evadee
//	 */
//	private static void registerEvasion(MovingNodeObstacle evader, NodeObstacle evadee) {
//		// tODO kinda hacky
//		NodeObstacle actualEvadee = evadee.getNode()
//			.getObstacleSection(evadee.getStartTime());
//		
//		evader.addEvadee(actualEvadee);
//		actualEvadee.addEvader(evader);
//	}
//	
//	/**
//	 * Silences the given section. The section will be removed from its node
//	 * and will unregister any evasions which it is involved in.
//	 * 
//	 * @param section
//	 */
//	private static void silenceSection(NodeObstacle section) {
//		section.getNode().removeObstacleSection(section);
//		
//		for (MovingNodeObstacle e : section.getEvaders())
//			e.removeEvadee(section);
//		
//		if (section instanceof MovingNodeObstacle) {
//			MovingNodeObstacle msection = (MovingNodeObstacle) section;
//			
//			for (NodeObstacle e : msection.getEvadees())
//				e.removeEvader(msection);
//		}
//		
//		section.clearEvasions();
//	}
//
//}
