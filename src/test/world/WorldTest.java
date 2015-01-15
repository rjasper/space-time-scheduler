package world;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static matchers.GeometryMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.vividsolutions.jts.io.ParseException;

public class WorldTest {

	@Test
	public void testEmpty() {
		World world = new World();

		assertThat(world.getMap(), isEmpty());
	}

	@Test
	public void testSome() throws ParseException {
		StaticObstacle o1 = new StaticObstacle(
			polygon(10., 10., 20., 10., 20., 20., 10., 20., 10., 10.));
		StaticObstacle o2 = new StaticObstacle(
			polygon(20., 15., 30., 15., 30., 25., 20., 25., 20., 15.));

		Collection<StaticObstacle> staticObstacles = Arrays.asList(o1, o2);

		World world = new World(staticObstacles, emptyList());

		assertThat(world.getMap(),
			topologicallyEqualTo(o1.getShape().union(o2.getShape())));
	}

}
