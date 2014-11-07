package world;

import static matchers.GeometryMatchers.*;
import static org.junit.Assert.assertThat;
import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

public class WorldMapTest {
	
	private EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();
	
	private World world;
	
	@Before
	public void setUp() {
		world = new World();
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
		Polygon p1 = geomBuilder.polygon(10., 10., 20., 10., 20., 20., 10., 20., 10., 10.);
		Polygon p2 = geomBuilder.polygon(20., 15., 30., 15., 30., 25., 20., 25., 20., 15.);
		
		world.add(p1, p2);
		world.ready();
		
		assertThat(world.getMap(), equalTo(p1.union(p2)));
	}

}
