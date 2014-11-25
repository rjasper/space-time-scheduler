package tasks;

import static java.util.stream.Collectors.toList;
import static util.DurationConv.inSeconds;
import static util.PathOperations.length;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import world.DecomposedTrajectory;
import world.DynamicObstacle;
import world.IdlingWorkerUnitObstacle;
import world.MovingWorkerUnitObstacle;
import world.OccupiedWorkerUnitObstacle;
import world.WorkerUnitObstacle;
import world.pathfinder.FixTimeVelocityPathfinder;
import world.pathfinder.FixTimeVelocityPathfinderImpl;
import world.pathfinder.MinimumTimeVelocityPathfinder;
import world.pathfinder.MinimumTimeVelocityPathfinderImpl;
import world.pathfinder.SpatialPathfinder;
import world.pathfinder.StraightEdgePathfinder;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class TaskPlanner {

	private WorkerUnit workerUnit = null;

	private Collection<WorkerUnit> workerPool = null;

	private Collection<Polygon> staticObstacles = null;

	private Collection<DynamicObstacle> dynamicObstacles = null;

	private Collection<DynamicObstacle> currentDynamicObstacles = new LinkedList<>();

	private Point location = null;

	private LocalDateTime earliestStartTime = null;

	private LocalDateTime latestStartTime = null;

	private Duration duration = null;

//	private Task resultTask = null;

//	private DecomposedTrajectory resultToTask;

//	private DecomposedTrajectory resultFromTask;

	private SpatialPathfinder spatialPathfinder = new StraightEdgePathfinder();

	private FixTimeVelocityPathfinder fixTimeVelocityPathfinder = new FixTimeVelocityPathfinderImpl();

	private MinimumTimeVelocityPathfinder minimumTimeVelocityPathfinder = new MinimumTimeVelocityPathfinderImpl();

	public boolean isReady() {
		return workerUnit        != null
			&& workerPool        != null
			&& staticObstacles   != null
			&& dynamicObstacles  != null
			&& location          != null
			&& earliestStartTime != null
			&& latestStartTime   != null
			&& duration          != null;
	}

	// TODO check setter args

	private WorkerUnit getWorkerUnit() {
		return workerUnit;
	}

	public SpatialPathfinder getSpatialPathfinder() {
		return spatialPathfinder;
	}

	public void setWorkerUnit(WorkerUnit worker) {
		this.workerUnit = worker;
	}

	private Collection<WorkerUnit> getWorkerPool() {
		return workerPool;
	}

	public void setWorkerPool(Collection<WorkerUnit> workerPool) {
		this.workerPool = new ArrayList<>(workerPool);
	}

	private Collection<Polygon> getStaticObstacles() {
		return staticObstacles;
	}

	public void setStaticObstacles(Collection<Polygon> staticObstacles) {
		this.staticObstacles = new ArrayList<>(staticObstacles);
	}

	private Collection<DynamicObstacle> getDynamicObstacles() {
		return dynamicObstacles;
	}

	public void setDynamicObstacles(Collection<DynamicObstacle> dynamicObstacles) {
		this.dynamicObstacles = dynamicObstacles;
	}

	private Collection<DynamicObstacle> getCurrentDynamicObstacles() {
		return currentDynamicObstacles;
	}

	private void clearCurrentDynamicObstacles() {
		currentDynamicObstacles.clear();
	}

	private void addWorkerUnitObstacle(WorkerUnitObstacle segment) {
		currentDynamicObstacles.add(segment);
	}

	private void addAllDynamicObstacles(Collection<DynamicObstacle> segments) {
		currentDynamicObstacles.addAll(segments);
	}

	private Point getLocation() {
		return location;
	}

	public void setLocation(Point location) {
		this.location = location;
	}

	private LocalDateTime getEarliestStartTime() {
		return earliestStartTime;
	}

	public void setEarliestStartTime(LocalDateTime earliestStartTime) {
		this.earliestStartTime = earliestStartTime;
	}

	private LocalDateTime getLatestStartTime() {
		return latestStartTime;
	}

	public void setLatestStartTime(LocalDateTime latestStartTime) {
		this.latestStartTime = latestStartTime;
	}

	private Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

//	public Task getResultTask() {
//		return resultTask;
//	}

//	private void setResultTask(Task resultTask) {
//		this.resultTask = resultTask;
//	}

//	public DecomposedTrajectory getResultToTask() {
//		return resultToTask;
//	}

//	private void setResultToTask(DecomposedTrajectory resultToTask) {
//		this.resultToTask = resultToTask;
//	}

//	public DecomposedTrajectory getResultFromTask() {
//		return resultFromTask;
//	}

//	private void setResultFromTask(DecomposedTrajectory resultFromTask) {
//		this.resultFromTask = resultFromTask;
//	}

//	public boolean plan() {
//		if (!isReady())
//			throw new IllegalStateException("not ready yet");
//
//		WorkerUnit worker = getWorkerUnit();
//		double maxSpeed = worker.getMaxSpeed();
//		Collection<Polygon> staticObstacles = getStaticObstacles();
//		Collection<DynamicObstacle> dynamicObstacles = buildDynamicObstacles();
//		LocalDateTime earliestStartTime = getEarliestStartTime();
//		LocalDateTime latestStartTime = getLatestStartTime();
//		Duration duration = getDuration();
//		Point location = getLocation();
//
//		MinimumTimePathfinder mtpf = new JavaMinimumTimePathfinder();
//
//		mtpf.setStaticObstacles(staticObstacles);
//		mtpf.setDynamicObstacles(dynamicObstacles);
//
//		Task pred = worker.getFloorTask(earliestStartTime);
//		Task succ = worker.getCeilingTask(earliestStartTime);
//
//		// trajectory to new task
//
//		LocalDateTime startTime;
//		Point startLocation;
//
//		// if there is no predecessor use initial position and time
//		if (pred == null) {
//			startTime = worker.getInitialTime();
//			startLocation = worker.getInitialLocation();
//		} else {
//			startTime = pred.getFinishTime();
//			startLocation = pred.getLocation();
//		}
//
//		mtpf.setStartPoint(startLocation);
//		mtpf.setFinishPoint(location);
//		mtpf.setStartTime(startTime);
//		mtpf.setEarliestFinishTime(earliestStartTime);
//		mtpf.setLatestFinishTime(latestStartTime);
//		mtpf.setBufferDuration(duration);
//		mtpf.setMaxSpeed(maxSpeed);
//
//		boolean status = mtpf.calculatePath();
//
//		if (!status)
//			return false;
//
//		Trajectory toTask = mtpf.getResultTrajectory();
//
//		LocalDateTime taskStartTime = toTask.getLastTime();
//		LocalDateTime taskFinishTime = taskStartTime.plus(duration);
//
//		// trajectory to following task
//
//		Trajectory fromTask;
//		if (succ != null) {
//			FixTimePathfinder stpf = new JavaFixTimePathfinder();
//
//			stpf.setStartPoint(location);
//			stpf.setFinishPoint(succ.getLocation());
//			stpf.setStartTime(taskFinishTime);
//			stpf.setFinishTime(succ.getStartTime());
//			stpf.setMaxSpeed(maxSpeed);
//
//			status = stpf.calculatePath();
//
//			if (!status)
//				return false;
//
//			fromTask = stpf.getResultTrajectory();
//		} else {
//			fromTask = null;
//		}
//
//		Task task = new Task(location, taskStartTime, taskFinishTime);
//
//		setResultTask(task);
//		setResultToTask(toTask);
//		setResultFromTask(fromTask);
//
//		return true;
//	}

	private FixTimeVelocityPathfinder getFixTimeVelocityPathfinder() {
		return fixTimeVelocityPathfinder;
	}

	private MinimumTimeVelocityPathfinder getMinimumTimeVelocityPathfinder() {
		return minimumTimeVelocityPathfinder;
	}

	public boolean plan() {
		boolean status = planImpl();

		clearCurrentDynamicObstacles();

		return status;
	}

	private boolean planImpl() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");

		WorkerUnit worker = getWorkerUnit();
		WorkerUnitObstacle segment = worker.getObstacleSegment( getEarliestStartTime() );

		prepareSpatialPathfinder();

		Point taskLocation = getLocation();
		Point segmentStartLocation = segment.getStartLocation();
		Point segmentFinishLocation = segment.getFinishLocation();

		List<Point> toTask = calculateSpatialPath(segmentStartLocation, taskLocation);
		if (toTask == null)
			return false;
		List<Point> fromTask = calculateSpatialPath(taskLocation, segmentFinishLocation);
		if (fromTask == null)
			return false;

		List<MovingWorkerUnitObstacle> evasions = buildEvasions(segment);

		// prepare current dynamic obstacles
		addAllDynamicObstacles( getDynamicObstacles() );
		addAllDynamicObstacles( buildWorkerPoolSegments(evasions) );

		// make jobs
		Stream<Job> createJob = Stream.of(new CreateJob(toTask, fromTask, segment));
		Stream<Job> updateJobs = evasions.stream().map(UpdateJob::new);

		// sort jobs
		List<Job> jobs = Stream.concat(createJob, updateJobs)
			.sorted()
			.collect(toList());

		// execute jobs or fail
		for (Job j : jobs) {
			boolean status = j.calculate();

			if (!status)
				return false;
		}

		// update obstacles
		for (Job j : jobs)
			j.execute();

		return true;
	}

	private void prepareSpatialPathfinder() {
		SpatialPathfinder pf = getSpatialPathfinder();

		pf.setStaticObstacles( getStaticObstacles() );
	}

	private List<Point> calculateSpatialPath(Point startLocation, Point finishLocation) {
		SpatialPathfinder pf = getSpatialPathfinder();

		pf.setStartLocation(startLocation);
		pf.setFinishLocation(finishLocation);

		boolean status = pf.calculate();

		if (!status)
			return null;

		return pf.getResultSpatialPath();
	}

	private static abstract class Job implements Comparable<Job> {

		private final Duration maxDuration;

		private transient double laxity = Double.NaN;

		public Job(Duration maxDuration) {
			this.maxDuration = maxDuration;
		}

		public Duration getMaxDuration() {
			return maxDuration;
		}

		// the time allowed to stop per length unit
		public double laxity() {
			if (Double.isNaN(laxity))
				laxity = calcLaxity();

			return laxity;
		};

		public abstract double calcLaxity();

		public abstract boolean calculate();

		public abstract void execute();

		@Override
		public int compareTo(Job o) {
			// the more laxity the less favored
			return Double.compare(laxity(), o.laxity());
		}

	}

	private class CreateJob extends Job {

		private final List<Point> toTask;

		private final List<Point> fromTask;

		private final WorkerUnitObstacle segment;

		private Collection<DynamicObstacle> dynamicObstacles;

		private Task resultTask;

		private List<WorkerUnitObstacle> resultEvadedWorkersToTask;

		private List<WorkerUnitObstacle> resultEvadedWorkersFromTask;

		private DecomposedTrajectory resultTrajectoryToTask;

		private DecomposedTrajectory resultTrajectoryFromTask;

		private MovingWorkerUnitObstacle resultSegmentToTask;

		private OccupiedWorkerUnitObstacle resultSegmentAtTask;

		private WorkerUnitObstacle resultSegmentFromTask;

		public CreateJob(List<Point> toTask, List<Point> fromTask, WorkerUnitObstacle segment) {
			super(segment.getDuration());

			this.toTask = toTask;
			this.fromTask = fromTask;
			this.segment = segment;
		}

		private List<Point> getToTask() {
			return toTask;
		}

		private List<Point> getFromTask() {
			return fromTask;
		}

		private WorkerUnitObstacle getSegment() {
			return segment;
		}

		private Collection<DynamicObstacle> getDynamicObstacles() {
			return dynamicObstacles;
		}

		private void setDynamicObstacles(Collection<DynamicObstacle> dynamicObstacles) {
			this.dynamicObstacles = dynamicObstacles;
		}

		private Task getResultTask() {
			return resultTask;
		}

		private void setResultTask(Task resultTask) {
			this.resultTask = resultTask;
		}

		private List<WorkerUnitObstacle> getResultEvadedWorkersToTask() {
			return resultEvadedWorkersToTask;
		}

		private void setResultEvadedWorkersToTask(List<WorkerUnitObstacle> resultEvadedObstaclesToTask) {
			this.resultEvadedWorkersToTask = resultEvadedObstaclesToTask;
		}

		private List<WorkerUnitObstacle> getResultEvadedWorkersFromTask() {
			return resultEvadedWorkersFromTask;
		}

		private void setResultEvadedWorkersFromTask(List<WorkerUnitObstacle> resultEvadedObstaclesFromTask) {
			this.resultEvadedWorkersFromTask = resultEvadedObstaclesFromTask;
		}

		private DecomposedTrajectory getResultTrajectoryToTask() {
			return resultTrajectoryToTask;
		}

		private void setResultTrajectoryToTask(DecomposedTrajectory resultTrajectoryToTask) {
			this.resultTrajectoryToTask = resultTrajectoryToTask;
		}

		private DecomposedTrajectory getResultTrajectoryFromTask() {
			return resultTrajectoryFromTask;
		}

		private void setResultTrajectoryFromTask(DecomposedTrajectory resultTrajectoryFromTask) {
			this.resultTrajectoryFromTask = resultTrajectoryFromTask;
		}

		private MovingWorkerUnitObstacle getResultSegmentToTask() {
			return resultSegmentToTask;
		}

		private void setResultSegmentToTask(MovingWorkerUnitObstacle resultSegmentToTask) {
			this.resultSegmentToTask = resultSegmentToTask;
		}

		private OccupiedWorkerUnitObstacle getResultSegmentAtTask() {
			return resultSegmentAtTask;
		}

		private void setResultSegmentAtTask(OccupiedWorkerUnitObstacle resultSegmentAtTask) {
			this.resultSegmentAtTask = resultSegmentAtTask;
		}

		private WorkerUnitObstacle getResultSegmentFromTask() {
			return resultSegmentFromTask;
		}

		private void setResultSegmentFromTask(WorkerUnitObstacle resultSegmentFromTask) {
			this.resultSegmentFromTask = resultSegmentFromTask;
		}

		@Override
		public double calcLaxity() {
			WorkerUnit worker = getWorkerUnit();
			double maxSpeed = worker.getMaxSpeed();
			double length = length( getToTask() ) + length( getFromTask() );
			double taskDuration = inSeconds( getDuration() );
			double maxDuration = inSeconds( getMaxDuration() );

			return (maxDuration - taskDuration)/length - 1./maxSpeed;
		}

		@Override
		public boolean calculate() {
			boolean status;

			WorkerUnit worker = getWorkerUnit();
			setDynamicObstacles( buildDynamicObstaclesFor(worker) );

			status = calculateTrajectoryToTask();

			if (!status)
				return false;

			Point taskLocation = getLocation();
			Duration taskDuration = getDuration();
			DecomposedTrajectory trajToTask = getResultTrajectoryToTask();
			LocalDateTime taskStartTime = trajToTask.getFinishTime();
			Task task = new Task(taskLocation, taskStartTime, taskDuration);
			MovingWorkerUnitObstacle segmentToTask = new MovingWorkerUnitObstacle(worker, trajToTask, task);

			setResultTask(task);
			setResultSegmentToTask(segmentToTask);
			addWorkerUnitObstacle(segmentToTask);

			status = calculateTrajectoryFromTask();

			if (!status)
				return false;

			WorkerUnitObstacle segment = getSegment();
			OccupiedWorkerUnitObstacle segmentAtTask = new OccupiedWorkerUnitObstacle(worker, task);
			DecomposedTrajectory trajFromTask = getResultTrajectoryFromTask();

			WorkerUnitObstacle segmentFromTask;
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

			setResultSegmentAtTask(segmentAtTask);
			setResultSegmentFromTask(segmentFromTask);
			addWorkerUnitObstacle(segmentAtTask);
			addWorkerUnitObstacle(segmentFromTask);

			return true;
		}

		private boolean calculateTrajectoryToTask() {
			MinimumTimeVelocityPathfinder pf = getMinimumTimeVelocityPathfinder();

			WorkerUnit worker = getWorkerUnit();
			WorkerUnitObstacle segment = getSegment();

			pf.setDynamicObstacles  ( getDynamicObstacles()  );
			pf.setSpatialPath       ( getToTask()            );
			pf.setMaxSpeed          ( worker.getMaxSpeed()   );
			pf.setStartTime         ( segment.getStartTime() );
			pf.setEarliestFinishTime( getEarliestStartTime() );
			pf.setLatestFinishTime  ( getLatestStartTime()   );
			pf.setBufferDuration    ( getDuration()          );

			boolean status = pf.calculate();

			if (!status)
				return false;

			List<DynamicObstacle> evadedObstacles = pf.getResultEvadedObstacles();
			List<WorkerUnitObstacle> evadedWorkers = onlyWorkerUnitObstacles(evadedObstacles);
			DecomposedTrajectory trajToTask = pf.getResultTrajectory();

			setResultEvadedWorkersToTask(evadedWorkers);
			setResultTrajectoryToTask(trajToTask);

			return true;
		}

		private boolean calculateTrajectoryFromTask() {
			// TODO IdlingWorkerUnitObstacle cannot evade other workers

			FixTimeVelocityPathfinder pf = getFixTimeVelocityPathfinder();

			WorkerUnit worker = getWorkerUnit();
			WorkerUnitObstacle segment = getSegment();
			Task task = getResultTask();

			pf.setDynamicObstacles( getDynamicObstacles()   );
			pf.setSpatialPath     ( getFromTask()           );
			pf.setMaxSpeed        ( worker.getMaxSpeed()    );
			pf.setStartTime       ( task.getFinishTime()    );
			pf.setFinishTime      ( segment.getFinishTime() );

			boolean status = pf.calculate();

			if (!status)
				return false;

			List<DynamicObstacle> evadedObstacles = pf.getResultEvadedObstacles();
			List<WorkerUnitObstacle> evadedWorkers = onlyWorkerUnitObstacles(evadedObstacles);
			DecomposedTrajectory trajFromTask = pf.getResultTrajectory();

			setResultEvadedWorkersFromTask(evadedWorkers);
			setResultTrajectoryFromTask(trajFromTask);

			return true;
		}

		@Override
		public void execute() {
			WorkerUnit worker = getWorkerUnit();
			Task task = getResultTask();

			MovingWorkerUnitObstacle segmentToTask = getResultSegmentToTask();
			WorkerUnitObstacle segmentAtTask = getResultSegmentAtTask();
			WorkerUnitObstacle segmentFromTask = getResultSegmentFromTask();

			// register evasions
			for (WorkerUnitObstacle e : getResultEvadedWorkersToTask())
				e.addEvasion(segmentToTask);

			// TODO IdlingWorkerUnitObstacles should also be able to evade
			if (segmentFromTask instanceof MovingWorkerUnitObstacle) {
				for (WorkerUnitObstacle e : getResultEvadedWorkersFromTask())
					e.addEvasion((MovingWorkerUnitObstacle) segmentFromTask);
			}

			// add obstacle segments and task
			worker.removeObstacleSegment(getSegment());
			worker.addObstacleSegment(segmentToTask);
			worker.addObstacleSegment(segmentAtTask);
			worker.addObstacleSegment(segmentFromTask);
			worker.addTask(task);
		}

	}

	private class UpdateJob extends Job {

		private final MovingWorkerUnitObstacle segment;

		private MovingWorkerUnitObstacle resultSegment;

		private List<WorkerUnitObstacle> resultEvadedWorkers;

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

		private List<WorkerUnitObstacle> getResultEvadedWorkers() {
			return resultEvadedWorkers;
		}

		private void setResultEvadedWorkers(List<WorkerUnitObstacle> resultEvadedWorkers) {
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
			List<Point> spatialPath = segment.getSpatialPath();
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
			List<DynamicObstacle> evadedObstacles = pf.getResultEvadedObstacles();
			List<WorkerUnitObstacle> evadedWorkers = onlyWorkerUnitObstacles(evadedObstacles);
			Task goal = segment.getGoal();
			MovingWorkerUnitObstacle resultSegment = new MovingWorkerUnitObstacle(worker, trajectory, goal);

			setResultSegment(resultSegment);
			setResultEvadedWorkers(evadedWorkers);
			addWorkerUnitObstacle(resultSegment);

			return true;
		}

		@Override
		public void execute() {
			MovingWorkerUnitObstacle evasion = getSegment();
			WorkerUnit worker = evasion.getWorkerUnit();
			MovingWorkerUnitObstacle resultSegment = getResultSegment();
			List<WorkerUnitObstacle> evadedWorkers = getResultEvadedWorkers();

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

	private static List<WorkerUnitObstacle> onlyWorkerUnitObstacles(List<DynamicObstacle> obstacles) {
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

	private Collection<DynamicObstacle> buildWorkerPoolSegments(Collection<? extends WorkerUnitObstacle> exclusions) {
		Collection<WorkerUnit> pool = getWorkerPool();

		return pool.stream()
			.flatMap(w -> w.getObstacleSegments().stream())
			.filter(o -> !exclusions.contains(o))
			.collect(toList());
	}

	private Collection<DynamicObstacle> buildDynamicObstaclesFor(WorkerUnit worker) {
		Collection<DynamicObstacle> dynamicObstacles = getCurrentDynamicObstacles();

		// might overestimate largest diameter
		double bufferDistance = 2.0*worker.getRadius();

		// an exact solution would be to calculate the minkowski sum
		// of each obstacle and the worker's shape

		return dynamicObstacles.stream()
			.filter(o -> !(o instanceof WorkerUnitObstacle)
				|| ((WorkerUnitObstacle) o).getWorkerUnit() != worker)
			.map(o -> o.buffer(bufferDistance))
			.collect(toList());
	}

}
