package scheduler;

import static java.util.Collections.*;
import static scheduler.util.IntervalSets.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jts.geom.immutable.ImmutablePoint;
import scheduler.util.IntervalSet;
import scheduler.util.SimpleIntervalSet;
import world.Trajectory;

import com.vividsolutions.jts.geom.Point;

public class Schedule {
	
	private final Map<String, Node> nodes = new HashMap<>();
	
	private final Set<ScheduleAlternative> alternatives = new HashSet<>();
	
	private final Map<Node, NodeLocks> locks = new IdentityHashMap<>();
	
	private final Map<UUID, Task> tasks = new HashMap<>();
	
	private static class NodeLocks {
		
		public final SimpleIntervalSet<LocalDateTime> trajectoryLock = new SimpleIntervalSet<>();
		
		public final Set<Task> taskRemovalLock = new HashSet<>();
		
	}
	
	public boolean hasAlternative(ScheduleAlternative alternative) {
		return alternatives.contains(alternative);
	}
	
	public Collection<Node> getNodes() {
		return unmodifiableCollection(nodes.values());
	}
	
	public Node getNode(String nodeId) {
		Objects.requireNonNull(nodeId, "nodeId");
		
		Node node = nodes.get(nodeId);
		
		if (node == null)
			throw new IllegalArgumentException("unknown node id");
		
		return node;
	}

	public void addNode(Node node) {
		Objects.requireNonNull(node, "node");
		
		Node previous = nodes.putIfAbsent(node.getId(), node);
		
		if (previous != null)
			throw new IllegalArgumentException("node id already assigned");
		
		locks.put(node, new NodeLocks());
	}
	
	public void removeNode(String nodeId) {
		Objects.requireNonNull(nodeId, "nodeId");
		
		Node node = nodes.get(nodeId);
		
		if (node == null)
			throw new IllegalArgumentException("unknown node id");
		if (!node.isIdle())
			throw new IllegalStateException("node still has scheduled tasks");
		
		nodes.remove(nodeId);
		locks.remove(node);
	}
	
	public Task getTask(UUID taskId) {
		Task task = tasks.get(taskId);
		
		if (task == null)
			throw new IllegalArgumentException("unknown task id");
		
		return task;
	}
	
	public void removeTask(UUID taskId) {
		Task task = getTask(taskId);
		Node node = task.getNodeReference().getActual();
		Set<Task> lock = getTaskRemovalLock(node);
		
		if (lock.contains(task))
			throw new IllegalStateException("given task is locked for removal");
		
		node.removeTask(task);
		tasks.remove(taskId);
	}
	
	public IntervalSet<LocalDateTime> getTrajectoryLock(Node node) {
		Objects.requireNonNull(node, "node");
		
		NodeLocks nodeLocks = locks.get(node);
		
		if (nodeLocks == null)
			throw new IllegalArgumentException("unknown node");
		
		return unmodifiableIntervalSet(nodeLocks.trajectoryLock);
	}
	
	public Set<Task> getTaskRemovalLock(Node node) {
		Objects.requireNonNull(node, "node");
		
		NodeLocks nodeLocks = locks.get(node);
		
		if (nodeLocks == null)
			throw new IllegalArgumentException("unknown node");
		
		return unmodifiableSet(nodeLocks.taskRemovalLock);
	}
	
	public Collection<ScheduleAlternative> getAlternatives() {
		return unmodifiableCollection(alternatives);
	}
	
	public void addAlternative(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		
		if (!alternative.isSealed())
			new IllegalArgumentException("alternative not sealed");
		
		checkCompatibility(alternative);
		
		alternatives.add(alternative);
		applyLocks(alternative);
	}
	
	public void integrate(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		
		boolean status = alternatives.remove(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");

		releaseLocks(alternative);
		applyChanges(alternative);
	}
	
	public void integrate(ScheduleAlternative alternative, Node node) {
		Objects.requireNonNull(alternative, "alternative");
		Objects.requireNonNull(node, "node");
		
		boolean status = alternatives.contains(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");
		
		NodeUpdate update = alternative.popUpdate(node);
		
		if (alternative.isEmpty())
			alternatives.remove(alternative);
		
		releaseLocks(update);
		applyChanges(update);
	}
	
	public void eliminate(ScheduleAlternative alternative) {
		Objects.requireNonNull(alternative, "alternative");
		
		boolean status = alternatives.remove(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");

		releaseLocks(alternative);
	}
	
	public void eliminate(ScheduleAlternative alternative, Node node) {
		Objects.requireNonNull(alternative, "alternative");
		Objects.requireNonNull(node, "node");
		
		boolean status = alternatives.contains(alternative);
		
		if (!status)
			throw new IllegalArgumentException("unknown alternative");
		
		NodeUpdate update = alternative.popUpdate(node);
		
		if (alternative.isEmpty())
			alternatives.remove(alternative);
		
		releaseLocks(update);
	}

	private void checkCompatibility(ScheduleAlternative alternative) {
		for (NodeUpdate u : alternative.getUpdates()) {
			Node node = u.getNode();
			NodeLocks nodeLocks = locks.get(node);
			
			if (nodeLocks == null)
				throw new IllegalArgumentException("unknown node");

			IntervalSet<LocalDateTime> originTasksIntervals = node.getTaskIntervals();
			IntervalSet<LocalDateTime> removalsIntervals = u.getTaskRemovalIntervals();
			IntervalSet<LocalDateTime> trajIntervals = u.getTrajectoryIntervals();
			IntervalSet<LocalDateTime> trajLockIntervals = u.getTrajectoryLock();
			
			Collection<Task> tasks = u.getTasks();
			Collection<Task> removals = u.getTaskRemovals();
			
			try {
				u.checkSelfConsistency();
			} catch (IllegalStateException e) {
				throw new IllegalArgumentException(e);
			}

			// no mutual trajectory locks with origin disregarding removed tasks
			if (!trajLockIntervals.intersection(originTasksIntervals)
				.difference(removalsIntervals).isEmpty())
			{
				throw new IllegalArgumentException("trajectory lock violation");
			}
			// no mutual trajectory locks with other alternatives
			if (trajLockIntervals.intersects(nodeLocks.trajectoryLock))
				throw new IllegalArgumentException("trajectory lock violation");
			// continuous trajectories
			if (!verifyTrajectoryContinuity(node, u.getTrajectories()))
				throw new IllegalArgumentException("trajectory continuity violation");
			// non-updated original trajectories lead to tasks
			if (!tasks.stream().allMatch(t -> verifyTaskLocation(t, trajIntervals)))
				throw new IllegalArgumentException("task location violation");
			// no unknown task removals
			if (!removals.stream().allMatch(node::hasTask))
				throw new IllegalArgumentException("unknown task removal");
			// no mutual task removals
			if (removals.stream().anyMatch(nodeLocks.taskRemovalLock::contains))
				throw new IllegalArgumentException("task removal lock violation");
		}
	}

	private boolean verifyTaskLocation(Task task, IntervalSet<LocalDateTime> trajectoryUpdates) {
		Node node = task.getNodeReference().getActual();
		Point location = task.getLocation();
		LocalDateTime taskStart = task.getStartTime();
		LocalDateTime taskFinish = task.getFinishTime();
		
		IntervalSet<LocalDateTime> originalSections = new SimpleIntervalSet<LocalDateTime>()
			.add(taskStart, taskFinish)
			.remove(trajectoryUpdates);
		
		return originalSections.isEmpty() ||
			// check if relevant original trajectory sections are stationary
			originalSections.stream()
			.allMatch(i -> {
				LocalDateTime start = i.getFromInclusive();
				LocalDateTime finish = i.getToExclusive();
			
				return
					node.isStationary(start, finish) &&
					node.interpolateLocation(start).equals(location);
			});
	}
	
	private boolean verifyTrajectoryContinuity(Node node, Collection<Trajectory> trajectories) {
		if (trajectories.isEmpty())
			return true;
		
		Iterator<Trajectory> it = trajectories.iterator();
		
		Trajectory first = it.next();
		Trajectory last = first;
		
		if (!verifyNodeLocation(node, first.getStartLocation(), first.getStartTime()))
			return false;
		
		while (it.hasNext()) {
			Trajectory curr = it.next();

			LocalDateTime finishTime = last.getFinishTime();
			LocalDateTime startTime = curr.getStartTime();
			
			if (!startTime.equals(finishTime) && (
				!verifyNodeLocation(node, last.getFinishLocation(), finishTime) ||
				!verifyNodeLocation(node, curr.getStartLocation(), startTime)))
			{
				return false;
			}
			
			last = curr;
		}
		
		if (!verifyNodeLocation(node, last.getFinishLocation(), last.getFinishTime()))
			return false;
		
		return true;
	}
	
	private boolean verifyNodeLocation(Node node, ImmutablePoint location, LocalDateTime time) {
		if (time.equals(Scheduler.END_OF_TIME))
			return true;
		
		return node.interpolateLocation(time).equals(location);
	}
	
	private void applyChanges(ScheduleAlternative alternative) {
		for (NodeUpdate u : alternative.getUpdates())
			applyChanges(u);
	}

	private void applyChanges(NodeUpdate update) {
		Node node = update.getNode();
		
		for (Task t : update.getTaskRemovals()) {
			node.removeTask(t);
			tasks.remove(t.getId());
		}
		for (Task t : update.getTasks()) {
			node.addTask(t);
			tasks.put(t.getId(), t);
		}
		for (Trajectory t : update.getTrajectories())
			node.updateTrajectory(t);
	}

	private void applyLocks(ScheduleAlternative alternative) {
		for (NodeUpdate u : alternative.getUpdates())
			applyLocks(u);
	}
	
	private void applyLocks(NodeUpdate update) {
		NodeLocks nodeLocks = locks.get(update.getNode());
		
		nodeLocks.trajectoryLock .add   ( update.getTrajectoryLock() );
		nodeLocks.taskRemovalLock.addAll( update.getTaskRemovals()     );
	}
	
	private void releaseLocks(ScheduleAlternative alternative) {
		for (NodeUpdate u : alternative.getUpdates())
			releaseLocks(u);
	}
	
	private void releaseLocks(NodeUpdate update) {
		NodeLocks nodeLocks = locks.get(update.getNode());
		
		nodeLocks.trajectoryLock .remove   ( update.getTrajectoryLock() );
		nodeLocks.taskRemovalLock.removeAll( update.getTaskRemovals()     );
	}

}
