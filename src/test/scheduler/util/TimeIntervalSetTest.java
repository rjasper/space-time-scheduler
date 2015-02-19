package scheduler.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeFactory.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import scheduler.util.TimeIntervalSet.TimeInterval;

public class TimeIntervalSetTest {
	
	@Test
	public void testIsEmptyPositive() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testIsEmptyNegative() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.isEmpty(), is(false));
	}
	
	@Test
	public void testAdd() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testAddLeft() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		set.add(atSecond(-2), atSecond(-1));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(-1),
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testAddRight() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(-2), atSecond(-1));
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(-1),
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testAddLeftTight() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		set.add(atSecond(-2), atSecond(0));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testAddRightTight() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(-2), atSecond(0));
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testAddLeftOverlap() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(-1), atSecond(1));
		set.add(atSecond(-2), atSecond(0));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testAddRightOverlap() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(-2), atSecond(0));
		set.add(atSecond(-1), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(-2), atSecond(1)
		));
	}
	
	@Test
	public void testAddSmall() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(10));
		set.add(atSecond(2), atSecond(8));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(10)
		));
	}
	
	@Test
	public void testAddBig() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(2), atSecond(8));
		set.add(atSecond(0), atSecond(10));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(10)
		));
	}

	@Test
	public void testAddIdentical() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}

	@Test
	public void testAddSet() {
		TimeIntervalSet set1 = new TimeIntervalSet();
		TimeIntervalSet set2 = new TimeIntervalSet();
		
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
	public void testAddSelf() {
		TimeIntervalSet set = new TimeIntervalSet();
	
		set.add(atSecond(0), atSecond(1));
		set.add(set);
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}

	@Test
	public void testRemoveEmpty() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.remove(atSecond(0), atSecond(1));
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testRemoveLeft() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(-2), atSecond(-1));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveRight() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(2), atSecond(3));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveLeftTight() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(-2), atSecond(0));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveRightTight() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(1), atSecond(3));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveLeftOverlap() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(-2), atSecond(0.5));
		
		assertThat(set, equalToIntervals(
			atSecond(0.5), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveRightOverlap() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(0.5), atSecond(3));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(0.5)
		));
	}
	
	@Test
	public void testRemoveSmall() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(0.25), atSecond(0.75));
		
		assertThat(set, equalToIntervals(
			atSecond(0), atSecond(0.25),
			atSecond(0.75), atSecond(1)
		));
	}
	
	@Test
	public void testRemoveBig() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(-1), atSecond(2));
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testRemoveIdentical() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		set.remove(atSecond(0), atSecond(1));
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test
	public void testRemoveSet() {
		TimeIntervalSet set1 = new TimeIntervalSet();
		TimeIntervalSet set2 = new TimeIntervalSet();
		
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
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		
		set.remove(set);
		
		assertThat(set.isEmpty(), is(true));
	}
	
	@Test(expected = IllegalStateException.class)
	public void testMinValueEmpty() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.minValue();
	}

	@Test(expected = IllegalStateException.class)
	public void testMaxValueEmpty() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.maxValue();
	}

	@Test
	public void testMinValue() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.minValue(), is(atSecond(0)));
	}

	@Test
	public void testMaxValue() {
		TimeIntervalSet set = new TimeIntervalSet();
	
		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.maxValue(), is(atSecond(1)));
	}

	@Test
	public void testContainsLeftTight() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.contains(atSecond(0)), is(true));
	}
	
	@Test
	public void testContainsWithin() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.contains(atSecond(0.5)), is(true));
	}
	
	@Test
	public void testContainsRightTight() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.contains(atSecond(1)), is(false));
	}
	
	@Test
	public void testContainsLeftOutside() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.contains(atSecond(-1)), is(false));
	}
	
	@Test
	public void testContainsRightOutside() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.contains(atSecond(2)), is(false));
	}
	
	@Test
	public void testIntersectsEmpty() {
		TimeIntervalSet set = new TimeIntervalSet();
		
		assertThat(set.intersects(atSecond(0), atSecond(1)), is(false));
	}
	
	@Test
	public void testIntersectsLeftOutside() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(-2), atSecond(-1)), is(false));
	}
	
	@Test
	public void testIntersectsRightOutside() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(2), atSecond(3)), is(false));
	}
	
	@Test
	public void testIntersectsLeftTight() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(-1), atSecond(0)), is(false));
	}
	
	@Test
	public void testIntersectsRightTight() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(1), atSecond(2)), is(false));
	}
	
	@Test
	public void testIntersectsLeft() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(-1), atSecond(0.5)), is(true));
	}
	
	@Test
	public void testIntersectsRight() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(0.5), atSecond(2)), is(true));
	}
	
	@Test
	public void testIntersectsSmall() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(0.25), atSecond(0.75)), is(true));
	}
	
	@Test
	public void testIntersectsBig() {
		TimeIntervalSet set = new TimeIntervalSet();

		set.add(atSecond(0), atSecond(1));
		
		assertThat(set.intersects(atSecond(-1), atSecond(2)), is(true));
	}
	
	@Test
	public void testIntersectsSetNegative() {
		TimeIntervalSet set1 = new TimeIntervalSet();
		TimeIntervalSet set2 = new TimeIntervalSet();
		
		set1.add(atSecond(0), atSecond(1));
		set1.add(atSecond(4), atSecond(5));
		set2.add(atSecond(2), atSecond(3));
		set2.add(atSecond(6), atSecond(7));
		
		assertThat(set1.intersects(set2), is(false));
	}
	
	@Test
	public void testIntersectsSetPositive() {
		TimeIntervalSet set1 = new TimeIntervalSet();
		TimeIntervalSet set2 = new TimeIntervalSet();
		
		set1.add(atSecond(0), atSecond(1));
		set1.add(atSecond(3), atSecond(5));
		set2.add(atSecond(2), atSecond(4));
		set2.add(atSecond(6), atSecond(7));
		
		assertThat(set1.intersects(set2), is(true));
	}

	public static Matcher<TimeIntervalSet> equalToIntervals(LocalDateTime... times) {
		if (times.length % 2 != 0)
			throw new IllegalArgumentException("invalid number of times");
		
		return new TypeSafeMatcher<TimeIntervalSet>() {
			@Override
			public void describeTo(Description description) {
				description
					.appendText("a TimeInterval equal to ")
					.appendText(Arrays.toString(times));
			}

			@Override
			protected boolean matchesSafely(TimeIntervalSet item) {
				List<TimeInterval> intervals = item.toList();
				
				if (2*intervals.size() != times.length)
					return false;
				
				int i = 0;
				
				for (TimeInterval ti : intervals) {
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
