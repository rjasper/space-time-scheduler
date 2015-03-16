package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

import static de.tu_berlin.mailbox.rjasper.lang.Comparables.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSets.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleAlternative;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.SpaceTimeSlot;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSet.Interval;

public class NodeSlotBuilder {

	private Node node = null;

	private ScheduleAlternative alternative = null;

	private LocalDateTime frozenHorizonTime = null;

	private LocalDateTime startTime = null;

	private LocalDateTime finishTime = null;

	private boolean overlapping = false;

	private transient IntervalSet<LocalDateTime> scheduledJobs;

	private transient IntervalSet<LocalDateTime> jobLock;

	private transient IntervalSet<LocalDateTime> trajectoryLock;

	private transient IntervalSet<LocalDateTime> removalIntervals;

	public void setNode(Node node) {
		this.node = Objects.requireNonNull(node, "node");
	}

	public void setFrozenHorizonTime(LocalDateTime frozenHorizonTime) {
		this.frozenHorizonTime = Objects.requireNonNull(frozenHorizonTime, "frozenHorizonTime");
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = Objects.requireNonNull(startTime, "startTime");
	}

	public void setFinishTime(LocalDateTime finishTime) {
		this.finishTime = Objects.requireNonNull(finishTime, "finishTime");
	}

	public void setAlternative(ScheduleAlternative alternative) {
		this.alternative = Objects.requireNonNull(alternative, "alternative");
	}

	public void setOverlapping(boolean overlapping) {
		this.overlapping = overlapping;
	}

	public List<SpaceTimeSlot> build() {
		checkParameters();

		if (node.getInitialTime().compareTo(finishTime) > 0)
			return emptyList();
		if (frozenHorizonTime.compareTo(finishTime) > 0)
			return emptyList();

		init();
		List<SpaceTimeSlot> slots = buildImpl();
		cleanUp();

		return slots;
	}

	private List<SpaceTimeSlot> buildImpl() {
		LocalDateTime from = determineLeft();
		LocalDateTime to = determineRight();

		if (from.compareTo(to) >= 0)
			return emptyList();

		IntervalSet<LocalDateTime> slotIntervals = calcSlotIntervals(from, to);
		List<SpaceTimeSlot> slots = makeSlots(slotIntervals);

		return slots;
	}

	private void checkParameters() {
		Objects.requireNonNull(node, "node");
		Objects.requireNonNull(startTime, "startTime");
		Objects.requireNonNull(finishTime, "finishTime");
		Objects.requireNonNull(alternative, "alternative");

		if (overlapping)
			Objects.requireNonNull(frozenHorizonTime, "frozenHorizon");
		if (startTime.isAfter(finishTime))
			throw new IllegalStateException("startTime is after finishTime");
	}

	private void init() {
		scheduledJobs = node.getJobIntervals();
		trajectoryLock = node.getTrajectoryLock();

		if (alternative.updatesNode(node)) {
			jobLock = alternative.getJobLock(node);
			removalIntervals = alternative.getJobRemovalIntervals(node);
		} else {
			jobLock = emptyIntervalSet();
			removalIntervals = emptyIntervalSet();
		}
	}

	private void cleanUp() {
		scheduledJobs = null;
		jobLock = null;
		trajectoryLock = null;
		removalIntervals = null;
	}

	private LocalDateTime determineLeft() {
		LocalDateTime leftLimit = max(node.getInitialTime(), frozenHorizonTime);

		if (startTime.compareTo(leftLimit) < 0)
			return leftLimit;

		if (overlapping) {
			LocalDateTime leftMost = max(
				leftLimit,
				floorValue(trajectoryLock, startTime),
				floorValue(jobLock, startTime));

			return determineLeftOverlapping(leftMost);
		} else {
			return startTime;
		}
	}

	private LocalDateTime determineRight() {
		if (overlapping) {
			LocalDateTime rightMost = min(
				ceilingValue(trajectoryLock, finishTime),
				ceilingValue(jobLock, finishTime));

			return determineRightOverlapping(rightMost);
		} else {
			return finishTime;
		}
	}

	private LocalDateTime determineLeftOverlapping(LocalDateTime leftMost) {
		Iterator<Interval<LocalDateTime>> it = scheduledJobs
			.subSet(leftMost, startTime)
			.descendingIterator();

		while (it.hasNext()) {
			Interval<LocalDateTime> curr = it.next();

			if (curr.getToExclusive().compareTo(leftMost) <= 0)
				return leftMost;

			if (!removalIntervals.contains( curr.getFromInclusive() ))
				return curr.getToExclusive();
		}

		return leftMost;
	}

	private LocalDateTime determineRightOverlapping(LocalDateTime rightMost) {
		Iterator<Interval<LocalDateTime>> it = scheduledJobs
			.subSet(finishTime, rightMost)
			.iterator();

		while (it.hasNext()) {
			Interval<LocalDateTime> curr = it.next();

			if (curr.getFromInclusive().compareTo(rightMost) >= 0)
				return rightMost;

			if (!removalIntervals.contains( curr.getFromInclusive() ))
				return curr.getFromInclusive();
		}

		return rightMost;
	}

	private LocalDateTime floorValue(IntervalSet<LocalDateTime> intervals, LocalDateTime time) {
		if (intervals.isEmpty() || intervals.minValue().compareTo(time) >= 0)
			return LocalDateTime.MIN;

		return intervals.floorValue(time);
	}

	private LocalDateTime ceilingValue(IntervalSet<LocalDateTime> intervals, LocalDateTime time) {
		if (intervals.isEmpty() || intervals.maxValue().compareTo(time) <= 0)
			return LocalDateTime.MAX;

		return intervals.ceilingValue(time);
	}

	private IntervalSet<LocalDateTime> calcSlotIntervals(LocalDateTime from, LocalDateTime to) {
		return new SimpleIntervalSet<LocalDateTime>()
			.add(from, to)
			.remove(scheduledJobs.difference(removalIntervals))
			.remove(trajectoryLock)
			.remove(jobLock);
	}

	private List<SpaceTimeSlot> makeSlots(IntervalSet<LocalDateTime> slotIntervals) {
		IntervalSet<LocalDateTime> trajUpdateIntervals = alternative.updatesNode(node)
			? alternative.getTrajectoryUpdateIntervals(node)
			: emptyIntervalSet();

		return slotIntervals.stream()
			.map(i -> {
				LocalDateTime startTime  = i.getFromInclusive();
				LocalDateTime finishTime = i.getToExclusive();

				return new SpaceTimeSlot(
					interpolateLocation(startTime , trajUpdateIntervals),
					interpolateLocation(finishTime, trajUpdateIntervals),
					startTime,
					finishTime);
			})
			.collect(toList());
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
