package tasks;

import static java.util.stream.Collectors.toList;
import static util.DurationConv.inSeconds;
import static util.PathOperations.length;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import world.DecomposedTrajectory;
import world.DynamicObstacle;
import world.DynamicWorldBuilder;
import world.Trajectory;
import world.WorkerUnitObstacle;
import world.pathfinder.FixTimeVelocityPathfinder;
import world.pathfinder.MinimumTimeVelocityPathfinder;
import world.pathfinder.SpatialPathfinder;
import world.pathfinder.StraightEdgePathfinder;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


public class TaskPlanner {

	private SpatialPathfinder spatialPathfinder = new StraightEdgePathfinder();

	private WorkerUnit workerUnit = null;

	private Collection<WorkerUnit> workerPool = null;

	private Collection<Polygon> staticObstacles = null;

	private Point location = null;

	private LocalDateTime earliestStartTime = null;

	private LocalDateTime latestStartTime = null;

	private Duration duration = null;

	private Task resultTask = null;

	private DecomposedTrajectory resultToTask;

	private DecomposedTrajectory resultFromTask;

	public boolean isReady() {
		return workerUnit != null
			&& workerPool != null
			&& staticObstacles != null
			&& location != null
			&& earliestStartTime != null
			&& latestStartTime != null
			&& duration != null;
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

	public Task getResultTask() {
		return resultTask;
	}

	private void setResultTask(Task resultTask) {
		this.resultTask = resultTask;
	}

	public DecomposedTrajectory getResultToTask() {
		return resultToTask;
	}

	private void setResultToTask(DecomposedTrajectory resultToTask) {
		this.resultToTask = resultToTask;
	}

	public DecomposedTrajectory getResultFromTask() {
		return resultFromTask;
	}

	private void setResultFromTask(DecomposedTrajectory resultFromTask) {
		this.resultFromTask = resultFromTask;
	}

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

	public boolean plan() {
		if (!isReady())
			throw new IllegalStateException("not ready yet");

		WorkerUnit worker = getWorkerUnit();
		WorkerUnitObstacle obstacleSegment =
			worker.getObstacleSegment( getEarliestStartTime() );

		prepareSpatialPathfinder();

		Point taskLocation = getLocation();
		Point segmentStartLocation = obstacleSegment.getStartLocation();
		Point segmentFinishLocation = obstacleSegment.getFinishLocation();
		LocalDateTime segmentStartTime = obstacleSegment.getStartTime();
		LocalDateTime segmentFinishTime = obstacleSegment.getFinishTime();

		Duration maxDuration = Duration.between(segmentStartTime, segmentFinishTime);

		List<Point> toTask = calculateSpatialPath(segmentStartLocation, taskLocation);
		List<Point> fromTask = calculateSpatialPath(taskLocation, segmentFinishLocation);

		List<WorkerUnitObstacle> evasions = buildEvasions(obstacleSegment);

		// make jobs
		Stream<Job> createJob = Stream.of(new CreateJob(toTask, fromTask, obstacleSegment));
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

//	private Job makeJob(List<Point> spatialPath) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	private Job makeJob(WorkerUnitObstacle evasion) {
//		// TODO implement
//
//		return null;
//	}

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

		private double laxity = Double.NaN;

		private final List<Point> toTask;

		private final List<Point> fromTask;

		private final WorkerUnitObstacle segment;

		private Collection<DynamicObstacle> dynamicObstacles;

		private Task resultTask;

		private List<WorkerUnitObstacle> resultEvadedObstaclesToTask;

		private List<WorkerUnitObstacle> resultEvadedObstaclesFromTask;

		private DecomposedTrajectory resultTrajectoryToTask;

//		private DecomposedTrajectory resultTrajectoryAtTask;

		private DecomposedTrajectory resultTrajectoryFromTask;

		private WorkerUnitObstacle resultSegmentToTask;

		private WorkerUnitObstacle resultSegmentAtTask;

		private WorkerUnitObstacle resultSegmentFromTask;

		public CreateJob(List<Point> toTask, List<Point> fromTask, WorkerUnitObstacle segment) {
			// TODO last edit

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

		private List<WorkerUnitObstacle> getResultEvadedObstaclesToTask() {
			return resultEvadedObstaclesToTask;
		}

		private void setResultEvadedObstaclesToTask(
			List<WorkerUnitObstacle> resultEvadedObstaclesToTask) {
			this.resultEvadedObstaclesToTask = resultEvadedObstaclesToTask;
		}

		private List<WorkerUnitObstacle> getResultEvadedObstaclesFromTask() {
			return resultEvadedObstaclesFromTask;
		}

		private void setResultEvadedObstaclesFromTask(
			List<WorkerUnitObstacle> resultEvadedObstaclesFromTask) {
			this.resultEvadedObstaclesFromTask = resultEvadedObstaclesFromTask;
		}

		private DecomposedTrajectory getResultTrajectoryToTask() {
			return resultTrajectoryToTask;
		}

		private void setResultTrajectoryToTask(
			DecomposedTrajectory resultTrajectoryToTask) {
			this.resultTrajectoryToTask = resultTrajectoryToTask;
		}

//		private DecomposedTrajectory getResultTrajectoryAtTask() {
//			return resultTrajectoryAtTask;
//		}
//
//		private void setResultTrajectoryAtTask(
//			DecomposedTrajectory resultTrajectoryAtTask) {
//			this.resultTrajectoryAtTask = resultTrajectoryAtTask;
//		}

		private DecomposedTrajectory getResultTrajectoryFromTask() {
			return resultTrajectoryFromTask;
		}

		private void setResultTrajectoryFromTask(
			DecomposedTrajectory resultTrajectoryFromTask) {
			this.resultTrajectoryFromTask = resultTrajectoryFromTask;
		}

		private WorkerUnitObstacle getResultSegmentToTask() {
			return resultSegmentToTask;
		}

		private void setResultSegmentToTask(WorkerUnitObstacle resultSegmentToTask) {
			this.resultSegmentToTask = resultSegmentToTask;
		}

		private WorkerUnitObstacle getResultSegmentAtTask() {
			return resultSegmentAtTask;
		}

		private void setResultSegmentAtTask(WorkerUnitObstacle resultSegmentAtTask) {
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
				return status;

			Trajectory trajToTask = getResultTrajectoryToTask();
			Task task = makeTask( trajToTask.getFinishTime() );
			setResultTask(task);

			status = calculateTrajectoryFromTask();

			if (!status)
				return status;

			WorkerUnitObstacle segmentAtTask = makeIdlingWorkerUnitObstacle(task);
			setResultSegmentAtTask( segmentAtTask );
			addDynamicObstacle( segmentAtTask );

			return status;
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
				return status; // false

			List<DynamicObstacle> evadedObstacles = pf.getResultEvadedObstacles();
			DecomposedTrajectory trajToTask = pf.getResultTrajectory();

//			setResultEvadedObstacles(evadedObstacles);
//			setResultTrajectory(trajToTask);
			setResultEvadedObstaclesToTask(evadedObstacles);
			setResultTrajectoryToTask(trajToTask);

			addDynamicObstacle( makeDynamicObstacle(worker, trajToTask) );

			return status; // true
		}

		private boolean calculateTrajectoryFromTask() {
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
				return status; // false

			List<DynamicObstacle> evadedObstacles = pf.getResultEvadedObstacles();
			DecomposedTrajectory trajFromTask = pf.getResultTrajectory();

			// TODO set results
			setResultEvadedObstaclesFromTask(evadedObstacles);
			setResultTrajectoryFromTask(trajFromTask);

			addDynamicObstacle( makeDynamicObstacle(worker, trajFromTask) );

			return status; // true
		}

		@Override
		public void execute() {
			WorkerUnit worker = getWorkerUnit();
			Task task = getResultTask();

			// construct worker unit obstacles

			WorkerUnitObstacle segmentToTask = getSegmentToTask();
			WorkerUnitObstacle segmentAtTask = getSegmentAtTask();
			WorkerUnitObstacle segmentFromTask = getSegmentFromTask();

			// register evasions
			for (WorkerUnitObstacle e : getResultEvadedObstaclesToTask())
				e.addEvasion(segmentToTask);
			for (WorkerUnitObstacle e : getResultEvadedObstaclesFromTask())
				e.addEvasion(segmentFromTask);

			// add obstacle segments and task
			worker.removeObstacleSegment(getSegment());
			worker.addObstacleSegment(segmentToTask);
			worker.addObstacleSegment(segmentAtTask);
			worker.addObstacleSegment(segmentFromTask);
			worker.addTask(task);
		}

	}

	private class UpdateJob extends Job {

		private final WorkerUnitObstacle evasion;

		public UpdateJob(WorkerUnitObstacle evasion) {
			super(calcMaxDuration(evasion));

			this.evasion = evasion;
		}

		public WorkerUnitObstacle getEvasion() {
			return evasion;
		}

		@Override
		public double calcLaxity() {
			WorkerUnitObstacle evasion = getEvasion();
			WorkerUnit worker = evasion.getWorkerUnit();
			double maxSpeed = worker.getMaxSpeed();
			double length = evasion.getTrajectory().getLength();
			double maxDuration = inSeconds( getMaxDuration() );

			return maxDuration/length - 1./maxSpeed;
		}

		@Override
		public boolean calculate() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void execute() {
			// TODO Auto-generated method stub

		}

	}

	private static Duration calcMaxDuration(WorkerUnitObstacle evasion) {
		LocalDateTime startTime = evasion.getStartTime();
		LocalDateTime finishTime = evasion.getFinishTime();

		return Duration.between(startTime, finishTime);
	}

	private List<Point> calculateSpatialPath(Point startLocation, Point taskLocation) {
		// TODO Auto-generated method stub
		return null;
	}

	private void prepareSpatialPathfinder() {
		SpatialPathfinder pf = getSpatialPathfinder();

		pf.setStaticObstacles( getStaticObstacles() );
	}

	private List<WorkerUnitObstacle> buildEvasions(WorkerUnitObstacle obstacleSegment) {
		// TODO Auto-generated method stub
		return null;
	}

	private Collection<DynamicObstacle> buildDynamicObstacles() {
		WorkerUnit worker = getWorkerUnit();
		Collection<WorkerUnit> pool = getWorkerPool();
		LocalDateTime latestStartTime = getLatestStartTime();
		Duration duration = getDuration();

		DynamicWorldBuilder builder = new DynamicWorldBuilder();

		Collection<WorkerUnit> others = new ArrayList<>(pool);
		others.remove(worker);

		builder.setWorkers(others);
		builder.setEndTime(latestStartTime.plus(duration));

		builder.build();

		return builder.getResultObstacles();
	}

}
