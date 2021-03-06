package de.tu_berlin.mailbox.rjasper.st_scheduler.world.util;

import java.time.LocalDateTime;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.Trajectory.Vertex;
import de.tu_berlin.mailbox.rjasper.util.function.TriFunction;

// TODO document
public class SubTrajectoryOperation<Q extends Comparable<? super Q>>
extends AbstractSubPathOperation<
	Trajectory.Vertex,
	Trajectory.Segment,
	Trajectory,
	Q,
	TrajectoryInterpolator.TrajectoryInterpolation>
{

	public SubTrajectoryOperation(
		Trajectory path,
		Function<? super Vertex, Q> positionMapper,
		TriFunction<Q, Q, Q, Double> relator)
	{
		super(path, positionMapper, relator);
	}

	@Override
	protected Interpolator<Q, TrajectoryInterpolator.TrajectoryInterpolation> getInterpolator(
		Seeker<Q, Vertex> seeker,
		TriFunction<Q, Q, Q, Double> relator)
	{
		return new TrajectoryInterpolator<Q>(seeker, relator);
	}

	@Override
	protected SimpleTrajectory construct(
		TrajectoryInterpolator.TrajectoryInterpolation start,
		Iterable<Vertex> innerVertices,
		TrajectoryInterpolator.TrajectoryInterpolation finish)
	{
		ImmutableList.Builder<ImmutablePoint> locationsBuilder = ImmutableList.builder();
		ImmutableList.Builder<LocalDateTime> timesBuilder = ImmutableList.builder();

		// start vertex

		locationsBuilder.add(start.getLocation());
		timesBuilder.add(start.getTime());
		
		// inner vertices
		
		for (Vertex v : innerVertices) {
			locationsBuilder.add(v.getLocation());
			timesBuilder.add(v.getTime());
		}
		
		// finish vertex

		locationsBuilder.add(finish.getLocation());
		timesBuilder.add(finish.getTime());

		SpatialPath subSpatialPath = new SpatialPath(locationsBuilder.build());
		ImmutableList<LocalDateTime> subTimes = timesBuilder.build();
		
		return new SimpleTrajectory(subSpatialPath, subTimes);
	}

	/* (non-Javadoc)
	 * @see world.util.AbstractSubPathOperation#subPath(java.lang.Object, java.lang.Object)
	 */
	@Override
	public SimpleTrajectory subPath(Q startPosition, Q finishPosition) {
		return (SimpleTrajectory) super.subPath(startPosition, finishPosition);
	}

}
