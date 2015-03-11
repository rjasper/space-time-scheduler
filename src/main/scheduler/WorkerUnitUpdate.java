package scheduler;

import static java.util.Collections.*;
import static scheduler.util.IntervalSets.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import scheduler.util.IntervalSet;
import scheduler.util.SimpleIntervalSet;
import world.Trajectory;
import world.TrajectoryContainer;

import com.vividsolutions.jts.geom.Point;

public class WorkerUnitUpdate implements Cloneable {
	
	private final WorkerUnit worker;
	
	private final TrajectoryContainer trajectoryContainer = new TrajectoryContainer();
	
	private final Set<Task> tasks = new HashSet<>();
	
	private final Set<Task> taskRemovals = new HashSet<>();
	
	private final SimpleIntervalSet<LocalDateTime> trajectoryLock = new SimpleIntervalSet<>();
	
	private final SimpleIntervalSet<LocalDateTime> taskLock = new SimpleIntervalSet<>();
	
	private final SimpleIntervalSet<LocalDateTime> taskRemovalIntervals = new SimpleIntervalSet<>();
	
	private boolean sealed = false;

	public WorkerUnitUpdate(WorkerUnit worker) {
		this.worker = Objects.requireNonNull(worker, "worker");
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

	public Set<Task> getTasks() {
		return unmodifiableSet(tasks);
	}

	public Set<Task> getTaskRemovals() {
		return unmodifiableSet(taskRemovals);
	}
	
	public IntervalSet<LocalDateTime> getTrajectoryIntervals() {
		return trajectoryContainer.getTrajectoryIntervals();
	}

	public IntervalSet<LocalDateTime> getTrajectoryLock() {
		return unmodifiableIntervalSet(trajectoryLock);
	}
	
	public IntervalSet<LocalDateTime> getTaskLock() {
		return unmodifiableIntervalSet(taskLock);
	}

	public IntervalSet<LocalDateTime> getTaskRemovalIntervals() {
		return unmodifiableIntervalSet(taskRemovalIntervals);
	}

	public void updateTrajectory(Trajectory trajectory) {
		Objects.requireNonNull(trajectory, "trajectory");
		
		if (isSealed())
			throw new IllegalStateException("update is sealed");

		LocalDateTime startTime  = trajectory.getStartTime();
		LocalDateTime finishTime = trajectory.getFinishTime();
		
		if (startTime.isBefore(worker.getInitialTime()))
			throw new IllegalArgumentException("trajectory predates initial time");

		trajectoryLock.add(startTime, finishTime);
		trajectoryContainer.update(trajectory);
	}
	
	public void addTask(Task task) {
		Objects.requireNonNull(task, "task");
		
		if (isSealed())
			throw new IllegalStateException("update is sealed");
		
		LocalDateTime startTime  = task.getStartTime();
		LocalDateTime finishTime = task.getFinishTime();
		
		if (startTime.isBefore(worker.getInitialTime()))
			throw new IllegalArgumentException("task predates initial time");
		if (task.getAssignedWorker().getActual() != worker) // identity comparison
			throw new IllegalArgumentException("invalid assigned worker");
		if (taskLock.intersects(startTime, finishTime))
			throw new IllegalArgumentException("task lock violation");
		
		trajectoryLock.add(startTime, finishTime);
		taskLock.add(startTime, finishTime);
		
		tasks.add(task);
	}
	
	public void addTaskRemoval(Task task) {
		Objects.requireNonNull(task, "task");
		
		if (isSealed())
			throw new IllegalStateException("update is sealed");
		if (task.getAssignedWorker().getActual() != worker) // identity comparison
			throw new IllegalArgumentException("invalid assigned worker");
		
		taskRemovalIntervals.add(task.getStartTime(), task.getFinishTime());
		taskRemovals.add(task);
	}

	public void checkSelfConsistency() {
		if (!tasks.stream().allMatch(this::verifyTaskLocation))
			throw new IllegalStateException("task location violation");
		if (!verifyTrajectoryContinuity())
			throw new IllegalStateException("trajectory continuity violation");
	}

	private boolean verifyTaskLocation(Task task) {
		Point location = task.getLocation();
		LocalDateTime taskStart = task.getStartTime();
		LocalDateTime taskFinish = task.getFinishTime();
		
		return trajectoryContainer.getTrajectories(taskStart, taskFinish).stream()
			.allMatch(t -> {
				IntervalSet<LocalDateTime> intersection = new SimpleIntervalSet<LocalDateTime>()
					.add(t.getStartTime(), t.getFinishTime())
					.intersect(taskStart, taskFinish);
				
				// intersection is either empty or continuous [min, max]
				
				if (intersection.isEmpty())
					return true;
				
				LocalDateTime start = intersection.minValue();
				LocalDateTime finish = intersection.maxValue();
				
				return t.isStationary(start, finish)
					&& t.interpolateLocation(start).equals(location);
			});
	}
	
	private boolean verifyTrajectoryContinuity() {
		if (trajectoryContainer.isEmpty())
			return true;
		
		Iterator<Trajectory> it = trajectoryContainer.getTrajectories().iterator();
		
		Trajectory last = it.next();
		
		while (it.hasNext()) {
			Trajectory curr = it.next();
			
			// curr.startTime == last.finishTime && curr.startLoc != last.finishLoc
			if (curr.getStartTime().equals(last.getFinishTime()) &&
				!curr.getStartLocation().equals(last.getFinishLocation()))
			{
				return false;
			}
			
			last = curr;
		}
		
		return true;
	}

	public void seal() {
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");

		trajectoryLock.seal();
		taskLock.seal();
		
		sealed = true;
	}
	
	@Override
	public WorkerUnitUpdate clone() {
		WorkerUnitUpdate clone = new WorkerUnitUpdate(worker);
		
		clone.trajectoryContainer.update(trajectoryContainer);
		clone.tasks.addAll(tasks);
		clone.taskRemovals.addAll(taskRemovals);
		clone.trajectoryLock.add(trajectoryLock);
		clone.taskLock.add(taskLock);
		clone.taskRemovalIntervals.add(taskRemovalIntervals);
		clone.sealed = sealed;
		
		return clone;
	}

}
