package world;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static world.factories.PathFactory.*;

import java.util.NoSuchElementException;

import jts.geom.immutable.ImmutablePoint;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;

public class ArcTimePathTest extends AbstractPointPathTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Override
	protected ExpectedException thrown() {
		return thrown;
	}

	@Override
	protected PointPath<?, ?> makePointPath(ImmutableList<ImmutablePoint> points) {
		return new ArcTimePath(points);
	}

	@Test
	public void testInterpolateLocationEmpty() {
		ArcTimePath path = ArcTimePath.empty();

		thrown.expect(NoSuchElementException.class);
		
		path.interpolateArc(0);
	}
	
	@Test
	public void testInterpolateLocationOnSpot1() {
		ArcTimePath path = arcTimePath(0, 0, 2, 1);
		
		double arc = path.interpolateArc(1);
		
		assertThat("interpolated unexpected arc",
			arc, equalTo(2.0));
	}
	
	@Test
	public void testInterpolateLocationOnSpot2() {
		ArcTimePath path = arcTimePath(0, 0, 1, 0.5, 2, 1);
		
		double arc = path.interpolateArc(0.5);
		
		assertThat("interpolated unexpected arc",
			arc, equalTo(1.0));
	}
	
	@Test
	public void testInterpolateLocationInbetween() {
		ArcTimePath path = arcTimePath(0, 0, 2, 1);

		double arc = path.interpolateArc(0.5);
		
		assertThat("interpolated unexpected arc",
			arc, equalTo(1.0));
	}
}
