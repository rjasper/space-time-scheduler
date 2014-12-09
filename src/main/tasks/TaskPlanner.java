package tasks;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static util.Comparables.max;
import static util.DurationConv.inSeconds;
import static util.PathOperations.length;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import world.DecomposedTrajectory;
import world.DynamicObstacle;
import world.IdlingWorkerUnitObstacle;
import world.MovingWorkerUnitObstacle;
import world.OccupiedWorkerUnitObstacle;
import world.WorkerUnitObstacle;
import world.WorldPerspective;
import world.WorldPerspectiveCache;
import world.pathfinder.FixTimeVelocityPathfinder;
import world.pathfinder.FixTimeVelocityPathfinderImpl;
import world.pathfinder.MinimumTimeVelocityPathfinder;
import world.pathfinder.MinimumTimeVelocityPathfinderImpl;
import world.pathfinder.SpatialPathfinder;

import com.vividsolutions.jts.geom.Point;

public class TaskPlanner {

	/**
	 * The current worker.
	 */
	private WorkerUnit workerUnit = null;

	/**
	 * The whole worker pool.
	 */
	private Collection<WorkerUnit> workerPool = null;

	/**
	 * A cache of the {@link WorldPerspective perspectives} of the
	 * {@link WorkerUnit workers}.
	 */
	private WorldPerspectiveCache perspectiveCache = null;

	/**
	 * A changing collection of dynamic obstacles of interest. During the planning of a
	 * {@link Task} the list of DynamicObstacles might be extended multiple
	 * times.
	 */
	private Collection<DynamicObstacle> currentDynamicObstacles = new LinkedList<>();

	/**
	 * The location of the {@link Task task}.
	 */
	private Point location = null;

	/**
	 * The earliest start time of the {@link Task task} to be planned.
	 */
	private LocalDateTime earliestStartTime = null;

	/**
	 * The latest start time of the {@link Task task} to be planned.
	 */
	private LocalDateTime latestStartTime = null;

	/**
	 * The duration of the {@link Task task} to be planned.
	 */
	private Duration duration = null;

	/**
	 * The FixTimeVelocityPathfinder to be used.
	 */
	private FixTimeVelocityPathfinder fixTimeVelocityPathfinder = new FixTimeVelocityPathfinderImpl();

	/**
	 * The MinimumTimeVelocityPathfinder to be used.
	 */
	private MinimumTimeVelocityPathfinder minimumTimeVelocityPathfinder = new MinimumTimeVelocityPathfinderImpl();

	/**
	 * <p>Returns {@code true} iff all necessary parameters are set.</p>
	 *
	 * <p>The following parameters are to be set by their respective setters:
	 * <ul>
	 * <li>workerUnit</li>
	 * <li>workerPool</li>
	 * <li>perspectiveCache</li>
	 * <li>location</li>
	 * <li>earliestStartTime</li>
	 * <li>latestStartTime</li>
	 * <li>duration</li>
	 * </ul>
	 * </p>
	 *
	 * @return {@code true} iff all necessary parameters are set.
	 */
	public boolean isReady() {
		return workerUnit        != null
			&& workerPool        != null
			&& perspectiveCache  != null
			&& location          != null
			&& earliestStartTime != null
			&& latestStartTime   != null
			&& duration          != null;
	}

	// TODO check setter args

	/**
	 * @return the current worker.
	 */
	private WorkerUnit getWorkerUnit() {
		return workerUnit;
	}

	/**
	 * Sets the current worker. The TaskPlanner uses this worker to plan a
	 * {@link Task} which is executed by this worker.
	 *
	 * @param worker to execute the task
	 * @throws NullPointerException iff worker is null
	 */
	public void setWorkerUnit(WorkerUnit worker) {
		Objects.requireNonNull(worker, "worker");

		this.workerUnit = worker;
	}

	/**
	 * @return the worker pool
	 */
	private Collection<WorkerUnit> getWorkerPool() {
		return workerPool;
	}

	/**
	 * Sets the worker pool. Only workers part of this pool will be regarded
	 * as {@link DynamicObstacle dynamic obstacles}.
	 *
	 * @param workerPool
	 * @throws iff workerPool is null
	 */
	public void setWorkerPool(Collection<WorkerUnit> workerPool) {
		this.workerPool = new ArrayList<>(workerPool); // throws NullPointException
	}

	/**
	 * @return the perspective cache
	 */
	private WorldPerspectiveCache getPerspectiveCache() {
		return perspectiveCache;
	}

	/**
	 * Sets the perspective cache.
	 *
	 * @param perspectiveCache
	 * @throws NullPointerException iff perspectiveCache is null
	 */
	public void setPerspectiveCache(WorldPerspectiveCache perspectiveCache) {
		Objects.requireNonNull(perspectiveCache, "perspectiveCache");

		this.perspectiveCache = perspectiveCache;
	}

	/**
	 * Returns the dynamic obstacles of the world. Does not include the worker
	 * pool.
	 *
	 * @return the dynamic obstacles of the world
	 */
	private Collection<DynamicObstacle> getDynamicObstacles() {
		// TODO retrieve dynamic obstacles from perspective as soon as implemented

		return emptyList();
	}

	/**
	 * @return the dynamic obstacles currently of interest. This also includes
	 *         already planned path segments of workers.
	 */
	private Collection<DynamicObstacle> getCurrentDynamicObstacles() {
		return currentDynamicObstacles;
	}

	/**
	 * Adds a path segment of a worker to the dynamic obstacles of interests.
	 *
	 * @param segment
	 */
	private void addWorkerUnitObstacle(WorkerUnitObstacle segment) {
		currentDynamicObstacles.add(segment);
	}

	/**
	 * Adds multiple dynamic obstacles to the ones of interest.
	 *
	 * @param segments
	 */
	private void addAllDynamicObstacles(Collection<DynamicObstacle> segments) {
		currentDynamicObstacles.addAll(segments);
	}

	/**
	 * Clears the collection of dynamic obstacles of interests.
	 */
	private void clearCurrentDynamicObstacles() {
		currentDynamicObstacles.clear();
	}

	/**
	 * @return the location of the {@link Task task} to be planned.
	 */
	private Point getLocation() {
		return location;
	}

	/**
	 * Sets the location of the {@link Task task} to be planned.
	 *
	 * @param location
	 */
	public void setLocation(Point location) {
		this.location = location;
	}

	/**
	 * <p>Returns the earliest start time of the {@link Task task} to be
	 * planned.<p>
	 *
	 * <p>Also consideres the {@link WorkerUnit#getInitialTime() initial time} of
	 * the {@link WorkerUnit current worker}.</p>
	 *
	 * @return the earliest start time of the task to be planned.
	 */
	private LocalDateTime getEarliestStartTime() {
		LocalDateTime initialTime = getWorkerUnit().getInitialTime();

		return max(earliestStartTime, initialTime);
	}

	/**
	 * Sets the earliest start time of the {@link Task task} to be planned.
	 *
	 * @param earliestStartTime
	 */
	public void setEarliestStartTime(LocalDateTime earliestStartTime) {
		this.earliestStartTime = earliestStartTime;
	}

	/**
	 * @return the latest start time of the {@link Task task} to be planned.
	 */
	private LocalDateTime getLatestStartTime() {
		return latestStartTime;
	}

	/**
	 * Sets the latest start time of the {@link Task task} to be planned.
	 *
	 * @param latestStartTime
	 */
	public void setLatestStartTime(LocalDateTime latestStartTime) {
		this.latestStartTime = latestStartTime;
	}

	/**
	 * @return the duration of the {@link Task task} to be planned.
	 */
	private Duration getDuration() {
		return duration;
	}

	/**
	 * Sets the duration of the {@link Task task} to be planned.
	 *
	 * @param duration
	 */
	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	/**
	 * @return the SpatialPathfinder.
	 */
	private SpatialPathfinder getSpatialPathfinder() {
		WorkerUnit worker = getWorkerUnit();
		WorldPerspectiveCache cache = getPerspectiveCache();

		WorldPerspective perspective = cache.getPerspectiveFor(worker);

		return perspective.getSpatialPathfinder();
	}

	/**
	 * @return the FixTimeVelocityPathfinder.
	 */
	private FixTimeVelocityPathfinder getFixTimeVelocityPathfinder() {
		return fixTimeVelocityPathfinder;
	}

	/**
	 * @return the MinimumTimeVelocityPathfinder.
	 */
	private MinimumTimeVelocityPathfinder getMinimumTimeVelocityPathfinder() {
		return minimumTimeVelocityPathfinder;
	}

	/**
	 * <p>Plans new path segments of the current worker to the new task and
	 * the following one. The old segment is replaced by the new ones.</p>
	 *
	 * <p>Other workers might also be affected. If a worker was previously
	 * evading the current worker while on a segment which now has been removed,
	 * then the evading worker's velocity profile will be recalculated. Affected
	 * workers might also trigger the recalculation of segments of other
	 * workers recursively.</p>
	 *
	 * @return {@code true} iff the task has been successfully planned.
	 */
	public boolean plan() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");

		boolean status = planImpl();

		clearCurrentDynamicObstacles();

		return status;
	}

	/**
	 * <p>Helper class which implements the actual planning algorithm.</p>
	 *
	 * <p>The sole reason for splitting {@link #plan()} and {@link #planImpl()}
	 * is the easier call to {@link #clearCurrentDynamicObstacles() clear}
	 * the list of dynamic obstacles.</p>
	 *
	 * @return {@code true} iff the task has been successfully planned.
	 */
	private boolean planImpl() {
		WorkerUnit worker = getWorkerUnit();

		// cannot plan with worker which is not initialized yet
		if (getLatestStartTime().compareTo( worker.getInitialTime() ) < 0)
			return false;

		// the segment to be replaced by two segment to and form the new task
		WorkerUnitObstacle segment = worker.getObstacleSegment( getEarliestStartTime() );

		Point taskLocation = getLocation();
		Point segmentStartLocation = segment.getStartLocation();
		Point segmentFinishLocation = segment instanceof IdlingWorkerUnitObstacle
			? taskLocation
			: segment.getFinishLocation();

		// calculate the path to the new task
		List<Point> toTask = calculateSpatialPath(segmentStartLocation, taskLocation);
		if (toTask == null)
			return false;
		// calculate the path from the new task
		List<Point> fromTask = calculateSpatialPath(taskLocation, segmentFinishLocation);
		if (fromTask == null)
			return false;

		// determine the path segments to be recalculated
		List<MovingWorkerUnitObstacle> evasions = buildEvasions(segment);

		// prepare current dynamic obstacles
		addAllDynamicObstacles( getDynamicObstacles() );
		addAllDynamicObstacles( buildWorkerPoolSegments(evasions, segment) );

		// make jobs
		Stream<Job> createJob = Stream.of(new CreateJob(toTask, fromTask, segment));
		Stream<Job> updateJobs = evasions.stream().map(UpdateJob::new);

		// sort jobs
		List<Job> jobs = Stream.concat(createJob, updateJobs)
			.sorted()
			.collect(toList());

		// calculate jobs or fail
		for (Job j : jobs) {
			boolean status = j.calculate();

			if (!status)
				return false;
		}

		// update obstacles
		for (Job j : jobs)
			j.commit();

		return true;
	}

	/**
	 * Calculates the path between to locations.
	 *
	 * @param startLocation
	 * @param finishLocation
	 * @return {@code true} iff a path connecting both locations was found.
	 */
	private List<Point> calculateSpatialPath(Point startLocation, Point finishLocation) {
		SpatialPathfinder pf = getSpatialPathfinder();

		pf.setStartLocation(startLocation);
		pf.setFinishLocation(finishLocation);

		boolean status = pf.calculate();

		if (!status)
			return null;

		return pf.getResultSpatialPath();
	}

	/**
	 * A abstract Job to first calculate a velocity profile and then to apply
	 * it existing path segment of workers. It also has the property to
	 * calculate a laxity to order jobs by importance.
	 */
	private static abstract class Job implements Comparable<Job> {

		/**
		 * The maximum duration available to complete a path.
		 */
		private final Duration maxDuration;

		// TODO don't cache since class is private and outside behavior is known

		/**
		 * Cached laxity value.
		 */
		private transient double laxity = Double.NaN;

		/**
		 * Constructs a Job with a maximum duration.
		 *
		 * @param maxDuration
		 */
		public Job(Duration maxDuration) {
			this.maxDuration = maxDuration;
		}

		/**
		 * @return the maximum duration available to complete a path.
		 */
		public Duration getMaxDuration() {
			return maxDuration;
		}

		/**
		 * Calculates and caches the laxity value. The laxity value is defined
		 * by the time allowed to stop per length unit for a worker and still
		 * being able to reach the destination in time.
		 *
		 * @return the laxity
		 */
		public double laxity() {
			if (Double.isNaN(laxity))
				laxity = calcLaxity();

			return laxity;
		};

		/**
		 * The actual calculation of the laxity value. Should comply with the
		 * definition of the laxity in {@link #laxity}.
		 *
		 * @return
		 */
		public abstract double calcLaxity();

		/**
		 * Calculates the new path segments but does not change any workers
		 * directly or indirectly. The calculation might be unsuccessful if
		 * a worker is unable to reach its destination in time.
		 *
		 * @return {@code true} iff valid path segments could be calculated.
		 */
		public abstract boolean calculate();

		/**
		 * Commits the calculated path segments and replaces the old ones. One
		 * can assume that calculate is always called before commit.
		 */
		public abstract void commit();

		@Override
		public int compareTo(Job o) {
			// the more laxity the less favored
			return Double.compare(this.laxity(), o.laxity());
		}

	}

	/**
	 * A CreateJob calculates the velocity profile for the current worker
	 * to the new task and to the following task. It calculates three entirely
	 * new trajectories (toTask, atTask, fromTask) which will replace the
	 * old segment of the current worker.
	 */
	private class CreateJob extends Job {

		// Since I am using side-effect to be more concise here,
		// I ordered the fields in the order they are assigned during
		// the livespan of this object to make life a bit easier for the reader.
		//
		// Conciseness was also the reason to omit any getter or setters.

		// The first three fields are set by the constructor.

		/**
		 * The spatial path to the new task.
		 */
		private final List<Point> toTask;

		/**
		 * The spatial path from the new task to the next one.
		 */
		private final List<Point> fromTask;

		/**
		 * The path segment to be replaced.
		 */
		private final WorkerUnitObstacle segment;

		// dynamicObstacles is set in the very beginning of calculate
		// and is used by calculateTrajectoryToTask and
		// calculateTrajectoryFromTask which are called by calculate.

		/**
		 * The view of the current worker on the dynamic obstacles.
		 */
		private Collection<DynamicObstacle> dynamicObstacles;

		// The next two fields are set by calculateTrajectoryToTask as its
		// result. trajToTask is needed to create the new task.

		/**
		 * The evaded workers to the new task.
		 */
		private Collection<WorkerUnitObstacle> evadedToTask;

		/**
		 * The trajectory to the new task.
		 */
		private DecomposedTrajectory trajToTask;

		// The task is created after calculateTrajectoryToTask has finished.
		// It is needed by calculateTrajectoryFromTask.

		/**
		 * The resulting task.
		 */
		private Task task;

		// The next two fields are set by calculateTrajectoryFrom which is
		// called after the task is created.

		/**
		 * The evaded workers from the new task to the next one.
		 */
		private Collection<WorkerUnitObstacle> evadedFromTask;

		/**
		 * The trajectory from the new task to the next one.
		 */
		private DecomposedTrajectory trajFromTask;

		// After both trajectories are calculated, the three segments which
		// will replace the old segment are created. They are needed by
		// the commit operation which is called externally.

		/**
		 * The path segment to the new task.
		 */
		private MovingWorkerUnitObstacle segmentToTask;

		/**
		 * The path segment at the new task.
		 */
		private OccupiedWorkerUnitObstacle segmentAtTask;

		/**
		 * The path segment form the new task to the next one.
		 */
		private WorkerUnitObstacle segmentFromTask;

		/**
		 * Constructs a CreateJob using the spatial path to the new task and to
		 * the one after and the segment to be replaced.
		 *
		 * @param toTask
		 * @param fromTask
		 * @param segment
		 */
		public CreateJob(List<Point> toTask, List<Point> fromTask, WorkerUnitObstacle segment) {
			// doesn't check inputs since class is private

			super(segment.getDuration());

			this.toTask = toTask;
			this.fromTask = fromTask;
			this.segment = segment;
		}

		/*
		 * (non-Javadoc)
		 *
		 * calcLaxity need toTask and fromTask to be set. This is already done
		 * by the constructor.
		 *
		 * @see tasks.TaskPlanner.Job#calcLaxity()
		 */
		@Override
		public double calcLaxity() {
			WorkerUnit worker = getWorkerUnit();
			double maxSpeed = worker.getMaxSpeed();
			double length = length( toTask ) + length( fromTask );
			double taskDuration = inSeconds( getDuration() );
			double maxDuration = inSeconds( getMaxDuration() );

			return (maxDuration - taskDuration)/length - 1./maxSpeed;
		}

		@Override
		public boolean calculate() {
			boolean status;
			WorkerUnit worker = getWorkerUnit();
			dynamicObstacles = buildDynamicObstaclesFor(worker);

			// calculate trajectory to task

			// sets trajToTask and evadedToTask
			status = calculateTrajectoryToTask();

			if (!status)
				return false;

			// create task

			Point taskLocation = getLocation();
			Duration taskDuration = getDuration();
			LocalDateTime taskStartTime = trajToTask.getFinishTime();

			task = new Task(taskLocation, taskStartTime, taskDuration);

			// calculate trajectory from task

			// needs task; sets trajFromTask and evadedFromTask
			status = calculateTrajectoryFromTask();

			if (!status)
				return false;

			// create segments

			segmentToTask = new MovingWorkerUnitObstacle(worker, trajToTask, task);
			segmentAtTask = new OccupiedWorkerUnitObstacle(worker, task);

			// TODO what if the trajFromTask's duration is 0?

			if (segment instanceof MovingWorkerUnitObstacle) {
				Task nextTask = ((MovingWorkerUnitObstacle) segment).getGoal();
				segmentFromTask = new MovingWorkerUnitObstacle(worker, trajFromTask, nextTask);
			} else if (segment instanceof IdlingWorkerUnitObstacle) {
				LocalDateTime taskFinishTime = task.getFinishTime();
				segmentFromTask = new IdlingWorkerUnitObstacle(worker, taskLocation, taskFinishTime);
			} else {
				// TODO reconsider error type
				throw new ClassCastException("unexpected WorkerUnitObstacle");
			}

			// add segments to current dynamic obstacles

			addWorkerUnitObstacle(segmentToTask);
			addWorkerUnitObstacle(segmentAtTask);
			addWorkerUnitObstacle(segmentFromTask);

			return true;
		}

		/**
		 * <p>Calculates the trajectory to the new task.</p>
		 *
		 * <p>This method makes use of side-effects where it isn't always easy
		 * to follow what effects it has on the object. This was done to keep
		 * the code more concise since this is only an inner helper class.</p>
		 *
		 * <p>This methods assumes that {@link #dynamicObstacles}, {@link #toTask},
		 * and {@link #segment} are already set. It sets {@link #evadedToTask}
		 * and {@link #trajToTask} if the path calculation was successful.</p>
		 *
		 * @return {@code true} iff a trajectory to the new task could be calculated.
		 */
		private boolean calculateTrajectoryToTask() {
			MinimumTimeVelocityPathfinder pf = getMinimumTimeVelocityPathfinder();

			pf.setDynamicObstacles  ( dynamicObstacles  );
			pf.setSpatialPath       ( toTask            );
			pf.setMaxSpeed          ( getWorkerUnit().getMaxSpeed()   );
			pf.setStartTime         ( segment.getStartTime() );
			pf.setEarliestFinishTime( getEarliestStartTime() );
			pf.setLatestFinishTime  ( getLatestStartTime()   );
			pf.setBufferDuration    ( getDuration()          );

			boolean status = pf.calculate();

			if (!status)
				return false;

			evadedToTask = onlyWorkerUnitObstacles( pf.getResultEvadedObstacles() );
			trajToTask = pf.getResultTrajectory();

			return true;
		}

		/**
		 * <p>Calculates the trajectory from the new task to the next one.</p>
		 *
		 * <p>This method makes use of side-effects where it isn't always easy
		 * to follow what effects it has on the object. This was done to keep
		 * the code more concise since this is only an inner helper class.</p>
		 *
		 * <p>This methods assumes that {@link #dynamicObstacles}, {@link #toTask},
		 * {@link #task}, and {@link #segment} are already set. It sets
		 * {@link #evadedFromTask} and {@link #trajFromTask} if the path
		 * calculation was successful.</p>
		 *
		 * @return {@code true} iff a trajectory from the new task to the next
		 *         one could be calculated.
		 */
		private boolean calculateTrajectoryFromTask() {
			// TODO IdlingWorkerUnitObstacle cannot evade other workers

			FixTimeVelocityPathfinder pf = getFixTimeVelocityPathfinder();

			pf.setDynamicObstacles( dynamicObstacles   );
			pf.setSpatialPath     ( fromTask           );
			pf.setMaxSpeed        ( getWorkerUnit().getMaxSpeed()    );
			pf.setStartTime       ( task.getFinishTime()    );
			pf.setFinishTime      ( segment.getFinishTime() );

			boolean status = pf.calculate();

			if (!status)
				return false;

			evadedFromTask = onlyWorkerUnitObstacles( pf.getResultEvadedObstacles() );
			trajFromTask = pf.getResultTrajectory();

			return true;
		}

		/*
		 * (non-Javadoc)
		 *
		 * commit needs segment, evadedToTask, evadedFromTask, segmentToTask,
		 * segmentAtTask, segmentFromTask, and task to be set. It expects
		 * that calculate was called before.
		 *
		 * @see tasks.TaskPlanner.Job#commit()
		 */
		@Override
		public void commit() {
			WorkerUnit worker = getWorkerUnit();

			// register evasions
			for (WorkerUnitObstacle e : evadedToTask)
				e.addEvasion(segmentToTask);

			// TODO IdlingWorkerUnitObstacles should also be able to evade
			if (segmentFromTask instanceof MovingWorkerUnitObstacle) {
				for (WorkerUnitObstacle e : evadedFromTask)
					e.addEvasion((MovingWorkerUnitObstacle) segmentFromTask);
			}

			// add obstacle segments and task
			worker.removeObstacleSegment(segment);
			worker.addObstacleSegment(segmentToTask);
			worker.addObstacleSegment(segmentAtTask);
			worker.addObstacleSegment(segmentFromTask);
			worker.addTask(task);
		}

	}

	private class UpdateJob extends Job {

		private final MovingWorkerUnitObstacle segment;

		private MovingWorkerUnitObstacle resultSegment;

		private Collection<WorkerUnitObstacle> resultEvadedWorkers;

		public UpdateJob(MovingWorkerUnitObstacle segment) {
			super(calcMaxDuration(segment));

			this.segment = segment;
		}

		private MovingWorkerUnitObstacle getSegment() {
			return segment;
		}

		private MovingWorkerUnitObstacle getResultSegment() {
			return resultSegment;
		}

		private void setResultSegment(MovingWorkerUnitObstacle resultSegment) {
			this.resultSegment = resultSegment;
		}

		private Collection<WorkerUnitObstacle> getResultEvadedWorkers() {
			return resultEvadedWorkers;
		}

		private void setResultEvadedWorkers(Collection<WorkerUnitObstacle> resultEvadedWorkers) {
			this.resultEvadedWorkers = resultEvadedWorkers;
		}

		@Override
		public double calcLaxity() {
			WorkerUnitObstacle segment = getSegment();
			WorkerUnit worker = segment.getWorkerUnit();
			double maxSpeed = worker.getMaxSpeed();
			double length = segment.getTrajectory().getLength();
			double maxDuration = inSeconds( getMaxDuration() );

			return maxDuration/length - 1./maxSpeed;
		}

		@Override
		public boolean calculate() {
			MovingWorkerUnitObstacle segment = getSegment();
			WorkerUnit worker = segment.getWorkerUnit();
			Collection<DynamicObstacle> dynamicObstacles = buildDynamicObstaclesFor(worker);
			List<Point> spatialPath = segment.getSpatialPathComponent();
			double maxSpeed = worker.getMaxSpeed();
			LocalDateTime startTime = segment.getStartTime();
			LocalDateTime finishTime = segment.getFinishTime();

			FixTimeVelocityPathfinder pf = getFixTimeVelocityPathfinder();

			pf.setDynamicObstacles(dynamicObstacles);
			pf.setSpatialPath(spatialPath);
			pf.setMaxSpeed(maxSpeed);
			pf.setStartTime(startTime);
			pf.setFinishTime(finishTime);

			boolean status = pf.calculate();

			if (!status)
				return false;

			DecomposedTrajectory trajectory = pf.getResultTrajectory();
			Collection<DynamicObstacle> evadedObstacles = pf.getResultEvadedObstacles();
			Collection<WorkerUnitObstacle> evadedWorkers = onlyWorkerUnitObstacles(evadedObstacles);
			Task goal = segment.getGoal();
			MovingWorkerUnitObstacle resultSegment = new MovingWorkerUnitObstacle(worker, trajectory, goal);

			setResultSegment(resultSegment);
			setResultEvadedWorkers(evadedWorkers);
			addWorkerUnitObstacle(resultSegment);

			return true;
		}

		@Override
		public void commit() {
			MovingWorkerUnitObstacle evasion = getSegment();
			WorkerUnit worker = evasion.getWorkerUnit();
			MovingWorkerUnitObstacle resultSegment = getResultSegment();
			Collection<WorkerUnitObstacle> evadedWorkers = getResultEvadedWorkers();

			// register evasions
			for (WorkerUnitObstacle e : evadedWorkers)
				e.addEvasion(resultSegment);

			// update obstacle segment
			worker.removeObstacleSegment(evasion);
			worker.addObstacleSegment(resultSegment);
		}

	}

	private static Duration calcMaxDuration(WorkerUnitObstacle evasion) {
		LocalDateTime startTime = evasion.getStartTime();
		LocalDateTime finishTime = evasion.getFinishTime();

		return Duration.between(startTime, finishTime);
	}

	private static Collection<WorkerUnitObstacle> onlyWorkerUnitObstacles(Collection<DynamicObstacle> obstacles) {
		return obstacles.stream()
			.filter(o -> o instanceof WorkerUnitObstacle)
			.map(o -> (WorkerUnitObstacle) o)
			.collect(toList());
	}

	private static List<MovingWorkerUnitObstacle> buildEvasions(WorkerUnitObstacle obstacleSegment) {
		return obstacleSegment.getEvasions().stream()
			.flatMap(TaskPlanner::buildEvasionsStream)
			.collect(toList());
	}

	private static Stream<MovingWorkerUnitObstacle> buildEvasionsStream(MovingWorkerUnitObstacle obstacleSegment) {
		Stream<MovingWorkerUnitObstacle> self = Stream.of(obstacleSegment);
		Stream<MovingWorkerUnitObstacle> ancestors = obstacleSegment.getEvasions().stream()
			.flatMap(TaskPlanner::buildEvasionsStream);

		return Stream.concat(self, ancestors);
	}

	private Collection<DynamicObstacle> buildWorkerPoolSegments(
		Collection<? extends WorkerUnitObstacle> exclusions,
		WorkerUnitObstacle obsoleteSegment)
	{
		Collection<WorkerUnit> pool = getWorkerPool();

		return pool.stream()
			.flatMap(w -> w.getObstacleSegments().stream())
			.filter(o -> !exclusions.contains(o) && !o.equals(obsoleteSegment))
			.collect(toList());
	}

	private Collection<DynamicObstacle> buildDynamicObstaclesFor(WorkerUnit worker) {
		Collection<DynamicObstacle> dynamicObstacles = getCurrentDynamicObstacles();

		double bufferDistance = worker.getRadius();

		// an exact solution would be to calculate the minkowski sum
		// of each obstacle and the worker's shape

		return dynamicObstacles.stream()
			.filter(o -> !(o instanceof WorkerUnitObstacle)
				|| ((WorkerUnitObstacle) o).getWorkerUnit() != worker)
			.map(o -> o.buffer(bufferDistance))
			.collect(toList());
	}

}
