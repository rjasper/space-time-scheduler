package de.tu_berlin.kbs.swarmos.st_scheduler.world;

import static de.tu_berlin.kbs.swarmos.st_scheduler.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.util.TimeFactory.*;
import static de.tu_berlin.kbs.swarmos.st_scheduler.world.factories.TrajectoryFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.tu_berlin.kbs.swarmos.st_scheduler.world.Trajectory;
import de.tu_berlin.kbs.swarmos.st_scheduler.world.TrajectoryContainer;

public class TrajectoryContainerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testEmptyPositive() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		assertThat(container.isEmpty(), is(true));
		assertThat(container.getTrajectories().isEmpty(), is(true));
	}

	@Test
	public void testEmptyNegative() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		container.update(trajectory(0, 10, 0, 0, 0, 10));
		
		assertThat(container.isEmpty(), is(false));
		assertThat(container.getTrajectories().isEmpty(), is(false));
	}
	
	@Test
	public void testUpdateSimple() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		container.update(trajectory(0, 10, 0, 0, 0, 10));
		
		assertThat(container.getFirstTrajectory(),
			equalTo( trajectory(0, 10, 0, 0, 0, 10)) );
	}
	
	@Test
	public void TestUpdateAdvanced() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		container.update(trajectory(0, 10, 0, 0, 0, 10));
		container.update(trajectory(5, 5, 0, 5, 5, 10));
		
		Iterator<Trajectory> it = container.getTrajectories().iterator();
		
		assertThat(it.next(),
			equalTo( trajectory(0, 5, 0, 0, 0, 5)) );
		assertThat(it.next(),
			equalTo( trajectory(5, 5, 0, 5, 5, 10)) );
	}
	
	@Test
	public void testGetTrajectories() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 20, 0, 0, 10, 20));
		container.update(trajectory(20, 30, 0, 0, 20, 30));
		
		Iterator<Trajectory> it = container.getTrajectories(atSecond(15), atSecond(20))
			.iterator();
		
		assertThat(it.next(), equalTo( trajectory(10, 20, 0, 0, 10, 20) ));
		assertThat(it.hasNext(), is(false));
	}
	
	@Test
	public void testGetTrajectory() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 20, 0, 0, 10, 20));
		container.update(trajectory(20, 30, 0, 0, 20, 30));
		
		assertThat(container.getTrajectory(atSecond(15)),
			equalTo( trajectory(10, 20, 0, 0, 10, 20) ));
	}
	
	@Test
	public void testGetTrajectoryNegative() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 20, 0, 0, 10, 20));
		container.update(trajectory(20, 30, 0, 0, 20, 30));
		
		thrown.expect(IllegalArgumentException.class);
		
		container.getTrajectory(atSecond(-10));
	}
	
	@Test
	public void testGetTrajectoryOrNull() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 20, 0, 0, 10, 20));
		container.update(trajectory(20, 30, 0, 0, 20, 30));
		
		assertThat(container.getTrajectoryOrNull(atSecond(-10)),
			equalTo(null));
	}
	
	@Test
	public void testInterpolateLocation() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 20, 0, 0, 10, 20));
		container.update(trajectory(20, 30, 0, 0, 20, 30));
		
		assertThat(container.interpolateLocation(atSecond(15)),
			equalTo(immutablePoint(15, 0)));
	}
	
	@Test
	public void testIsContinuousPositive() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 20, 0, 0, 10, 20));
		container.update(trajectory(20, 30, 0, 0, 20, 30));
		
		assertThat(container.isContinuous(),
			is(true));
	}
	
	@Test
	public void testIsContinuousNegative() {
		TrajectoryContainer container = new TrajectoryContainer();
		
		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(15, 20, 0, 0, 15, 20));
		container.update(trajectory(20, 30, 0, 0, 20, 30));
		
		assertThat(container.isContinuous(),
			is(false));
	}
	
	@Test
	public void testIsStationaryPositive() {
		TrajectoryContainer container = new TrajectoryContainer();

		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 10, 0, 0, 10, 20));
		container.update(trajectory(10, 20, 0, 0, 20, 30));
		
		assertThat(container.isStationary(atSecond(10), atSecond(20)),
			is(true));
	}
	
	@Test
	public void testIsStationaryNegative() {
		TrajectoryContainer container = new TrajectoryContainer();

		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 20, 0, 0, 10, 20));
		container.update(trajectory(20, 30, 0, 0, 20, 30));
		
		assertThat(container.isStationary(atSecond(10), atSecond(20)),
			is(false));
	}
	
	@Test
	public void testDeleteHeadTight() {
		TrajectoryContainer container = new TrajectoryContainer();

		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 20, 0, 0, 10, 20));
		container.update(trajectory(20, 30, 0, 0, 20, 30));
		
		container.deleteBefore(atSecond(10));
		
		Iterator<Trajectory> it = container.getTrajectories().iterator();
		
		assertThat(it.next(), equalTo( trajectory(10, 20, 0, 0, 10, 20) ));
		assertThat(it.next(), equalTo( trajectory(20, 30, 0, 0, 20, 30) ));
		assertThat(it.hasNext(), is(false));
	}
	
	@Test
	public void testDeleteHeadOverlapping() {
		TrajectoryContainer container = new TrajectoryContainer();

		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 20, 0, 0, 10, 20));
		
		container.deleteBefore(atSecond(5));
		
		Iterator<Trajectory> it = container.getTrajectories().iterator();

		assertThat(it.next(), equalTo( trajectory( 0, 10, 0, 0,  0, 10) ));
		assertThat(it.next(), equalTo( trajectory(10, 20, 0, 0, 10, 20) ));
		assertThat(it.hasNext(), is(false));
	}
	
	@Test
	public void testCalcTrajectory() {
		TrajectoryContainer container = new TrajectoryContainer();

		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(10, 20, 0, 0, 10, 20));

		assertThat(container.calcTrajectory(), equalTo(trajectory(
			0, 10, 10, 20,
			0,  0,  0,  0,
			0, 10, 10, 20)));
	}
	
	@Test
	public void testCalcTrajectoryNonContinuous() {
		TrajectoryContainer container = new TrajectoryContainer();

		container.update(trajectory( 0, 10, 0, 0,  0, 10));
		container.update(trajectory(15, 20, 0, 0, 15, 20));
		
		thrown.expect(IllegalStateException.class);

		container.calcTrajectory();
	}

}
