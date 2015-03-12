package de.tu_berlin.kbs.swarmos.st_scheduler.world;

import static de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeFactory.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.world.factories.PathFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.SpatialPath;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory;

public class SimpleTrajectoryTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testIsStationaryEmpty() {
		SimpleTrajectory traj = new SimpleTrajectory(SpatialPath.empty(), ImmutableList.of());

		thrown.expect(IllegalStateException.class);
		
		traj.isStationary(atSecond(0), atSecond(1));
	}
	
	@Test
	public void testIsStationaryOutside() {
		SimpleTrajectory traj = new SimpleTrajectory(
			spatialPath(0, 0, 1, 1),
			ImmutableList.of(atSecond(0), atSecond(1)));

		thrown.expect(IllegalArgumentException.class);
		
		traj.isStationary(atSecond(1), atSecond(2));
	}
	
	public void testIsStationaryPositiveSimple() {
		SimpleTrajectory traj = new SimpleTrajectory(
			spatialPath(0, 0, 0, 0),
			ImmutableList.of(atSecond(0), atSecond(1)));
		
		
		assertThat(traj.isStationary(atSecond(0), atSecond(1)), is(true));
	}
	
	public void testIsStationaryNegativeSimple() {
		SimpleTrajectory traj = new SimpleTrajectory(
			spatialPath(0, 0, 1, 1),
			ImmutableList.of(atSecond(0), atSecond(1)));
		
		
		assertThat(traj.isStationary(atSecond(0), atSecond(1)), is(false));
	}
	
	public void testIsStationaryPositiveAdvanced() {
		SimpleTrajectory traj = new SimpleTrajectory(
			spatialPath(0, 0, 0, 0, 1, 1),
			ImmutableList.of(atSecond(0), atSecond(1), atSecond(2)));
		
		
		assertThat(traj.isStationary(atSecond(0.25), atSecond(1.75)), is(true));
	}
	
	public void testIsStationaryNegativeAdvanced() {
		SimpleTrajectory traj = new SimpleTrajectory(
			spatialPath(0, 0, 1, 1),
			ImmutableList.of(atSecond(0), atSecond(1)));
		
		
		assertThat(traj.isStationary(atSecond(0.25), atSecond(0.75)), is(false));
	}
	
	@Test
	public void testInterpolateLocationEmpty() {
		Trajectory t = SimpleTrajectory.empty();

		thrown.expect(NoSuchElementException.class);
		
		t.interpolateLocation(atSecond(0));
	}
	
	@Test
	public void testInterpolateLocationOnSpot() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		
		ImmutablePoint location = t.interpolateLocation(atSecond(1));
		ImmutablePoint expected = immutablePoint(-1, 1);
		
		assertThat("unexpected interpolated location",
			location, equalTo(expected));
	}
	
	@Test
	public void testInterpolateLocationInBetween() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));
		
		ImmutablePoint location = t.interpolateLocation(atSecond(1.5));
		ImmutablePoint expected = immutablePoint(0, 0);
		
		assertThat("unexpected interpolated location",
			location, equalTo(expected));
	}
	
	@Test
	public void testSubTrajectoryEmptyTrajectory() {
		Trajectory t = SimpleTrajectory.empty();

		thrown.expect(NoSuchElementException.class);
		
		t.subPath(LocalDateTime.MIN, LocalDateTime.MAX);
	}
	
	@Test
	public void testSubTrajectoryEmptyInterval() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));

		thrown.expect(IllegalArgumentException.class);
		
		t.subPath(atSecond(2), atSecond(1));
	}
	
	@Test
	public void testSubTrajectoryEmptyIntersection() {
		Trajectory t = new SimpleTrajectory(
			spatialPath(-1, 1, 1, -1),
			ImmutableList.of(atSecond(1), atSecond(2)));

		thrown.expect(IllegalArgumentException.class);
		
		t.subPath(atSecond(2), atSecond(3));
	}
	
	@Test
	public void testSubTrajectoryIdentical() {
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
