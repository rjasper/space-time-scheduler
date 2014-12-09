package world.pathfinder;

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import world.World;
import world.fixtures.WorldFixtures;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class StraightEdgePathfinderTest {
	
	// TODO implement abstract general spatial pathfinder test

	@Test
	public void test() {
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();
		
		World world = WorldFixtures.twoRooms();
		Collection<Polygon> obstacles = world.getStaticObstacles();
		
		Point startPoint = builder.point(23.0, 29.0);
		Point finishPoint = builder.point(11.0, 11.0);
		
		StraightEdgePathfinder pf = new StraightEdgePathfinder();
		
		pf.setMaxConnectionDistance(100.0);
		pf.setStaticObstacles(obstacles);
		pf.setStartLocation(startPoint);
		pf.setFinishLocation(finishPoint);
		
		boolean status = pf.calculate();
		
		assertTrue(status);
	}

}
