package world.pathfinder;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import world.StaticObstacle;
import world.World;
import world.fixtures.WorldFixtures;

import com.vividsolutions.jts.geom.Point;

public class StraightEdgePathfinderTest {
	
	// TODO implement abstract general spatial pathfinder test

	@Test
	public void test() {
		World world = WorldFixtures.twoRooms();
		Collection<StaticObstacle> obstacles = world.getStaticObstacles();
		
		Point startPoint = point(23.0, 29.0);
		Point finishPoint = point(11.0, 11.0);
		
		StraightEdgePathfinder pf = new StraightEdgePathfinder();
		
		pf.setMaxConnectionDistance(100.0);
		pf.setStaticObstacles(obstacles);
		pf.setStartLocation(startPoint);
		pf.setFinishLocation(finishPoint);
		
		boolean status = pf.calculate();
		
		assertTrue(status);
	}

}
