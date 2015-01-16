package world;

// TODO move to tests

import static jts.geom.immutable.StaticGeometryBuilder.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jts.geom.immutable.ImmutablePoint;

import com.google.common.collect.ImmutableList;

public class TrajectoryFactory {
	
	private static TrajectoryFactory instance = null;
	
//	private EnhancedGeometryBuilder geomBuilder;
	
	private LocalDateTimeFactory timeFact;
	
	public TrajectoryFactory() {
		this(
//			EnhancedGeometryBuilder.getBuilderInstance(),
			LocalDateTimeFactory.getInstance());
	}
	
	public TrajectoryFactory(
//		EnhancedGeometryBuilder geomBuilder,
		LocalDateTimeFactory timeFact)
	{
//		this.geomBuilder = geomBuilder;
		this.timeFact = timeFact;
	}
	
	public static TrajectoryFactory getInstance() {
		if (instance == null)
			instance = new TrajectoryFactory();
		
		return instance;
	}
	
	public void setBaseTime(LocalDateTime baseTime) {
		this.timeFact = new LocalDateTimeFactory(baseTime);
	}

	public SimpleTrajectory trajectory(double[] x, double[] y, double[] t) {
		int n = x.length;
		
		return trajectory(x, y, t, n);
	}

	public SimpleTrajectory trajectory(double[] x, double[] y, double[] t, int n) {
		Objects.requireNonNull(x, "x");
		Objects.requireNonNull(y, "y");
		Objects.requireNonNull(t, "t");
		
		if (n < 0)
			throw new IllegalArgumentException("n less than 0");
		
		ImmutableList.Builder<ImmutablePoint> builder = ImmutableList.builder();
		
		for (int i = 0; i < n; ++i)
			builder.add(immutablePoint(x[i], y[i]));
		
		SpatialPath path = new SpatialPath(builder.build());
		
		List<LocalDateTime> times = Arrays.stream(t, 0, n)
			.mapToObj(timeFact::seconds)
			.collect(Collectors.toList());
		
		return new SimpleTrajectory(path, times);
	}

}
