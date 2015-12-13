package de.tu_berlin.mailbox.rjasper.st_scheduler.world.factories;

import static de.tu_berlin.mailbox.rjasper.jts.geom.immutable.StaticGeometryBuilder.immutablePoint;

import com.google.common.collect.ImmutableList;

import de.tu_berlin.mailbox.rjasper.jts.geom.immutable.ImmutablePoint;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.ArcTimePath;
import de.tu_berlin.mailbox.rjasper.st_scheduler.world.SpatialPath;

public final class PathFactory {
	
	public static SpatialPath spatialPath(double... ordinates) {
		return new SpatialPath(points(ordinates));
	}
	
	public static ArcTimePath arcTimePath(double... ordinates) {
		return new ArcTimePath(points(ordinates));
	}
	
	public static ImmutableList<ImmutablePoint> points(double... ordinates) {
		if (ordinates.length % 2 != 0)
			throw new IllegalArgumentException("invalid number of ordinates");
		
		ImmutableList.Builder<ImmutablePoint> builder = ImmutableList.builder();
		
		for (int i = 0; i < ordinates.length;)
			builder.add(immutablePoint(ordinates[i++], ordinates[i++]));
		
		return builder.build();
	}

}
