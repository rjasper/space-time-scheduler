package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

import static de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSets.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static de.tu_berlin.mailbox.rjasper.lang.Comparables.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.IdleSlot;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Schedule;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleAlternative;

// TODO test
public class NodeSlotBuilder {

	private Node node = null;

	private Schedule schedule = null;

	private ScheduleAlternative alternative = null;

	private LocalDateTime startTime = null;

	private LocalDateTime finishTime = null;

	// XXX last edition
	private boolean overlapping = false;

	public void setNode(Node node) {
		this.node = Objects.requireNonNull(node, "node");
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = Objects.requireNonNull(startTime, "startTime");
	}

	public void setFinishTime(LocalDateTime finishTime) {
		this.finishTime = Objects.requireNonNull(finishTime, "finishTime");
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = Objects.requireNonNull(schedule, "schedule");
	}

	public void setAlternative(ScheduleAlternative alternative) {
		this.alternative = Objects.requireNonNull(alternative, "alternative");
	}

	public List<IdleSlot> build() {
		checkParameters();

		// if initial >= finish
		if (!node.getInitialTime().isBefore(finishTime))
			return emptyList();

		boolean nodeUpdated = alternative.updatesNode(node);
		IntervalSet<LocalDateTime> slotIntervals = calcSlotIntervals(nodeUpdated);
		IntervalSet<LocalDateTime> trajUpdateIntervals = alternative.updatesNode(node)
			? alternative.getTrajectoryUpdateIntervals(node)
			: emptyIntervalSet();

		return slotIntervals.stream()
			.map(i -> {
				LocalDateTime startTime  = i.getFromInclusive();
				LocalDateTime finishTime = i.getToExclusive();



				return new IdleSlot(
					interpolateLocation(startTime , trajUpdateIntervals),
					interpolateLocation(finishTime, trajUpdateIntervals),
					startTime,
					finishTime);
			})
			.collect(toList());
	}

	private void checkParameters() {
		Objects.requireNonNull(node, "node");
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(finishTime, "finishTime");
		Objects.requireNonNull(schedule, "schedule");
		Objects.requireNonNull(alternative, "alternative");

		if (!startTime.isBefore(finishTime))
			throw new IllegalStateException("startTime is not before finishTime");
	}

	private IntervalSet<LocalDateTime> calcSlotIntervals(boolean nodeUpdated) {
		// TODO consider overlapping flag

		IntervalSet<LocalDateTime> scheduledJobs = node.getJobIntervals();
		IntervalSet<LocalDateTime> trajectoryLock = schedule.getTrajectoryLock(node);

		IntervalSet<LocalDateTime> jobLock = nodeUpdated
			? alternative.getJobLock(node)
			: emptyIntervalSet();
		IntervalSet<LocalDateTime> removalIntervals = nodeUpdated
			? alternative.getJobRemovalIntervals(node)
			: emptyIntervalSet();

		return new SimpleIntervalSet<LocalDateTime>()
			.add(max(node.getInitialTime(), startTime), finishTime)
			.remove(scheduledJobs.difference(removalIntervals))
			.remove(trajectoryLock)
			.remove(jobLock);
	}

	private ImmutablePoint interpolateLocation(
		LocalDateTime time,
		IntervalSet<LocalDateTime> trajUpdateIntervals)
	{
		if (trajUpdateIntervals.contains(time))
			return alternative.interpolateLocation(node, time);
		else
			return node.interpolateLocation(time);
	}

}
