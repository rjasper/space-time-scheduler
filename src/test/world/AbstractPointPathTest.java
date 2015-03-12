package world;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import jts.geom.immutable.ImmutablePoint;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import world.factories.PathFactory;

import com.google.common.collect.ImmutableList;

public abstract class AbstractPointPathTest
{
	
	protected abstract PointPath<?, ?> makePointPath(ImmutableList<ImmutablePoint> points);
	
	protected abstract ExpectedException thrown();
	
	private PointPath<?, ?> pointPath(double... ordinates) {
		return makePointPath(PathFactory.points(ordinates));
	}
	
	@Test
	public void testSubPathInvalidInterval() {
		PointPath<?, ?> path = pointPath(0, 0, 8, 6);

		thrown().expect(IllegalArgumentException.class);
		
		path.subPath(1, 0);
	}
	
	@Test
	public void testSubPathEmptyInterval() {
		PointPath<?, ?> path = pointPath(0, 0, 8, 6);

		thrown().expect(IllegalArgumentException.class);
		
		path.subPath(0, 0);
	}

	@Test
	public void testSubPathEmpty() {
		PointPath<?, ?> path = pointPath();

		thrown().expect(NoSuchElementException.class);
		
		path.subPath(0, 1);
	}
	
	@Test
	public void testSubPathIdentical() {
		PointPath<?, ?> path = pointPath(0, 0, 8, 6);
		PointPath<?, ?> subPath = path.subPath(0, 1);
		
		assertThat("expected identical sub-path",
			subPath, equalTo(path));
	}
	
	@Test
	public void testSubPathLeft() {
		PointPath<?, ?> path = pointPath(0, 0, 8, 6);
		PointPath<?, ?> subPath = path.subPath(0, 0.5);
		PointPath<?, ?> expected = pointPath(0, 0, 4, 3);
		
		assertThat("unexpected sub-path",
			subPath, equalTo(expected));
	}
	
	@Test
	public void testSubPathRight() {
		PointPath<?, ?> path = pointPath(0, 0, 8, 6);
		PointPath<?, ?> subPath = path.subPath(0.5, 1);
		PointPath<?, ?> expected = pointPath(4, 3, 8, 6);
		
		assertThat("unexpected sub-path",
			subPath, equalTo(expected));
	}
	
	@Test
	public void testSubPathSmallCore() {
		PointPath<?, ?> path = pointPath(0, 0, 8, 6);
		PointPath<?, ?> subPath = path.subPath(0.25, 0.75);
		PointPath<?, ?> expected = pointPath(2, 1.5, 6, 4.5);
		
		assertThat("unexpected sub-path",
			subPath, equalTo(expected));
	}
	
	@Test
	public void testSubPathBigCore() {
		PointPath<?, ?> path = pointPath(0, 0, 2, 1.5, 4, 3, 6, 4.5, 8, 6);
		PointPath<?, ?> subPath = path.subPath(0.5, 3.5);
		PointPath<?, ?> expected = pointPath(1, 0.75, 2, 1.5, 4, 3, 6, 4.5, 7, 5.25);
		
		assertThat("unexpected sub-path",
			subPath, equalTo(expected));
	}
	
	@Test
	public void testSubPathTightCore() {
		PointPath<?, ?> path = pointPath(0, 0, 2, 1.5, 4, 3, 6, 4.5, 8, 6);
		PointPath<?, ?> subPath = path.subPath(1, 3);
		PointPath<?, ?> expected = pointPath(2, 1.5, 4, 3, 6, 4.5);
		
		assertThat("unexpected sub-path",
			subPath, equalTo(expected));
	}

}
