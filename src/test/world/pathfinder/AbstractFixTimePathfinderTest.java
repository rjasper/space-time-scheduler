package world.pathfinder;

import static java.util.Collections.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static util.TimeFactory.*;
import static world.factories.PathFactory.*;
import static world.factories.TrajectoryFactory.*;

import java.time.LocalDateTime;
import java.util.Collection;

import org.junit.Test;

import world.DynamicObstacle;
import world.SimpleTrajectory;
import world.SpatialPath;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

public abstract class AbstractFixTimePathfinderTest {
	
	protected abstract AbstractFixTimePathfinder createPathfinder();
	
	private boolean calcPath(
		AbstractFixTimePathfinder pf,
		Collection<DynamicObstacle> dynObst,
		SpatialPath path,
		double maxSpeed,
		LocalDateTime startTime,
		LocalDateTime finishTime)
	{
		pf.setDynamicObstacles(dynObst);
		pf.setMaxSpeed(maxSpeed);
		pf.setSpatialPath(path);
		pf.setMinArc(0.0);
		pf.setMaxArc(path.length());
		pf.setStartArc(0.0);
		pf.setFinishArc(path.length());
		pf.setStartTime(startTime);
		pf.setFinishTime(finishTime);
		
		return pf.calculate();
	}
	
	@Test
	public void test() {
		double maxSpeed = 1.0;
		ImmutableCollection<DynamicObstacle> dynamicObstacles = ImmutableList.of(
			new DynamicObstacle(
				immutableBox(-0.5, -0.5, 0.5, 0.5),
				trajectory(
					2.5, 2.5, 2.5,  2.5,
					5.5, 2.5, 2.5, -0.5,
					0.0, 3.0, 7.0, 10.0)));
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(
			immutablePoint(1., 4.),
			immutablePoint(4., 4.),
			immutablePoint(4., 1.),
			immutablePoint(1., 1.)));
		LocalDateTime startTime = atSecond(0);
		LocalDateTime finishTime = atSecond(11);
		
		AbstractFixTimePathfinder pf = createPathfinder();

		boolean validPath = calcPath(pf,
			dynamicObstacles,
			spatialPath,
			maxSpeed,
			startTime,
			finishTime);
		
		assertTrue(validPath);
		
		SimpleTrajectory trajectory = pf.getResultTrajectory().composed();
		
		SimpleTrajectory expected = trajectory(
			1, 2,      4,      4, 3,  1,
			4, 4,      4,      1, 1,  1,
			0, 2, 13./3., 47./6., 9, 11);
		
		assertThat(trajectory, equalTo(expected));
	}
	
	@Test
	public void testInsufficientTime() {
		AbstractFixTimePathfinder pf = createPathfinder();
		
		SpatialPath spatialPath = spatialPath(0, 0, 10, 0);
		
		boolean status = calcPath(pf,
			emptyList(),
			spatialPath,
			1.0,
			atSecond(0),
			atSecond(5));

		assertThat("path found when it shouldn't",
			status, equalTo(false));
	}

}
