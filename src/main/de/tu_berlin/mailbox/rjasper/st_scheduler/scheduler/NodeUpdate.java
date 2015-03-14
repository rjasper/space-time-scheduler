package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler;

import static de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSets.*;
import static java.util.Collections.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSet;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.SimpleIntervalSet;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.TrajectoryContainer;

public class NodeUpdate implements Cloneable {

	private final Node node;

	private final TrajectoryContainer trajectoryContainer = new TrajectoryContainer();

	private final Set<Job> jobs = new HashSet<>();

	private final Set<Job> jobRemovals = new HashSet<>();

	private final SimpleIntervalSet<LocalDateTime> trajectoryLock = new SimpleIntervalSet<>();

	private final SimpleIntervalSet<LocalDateTime> jobLock = new SimpleIntervalSet<>();

	private final SimpleIntervalSet<LocalDateTime> jobRemovalIntervals = new SimpleIntervalSet<>();

	private boolean sealed = false;

	public NodeUpdate(Node node) {
		this.node = Objects.requireNonNull(node, "node");
	}

	public boolean isSealed() {
		return sealed;
	}

	public Node getNode() {
		return node;
	}

	public Collection<Trajectory> getTrajectories() {
		return trajectoryContainer.getTrajectories();
	}

	public Set<Job> getJobs() {
		return unmodifiableSet(jobs);
	}

	public Set<Job> getJobRemovals() {
		return unmodifiableSet(jobRemovals);
	}

	public IntervalSet<LocalDateTime> getTrajectoryIntervals() {
		return trajectoryContainer.getTrajectoryIntervals();
	}

	public IntervalSet<LocalDateTime> getTrajectoryLock() {
		return unmodifiableIntervalSet(trajectoryLock);
	}

	public IntervalSet<LocalDateTime> getJobLock() {
		return unmodifiableIntervalSet(jobLock);
	}

	public IntervalSet<LocalDateTime> getJobRemovalIntervals() {
		return unmodifiableIntervalSet(jobRemovalIntervals);
	}

	public ImmutablePoint interpolateLocation(LocalDateTime time) {
		return trajectoryContainer.interpolateLocation(time);
	}

	public void updateTrajectory(Trajectory trajectory) {
		Objects.requireNonNull(trajectory, "trajectory");

		if (isSealed())
			throw new IllegalStateException("update is sealed");

		LocalDateTime startTime  = trajectory.getStartTime();
		LocalDateTime finishTime = trajectory.getFinishTime();

		if (startTime.isBefore(node.getInitialTime()))
			throw new IllegalArgumentException("trajectory predates initial time");

		trajectoryLock.add(startTime, finishTime);
		trajectoryContainer.update(trajectory);
	}

	public void addJob(Job job) {
		Objects.requireNonNull(job, "job");

		if (isSealed())
			throw new IllegalStateException("update is sealed");

		LocalDateTime startTime  = job.getStartTime();
		LocalDateTime finishTime = job.getFinishTime();

		if (startTime.isBefore(node.getInitialTime()))
			throw new IllegalArgumentException("job predates initial time");
		if (job.getNodeReference().getActual() != node) // identity comparison
			throw new IllegalArgumentException("invalid assigned node");
		if (jobLock.intersects(startTime, finishTime))
			throw new IllegalArgumentException("job lock violation");

		trajectoryLock.add(startTime, finishTime);
		jobLock.add(startTime, finishTime);

		jobs.add(job);
	}

	public void addJobRemoval(Job job) {
		Objects.requireNonNull(job, "job");

		if (isSealed())
			throw new IllegalStateException("update is sealed");
		if (job.getNodeReference().getActual() != node) // identity comparison
			throw new IllegalArgumentException("invalid assigned node");

		jobRemovalIntervals.add(job.getStartTime(), job.getFinishTime());
		jobRemovals.add(job);
	}

	public void checkSelfConsistency() {
		if (!jobs.stream().allMatch(this::verifyJobLocation))
			throw new IllegalStateException("job location violation");
		if (!verifyTrajectoryContinuity())
			throw new IllegalStateException("trajectory continuity violation");
	}

	private boolean verifyJobLocation(Job job) {
		Point location = job.getLocation();
		LocalDateTime jobStart = job.getStartTime();
		LocalDateTime jobFinish = job.getFinishTime();

		return trajectoryContainer.getTrajectories(jobStart, jobFinish).stream()
			.allMatch(t -> {
				IntervalSet<LocalDateTime> intersection = new SimpleIntervalSet<LocalDateTime>()
					.add(t.getStartTime(), t.getFinishTime())
					.intersect(jobStart, jobFinish);

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
		jobLock.seal();

		sealed = true;
	}

	@Override
	public NodeUpdate clone() {
		NodeUpdate clone = new NodeUpdate(node);

		clone.trajectoryContainer.update(trajectoryContainer);
		clone.jobs.addAll(jobs);
		clone.jobRemovals.addAll(jobRemovals);
		clone.trajectoryLock.add(trajectoryLock);
		clone.jobLock.add(jobLock);
		clone.jobRemovalIntervals.add(jobRemovalIntervals);
		clone.sealed = sealed;

		return clone;
	}

}
