package tasks;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static util.Comparables.*;
import static util.DurationConv.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import jts.geom.util.GeometriesRequire;
import util.CollectionsRequire;
import world.DecomposedTrajectory;
import world.DynamicObstacle;
import world.IdlingWorkerUnitObstacle;
import world.MovingWorkerUnitObstacle;
import world.OccupiedWorkerUnitObstacle;
import world.SpatialPath;
import world.WorkerUnitObstacle;
import world.WorldPerspective;
import world.WorldPerspectiveCache;
import world.pathfinder.FixTimeVelocityPathfinder;
import world.pathfinder.FixTimeVelocityPathfinderImpl;
import world.pathfinder.MinimumTimeVelocityPathfinder;
import world.pathfinder.MinimumTimeVelocityPathfinderImpl;
import world.pathfinder.SpatialPathfinder;

import com.vividsolutions.jts.geom.Point;

/**
 * <p>The TaskPlanner plans a new {@link Task} into an established set of tasks.
 * It requires multiple parameters which determine the {@link WorkerUnit worker}
 * to execute the new task, and the location, duration, and time interval of the
 * execution. It is also responsible for ensuring that the designated worker is
 * able to reach the task's location with colliding with any other object; be it
 * stationary or another worker.</p>
 *
 * <p>Should it be impossible to plan the new task then the TaskPlanner will
 * not change the current task set. This might be the case when the designated
 * worker is unable to reach the location without violating any time
 * constraints.</p>
 *
 * <p>The planning involves the calculation of a spatial path from the previous
 * location of the worker to the task's location and the successive path to
 * the next location the worker is required to be. The next step is to calculate
 * a velocity profile to evade dynamic obstacles. Since the old path which
 * the worker was previously planned to follow will be obsolete other workers
 * might be affected. Workers which were previously evading the now obsolete
 * path segment should update their affected path segments with a new
 * velocity profile.</p>
 *
 * <p>The TaskPlanner creates a job queue to calculate the new velocity profile
 * for the new spatial paths of the designated worker and all other affected
 * path segments of other workers. The jobs are sorted by the
 * {@link Job#laxity()} to give priority to workers in a hurry.</p>
 *
 * @author Rico Jasper
 */
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
	private Collection<WorkerUnitObstacle> workerObstacles = new LinkedList<>();

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
	 * @throws NullPointerException if worker is null
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
	 * Sets the worker pool. Only workers part of this pool will be regarded as
	 * {@link DynamicObstacle dynamic obstacles}.
	 *
	 * @param workerPool
	 * @throws NullPointerException
	 *             if workerPool is null of contains {@code null}
	 */
	public void setWorkerPool(Collection<WorkerUnit> workerPool) {
		CollectionsRequire.requireContainsNonNull(workerPool, "workerPool");
		
		this.workerPool = unmodifiableCollection( workerPool );
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
	 * @throws NullPointerException if perspectiveCache is null
	 */
	public void setPerspectiveCache(WorldPerspectiveCache perspectiveCache) {
		Objects.requireNonNull(perspectiveCache, "perspectiveCache");

		this.perspectiveCache = perspectiveCache;
	}

	/**
	 * @return the worker obstacles currently of interest. This also includes
	 *         already planned path segments of workers.
	 */
	private Collection<WorkerUnitObstacle> getWorkerObstacles() {
		return workerObstacles;
	}

	/**
	 * Adds a path segment of a worker to the worker obstacles of interests.
	 *
	 * @param segment
	 */
	private void addWorkerUnitObstacle(WorkerUnitObstacle segment) {
		workerObstacles.add(segment);
	}

	/**
	 * Adds multiple worker obstacles to the ones of interest.
	 *
	 * @param segments
	 */
	private void addAllWorkerObstacles(Collection<WorkerUnitObstacle> segments) {
		workerObstacles.addAll(segments);
	}

	/**
	 * Clears the collection of dynamic obstacles of interests.
	 */
	private void clearCurrentDynamicObstacles() {
		workerObstacles.clear();
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
		GeometriesRequire.requireValid2DPoint(location, "location");
		
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
		this.earliestStartTime = Objects.requireNonNull(earliestStartTime, "earliestStartTime");
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
		this.latestStartTime = Objects.requireNonNull(latestStartTime, "latestStartTime");
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
		this.duration = Objects.requireNonNull(duration, "duration");
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
	 * Checks if all parameters are properly set. Throws an exception otherwise.
	 * 
	 * <p>
	 * The following parameters are to be set by their respective setters:
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
	 * @throws IllegalStateException
	 *             if any parameter is not set or if {@code earliestStartTime}
	 *             is after {@code latestStartTime}.
	 */
	private void checkParameters() {
		// assert all parameters set
		if (workerUnit        == null ||
			workerPool        == null ||
			perspectiveCache  == null ||
			location          == null ||
			earliestStartTime == null ||
			latestStartTime   == null ||
			duration          == null)
		{
			throw new IllegalStateException("some parameters are not set");
		}
		
		// assert earliest <= latest 
		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalStateException("earliestStartTime is after latestStartTime");
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
	 * @return {@code true} if the task has been successfully planned.
	 */
	public boolean plan() {
		checkParameters();
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
	 * @return {@code true} if the task has been successfully planned.
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
		SpatialPath toTask = calculateSpatialPath(segmentStartLocation, taskLocation);
		if (toTask == null)
			return false;
		// calculate the path from the new task
		SpatialPath fromTask = calculateSpatialPath(taskLocation, segmentFinishLocation);
		if (fromTask == null)
			return false;

		// determine the path segments to be recalculated
		Collection<MovingWorkerUnitObstacle> evasions = buildEvasions(segment);

		// prepare worker obstacles
		addAllWorkerObstacles( buildWorkerPoolSegments(evasions, segment) );

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
	 * @return {@code true} if a path connecting both locations was found.
	 */
	private SpatialPath calculateSpatialPath(Point startLocation, Point finishLocation) {
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
		 * The duration available to complete a path.
		 */
		private final Duration jobDuration;

		/**
		 * Cached laxity value.
		 */
		private double laxity;

		/**
		 * Constructs a Job with a duration.
		 *
		 * @param jobDuration
		 */
		public Job(Duration jobDuration) {
			this.jobDuration = jobDuration;
		}

		/**
		 * @return the maximum duration available to complete a path.
		 */
		public Duration getJobDuration() {
			return jobDuration;
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
		 * @return {@code true} if valid path segments could be calculated.
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

		// Since I am using side-effects to be more concise here,
		// I ordered the fields in the order they are assigned during
		// the lifespan of this object to make life a bit easier for the reader.
		//
		// Conciseness was also the reason to omit any getter or setters.

		// The first three fields are set by the constructor.

		/**
		 * The spatial path to the new task.
		 */
		private final SpatialPath toTask;

		/**
		 * The spatial path from the new task to the next one.
		 */
		private final SpatialPath fromTask;

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
		 * The evaded path segments to the new task.
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
		 * The evaded path segments from the new task to the next one.
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
		public CreateJob(SpatialPath toTask, SpatialPath fromTask, WorkerUnitObstacle segment) {
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
			double length = toTask.length() + fromTask.length();
			double taskDuration = inSeconds( getDuration() );
			double maxDuration = inSeconds( getJobDuration() );

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

			if (segment instanceof MovingWorkerUnitObstacle) {
				Task nextTask = ((MovingWorkerUnitObstacle) segment).getGoal();
				
				// don't introduce trajectories without duration
				segmentFromTask = trajFromTask.getDuration().isZero()
					? null
					: new MovingWorkerUnitObstacle(worker, trajFromTask, nextTask);
			} else if (segment instanceof IdlingWorkerUnitObstacle) {
				LocalDateTime taskFinishTime = task.getFinishTime();
				segmentFromTask = new IdlingWorkerUnitObstacle(worker, taskLocation, taskFinishTime);
			} else {
				throw new RuntimeException("unexpected WorkerUnitObstacle");
			}

			// add segments to current dynamic obstacles

			addWorkerUnitObstacle(segmentToTask);
			addWorkerUnitObstacle(segmentAtTask);
			// there might not be a successive trajectory
			if (segmentFromTask != null)
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
		 * @return {@code true} if a trajectory to the new task could be calculated.
		 */
		private boolean calculateTrajectoryToTask() {
			MinimumTimeVelocityPathfinder pf = getMinimumTimeVelocityPathfinder();

			pf.setDynamicObstacles  ( dynamicObstacles       );
			pf.setSpatialPath       ( toTask                 );
			pf.setMaxSpeed          ( getWorkerUnit().getMaxSpeed() );
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
		 * @return {@code true} if a trajectory from the new task to the next
		 *         one could be calculated.
		 */
		private boolean calculateTrajectoryFromTask() {
			FixTimeVelocityPathfinder pf = getFixTimeVelocityPathfinder();

			pf.setDynamicObstacles( dynamicObstacles        );
			pf.setSpatialPath     ( fromTask                );
			pf.setMaxSpeed        ( getWorkerUnit().getMaxSpeed() );
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
			// register evasions
			for (WorkerUnitObstacle e : evadedToTask)
				e.addEvasion(segmentToTask);

			if (segmentFromTask instanceof MovingWorkerUnitObstacle) {
				for (WorkerUnitObstacle e : evadedFromTask)
					e.addEvasion((MovingWorkerUnitObstacle) segmentFromTask);
			}

			// add obstacle segments and task
			WorkerUnit worker = getWorkerUnit();
			worker.removeObstacleSegment(segment);
			worker.addObstacleSegment(segmentToTask);
			worker.addObstacleSegment(segmentAtTask);
			// there might not be a successive trajectory
			if (segmentFromTask != null)
				worker.addObstacleSegment(segmentFromTask);
			worker.addTask(task);
		}

	}

	/**
	 * An UpdateJob recalculates the existing velocity profile of a path
	 * segment of a worker directly or indirectly affected by the new spatial
	 * path of the current worker.
	 */
	private class UpdateJob extends Job {

		/**
		 * The path segment to be updated.
		 */
		private final MovingWorkerUnitObstacle segment;

		// The next two fields are set by calculate.

		/**
		 * The resulting updated path segment.
		 */
		private MovingWorkerUnitObstacle updatedSegment;

		/**
		 * Evaded obstacles by the updated path segment.
		 */
		private Collection<WorkerUnitObstacle> evaded;

		/**
		 * Constructs a UpdateJob which updates the velocity profile of the
		 * given path segment.
		 *
		 * @param segment
		 */
		public UpdateJob(MovingWorkerUnitObstacle segment) {
			// doesn't check inputs since class is private

			super(Duration.between(segment.getStartTime(), segment.getFinishTime()));

			this.segment = segment;
		}

		@Override
		public double calcLaxity() {
			WorkerUnit worker = segment.getWorkerUnit();
			double maxSpeed = worker.getMaxSpeed();
			double length = segment.getTrajectory().getLength();
			double maxDuration = inSeconds( getJobDuration() );

			return maxDuration/length - 1./maxSpeed;
		}

		/*
		 * (non-Javadoc)
		 *
		 * Sets evaded and updatedSegment if path could be found.
		 *
		 * @see tasks.TaskPlanner.Job#calculate()
		 */
		@Override
		public boolean calculate() {
			WorkerUnit worker = segment.getWorkerUnit();
			FixTimeVelocityPathfinder pf = getFixTimeVelocityPathfinder();

			pf.setDynamicObstacles( buildDynamicObstaclesFor(worker)  );
			pf.setSpatialPath     ( segment.getSpatialPathComponent() );
			pf.setMaxSpeed        ( worker.getMaxSpeed()              );
			pf.setStartTime       ( segment.getStartTime()            );
			pf.setFinishTime      ( segment.getFinishTime()           );

			boolean status = pf.calculate();

			if (!status)
				return false;

			evaded = onlyWorkerUnitObstacles( pf.getResultEvadedObstacles() );
			updatedSegment = new MovingWorkerUnitObstacle(
				worker, pf.getResultTrajectory(), segment.getGoal());

			addWorkerUnitObstacle(updatedSegment);

			return true;
		}

		/*
		 * (non-Javadoc)
		 *
		 * Uses all fields. Expects that calculate was already called and successful.
		 *
		 * @see tasks.TaskPlanner.Job#commit()
		 */
		@Override
		public void commit() {
			// register evasions
			for (WorkerUnitObstacle e : evaded)
				e.addEvasion(updatedSegment);

			// update obstacle segment
			WorkerUnit worker = segment.getWorkerUnit();
			worker.removeObstacleSegment(segment);
			worker.addObstacleSegment(updatedSegment);
		}

	}

	/**
	 * Filters a given list of dynamic obstacles. Only accepts
	 * {@code WorkerUnitObstacle}s.
	 *
	 * @param obstacles
	 * @return
	 */
	private static Collection<WorkerUnitObstacle> onlyWorkerUnitObstacles(Collection<DynamicObstacle> obstacles) {
		return obstacles.stream()
			.filter(o -> o instanceof WorkerUnitObstacle)
			.map(o -> (WorkerUnitObstacle) o)
			.collect(toList());
	}

	/**
	 * Builds a collection of all potentially affected path segments when
	 * removing the original path segment of the current worker.
	 *
	 * @param obstacleSegment the original path segment to be removed
	 * @return the potentially affected path segments
	 */
	private static Collection<MovingWorkerUnitObstacle> buildEvasions(WorkerUnitObstacle obstacleSegment) {
		return obstacleSegment.getEvasions().stream()
			.flatMap(TaskPlanner::buildEvasionsStream)
			.collect(toList());
	}

	/**
	 * Helps building a collection of affected evasions. Builds a stream
	 * all ancestor evasions of the given segment and the segment itself.
	 *
	 * @param obstacleSegment
	 * @return
	 * @see #buildEvasions(WorkerUnitObstacle)
	 */
	private static Stream<MovingWorkerUnitObstacle> buildEvasionsStream(MovingWorkerUnitObstacle obstacleSegment) {
		Stream<MovingWorkerUnitObstacle> self = Stream.of(obstacleSegment);
		Stream<MovingWorkerUnitObstacle> ancestors = obstacleSegment.getEvasions().stream()
			.flatMap(TaskPlanner::buildEvasionsStream);

		return Stream.concat(self, ancestors);
	}

	/**
	 * Builds a collection of worker obstacles using the {@link #workerPool}.
	 * Exculdes all path segments given by exclusions and the obsolete segment
	 * to be removed.
	 *
	 * @param exclusions
	 * @param obsoleteSegment
	 * @return
	 */
	private Collection<WorkerUnitObstacle> buildWorkerPoolSegments(
		Collection<? extends WorkerUnitObstacle> exclusions,
		WorkerUnitObstacle obsoleteSegment)
	{
		Collection<WorkerUnit> pool = getWorkerPool();

		return pool.stream()
			.flatMap(w -> w.getObstacleSegments().stream())
			.filter(o -> !exclusions.contains(o) && !o.equals(obsoleteSegment))
			.collect(toList());
	}

	/**
	 * Builds the the given worker's perspective on the dynamic obstacles.
	 * It buffers all {@link #workerObstacles obstacles of interests}
	 * by the worker's radius and excludes the path segments of the worker
	 * itself.
	 *
	 * @param worker to build the perspective for
	 * @return a collection of dynamic obstacles in the worker's perspective.
	 */
	private Collection<DynamicObstacle> buildDynamicObstaclesFor(WorkerUnit worker) {
		double bufferDistance = worker.getRadius();

		// an exact solution would be to calculate the minkowski sum
		// of each obstacle and the worker's shape
		
		Stream<DynamicObstacle> worldObstacles = getPerspectiveCache()
			.getPerspectiveFor(worker)
			.getView()
			.getDynamicObstacles()
			.stream();
		
		Stream<DynamicObstacle> workerObstacles = getWorkerObstacles().stream()
			.filter(o -> !(o instanceof WorkerUnitObstacle)
				|| o.getWorkerUnit() != worker)
			.map(o -> o.buffer(bufferDistance));

		return Stream.concat(worldObstacles, workerObstacles)
			.collect(toList());
	}

}
