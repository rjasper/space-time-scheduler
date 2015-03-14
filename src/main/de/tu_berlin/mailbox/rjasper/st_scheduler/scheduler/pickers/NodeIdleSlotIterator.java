package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers;

import static com.vividsolutions.jts.operation.distance.DistanceOp.*;
import static de.tu_berlin.mailbox.rjasper.lang.Comparables.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeConv.*;
import static java.util.Collections.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.IdleSlot;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Node;
import de.tu_berlin.mailbox.rjasper.util.function.TriFunction;

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
public class NodeIdleSlotIterator implements Iterator<NodeIdleSlotIterator.NodeIdleSlot> {

	// TODO sort by least detour

	/**
	 * Helper class to pair a node and one of its idle slots.
	 */
	public static class NodeIdleSlot {

		/**
		 * The node of the idle slot.
		 */
		private final Node node;

		/**
		 * The idle slot.
		 */
		private final IdleSlot idleSlot;

		/**
		 * Pairs an idle slot with its node.
		 *
		 * @param node
		 * @param idleSlot
		 */
		public NodeIdleSlot(Node node, IdleSlot idleSlot) {
			this.node = node;
			this.idleSlot = idleSlot;
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
		public IdleSlot getIdleSlot() {
			return idleSlot;
		}

	}

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

	/**
	 * Generates the slot iterator for the given node and time interval.
	 */
	private final TriFunction<
		Node,
		LocalDateTime,
		LocalDateTime,
		Iterator<IdleSlot>>
	slotsGenerator;

	/**
	 * An iterator over the nodes to be considered.
	 */
	private Iterator<Node> nodeIterator;

	/**
	 * An iterator over the idle slots of the current node.
	 */
	private Iterator<IdleSlot> slotIterator;

	/**
	 * The next node to be returned as current node.
	 */
	private Node nextNode = null;

	/**
	 * The next slot to be returned as current slot.
	 */
	private IdleSlot nextSlot = null;

	/**
	 * The current node of the iteration.
	 */
	private Node currentNode = null;

	/**
	 * The current slot of the iteration.
	 */
	private IdleSlot currentSlot = null;

	/**
	 * Constructs a NodeSlotIterator which iterates over the given set of
	 * nodes to while checking against the given job specifications.
	 *
	 * @param nodes the node pool to check
	 * @param location of the job
	 * @param earliestStartTime the earliest time to begin the job execution
	 * @param latestStartTime the latest time to begin the job execution
	 * @param duration of the job
	 *
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if any of the following is true:
	 * <ul>
	 * <li>The location is empty or invalid.</li>
	 * <li>The earliestStartTime is after the latestStartTime.</li>
	 * <li>The duration is negative.</li>
	 * </ul>
	 */
	public NodeIdleSlotIterator(
		Iterable<Node> nodes,
		TriFunction<Node, LocalDateTime, LocalDateTime, Iterator<IdleSlot>> slotGenerator,
		LocalDateTime frozenHorizonTime,
		Point location,
		LocalDateTime earliestStartTime,
		LocalDateTime latestStartTime,
		Duration duration)
	{
		Objects.requireNonNull(nodes, "nodes");
		Objects.requireNonNull(slotGenerator, "slotGenerator");
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
		this.slotsGenerator = slotGenerator;
		this.frozenHorizonTime = frozenHorizonTime;
		this.location = location;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.duration = duration;

		// The next node and idle slot pair is calculated before they are
		// requested. This enables an easy check whether or not there is a next
		// pair.
		if (!frozenHorizonTime.isAfter(latestStartTime)) {
			nextNode();
			nextSlot();
		}
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
	public IdleSlot getCurrentSlot() {
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
	public NodeIdleSlot next() {
		if (!hasNext())
			throw new NoSuchElementException();

		currentNode = nextNode;
		currentSlot = nextSlot;

		nextSlot();

		return new NodeIdleSlot(getCurrentNode(), getCurrentSlot());
	}

	/**
	 * Determines the next node of the iteration.
	 *
	 * @return the next node.
	 */
	private Node nextNode() {
		// sets the next node and initializes an new idle slot iterator

//		Node node;
//		LocalDateTime from, to;
//		do {
//			if (!nodeIterator.hasNext()) {
//				node = null;
//				from = null;
//				to = null;
//
//				break;
//			}
//
//			node = nodeIterator.next();
//
//			LocalDateTime earliest = earliestStartTime(node);
//			LocalDateTime latest = latestStartTime();
//			// FIXME not accurate
//			LocalDateTime floorIdle = node.floorIdleTimeOrNull(earliest);
//			LocalDateTime ceilIdle = node.ceilingIdleTimeOrNull(latest);
//
//			from = max(
//				floorIdle != null ? floorIdle : earliest,
//				frozenHorizonTime);
//			to = ceilIdle  != null ? ceilIdle  : latest;
//		} while (from.isAfter(to));
//
//
//		nextNode = node;
//		slotIterator = node == null // indicates loop break
//			? emptyIterator()
//			: slotsGenerator.apply(node, from, to);
		
		do {
			if (!nodeIterator.hasNext()) {
				nextNode = null;
				slotIterator = emptyIterator();
				
				break;
			}
			
			nextNode = nodeIterator.next();
			
			slotIterator = slotsGenerator.apply(nextNode, earliestStartTime, latestStartTime);
		} while (!slotIterator.hasNext());

		return nextNode;
	}

	/**
	 * Determines the next idle slot of the iteration.
	 *
	 * @return the next idle slot.
	 */
	private IdleSlot nextSlot() {
//		Node node = nextNode;
		IdleSlot slot = null;

		// iterates over the remaining idle slots of the remaining nodes
		// until a valid slot was found
		while (nodeIterator.hasNext() || slotIterator.hasNext()) {
			// if there are no more idle slots of the current node
			// then get the next one
			if (!slotIterator.hasNext()) {
				nextNode();
			// otherwise check the next idle slot
			} else {
				IdleSlot candidate = slotIterator.next();

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
	private boolean check(Node node, IdleSlot slot) {
		double vInv = 1. / node.getMaxSpeed();
		LocalDateTime t1 = slot.getStartTime();
		LocalDateTime t2 = slot.getFinishTime();
		Point p1 = slot.getStartLocation();
		Point p2 = slot.getFinishLocation(); // FIXME finish location not mandatory
		double l1 = distance(p1, location);
		double l2 = p2 == null ? 0. : distance(location, p2);

		// job can be started in time
		// t_max - t1 < l1 / v_max
		if (Duration.between(t1, latestStartTime()).compareTo(
			secondsToDuration(vInv * l1)) < 0)
		{
			return false;
		}
		// job can be finished in time
		// t2 - t_min < l2 / v_max + d
		if (Duration.between(earliestStartTime(node), t2).compareTo(
			secondsToDuration(vInv * l2).plus(duration)) < 0)
		{
			return false;
		}
		// enough time to complete job
		// t2 - t1 < (l1 + l2) / v_max + d
		if (Duration.between(t1, t2).compareTo(
			secondsToDuration(vInv * (l1 + l2)).plus(duration)) < 0)
		{
			return false;
		}

		return true;
	}

}
