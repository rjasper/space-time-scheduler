package de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories;

import static de.tu_berlin.mailbox.rjasper.collect.ImmutablesCollectors.*;
import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SimpleTrajectory;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;
import de.tu_berlin.mailbox.rjasper.time.TimeConv;
import de.tu_berlin.mailbox.rjasper.time.TimeFactory;

public class TrajectoryFactory {

	public static SimpleTrajectory trajectory(double... ordinates) {
		Objects.requireNonNull(ordinates, "ordinates");
		
		if (ordinates.length % 3 != 0)
			throw new IllegalArgumentException("ordinates not a multiple of 3");
		
		int n = ordinates.length / 3;
		// xOffset = 0
		int yOffset = n;
		int tOffset = 2*n;

		ImmutableList.Builder<ImmutablePoint> builder = ImmutableList.builder();
		
		for (int i = 0; i < n; ++i)
			builder.add(immutablePoint(ordinates[i], ordinates[yOffset + i]));
		
		SpatialPath path = new SpatialPath(builder.build());
		
		ImmutableList<LocalDateTime> times = Arrays.stream(ordinates, tOffset, ordinates.length)
			.mapToObj(TimeConv::secondsToDurationSafe)
			.map(TimeFactory.BASE_TIME::plus)
			.collect(toImmutableList());
		
		return new SimpleTrajectory(path, times);
	}

}
