package world;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import static matchers.GeometryMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
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
			immutablePolygon(10., 10., 20., 10., 20., 20., 10., 20., 10., 10.));
		StaticObstacle o2 = new StaticObstacle(
			immutablePolygon(20., 15., 30., 15., 30., 25., 20., 25., 20., 15.));

		ImmutableCollection<StaticObstacle> staticObstacles = ImmutableList.of(o1, o2);

		World world = new World(staticObstacles, ImmutableList.of());

		assertThat(world.getMap(),
			topologicallyEqualTo(o1.getShape().union(o2.getShape())));
	}

}
