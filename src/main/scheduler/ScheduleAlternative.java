package scheduler;

import static java.util.Collections.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import world.Trajectory;

public class ScheduleAlternative {
	
	private final ScheduleAlternative parent;
	
	private int branches = 0;
	
	private boolean invalid = false;
	
	private boolean sealed = false;

	private Map<Node, NodeUpdate> updates =
		new IdentityHashMap<>();
	
	private Map<UUID, Job> jobs = new HashMap<>();
	
	public ScheduleAlternative() {
		this.parent = null;
	}
	
	private ScheduleAlternative(ScheduleAlternative parent) {
		this.parent = parent;
		
		parent.updates.values().stream()
			.map(NodeUpdate::clone)
			.forEach(u -> this.updates.put(u.getNode(), u));
		
		this.jobs.putAll(parent.jobs);
	}
	
	public boolean isRootBranch() {
		return parent == null;
	}
	
	public boolean isBranched() {
		return branches > 0;
	}
	
	public boolean isInvalid() {
		return invalid;
	}
	
	public boolean isModifiable() {
		return !isBranched() && !isInvalid() && !isSealed();
	}
	
	public boolean isSealed() {
		return sealed;
	}
	
	public boolean isEmpty() {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		
		return updates.isEmpty();
	}
	
	public boolean updatesNode(Node node) {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		
		return updates.containsKey(node);
	}

	public Collection<NodeUpdate> getUpdates() {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		if (!isSealed())
			throw new IllegalStateException("alternative not sealed");
		
		return unmodifiableCollection(updates.values());
	}
	
	public NodeUpdate popUpdate(Node node) {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		if (!isSealed())
			throw new IllegalStateException("alternative not sealed");
		
		NodeUpdate update = updates.remove(node);
		
		if (update == null)
			throw new IllegalArgumentException("unknown node");
		
		for (Job t : update.getJobs())
			jobs.remove(t.getId());
		
		return update;
	}
	
	private NodeUpdate getUpdate(Node node) {
		return updates.computeIfAbsent(node, NodeUpdate::new);
	}

	public void updateTrajectory(Node node, Trajectory trajectory) {
		Objects.requireNonNull(node, "node");
		Objects.requireNonNull(trajectory, "trajectory");

		if (!isModifiable())
			throw new IllegalStateException("alternative is unmodifiable");
		
		getUpdate(node).updateTrajectory(trajectory);
	}
	
	public Collection<Node> getNodes() {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		
		return unmodifiableCollection(updates.keySet());
	}
	
	public Collection<Trajectory> getTrajectoryUpdates(Node node) {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		
		return updatesNode(node)
			? getUpdate(node).getTrajectories()
			: emptyList();
	}
	
	public boolean hasJob(UUID jobId) {
		return jobs.containsKey(jobId);
	}
	
	public Job getJob(UUID jobId) {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		
		return jobs.get(jobId);
	}

	public void addJob(Job job) {
		Objects.requireNonNull(job, "job");

		if (!isModifiable())
			throw new IllegalStateException("alternative is unmodifiable");
		
		jobs.put(job.getId(), job);
		getUpdate(job.getNodeReference().getActual())
			.addJob(job);
	}
	
	public void addJobRemoval(Job job) {
		Objects.requireNonNull(job, "job");

		if (!isModifiable())
			throw new IllegalStateException("alternative is unmodifiable");
		
		getUpdate(job.getNodeReference().getActual())
			.addJobRemoval(job);
	}
	
	public ScheduleAlternative branch() {
		if (isInvalid())
			throw new IllegalStateException("alternative is invalid");
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");
		
		++branches;
		
		return new ScheduleAlternative(this);
	}
	
	public ScheduleAlternative merge() {
		if (isRootBranch())
			throw new IllegalStateException("cannot merge root branch");
		if (!isModifiable())
			throw new IllegalStateException("alternative is unmodifiable");
		
		parent.mergeBranch(this);
		
		return parent;
	}
	
	private void mergeBranch(ScheduleAlternative branch) {
		if (branches > 1)
			throw new IllegalStateException("cannot merge while there are multiple branches");
		
		updates = branch.updates;
		jobs = branch.jobs;
		
		--branches;
	}
	
	public ScheduleAlternative delete() {
		if (isRootBranch())
			throw new IllegalStateException("cannot delete root branch");
		if (!isModifiable())
			throw new IllegalStateException("alternative is unmodifiable");
		
		parent.deleteBranch();

		invalid = true;
		updates = null;
		jobs = null;
		
		return parent;
	}
	
	private void deleteBranch() {
		--branches;
	}
	
	public void seal() {
		if (!isRootBranch())
			throw new IllegalStateException("can only seal root branch");
		if (isBranched())
			throw new IllegalStateException("cannot seal branched alternative");
		if (isSealed())
			throw new IllegalStateException("alternative is sealed");
		
		for (NodeUpdate u : updates.values())
			u.seal();
		
		sealed = true;
	}

}
