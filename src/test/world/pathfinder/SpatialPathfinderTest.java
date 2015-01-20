package world.pathfinder;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import world.StaticObstacle;
import world.World;
import world.fixtures.WorldFixtures;

import com.vividsolutions.jts.geom.Point;

public abstract class SpatialPathfinderTest {
	
	protected abstract SpatialPathfinder createPathfinder();
	
	@Test
	public void test() {
		World world = WorldFixtures.twoRooms();
		Collection<StaticObstacle> obstacles = world.getStaticObstacles();
		
		Point startPoint = point(23.0, 29.0);
		Point finishPoint = point(11.0, 11.0);
		
		SpatialPathfinder pf = createPathfinder();
		
		pf.setStaticObstacles(obstacles);
		pf.setStartLocation(startPoint);
		pf.setFinishLocation(finishPoint);
		
		boolean status = pf.calculate();
		
		assertTrue(status);
	}

}
