package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.GeometryMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public class WorldTest {

	@Test
	public void testEmpty() {
		World world = new World();

		assertThat(world.getMap(), isEmpty());
	}

	@Test
	public void testSome() {
		StaticObstacle o1 = new StaticObstacle(
			immutablePolygon(10., 10., 20., 10., 20., 20., 10., 20., 10., 10.));
		StaticObstacle o2 = new StaticObstacle(
			immutablePolygon(20., 15., 30., 15., 30., 25., 20., 25., 20., 15.));

		ImmutableCollection<StaticObstacle> staticObstacles = ImmutableList.of(o1, o2);

		World world = new World(staticObstacles, ImmutableList.of());

		assertThat(world.getMap(),
			topologicallyEqualTo(o1.getShape().union(o2.getShape())));
	}

	@Test
	public void testBufferAdjacentObstacles() {
		StaticObstacle o1 = new StaticObstacle(
			immutableBox(0, 0, 10, 10));
		StaticObstacle o2 = new StaticObstacle(
			immutableBox(10, 0, 20, 10));

		World world = new World(ImmutableList.of(o1, o2), ImmutableList.of());

		World buffered = world.buffer(1.0);

		assertThat(buffered.getMap().isValid(), is(true));
	}

}
