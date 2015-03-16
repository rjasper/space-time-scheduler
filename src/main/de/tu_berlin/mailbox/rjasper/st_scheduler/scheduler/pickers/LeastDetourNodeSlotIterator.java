package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers;

import static com.vividsolutions.jts.operation.distance.DistanceOp.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.jts.geom.util.GeometriesRequire;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.SpaceTimeSlot;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers.NodeSlotIterator.NodeSlot;

// TODO test and use
public class LeastDetourNodeSlotIterator implements Iterator<NodeSlot> {

	private final ImmutablePoint location;

	private final Iterator<WeightedNodeSlot> iterator;

	private static final Comparator<WeightedNodeSlot> COMPARATOR =
		(lhs, rhs) -> Double.compare(lhs.weight, rhs.weight);

	private static class WeightedNodeSlot {
		private final NodeSlot slot;
		private final double weight;

		public WeightedNodeSlot(NodeSlot slot, double weight) {
			this.slot = slot;
			this.weight = weight;
		}
	}

	public LeastDetourNodeSlotIterator(Iterable<NodeSlot> slots, ImmutablePoint location) {
		this.iterator = makeQueue( Objects.requireNonNull(slots, "slots") )
			.iterator();
		this.location = GeometriesRequire.requireValid2DPoint(location, "location");
	}

	private Queue<WeightedNodeSlot> makeQueue(Iterable<NodeSlot> slots) {
		Queue<WeightedNodeSlot> queue = new PriorityQueue<>(COMPARATOR);

		for (NodeSlot s : slots)
			queue.add(new WeightedNodeSlot(s, calcWeight(s)));

		return queue;
	}

	private double calcWeight(NodeSlot slot) {
		SpaceTimeSlot stSlot = slot.getSlot();
		Point p1 = stSlot.getStartLocation();
		Point p2 = location;
		Point p3 = stSlot.getFinishLocation();

		// detour
		return (distance(p1, p2) + distance(p2, p3)) - distance(p1, p3);
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public NodeSlot next() {
		return iterator.next().slot;
	}

}
