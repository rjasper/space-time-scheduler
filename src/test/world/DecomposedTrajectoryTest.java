package world;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeFactory.*;
import static world.factories.PathFactory.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import jts.geom.immutable.ImmutablePoint;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;

public class DecomposedTrajectoryTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
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
	
	@Test
	public void testIsStationaryEmpty() {
		DecomposedTrajectory traj = new DecomposedTrajectory(
			atSecond(0),
			SpatialPath.empty(),
			ArcTimePath.empty());

		thrown.expect(IllegalStateException.class);
		
		traj.isStationary(atSecond(0), atSecond(1));
	}
	
	@Test
	public void testIsStationaryOutside() {
		DecomposedTrajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 1, 0),
			arcTimePath(0, 0, 1, 1));

		thrown.expect(IllegalArgumentException.class);
		
		traj.isStationary(atSecond(1), atSecond(2));
	}
	
	public void testIsStationaryPositiveSimple() {
		DecomposedTrajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 0, 0),
			arcTimePath(0, 0, 1, 1));
		
		assertThat(traj.isStationary(atSecond(0), atSecond(1)), is(true));
	}
	
	public void testIsStationaryNegativeSimple() {
		DecomposedTrajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 1, 0),
			arcTimePath(0, 0, 1, 1));
		
		assertThat(traj.isStationary(atSecond(0), atSecond(1)), is(false));
	}
	
	public void testIsStationaryPositiveAdvanced() {
		DecomposedTrajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 1, 0),
			arcTimePath(0, 0, 0, 1, 1, 2));
		
		assertThat(traj.isStationary(atSecond(0.25), atSecond(0.75)), is(true));
	}
	
	public void testIsStationaryNegativeAdvanced() {
		DecomposedTrajectory traj = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(0, 0, 1, 0),
			arcTimePath(0, 0, 1, 1));
		
		
		assertThat(traj.isStationary(atSecond(0.25), atSecond(0.75)), is(false));
	}
	
	@Test
	public void testInterpolateLocationEmpty() {
		Trajectory t = DecomposedTrajectory.empty();

		thrown.expect(NoSuchElementException.class);
		
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
	
	@Test
	public void testSubTrajectoryEmptyTrajectory() {
		Trajectory t = DecomposedTrajectory.empty();

		thrown.expect(NoSuchElementException.class);
		
		t.subPath(LocalDateTime.MIN, LocalDateTime.MAX);
	}
	
	@Test
	public void testSubTrajectoryEmptyInterval() {
		Trajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));

		thrown.expect(IllegalArgumentException.class);
		
		t.subPath(atSecond(10), atSecond(0));
	}
	
	@Test
	public void testSubTrajectoryEmptyIntersection() {
		Trajectory t = new DecomposedTrajectory(
			atSecond(0),
			spatialPath(-4, 3, 4, -3),
			arcTimePath(0, 0, 10, 10));

		thrown.expect(IllegalArgumentException.class);
		
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
