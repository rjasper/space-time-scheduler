package world;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeFactory.*;
import static world.factories.PathFactory.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import jts.geom.immutable.ImmutablePoint;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class DecomposedTrajectoryTest {
	
	@Test
	public void testComposeEmpty() {
		DecomposedTrajectory t = DecomposedTrajectory.empty();
		
		assertThat("composition did not produce empty trajectory",
			t.composed(), equalTo(SimpleTrajectory.empty()));
	}
	
	@Test
	public void testComposeSimple() {
		DecomposedTrajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));
		Trajectory expected = new SimpleTrajectory(
			spatialPath(-4, 3, 4, -3),
			ImmutableList.of(atSecond(0), atSecond(10)));
		
		assertThat("composition did not produce empty trajectory",
			t.composed(), equalTo(expected));
	}
	
	@Test
	public void testComposeAdvanced() {
		DecomposedTrajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 0, 0, 4, -3),
			arcTimePath(0, 0, 2.5, 2.5, 2.5, 5, 7.5, 10, 7.5, 12.5, 10, 15));
		Trajectory expected = new SimpleTrajectory(
			spatialPath(-4, 3, -2, 1.5, -2, 1.5, 0, 0, 2, -1.5, 2, -1.5, 4, -3),
			ImmutableList.of(
				atSecond(0),
				atSecond(2.5),
				atSecond(5),
				atSecond(7.5),
				atSecond(10),
				atSecond(12.5),
				atSecond(15)));
		
		assertThat("composition did not produce empty trajectory",
			t.composed(), equalTo(expected));
	}
	
	@Test
	public void testComposeComplex() {
		DecomposedTrajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(7.5, 0, 2.5, 5));
		Trajectory expected = new SimpleTrajectory(
			spatialPath(2, -1.5, -2, 1.5),
			ImmutableList.of(atSecond(0), atSecond(5)));
		
		assertThat("composition did not produce empty trajectory",
			t.composed(), equalTo(expected));
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testInterpolateLocationEmpty() {
		Trajectory t = DecomposedTrajectory.empty();
		
		t.interpolateLocation(atSecond(0));
	}
	
	@Test
	public void testInterpolateLocationOnSpot() {
		Trajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));
		
		ImmutablePoint location = t.interpolateLocation(atSecond(5));
		ImmutablePoint expected = immutablePoint(0, 0);
		
		assertThat("unexpected interpolated location",
			location, equalTo(expected));
	}
	
	@Test
	public void testInterpolateLocationInBetween() {
		Trajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));
		
		ImmutablePoint location = t.interpolateLocation(atSecond(5));
		ImmutablePoint expected = immutablePoint(0, 0);
		
		assertThat("unexpected interpolated location",
			location, equalTo(expected));
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testSubTrajectoryEmptyTrajectory() {
		Trajectory t = DecomposedTrajectory.empty();
		
		t.subPath(LocalDateTime.MIN, LocalDateTime.MAX);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSubTrajectoryEmptyInterval() {
		Trajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));
		
		t.subPath(atSecond(10), atSecond(0));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSubTrajectoryEmptyIntersection() {
		Trajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));
		
		t.subPath(atSecond(10), atSecond(11));
	}
	
	@Test
	public void testSubTrajectoryIdentical() {
		DecomposedTrajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));
		DecomposedTrajectory sub = t.subPath(atSecond(0), atSecond(10));
		
		assertThat("sub-trajectory is not identical",
			sub.composed(), equalTo(t.composed()));
	}
	
	@Test
	public void testSubTrajectoryIntersectLeft() {
		DecomposedTrajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));
		
		DecomposedTrajectory sub = t.subPath(atSecond(0), atSecond(5));
		
		Trajectory expected = new SimpleTrajectory(
			spatialPath(-4, 3, 0, 0),
			ImmutableList.of(atSecond(0), atSecond(5)));

		assertThat("sub-trajectory not as expected",
			sub.composed(), equalTo(expected));
	}
	
	@Test
	public void testSubTrajectoryIntersectRight() {
		DecomposedTrajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));
		
		DecomposedTrajectory sub = t.subPath(atSecond(5), atSecond(10));
		
		Trajectory expected = new SimpleTrajectory(
			spatialPath(0, 0, 4, -3),
			ImmutableList.of(atSecond(5), atSecond(10)));

		assertThat("sub-trajectory not as expected",
			sub.composed(), equalTo(expected));
	}
	
	@Test
	public void testSubTrajectoryIntersectCore() {
		DecomposedTrajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));
		
		DecomposedTrajectory sub = t.subPath(atSecond(2.5), atSecond(7.5));
		
		Trajectory expected = new SimpleTrajectory(
			spatialPath(-2, 1.5, 2, -1.5),
			ImmutableList.of(atSecond(2.5), atSecond(7.5)));

		assertThat("sub-trajectory not as expected",
			sub.composed(), equalTo(expected));
	}

}
