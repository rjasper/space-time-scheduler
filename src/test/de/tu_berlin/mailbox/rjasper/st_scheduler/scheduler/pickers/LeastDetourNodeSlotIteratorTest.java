package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.*;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.SpaceTimeSlot;
import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.pickers.NodeSlotIterator.NodeSlot;

public class LeastDetourNodeSlotIteratorTest {

	private static NodeSlot slot(
		double x1, double y1, double t1,
		double x2, double y2, double t2)
	{
		return new NodeSlot(
			null,
			new SpaceTimeSlot(
				immutablePoint(x1, y1),
				immutablePoint(x2, y2),
				atSecond(t1),
				atSecond(t2)));
	}

	private static <T> List<T> collect(Iterator<T> iterator) {
		List<T> list = new LinkedList<>();

		while (iterator.hasNext())
			list.add(iterator.next());

		return list;
	}

	@Test
	public void testEmpty() {
		Collection<NodeSlot> slots = emptyList();
		ImmutablePoint location = immutablePoint(0, 0);

		LeastDetourNodeSlotIterator it = new LeastDetourNodeSlotIterator(slots, location);

		assertThat("iterator has unexpected element",
			it.hasNext(), is(false));
	}

	@Test
	public void testSingle() {
		ImmutablePoint location = immutablePoint(1, 0);
		NodeSlot slot = slot(0, 0, 0, 1, 1, 1);
		Collection<NodeSlot> slots = singletonList(slot);

		LeastDetourNodeSlotIterator it = new LeastDetourNodeSlotIterator(slots, location);

		assertThat(collect(it), equalTo( singletonList(slot)) );
	}

	@Test
	public void testTwo1() {
		ImmutablePoint location = immutablePoint(-0.5, 0.5);
		NodeSlot slot1 = slot(0, 0, 0, 0, 1, 1);
		NodeSlot slot2 = slot(0, 1, 1, 1, 1, 2);
		Collection<NodeSlot> slots = Arrays.asList(slot1, slot2);

		LeastDetourNodeSlotIterator it = new LeastDetourNodeSlotIterator(slots, location);

		assertThat(collect(it), equalTo( Arrays.asList(slot1, slot2)) );
	}

	@Test
	public void testTwo2() {
		ImmutablePoint location = immutablePoint(0.5, 1.5);
		NodeSlot slot1 = slot(0, 0, 0, 0, 1, 1);
		NodeSlot slot2 = slot(0, 1, 1, 1, 1, 2);
		Collection<NodeSlot> slots = Arrays.asList(slot1, slot2);

		LeastDetourNodeSlotIterator it = new LeastDetourNodeSlotIterator(slots, location);

		assertThat(collect(it), equalTo( Arrays.asList(slot2, slot1)) );
	}

}
