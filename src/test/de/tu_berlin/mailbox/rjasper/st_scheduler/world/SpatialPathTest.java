package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.PathFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;

public class SpatialPathTest extends AbstractPointPathTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Override
	protected ExpectedException thrown() {
		return thrown;
	}

	@Override
	protected SpatialPath makePointPath(ImmutableList<ImmutablePoint> points) {
		return new SpatialPath(points);
	}

	@Test
	public void testInterpolateLocationEmpty() {
		SpatialPath path = SpatialPath.empty();
		
		thrown.expect(NoSuchElementException.class);
		
		path.interpolateLocation(0);
	}
	
	@Test
	public void testInterpolateLocationOnSpot1() {
		SpatialPath path = spatialPath(-2, 1, 2, -1);
		
		ImmutablePoint location = path.interpolateLocation(0);
		ImmutablePoint expected = immutablePoint(-2, 1);
		
		assertThat("interpolated unexpected location",
			location, equalTo(expected));
	}
	
	@Test
	public void testInterpolateLocationOnSpot2() {
		SpatialPath path = spatialPath(-4, 3, 0, 0, 4, -3);
		
		ImmutablePoint location = path.interpolateLocation(5);
		ImmutablePoint expected = immutablePoint(0, 0);
		
		assertThat("interpolated unexpected location",
			location, equalTo(expected));
	}
	
	@Test
	public void testInterpolateLocationInbetween() {
		SpatialPath path = spatialPath(-4, 3, 4, -3);
		
		ImmutablePoint location = path.interpolateLocation(5);
		ImmutablePoint expected = immutablePoint(0, 0);
		
		assertThat("interpolated unexpected location",
			location, equalTo(expected));
	}
	
}
