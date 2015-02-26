package world.factories;

import static common.collect.ImmutablesCollectors.*;
import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import jts.geom.immutable.ImmutablePoint;
import util.TimeConv;
import util.TimeFactory;
import world.SimpleTrajectory;
import world.SpatialPath;

import com.google.common.collect.ImmutableList;

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
			.mapToObj(TimeConv::secondsToDuration)
			.map(TimeFactory.BASE_TIME::plus)
			.collect(toImmutableList());
		
		return new SimpleTrajectory(path, times);
	}

}
