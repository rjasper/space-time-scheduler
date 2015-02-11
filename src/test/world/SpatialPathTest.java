package world;

import static org.hamcrest.CoreMatchers.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static world.factories.PathFactory.*;
import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import jts.geom.immutable.ImmutablePoint;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class SpatialPathTest extends AbstractPointPathTest {

	@Override
	protected SpatialPath makePointPath(ImmutableList<ImmutablePoint> points) {
		return new SpatialPath(points);
	}

	@Test(expected = NoSuchElementException.class)
	public void testInterpolateLocationEmpty() {
		SpatialPath path = SpatialPath.empty();
		
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
