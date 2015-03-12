package de.tu_berlin.mailbox.rjasper.st_scheduler.world.pathfinder;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.StaticObstacle;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.World;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.fixtures.WorldFixtures;

public abstract class AbstractSpatialPathfinderTest {
	
	protected abstract AbstractSpatialPathfinder createPathfinder();
	
	private boolean findPath(Collection<StaticObstacle> obstacles, Point startLocation, Point finishLocation) {
		AbstractSpatialPathfinder pf = createPathfinder();
		
		pf.setStaticObstacles(obstacles);
		pf.setStartLocation(startLocation);
		pf.setFinishLocation(finishLocation);
		
		return pf.calculate();
	}
	
	@Test
	public void test() {
		World world = WorldFixtures.twoRooms();
		Collection<StaticObstacle> obstacles = world.getStaticObstacles();
		
		Point startLocation = point(23, 29);
		Point finishLocation = point(11, 11);

		boolean status = findPath(obstacles, startLocation, finishLocation);
		
		assertTrue(status);
	}
	
	@Test
	public void testPolygonHolesPositive() {
		StaticObstacle obstacle = new StaticObstacle(
			immutablePolygon(
				linearRing(1, 1, 6, 1, 6, 5, 1, 5, 1, 1),
				linearRing(2, 2, 5, 2, 5, 4, 2, 4, 2, 2)));
		
		// inside the hole
		Point startLocation  = point(3, 3);
		Point finishLocation = point(4, 3);
		
		boolean status = findPath(singletonList(obstacle), startLocation, finishLocation);
		
		assertThat("no path found",
			status, equalTo(true));
	}
	
	@Test
	public void testPolygonHolesNegative() {
		StaticObstacle obstacle = new StaticObstacle(
			immutablePolygon(
				linearRing(1, 1, 6, 1, 6, 5, 1, 5, 1, 1),
				linearRing(2, 2, 5, 2, 5, 4, 2, 4, 2, 2)));
		
		Point startLocation  = point(1.5, 3); // within the wall
		Point finishLocation = point(4  , 3); // inside the hole
		
		boolean status = findPath(singletonList(obstacle), startLocation, finishLocation);
		
		assertThat("path found when it shouldn't",
			status, equalTo(false));
	}

}
