package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers;

import static com.vividsolutions.jts.operation.distance.DistanceOp.distance;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.Scheduler;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.SpaceTimeSlot;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers.NodeSlotIterator.NodeSlot;

public class LeastDetourNodeSlotIterator implements Iterator<NodeSlot> {

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

	public LeastDetourNodeSlotIterator(Iterable<NodeSlot> slots, Point location) {
		this.iterator = makeIterator( Objects.requireNonNull(slots, "slots"), location );
	}

	private static Iterator<WeightedNodeSlot> makeIterator(Iterable<NodeSlot> slots, Point location) {
		List<WeightedNodeSlot> list = new LinkedList<>();

		for (NodeSlot s : slots)
			list.add(new WeightedNodeSlot(s, calcWeight(s, location)));

		return list.stream().sorted(COMPARATOR).iterator();
	}

	private static double calcWeight(NodeSlot slot, Point location) {
		SpaceTimeSlot stSlot = slot.getSlot();
		Point p1 = stSlot.getStartLocation();
		Point p2 = location;
		Point p3 = stSlot.getFinishLocation();

		// TODO bug not reproduced and tested

		// detour
		if (stSlot.getFinishTime().compareTo(Scheduler.END_OF_TIME) == 0)
			return distance(p1, p2);
		else
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
