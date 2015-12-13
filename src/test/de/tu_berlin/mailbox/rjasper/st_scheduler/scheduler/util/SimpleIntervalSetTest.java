package de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util;

import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.atSecond;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.tu_berlin.mailbox.rjasper.st_scheduler.scheduler.util.IntervalSet.Interval;

public class SimpleIntervalSetTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testIsEmptyPositive() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testIsEmptyNegative() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.isEmpty(), is(false));
	}
	
	@Test
	public void testAdd() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testAddLeft() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		set.add(atSecond(-2), atSecond(-1));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(-1),
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testAddRight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(-2), atSecond(-1));
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(-1),
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testAddLeftTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		set.add(atSecond(-2), atSecond(0));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testAddRightTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(-2), atSecond(0));
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testAddLeftOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(-1), atSecond(1));
		set.add(atSecond(-2), atSecond(0));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testAddRightOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(-2), atSecond(0));
		set.add(atSecond(-1), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testAddSmall() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(10));
		set.add(atSecond(2), atSecond(8));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(10)
		));
	}
	
	@Test
	public void testAddBig() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(2), atSecond(8));
		set.add(atSecond(0), atSecond(10));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(10)
		));
	}

	@Test
	public void testAddIdentical() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}

	@Test
	public void testAddSet() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		
		set1.add(atSecond(0), atSecond(1));
		set1.add(atSecond(4), atSecond(5));
		set2.add(atSecond(2), atSecond(3));
		set2.add(atSecond(6), atSecond(7));
		
		set1.add(set2);
		
		assertThat(set1, equalToIntervals(
			atSecond(0), atSecond(1),
			atSecond(2), atSecond(3),
			atSecond(4), atSecond(5),
			atSecond(6), atSecond(7)
		));
	}
	
	@Test
	public void testClear() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
	
		set.add(atSecond(0), atSecond(1));
		set.clear();
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testAddSelf() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
	
		set.add(atSecond(0), atSecond(1));
		set.add(set);
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}

	@Test
	public void testRemoveEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.remove(atSecond(0), atSecond(1));
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testRemoveLeft() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(-2), atSecond(-1));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveRight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(2), atSecond(3));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveLeftTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(-2), atSecond(0));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveRightTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(1), atSecond(3));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveLeftOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(-2), atSecond(0.5));
		
		assertThat(set, equalToIntervals(
			atSecond(0.5), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveRightOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(0.5), atSecond(3));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(0.5)
		));
	}
	
	@Test
	public void testRemoveSmall() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(0.25), atSecond(0.75));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(0.25),
			atSecond(0.75), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveBig() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(-1), atSecond(2));
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testRemoveIdentical() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(0), atSecond(1));
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testRemoveSet() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		
		set1.add(atSecond(0), atSecond(2));
		set1.add(atSecond(3), atSecond(5));
		set2.add(atSecond(1), atSecond(4));
		set2.add(atSecond(6), atSecond(7));
		
		set1.remove(set2);
		
		assertThat(set1, equalToIntervals(
			atSecond(0), atSecond(1),
			atSecond(4), atSecond(5)
		));
	}
	
	@Test
	public void testRemoveSelf() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		
		set.remove(set);
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testIntersectEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.intersectImpl(atSecond(0), atSecond(1));
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testIntersectLeftEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.intersectImpl(atSecond(-1), atSecond(-0.5));

		assertThat(set.isEmpty(), is(true));
	}

	@Test
	public void testIntersectRightEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.intersectImpl(atSecond(1.5), atSecond(2));

		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testIntersectLeftTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.intersectImpl(atSecond(-1), atSecond(0));
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testIntersectRightTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.intersectImpl(atSecond(1), atSecond(2));

		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testIntersectLeftOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.intersectImpl(atSecond(-1), atSecond(0.5));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(0.5)));
	}

	@Test
	public void testIntersectRightOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.intersectImpl(atSecond(0.5), atSecond(2));
		
		assertThat(set, equalToIntervals(
			atSecond(0.5), atSecond(1)));
	}
	
	@Test
	public void testIntersectSmall() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.intersectImpl(atSecond(0.25), atSecond(0.75));
		
		assertThat(set, equalToIntervals(
			atSecond(0.25), atSecond(0.75)));
	}
	
	@Test
	public void testIntersectBig() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.intersectImpl(atSecond(-1), atSecond(2));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)));
	}
	
	public void testIntesectIdentical() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.intersectImpl(atSecond(0), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)));
	}
	
	@Test
	public void testIntersectSet() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		
		set1.add(atSecond(0), atSecond(2));
		set1.add(atSecond(3), atSecond(5));
		set2.add(atSecond(1), atSecond(4));
		set2.add(atSecond(6), atSecond(7));
		
		set1.intersect(set2);
		
		assertThat(set1, equalToIntervals(
			atSecond(1), atSecond(2),
			atSecond(3), atSecond(4)
		));
	}
	
	@Test
	public void testIntersectSelf() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		set.intersect(set);
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)));
	}
	
	@Test
	public void testSealAddSimple() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		set.seal();
		
		thrown.expect(IllegalStateException.class);
		
		set.add(atSecond(0), atSecond(1));
	}

	@Test
	public void testSealAddSet() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		set1.seal();

		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		set2.add(atSecond(0), atSecond(1));
		
		thrown.expect(IllegalStateException.class);
		
		set1.add(set2);
	}

	@Test
	public void testSealRemoveSimple() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		set.seal();
		
		thrown.expect(IllegalStateException.class);
		
		set.remove(atSecond(0), atSecond(1));
	}

	@Test
	public void testSealRemoveSet() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		set1.seal();

		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		set2.add(atSecond(0), atSecond(1));
		
		thrown.expect(IllegalStateException.class);
		
		set1.remove(set2);
	}

	@Test
	public void testSealIntersectSimple() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		set.seal();
		
		thrown.expect(IllegalStateException.class);
		
		set.intersect(atSecond(0), atSecond(1));
	}

	@Test
	public void testSealIntsectSet() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		set1.seal();

		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		set2.add(atSecond(0), atSecond(1));

		thrown.expect(IllegalStateException.class);
		set1.intersect(set2);
	}

	@Test
	public void testSealClear() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
	
		set.seal();
		
		thrown.expect(IllegalStateException.class);
		set.clear();
	}
	
	@Test
	public void testUnion() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		SimpleIntervalSet<LocalDateTime> res = set.union(atSecond(0), atSecond(1));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testUnionLeft() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.union(atSecond(-2), atSecond(-1));
		
		assertThat(res, equalToIntervals(
			atSecond(-2), atSecond(-1),
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testUnionRight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(-2), atSecond(-1));
		SimpleIntervalSet<LocalDateTime> res = set.union(atSecond(0), atSecond(1));
		
		assertThat(res, equalToIntervals(
			atSecond(-2), atSecond(-1),
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testUnionLeftTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.union(atSecond(-2), atSecond(0));
		
		assertThat(res, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testUnionRightTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(-2), atSecond(0));
		SimpleIntervalSet<LocalDateTime> res = set.union(atSecond(0), atSecond(1));
		
		assertThat(res, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testUnionLeftOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(-1), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.union(atSecond(-2), atSecond(0));
		
		assertThat(res, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testUnionRightOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(-2), atSecond(0));
		SimpleIntervalSet<LocalDateTime> res = set.union(atSecond(-1), atSecond(1));
		
		assertThat(res, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testUnionSmall() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(10));
		SimpleIntervalSet<LocalDateTime> res = set.union(atSecond(2), atSecond(8));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(10)
		));
	}
	
	@Test
	public void testUnionBig() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(2), atSecond(8));
		SimpleIntervalSet<LocalDateTime> res = set.union(atSecond(0), atSecond(10));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(10)
		));
	}

	@Test
	public void testUnionIdentical() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.union(atSecond(0), atSecond(1));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}

	@Test
	public void testUnionSet() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		
		set1.add(atSecond(0), atSecond(1));
		set1.add(atSecond(4), atSecond(5));
		set2.add(atSecond(2), atSecond(3));
		set2.add(atSecond(6), atSecond(7));
		
		SimpleIntervalSet<LocalDateTime> res = set1.union(set2);
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1),
			atSecond(2), atSecond(3),
			atSecond(4), atSecond(5),
			atSecond(6), atSecond(7)
		));
	}
	
	@Test
	public void testUnionSelf() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
	
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.union(set);
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}

	@Test
	public void testDifferenceEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		SimpleIntervalSet<LocalDateTime> res = set.difference(atSecond(0), atSecond(1));
		
		assertThat(res.isEmpty(), is(true));
	}
	
	@Test
	public void testDifferenceLeft() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.difference(atSecond(-2), atSecond(-1));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testDifferenceRight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.difference(atSecond(2), atSecond(3));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testDifferenceLeftTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.difference(atSecond(-2), atSecond(0));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testDifferenceRightTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.difference(atSecond(1), atSecond(3));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testDifferenceLeftOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.difference(atSecond(-2), atSecond(0.5));
		
		assertThat(res, equalToIntervals(
			atSecond(0.5), atSecond(1)
		));
	}
	
	@Test
	public void testDifferenceRightOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.difference(atSecond(0.5), atSecond(3));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(0.5)
		));
	}
	
	@Test
	public void testDifferenceSmall() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.difference(atSecond(0.25), atSecond(0.75));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(0.25),
			atSecond(0.75), atSecond(1)
		));
	}
	
	@Test
	public void testDifferenceBig() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.difference(atSecond(-1), atSecond(2));
		
		assertThat(res.isEmpty(), is(true));
	}
	
	@Test
	public void testDifferenceIdentical() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.difference(atSecond(0), atSecond(1));
		
		assertThat(res.isEmpty(), is(true));
	}
	
	@Test
	public void testDifferenceSet() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		
		set1.add(atSecond(0), atSecond(2));
		set1.add(atSecond(3), atSecond(5));
		set2.add(atSecond(1), atSecond(4));
		set2.add(atSecond(6), atSecond(7));
		
		SimpleIntervalSet<LocalDateTime> res = set1.difference(set2);
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1),
			atSecond(4), atSecond(5)
		));
	}
	
	@Test
	public void testDifferenceSelf() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		
		SimpleIntervalSet<LocalDateTime> res = set.difference(set);
		
		assertThat(res.isEmpty(), is(true));
	}
	
	@Test
	public void testIntersectionEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		SimpleIntervalSet<LocalDateTime> res = set.intersection(atSecond(0), atSecond(1));
		
		assertThat(res.isEmpty(), is(true));
	}
	
	@Test
	public void testIntersectionLeftEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.intersection(atSecond(-1), atSecond(-0.5));

		assertThat(res.isEmpty(), is(true));
	}

	@Test
	public void testIntersectionRightEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.intersection(atSecond(1.5), atSecond(2));

		assertThat(res.isEmpty(), is(true));
	}
	
	@Test
	public void testIntersectionLeftTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.intersection(atSecond(-1), atSecond(0));
		
		assertThat(res.isEmpty(), is(true));
	}
	
	@Test
	public void testIntersectionRightTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.intersection(atSecond(1), atSecond(2));

		assertThat(res.isEmpty(), is(true));
	}
	
	@Test
	public void testIntersectionLeftOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.intersection(atSecond(-1), atSecond(0.5));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(0.5)));
	}

	@Test
	public void testIntersectionRightOverlap() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.intersection(atSecond(0.5), atSecond(2));
		
		assertThat(res, equalToIntervals(
			atSecond(0.5), atSecond(1)));
	}
	
	@Test
	public void testIntersectionSmall() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.intersection(atSecond(0.25), atSecond(0.75));
		
		assertThat(res, equalToIntervals(
			atSecond(0.25), atSecond(0.75)));
	}
	
	@Test
	public void testIntersectionBig() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.intersection(atSecond(-1), atSecond(2));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1)));
	}
	
	public void testIntesectionIdentical() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.intersection(atSecond(0), atSecond(1));
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1)));
	}
	
	@Test
	public void testIntersectionSet() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		
		set1.add(atSecond(0), atSecond(2));
		set1.add(atSecond(3), atSecond(5));
		set2.add(atSecond(1), atSecond(4));
		set2.add(atSecond(6), atSecond(7));
		
		SimpleIntervalSet<LocalDateTime> res = set1.intersection(set2);
		
		assertThat(res, equalToIntervals(
			atSecond(1), atSecond(2),
			atSecond(3), atSecond(4)
		));
	}
	
	@Test
	public void testIntersectionSelf() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		SimpleIntervalSet<LocalDateTime> res = set.intersection(set);
		
		assertThat(res, equalToIntervals(
			atSecond(0), atSecond(1)));
	}

	@Test
	public void testMinValueEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		thrown.expect(IllegalStateException.class);
		
		set.minValue();
	}

	@Test
	public void testMaxValueEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		thrown.expect(IllegalStateException.class);
		
		set.maxValue();
	}

	@Test
	public void testMinValue() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.minValue(), is(atSecond(0)));
	}

	@Test
	public void testMaxValue() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
	
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.maxValue(), is(atSecond(1)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFloorValue1() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		set.floorValue(-1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFloorValue2() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		set.floorValue(0);
	}

	@Test
	public void testFloorValue3() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		assertThat(set.floorValue(1), is(1));
	}

	@Test
	public void testFloorValue4() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		assertThat(set.floorValue(2), is(2));
	}

	@Test
	public void testFloorValue5() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		assertThat(set.floorValue(3), is(2));
	}
	
	@Test
	public void testFloorValue6() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		assertThat(set.floorValue(6), is(6));
	}
	
	@Test
	public void testFloorValue7() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		assertThat(set.floorValue(7), is(6));
	}
	
	@Test
	public void testCeilingValue1() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		assertThat(set.ceilingValue(-1), is(0));
	}

	@Test
	public void testCeilingValue2() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		assertThat(set.ceilingValue(0), is(0));
	}

	@Test
	public void testCeilingValue3() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		assertThat(set.ceilingValue(1), is(1));
	}

	@Test
	public void testCeilingValue4() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		assertThat(set.ceilingValue(2), is(4));
	}

	@Test
	public void testCeilingValue5() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		assertThat(set.ceilingValue(3), is(4));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCeilingValue6() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);
		
		set.ceilingValue(6);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCeilingValue7() {
		SimpleIntervalSet<Integer> set = new SimpleIntervalSet<>();
		set.add(0, 2);
		set.add(4, 6);

		set.ceilingValue(7);
	}

	@Test
	public void testContainsLeftTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.contains(atSecond(0)), is(true));
	}
	
	@Test
	public void testContainsWithin() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.contains(atSecond(0.5)), is(true));
	}
	
	@Test
	public void testContainsRightTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.contains(atSecond(1)), is(false));
	}
	
	@Test
	public void testContainsLeftOutside() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.contains(atSecond(-1)), is(false));
	}
	
	@Test
	public void testContainsRightOutside() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.contains(atSecond(2)), is(false));
	}
	
	@Test
	public void testIntersectsEmpty() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();
		
		assertThat(set.intersects(atSecond(0), atSecond(1)), is(false));
	}
	
	@Test
	public void testIntersectsLeftOutside() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(-2), atSecond(-1)), is(false));
	}
	
	@Test
	public void testIntersectsRightOutside() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(2), atSecond(3)), is(false));
	}
	
	@Test
	public void testIntersectsLeftTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(-1), atSecond(0)), is(false));
	}
	
	@Test
	public void testIntersectsRightTight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(1), atSecond(2)), is(false));
	}
	
	@Test
	public void testIntersectsLeft() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(-1), atSecond(0.5)), is(true));
	}
	
	@Test
	public void testIntersectsRight() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(0.5), atSecond(2)), is(true));
	}
	
	@Test
	public void testIntersectsSmall() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(0.25), atSecond(0.75)), is(true));
	}
	
	@Test
	public void testIntersectsBig() {
		SimpleIntervalSet<LocalDateTime> set = new SimpleIntervalSet<>();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(-1), atSecond(2)), is(true));
	}
	
	@Test
	public void testIntersectsSetNegative() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		
		set1.add(atSecond(0), atSecond(1));
		set1.add(atSecond(4), atSecond(5));
		set2.add(atSecond(2), atSecond(3));
		set2.add(atSecond(6), atSecond(7));
		
		assertThat(set1.intersects(set2), is(false));
	}
	
	@Test
	public void testIntersectsSetPositive() {
		SimpleIntervalSet<LocalDateTime> set1 = new SimpleIntervalSet<>();
		SimpleIntervalSet<LocalDateTime> set2 = new SimpleIntervalSet<>();
		
		set1.add(atSecond(0), atSecond(1));
		set1.add(atSecond(3), atSecond(5));
		set2.add(atSecond(2), atSecond(4));
		set2.add(atSecond(6), atSecond(7));
		
		assertThat(set1.intersects(set2), is(true));
	}

	public static Matcher<SimpleIntervalSet<LocalDateTime>> equalToIntervals(LocalDateTime... times) {
		if (times.length % 2 != 0)
			throw new IllegalArgumentException("invalid number of times");
		
		return new TypeSafeMatcher<SimpleIntervalSet<LocalDateTime>>() {
			@Override
			public void describeTo(Description description) {
				description
					.appendText("a TimeInterval equal to ")
					.appendText(Arrays.toString(times));
			}

			@Override
			protected boolean matchesSafely(SimpleIntervalSet<LocalDateTime> item) {
				List<Interval<LocalDateTime>> intervals = item.toList();
				
				if (2*intervals.size() != times.length)
					return false;
				
				int i = 0;
				
				for (Interval<LocalDateTime> ti : intervals) {
					boolean status;
					
					status = ti.getFromInclusive().isEqual(times[i++]);
					
					if (!status)
						return false;
					
					status = ti.getToExclusive().isEqual(times[i++]);
					
					if (!status)
						return false;
				}
				
				return true;
			}
		};
	}

}
