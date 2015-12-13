package de.tu_berlin.mailbox.rjasper.st_scheduler.world;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;
import static de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories.TrajectoryFactory.trajectory;
import static de.tu_berlin.mailbox.rjasper.time.TimeFactory.atSecond;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.st_scheduler.world.util.TrajectoryComposer;

public class TrajectoryComposerTest {

	@Test
	public void test() {
		SpatialPath spatialPath = new SpatialPath(ImmutableList.of(
			immutablePoint(0., 1.),
			immutablePoint(2., 1.),
			immutablePoint(2., 4.),
			immutablePoint(5., 4.),
			immutablePoint(5., 1.)));
		ArcTimePath arcTimePath = new ArcTimePath(ImmutableList.of(
			immutablePoint(0., 0.),
			immutablePoint(1., 2.),
			immutablePoint(4., 5.),
			immutablePoint(6., 6.),
			immutablePoint(7., 7.),
			immutablePoint(8., 9.),
			immutablePoint(8., 10.),
			immutablePoint(11., 13.)));
		
		DecomposedTrajectory decomposed =
			new DecomposedTrajectory(atSecond(0), spatialPath, arcTimePath);
		
		SimpleTrajectory trajectory = TrajectoryComposer.compose(decomposed);
		
		SimpleTrajectory expected = trajectory(
			0, 1, 2, 2, 2  , 3, 4, 5,  5,  5,
			1, 1, 1, 3, 4  , 4, 4, 4,  4,  1,
			0, 2, 3, 5, 5.5, 6, 7, 9, 10, 13);
		
		assertThat(trajectory, equalTo(expected));
	}

}
