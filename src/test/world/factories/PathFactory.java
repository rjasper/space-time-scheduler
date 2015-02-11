package world.factories;

import static jts.geom.immutable.StaticGeometryBuilder.*;
import jts.geom.immutable.ImmutablePoint;
import world.ArcTimePath;
import world.SpatialPath;

import com.google.common.collect.ImmutableList;

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
