package world;

import static matchers.GeometryMatchers.*;
import static org.junit.Assert.assertThat;
import jts.geom.factories.StaticJstFactories;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class WorldMapTest {
	
	private static GeometryFactory factory() {
		return StaticJstFactories.floatGeometryFactory();
	}
	
	private static WKTReader wkt() {
		return StaticJstFactories.wktReader();
	}
	
	private WorldMap world;
	
	@Before
	public void setUp() {
		world = new WorldMap();
	}
	
	@Test
	public void testAddNone() {
		world.ready();
		
		assertThat(world.getMap(), isEmpty());
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddNull1() {
		world.add((Polygon) null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddNull2() {
		world.add((Polygon[]) null);
	}
	
	@Test
	public void testAddSome() throws ParseException {
		Polygon p1 = (Polygon) wkt().read("POLYGON ((10 10, 20 10, 20 20, 10 20, 10 10))");
		Polygon p2 = (Polygon) wkt().read("POLYGON ((20 15, 30 15, 30 25, 20 25, 20 15))");
		
		world.add(p1, p2);
		world.ready();
		
		assertThat(world.getMap(), equalTo(p1.union(p2)));
	}

}
