package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers;

import static com.vividsolutions.jts.operation.distance.DistanceOp.distance;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutableGeometries.immutable;
import static de.tu_berlin.mailbox.rjasper.lang.Comparables.max;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.secondsToDuration;
import static java.util.Collections.emptyIterator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.ScheduleAlternative;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.SpaceTimeSlot;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.NodeSlotBuilder;

// TODO document
/**
 * A NodeSlotIterator iterates over all idle slots of nodes which
 * satisfy the given specifications of a job. To satisfy means that a node is
 * capable to reach the job location while driving at maximum speed without
 * violating any time specification of the new job or other jobs. The
 * avoidance of obstacles are not considered.
 *
 * @author Rico Jasper
 */
public class NodeSlotIterator implements Iterator<NodeSlotIterator.NodeSlot> {

	/**
	 * Helper class to pair a node and one of its idle slots.
	 */
	public static class NodeSlot {

		/**
		 * The node of the idle slot.
		 */
		private final Node node;

		/**
		 * The idle slot.
		 */
		private final SpaceTimeSlot slot;

		/**
		 * Pairs an idle slot with its node.
		 *
		 * @param node
		 * @param slot
		 */
		public NodeSlot(Node node, SpaceTimeSlot slot) {
			this.node = node;
			this.slot = slot;
		}

		/**
		 * @return the node of the idle slot.
		 */
		public Node getNode() {
			return node;
		}

		/**
		 * @return the idle slot.
		 */
		public SpaceTimeSlot getSlot() {
			return slot;
		}

	}

	private final ScheduleAlternative alternative;

	private final LocalDateTime frozenHorizonTime;

	/**
	 * The location of the job specification.
	 */
	private final Point location;

	/**
	 * The earliest time to execute the job.
	 */
	private final LocalDateTime earliestStartTime;

	/**
	 * The latest time to execute the job.
	 */
	private final LocalDateTime latestStartTime;

	/**
	 * The duration of the job execution.
	 */
	private final Duration duration;

	private final NodeSlotBuilder slotBuilder;

	/**
	 * An iterator over the nodes to be considered.
	 */
	private Iterator<Node> nodeIterator;

	/**
	 * An iterator over the idle slots of the current node.
	 */
	private Iterator<SpaceTimeSlot> slotIterator;

	/**
	 * The next node to be returned as current node.
	 */
	private Node nextNode = null;

	/**
	 * The next slot to be returned as current slot.
	 */
	private SpaceTimeSlot nextSlot = null;

	/**
	 * The current node of the iteration.
	 */
	private Node currentNode = null;

	/**
	 * The current slot of the iteration.
	 */
	private SpaceTimeSlot currentSlot = null;

	/**
	 * Constructs a NodeSlotIterator which iterates over the given set of nodes
	 * to while checking against the given job specifications.
	 *
	 * @param nodes
	 *            the node pool to check
	 * @param alternative
	 * @param frozenHorizonTime
	 * @param location
	 *            of the job
	 * @param earliestStartTime
	 *            the earliest time to begin the job execution
	 * @param latestStartTime
	 *            the latest time to begin the job execution
	 * @param duration
	 *            of the job
	 *
	 * @throws NullPointerException
	 *             if any argument is {@code null}.
	 * @throws IllegalArgumentException
	 *             if any of the following is true:
	 *             <ul>
	 *             <li>The location is empty or invalid.</li>
	 *             <li>The earliestStartTime is after the latestStartTime.</li>
	 *             <li>The duration is negative.</li>
	 *             </ul>
	 */
	public NodeSlotIterator(
		Iterable<Node> nodes,
		ScheduleAlternative alternative,
		LocalDateTime frozenHorizonTime,
		Point location,
		LocalDateTime earliestStartTime,
		LocalDateTime latestStartTime,
		Duration duration)
	{
		Objects.requireNonNull(nodes, "nodes");
		Objects.requireNonNull(alternative, "alternative");
		Objects.requireNonNull(frozenHorizonTime, "frozenHorizon");
		Objects.requireNonNull(location, "location");
		Objects.requireNonNull(earliestStartTime, "earliestStartTime");
		Objects.requireNonNull(latestStartTime, "latestStartTime");
		Objects.requireNonNull(duration, "duration");

		GeometriesRequire.requireValid2DPoint(location, "location");

		if (earliestStartTime.compareTo(latestStartTime) > 0)
			throw new IllegalArgumentException("earliestStartTime is after latestStartTime");
		if (duration.isNegative())
			throw new IllegalArgumentException("duration is negative");

		this.nodeIterator = nodes.iterator();
		this.alternative = alternative;
		this.slotBuilder = new NodeSlotBuilder();
		this.frozenHorizonTime = frozenHorizonTime;
		this.location = location;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.duration = duration;

		// The next node and idle slot pair is calculated before they are
		// requested. This enables an easy check whether or not there is a next
		// pair.
		if (!frozenHorizonTime.isAfter(latestStartTime)) {
			initSlotBuilder();

			nextNode();
			nextSlot();
		}
	}

	private void initSlotBuilder() {
		slotBuilder.setAlternative(alternative);
		slotBuilder.setFrozenHorizonTime(frozenHorizonTime);
		slotBuilder.setStartTime(earliestStartTime);
		slotBuilder.setFinishTime(latestStartTime());
		slotBuilder.setOverlapping(true);
	}

	@Override
	public boolean hasNext() {
		return nextNode != null;
	}

	/**
	 * @return the current node of the iteration.
	 */
	public Node getCurrentNode() {
		return currentNode;
	}

	/**
	 * @return the current slot of the iteration.
	 */
	public SpaceTimeSlot getCurrentSlot() {
		return currentSlot;
	}

	private LocalDateTime earliestStartTime(Node node) {
		return max(earliestStartTime, frozenHorizonTime, node.getInitialTime());
	}

	private LocalDateTime latestStartTime() {
		return latestStartTime;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public NodeSlot next() {
		if (!hasNext())
			throw new NoSuchElementException();

		currentNode = nextNode;
		currentSlot = nextSlot;

		nextSlot();

		return new NodeSlot(getCurrentNode(), getCurrentSlot());
	}

	/**
	 * Determines the next node of the iteration.
	 *
	 * @return the next node.
	 */
	private Node nextNode() {
		// sets the next node and initializes an new idle slot iterator

		// seek the next node with available slots
		do {
			if (!nodeIterator.hasNext()) {
				nextNode = null;
				slotIterator = emptyIterator();

				break;
			}

			nextNode = nodeIterator.next();

			slotBuilder.setNode(nextNode);
			slotIterator = slotBuilder.build().iterator();
		} while (!slotIterator.hasNext());

		return nextNode;
	}

	/**
	 * Determines the next idle slot of the iteration.
	 *
	 * @return the next idle slot.
	 */
	private SpaceTimeSlot nextSlot() {
		SpaceTimeSlot slot = null;

		// iterates over the remaining idle slots of the remaining nodes
		// until a valid slot was found
		while (nodeIterator.hasNext() || slotIterator.hasNext()) {
			// if there are no more idle slots of the current node
			// then get the next one
			if (!slotIterator.hasNext()) {
				nextNode();
			// otherwise check the next idle slot
			} else {
				SpaceTimeSlot candidate = slotIterator.next();

				// if no more job after candidate
				if (candidate.getFinishTime().equals(Scheduler.END_OF_TIME)) {
					// overwrite candidate using the task location as final location
					candidate = new SpaceTimeSlot(
						candidate.getStartLocation(),
						immutable(location),
						candidate.getStartTime(),
						candidate.getFinishTime());
				}

				// break if the current idle slot is accepted
				if (check(nextNode, candidate)) {
					slot = candidate;
					break;
				}
			}
		}

		// if there are no more valid idle slots
		if (slot == null)
			// #hasNext checks if #nextNode is null
			nextNode = null;

		nextSlot = slot;

		return slot;
	}

	/**
	 * Checks if a node is able during a given idle slot to drive to a job
	 * location without violating any time constraints of the new job or
	 * the next job. It does not considers the presence of any obstacles to
	 * avoid.
	 *
	 * @param node
	 * @param slot
	 * @return {@code true} iff node can potentially execute the job in time.
	 */
	private boolean check(Node node, SpaceTimeSlot slot) {
		double vInv = 1. / node.getMaxSpeed();
		LocalDateTime t1 = slot.getStartTime();
		LocalDateTime t2 = slot.getFinishTime();
		Point p1 = slot.getStartLocation();
		Point p2 = slot.getFinishLocation();
		double l1 = distance(p1, location);
		double l2 = p2 == null ? 0. : distance(location, p2);

		// job cannot be started in time
		// t_max - t1 < l1 / v_max
		if (Duration.between(t1, latestStartTime()).compareTo(
			secondsToDuration(vInv * l1)) < 0)
		{
			return false;
		}
		// job cannot be finished in time
		// t2 - t_min < l2 / v_max + d
		if (Duration.between(earliestStartTime(node), t2).compareTo(
			secondsToDuration(vInv * l2).plus(duration)) < 0)
		{
			return false;
		}
		// insufficient time to complete job
		// t2 - t1 < (l1 + l2) / v_max + d
		if (Duration.between(t1, t2).compareTo(
			secondsToDuration(vInv * (l1 + l2)).plus(duration)) < 0)
		{
			return false;
		}

		return true;
	}

}
