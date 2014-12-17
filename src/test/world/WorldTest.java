package world;

import static java.util.Collections.emptyList;
import static matchers.GeometryMatchers.isEmpty;
import static matchers.GeometryMatchers.topologicallyEqualTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import jts.geom.factories.EnhancedGeometryBuilder;

import org.junit.Test;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

public class WorldTest {

	private EnhancedGeometryBuilder geomBuilder = EnhancedGeometryBuilder.getInstance();

	@Test
	public void testEmpty() {
		World world = new World();

		assertThat(world.getMap(), isEmpty());
	}

	@Test
	public void testSome() throws ParseException {
		Polygon p1 = geomBuilder.polygon(10., 10., 20., 10., 20., 20., 10., 20., 10., 10.);
		Polygon p2 = geomBuilder.polygon(20., 15., 30., 15., 30., 25., 20., 25., 20., 15.);

		Collection<Polygon> staticObstacles = Arrays.asList(p1, p2);

		World world = new World(staticObstacles, emptyList());

		assertThat(world.getMap(), topologicallyEqualTo(p1.union(p2)));
	}

}
