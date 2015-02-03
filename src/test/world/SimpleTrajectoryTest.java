package world;

import static util.TimeFactory.*;
import static world.factories.PathFactory.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SimpleTrajectoryTest {

	@Test
	public void testSubTrajectoryEmptyTrajectory() {
		Trajectory t = SimpleTrajectory.empty();
		Trajectory sub = t.subTrajectory(LocalDateTime.MIN, LocalDateTime.MAX);
		
		assertThat("sub-trajectory is not empty",
			sub, equalTo(SimpleTrajectory.empty()));
	}
	
	@Test
	public void testSubTrajectoryEmptyInterval() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		Trajectory sub = t.subTrajectory(atSecond(2), atSecond(1));
		
		assertThat("sub-trajectory is not empty",
			sub, equalTo(SimpleTrajectory.empty()));
	}
	
	@Test
	public void testSubTrajectoryEmptyIntersection() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		Trajectory sub = t.subTrajectory(atSecond(2), atSecond(3));
		
		assertThat("sub-trajectory is not empty",
			sub, equalTo(SimpleTrajectory.empty()));
	}
	
	@Test
	public void testSubTrajectoryIdentical() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		Trajectory sub = t.subTrajectory(LocalDateTime.MIN, LocalDateTime.MAX);

		assertThat("sub-trajectory is not identical",
			sub, equalTo(t));
	}
	
	@Test
	public void testSubTrajectoryIdenticalTight() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		Trajectory sub = t.subTrajectory(atSecond(1), atSecond(2));

		assertThat("sub-trajectory is not identical",
			sub, equalTo(t));
	}
	
	@Test
	public void testSubTrajectoryIntersectLeft() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		Trajectory sub = t.subTrajectory(atSecond(0), atSecond(1.5));
		
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
		Trajectory sub = t.subTrajectory(atSecond(1.5), atSecond(3));
		
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
		Trajectory sub = t.subTrajectory(atSecond(1.25), atSecond(1.75));
		
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
		Trajectory sub = t.subTrajectory(atSecond(1.25), atSecond(1.75));
		
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
		Trajectory sub = t.subTrajectory(atSecond(1.25), atSecond(1.75));
		
		Trajectory expected = new SimpleTrajectory(
			spatialPath(-0.5, 0.5, 0.5, -0.5),
			ImmutableList.of(atSecond(1.25), atSecond(1.75)));

		assertThat("sub-trajectory not as expected",
			sub, equalTo(expected));
	}

}
