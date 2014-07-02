package world;

import static matchers.GeometryMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class WorldMapTest {
	
	private static final GeometryFactory factory = new GeometryFactory();
	
	private static final WKTReader wkt = new WKTReader();
	
	private static GeometryFactory factory() {
		return factory;
	}
	
	private static WKTReader wkt() {
		return wkt;
	}
	
	private WorldMap world;
	
	@Before
	public void setUp() {
		world = new WorldMap();
	}
	
	@Test
	public void testAddNone() {
		world.ready();
		
		assertThat(world._getMap(), isEmpty());
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddNull1() {
		world.add((Geometry) null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddNull2() {
		world.add((Geometry[]) null);
	}
	
	@Test
	public void testAddSome() throws ParseException {
		Geometry g1 = wkt().read("POLYGON ((10 10, 20 10, 20 20, 10 20, 10 10))");
		Geometry g2 = wkt().read("POLYGON ((20 15, 30 15, 30 25, 20 25, 20 15))");
		
		world.add(g1, g2);
		world.ready();
		
		assertTrue(g1.union(g2) .equals (world._getMap()));
		assertThat(world.getMap(), equalTo(g1.union(g2)));
	}

}
