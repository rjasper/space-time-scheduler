package world.pathfinder;

import static org.junit.Assert.*;

import java.util.Collection;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import world.World;
import world.WorldFixtures;

public class StraightEdgePathfinderTest {

	@Test
	public void test() {
		EnhancedGeometryBuilder builder = EnhancedGeometryBuilder.getInstance();
		
		World world = WorldFixtures.twoRooms();
		Collection<Polygon> obstacles = world.getPolygonMap();
		
		Point startPoint = builder.point(23.0, 29.0);
		Point finishPoint = builder.point(11.0, 11.0);
		
		StraightEdgePathfinder pf = new StraightEdgePathfinder();
		
		pf.setMaxConnectionDistance(100.0);
		pf.setStaticObstacles(obstacles);
		pf.setStartPoint(startPoint);
		pf.setFinishPoint(finishPoint);
		
		boolean status = pf.calculate();
		
		assertTrue(status);
	}

}
