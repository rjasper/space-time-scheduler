package world;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeFactory.*;
import static world.factories.PathFactory.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SimpleTrajectoryTest {
	
	@Test(expected = NoSuchElementException.class)
	public void testSubTrajectoryEmptyTrajectory() {
		Trajectory t = SimpleTrajectory.empty();
		
		t.subPath(LocalDateTime.MIN, LocalDateTime.MAX);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSubTrajectoryEmptyInterval() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		
		t.subPath(atSecond(2), atSecond(1));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSubTrajectoryEmptyIntersection() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		t.subPath(atSecond(2), atSecond(3));
	}
	
	@Test
	public void testSubTrajectoryIdenticalTight() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		Trajectory sub = t.subPath(atSecond(1), atSecond(2));

		assertThat("sub-trajectory is not identical",
			sub, equalTo(t));
	}
	
	@Test
	public void testSubTrajectoryIntersectLeft() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		Trajectory sub = t.subPath(atSecond(1), atSecond(1.5));
		
		Trajectory expected = new SimpleTrajectory(
			spatialPath(-1, 1, 0, 0),
			ImmutableList.of(atSecond(1), atSecond(1.5)));

		assertThat("sub-trajectory not as expected",
			sub, equalTo(expected));
	}
	
	@Test
	public void testSubTrajectoryIntersectRight() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		Trajectory sub = t.subPath(atSecond(1.5), atSecond(2));
		
		Trajectory expected = new SimpleTrajectory(
			spatialPath(0, 0, 1, -1),
			ImmutableList.of(atSecond(1.5), atSecond(2)));

		assertThat("sub-trajectory not as expected",
			sub, equalTo(expected));
	}
	
	@Test
	public void testSubTrajectoryIntersectSmallCore() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		Trajectory sub = t.subPath(atSecond(1.25), atSecond(1.75));
		
		Trajectory expected = new SimpleTrajectory(
			spatialPath(-0.5, 0.5, 0.5, -0.5),
			ImmutableList.of(atSecond(1.25), atSecond(1.75)));

		assertThat("sub-trajectory not as expected",
			sub, equalTo(expected));
	}
	
	@Test
	public void testSubTrajectoryIntersectBigCore() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 0, 0, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(1.5), atSecond(2)));
		Trajectory sub = t.subPath(atSecond(1.25), atSecond(1.75));
		
		Trajectory expected = new SimpleTrajectory(
			spatialPath(-0.5, 0.5, 0.0, 0.0, 0.5, -0.5),
			ImmutableList.of(atSecond(1.25), atSecond(1.5), atSecond(1.75)));

		assertThat("sub-trajectory not as expected",
			sub, equalTo(expected));
	}
	
	@Test
	public void testSubTrajectoryIntersectCoreTight() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, -0.5, 0.5, 0.5, -0.5, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(1.25), atSecond(1.75), atSecond(2)));
		Trajectory sub = t.subPath(atSecond(1.25), atSecond(1.75));
		
		Trajectory expected = new SimpleTrajectory(
			spatialPath(-0.5, 0.5, 0.5, -0.5),
			ImmutableList.of(atSecond(1.25), atSecond(1.75)));

		assertThat("sub-trajectory not as expected",
			sub, equalTo(expected));
	}

}
