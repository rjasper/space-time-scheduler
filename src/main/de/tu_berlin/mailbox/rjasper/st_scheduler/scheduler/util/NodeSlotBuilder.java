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
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.IdleSlot;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Schedule;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleAlternative;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSet.Interval;

// TODO test
public class NodeSlotBuilder {

	private Node node = null;

	private Schedule schedule = null;

	private ScheduleAlternative alternative = null;
	
	private LocalDateTime frozenHorizonTime = null;

	private LocalDateTime startTime = null;

	private LocalDateTime finishTime = null;

	private boolean overlapping = false;

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

	public void setSchedule(Schedule schedule) {
		this.schedule = Objects.requireNonNull(schedule, "schedule");
	}

	public void setAlternative(ScheduleAlternative alternative) {
		this.alternative = Objects.requireNonNull(alternative, "alternative");
	}

	public void setOverlapping(boolean overlapping) {
		this.overlapping = overlapping;
	}

	public List<IdleSlot> build() {
		checkParameters();

		// if initial >= finish
		if (!node.getInitialTime().isBefore(finishTime))
			return emptyList();
		if (!frozenHorizonTime.isBefore(finishTime))
			return emptyList();
		if (startTime.equals(finishTime))
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
		
		if (overlapping)
			Objects.requireNonNull(frozenHorizonTime, "frozenHorizon");
		if (startTime.isAfter(finishTime))
			throw new IllegalStateException("startTime is after finishTime");
//		if (!startTime.isBefore(finishTime))
//			throw new IllegalStateException("bugfix");
	}

	private IntervalSet<LocalDateTime> calcSlotIntervals(boolean nodeUpdated) {
		IntervalSet<LocalDateTime> scheduledJobs = node.getJobIntervals();
		IntervalSet<LocalDateTime> trajectoryLock = schedule.getTrajectoryLock(node);

		IntervalSet<LocalDateTime> jobLock = nodeUpdated
			? alternative.getJobLock(node)
			: emptyIntervalSet();
		IntervalSet<LocalDateTime> removalIntervals = nodeUpdated
			? alternative.getJobRemovalIntervals(node)
			: emptyIntervalSet();

		SimpleIntervalSet<LocalDateTime> slots = new SimpleIntervalSet<LocalDateTime>()
			.add(max(node.getInitialTime(), startTime), finishTime)
			.remove(scheduledJobs.difference(removalIntervals))
			.remove(trajectoryLock)
			.remove(jobLock);
		
		if (overlapping) {
			if (frozenHorizonTime.compareTo(startTime) < 0) {
				LocalDateTime leftMost = max(
					node.getInitialTime(),
					frozenHorizonTime,
					floorValue(trajectoryLock, startTime),
					floorValue(jobLock, startTime));
				
				LocalDateTime left = determineLeftOverlapping(scheduledJobs, removalIntervals, leftMost);
				
				if (left.compareTo(startTime) < 0)
					slots.add(left, startTime);
			}
			LocalDateTime rightMost = min(
				ceilingValue(trajectoryLock, finishTime),
				ceilingValue(jobLock, finishTime));
			
			LocalDateTime right = determineRightOverlapping(scheduledJobs, removalIntervals, rightMost);
			
			if (finishTime.compareTo(right) < 0)
				slots.add(finishTime, right);
		}
		
		return slots;
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
	
	private LocalDateTime determineLeftOverlapping(
		IntervalSet<LocalDateTime> scheduledJobs,
		IntervalSet<LocalDateTime> removalIntervals,
		LocalDateTime leftMost)
	{
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
	
	private LocalDateTime determineRightOverlapping(
		IntervalSet<LocalDateTime> scheduledJobs,
		IntervalSet<LocalDateTime> removalIntervals,
		LocalDateTime rightMost)
	{
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

}
