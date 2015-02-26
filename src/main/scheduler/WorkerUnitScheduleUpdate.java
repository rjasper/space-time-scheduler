package scheduler;

import static java.util.Collections.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import scheduler.util.IntervalSet;
import scheduler.util.SimpleIntervalSet;
import world.Trajectory;
import world.TrajectoryContainer;

import com.vividsolutions.jts.geom.Point;

public class WorkerUnitScheduleUpdate {
	
	private final WorkerUnit worker;
	
//	private final List<Trajectory> trajectories = new LinkedList<>();
	
	private final TrajectoryContainer trajectoryContainer = new TrajectoryContainer();
	
	private final Set<Task> tasks = new HashSet<>();
	
	private final Set<Task> taskRemovals = new HashSet<>();
	
	private final SimpleIntervalSet<LocalDateTime> trajectoryLock = new SimpleIntervalSet<>();
	
	private final SimpleIntervalSet<LocalDateTime> taskLock = new SimpleIntervalSet<>();
	
	private final SimpleIntervalSet<LocalDateTime> taskRemovalIntervals = new SimpleIntervalSet<>();
	
	private boolean sealed = false;

	public WorkerUnitScheduleUpdate(WorkerUnit worker)
	{
		this.worker       = Objects.requireNonNull(worker      , "worker"      );
//		this.trajectories = Objects.requireNonNull(trajectories, "trajectories");
//		this.tasks        = Objects.requireNonNull(tasks       , "tasks"       );
//		this.taskRemovals = Objects.requireNonNull(taskRemovals, "taskRemovals");
//		
//		// TODO updates should not predate initialTime
//		
//		// TODO check tasks
//		// tasks should not overlap
//		
//		// TODO check trajectories
//		// consecutive trajectories should touch
//		// trajectories if present should lead to tasks
//		
//		// TODO check task removals
//		// removals should not overlap
//
//		// lock new tasks
//		for (Task t : tasks)
//			tasksLock.add(t.getStartTime(), t.getFinishTime());
//		tasksLock.seal();
//		
//		// lock trajectory updates
//		for (Trajectory t : trajectories)
//			trajectoriesLock.add(t.getStartTime(), t.getFinishTime());
//		// lock fixated trajectories
////		trajectoriesLock.add(tasksLock);
//		for (Task t : tasks)
//			trajectoriesLock.add(t.getStartTime(), t.getFinishTime());
//		trajectoriesLock.seal();
//		
//		// add removal intervals
//		for (Task t : taskRemovals)
//			taskRemovalIntervals.add(t.getStartTime(), t.getFinishTime());
//		taskRemovalIntervals.seal();
	}
	
	public boolean isSealed() {
		return sealed;
	}

	public WorkerUnit getWorker() {
		return worker;
	}

	public Collection<Trajectory> getTrajectories() {
		return trajectoryContainer.getTrajectories();
	}

	public Collection<Task> getTasks() {
		return unmodifiableCollection(tasks);
	}

	public Collection<Task> getTaskRemovals() {
		return unmodifiableCollection(taskRemovals);
	}

	public SimpleIntervalSet<LocalDateTime> getTrajectoriesLock() {
		if (!isSealed())
			throw new IllegalStateException("update not sealed");
		
		return trajectoryLock;
	}

	public SimpleIntervalSet<LocalDateTime> getTaskRemovalIntervals() {
		if (!isSealed())
			throw new IllegalStateException("update not sealed");
		
		return taskRemovalIntervals;
	}

//	public SimpleIntervalSet<LocalDateTime> getTasksLock() {
//		return taskLock;
//	}

	public void updateTrajectory(Trajectory trajectory) {
		Objects.requireNonNull(trajectory, "trajectory");
		
		if (isSealed())
			throw new IllegalStateException("update is sealed");

		LocalDateTime startTime  = trajectory.getStartTime();
		LocalDateTime finishTime = trajectory.getFinishTime();
		
		checkTrajectoryLock(startTime, finishTime);
		
		trajectoryLock.add(startTime, finishTime);
		trajectoryContainer.update(trajectory);
	}
	
	private void checkTrajectoryLock(LocalDateTime from, LocalDateTime to) {
		if (trajectoryLock.intersects(from, to))
			throw new IllegalArgumentException("trajectory lock violation");
	}

	public void addTask(Task task) {
		Objects.requireNonNull(task, "task");
		
		if (isSealed())
			throw new IllegalStateException("update is sealed");
		
		LocalDateTime startTime  = task.getStartTime();
		LocalDateTime finishTime = task.getFinishTime();
		
//		checkTrajectoryLock(task.getStartTime(), task.getFinishTime());
		checkTaskLock(startTime, finishTime);
		checkTask(task);
		checkTaskLocation(task);
		
		// TODO implement
		trajectoryLock.add(startTime, finishTime);
		taskLock.add(startTime, finishTime);
		
		tasks.add(task);
	}
	
	private void checkTask(Task task) {
		if (task.getAssignedWorker().getActual() != worker) // identity comparision
			throw new IllegalArgumentException("invalid assigned worker");
	}
	
	private void checkTaskLock(LocalDateTime from, LocalDateTime to) {
		if (taskLock.intersects(from, to))
			throw new IllegalArgumentException("task lock violation");
	}
	
//	public boolean verify() {
//		// TODO implement
//		
//		return false;
//	}

	private void checkTaskLocation(Task task) {
		Point location = task.getLocation();
		LocalDateTime taskStart = task.getStartTime();
		LocalDateTime taskFinish = task.getFinishTime();
		
		boolean valid = true;
		
		// check if relevant alternative trajectory sections are stationary
		if (trajectoryLock.intersects(taskStart, taskFinish)) {
			valid = trajectoryContainer.getTrajectories().stream()
				.allMatch(t -> {
					IntervalSet<LocalDateTime> intersection = new SimpleIntervalSet<LocalDateTime>()
						.add(t.getStartTime(), t.getFinishTime())
						.intersect(taskStart, taskFinish);
					
					// intersection is either empty or continuous [min, max]
					
					LocalDateTime start = intersection.minValue();
					LocalDateTime finish = intersection.maxValue();
					
					return intersection.isEmpty() || (
						t.isStationary(start, finish) &&
						t.interpolateLocation(start).equals(location));
				});
		}
		
		if (!valid)
			throw new IllegalArgumentException("trajectory stationarity violated");
	}

	public void addTaskRemoval(Task task) {
		Objects.requireNonNull(task, "task");
		
		if (isSealed())
			throw new IllegalStateException("update is sealed");
		
		checkTask(task);
		
		taskRemovals.add(task);
	}
	
//	private void checkTaskRemoval(Task task) {
//		// TODO Auto-generated method stub
//		
//	}

	public void seal() {
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");

		trajectoryLock.seal();
		taskLock.seal();
		
		sealed = true;
	}

}
