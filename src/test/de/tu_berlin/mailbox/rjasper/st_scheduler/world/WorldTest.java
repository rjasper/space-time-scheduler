package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutableBox;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePolygon;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.GeometryMatchers.isEmpty;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.matchers.GeometryMatchers.topologicallyEqualTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

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
		Geometry bufferedMap = buffered.getMap();

		assertThat("map is invalid",
			buffered.getMap().isValid(), is(true));
		assertThat("map is no multipolygon",
			bufferedMap instanceof MultiPolygon, is(true));
	}

	@Test
	public void testComplexWorld() {
		StaticObstacle o1 = new StaticObstacle(
			immutablePolygon(0, 0, 24, 0, 24, 0.1, 0, 0.1, 0, 0));
		StaticObstacle o2 = new StaticObstacle(
			immutablePolygon(4.0, 7.0, 9.0, 7.0, 9.0, 6.0, 5.0, 6.0, 5.0, 0.0, 4.0, 0.0, 4.0, 7.0));
		StaticObstacle o3 = new StaticObstacle(
			immutablePolygon(4.0, 9.0, 9.0, 9.0, 9.0, 10.0, 5.0, 10.0, 5.0, 15.0, 4.0, 15.0, 4.0, 9.0));
		StaticObstacle o4 = new StaticObstacle(
			immutablePolygon(6.0, 1.0, 15.0, 1.0, 15.0, 2.0, 6.0, 2.0, 6.0, 1.0));
		StaticObstacle o5 = new StaticObstacle(
			immutablePolygon(11.0, 3.0, 14.0, 3.0, 14.0, 13.0, 7.0, 13.0, 7.0, 12.0, 11.0, 12.0, 11.0, 3.0));
		StaticObstacle o6 = new StaticObstacle(
			immutablePolygon(16.0, 7.0, 17.0, 7.0, 17.0, 0.0, 16.0, 0.0, 16.0, 7.0));
		StaticObstacle o7 = new StaticObstacle(
			immutablePolygon(16.0, 9.0, 17.0, 9.0, 17.0, 15.0, 16.0, 15.0, 16.0, 9.0));

		World world = new World(ImmutableList.of(o1, o2, o3, o4, o5, o6, o7), ImmutableList.of());
		World buffered = world.buffer(0.5*Math.sqrt(2.0));
		Geometry bufferedMap = buffered.getMap();

		assertThat("map is invalid",
			buffered.getMap().isValid(), is(true));
		assertThat("map is no multipolygon",
			bufferedMap instanceof MultiPolygon, is(true));
	}

}
