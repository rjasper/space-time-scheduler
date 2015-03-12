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
	
	private final Map<UUID, Job> jobs = new HashMap<>();
	
	private static class NodeLocks {
		
		public final SimpleIntervalSet<LocalDateTime> trajectoryLock = new SimpleIntervalSet<>();
		
		public final Set<Job> jobRemovalLock = new HashSet<>();
		
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
			throw new IllegalStateException("node still has scheduled jobs");
		
		nodes.remove(nodeId);
		locks.remove(node);
	}
	
	public Job getJob(UUID jobId) {
		Job job = jobs.get(jobId);
		
		if (job == null)
			throw new IllegalArgumentException("unknown job id");
		
		return job;
	}
	
	public void removeJob(UUID jobId) {
		Job job = getJob(jobId);
		Node node = job.getNodeReference().getActual();
		Set<Job> lock = getJobRemovalLock(node);
		
		if (lock.contains(job))
			throw new IllegalStateException("given job is locked for removal");
		
		node.removeJob(job);
		jobs.remove(jobId);
	}
	
	public IntervalSet<LocalDateTime> getTrajectoryLock(Node node) {
		Objects.requireNonNull(node, "node");
		
		NodeLocks nodeLocks = locks.get(node);
		
		if (nodeLocks == null)
			throw new IllegalArgumentException("unknown node");
		
		return unmodifiableIntervalSet(nodeLocks.trajectoryLock);
	}
	
	public Set<Job> getJobRemovalLock(Node node) {
		Objects.requireNonNull(node, "node");
		
		NodeLocks nodeLocks = locks.get(node);
		
		if (nodeLocks == null)
			throw new IllegalArgumentException("unknown node");
		
		return unmodifiableSet(nodeLocks.jobRemovalLock);
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

			IntervalSet<LocalDateTime> originJobsIntervals = node.getJobIntervals();
			IntervalSet<LocalDateTime> removalsIntervals = u.getJobRemovalIntervals();
			IntervalSet<LocalDateTime> trajIntervals = u.getTrajectoryIntervals();
			IntervalSet<LocalDateTime> trajLockIntervals = u.getTrajectoryLock();
			
			Collection<Job> jobs = u.getJobs();
			Collection<Job> removals = u.getJobRemovals();
			
			try {
				u.checkSelfConsistency();
			} catch (IllegalStateException e) {
				throw new IllegalArgumentException(e);
			}

			// no mutual trajectory locks with origin disregarding removed jobs
			if (!trajLockIntervals.intersection(originJobsIntervals)
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
			// non-updated original trajectories lead to jobs
			if (!jobs.stream().allMatch(t -> verifyJobLocation(t, trajIntervals)))
				throw new IllegalArgumentException("job location violation");
			// no unknown job removals
			if (!removals.stream().allMatch(node::hasJob))
				throw new IllegalArgumentException("unknown job removal");
			// no mutual job removals
			if (removals.stream().anyMatch(nodeLocks.jobRemovalLock::contains))
				throw new IllegalArgumentException("job removal lock violation");
		}
	}

	private boolean verifyJobLocation(Job job, IntervalSet<LocalDateTime> trajectoryUpdates) {
		Node node = job.getNodeReference().getActual();
		Point location = job.getLocation();
		LocalDateTime jobStart = job.getStartTime();
		LocalDateTime jobFinish = job.getFinishTime();
		
		IntervalSet<LocalDateTime> originalSections = new SimpleIntervalSet<LocalDateTime>()
			.add(jobStart, jobFinish)
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
		
		for (Job t : update.getJobRemovals()) {
			node.removeJob(t);
			jobs.remove(t.getId());
		}
		for (Job t : update.getJobs()) {
			node.addJob(t);
			jobs.put(t.getId(), t);
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
		nodeLocks.jobRemovalLock.addAll( update.getJobRemovals()     );
	}
	
	private void releaseLocks(ScheduleAlternative alternative) {
		for (NodeUpdate u : alternative.getUpdates())
			releaseLocks(u);
	}
	
	private void releaseLocks(NodeUpdate update) {
		NodeLocks nodeLocks = locks.get(update.getNode());
		
		nodeLocks.trajectoryLock .remove   ( update.getTrajectoryLock() );
		nodeLocks.jobRemovalLock.removeAll( update.getJobRemovals()     );
	}

}
