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
import java.util.Collections;

import org.junit.Test;

import world.DynamicObstacle;
import world.SimpleTrajectory;
import world.SpatialPath;

import com.google.common.collect.ImmutableList;

public abstract class FixTimeVelocityPathfinderTest {
	
	protected abstract FixTimeVelocityPathfinder createPathfinder();
	
	@Test
	public void test() {
		double maxSpeed = 1.0;
		Collection<DynamicObstacle> dynamicObstacles = Collections.singleton(
			new DynamicObstacle(
				box(-0.5, -0.5, 0.5, 0.5),
				trajectory(
					2.5, 2.5, 2.5,  2.5,
					5.5, 2.5, 2.5, -0.5,
					0.0, 3.0, 7.0, 10.0)));
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(
			point(1., 4.),
			point(4., 4.),
			point(4., 1.),
			point(1., 1.)));
		LocalDateTime startTime = atSecond(0);
		LocalDateTime finishTime = atSecond(11);
		
		FixTimeVelocityPathfinder pf = createPathfinder();

		pf.setMaxSpeed(maxSpeed);
		pf.setDynamicObstacles(dynamicObstacles);
		pf.setSpatialPath(spatialPath);
		pf.setStartTime(startTime);
		pf.setFinishTime(finishTime);
		
		boolean validPath = pf.calculate();
		
		assertTrue(validPath);
		
		SimpleTrajectory trajectory = pf.getResultTrajectory().getComposedTrajectory();
		
		SimpleTrajectory expected = trajectory(
			1, 2,      4,      4, 3,  1,
			4, 4,      4,      1, 1,  1,
			0, 2, 13./3., 47./6., 9, 11);
		
		assertThat(trajectory, equalTo(expected));
	}
	
	@Test
	public void testInsufficientTime() {
		FixTimeVelocityPathfinder pf = createPathfinder();
		
		pf.setSpatialPath     ( spatialPath(0, 0, 10, 0) );
		pf.setDynamicObstacles( emptyList()              );
		pf.setMaxSpeed        ( 1.0                      );
		pf.setStartTime       ( atSecond(0)              );
		pf.setFinishTime      ( atSecond(5)              );
		
		boolean status = pf.calculate();

		assertThat("path found when it shouldn't",
			status, equalTo(false));
	}

}
