package world;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jts.geom.factories.EnhancedGeometryBuilder;

import com.vividsolutions.jts.geom.Point;

public class TrajectoryFactory {
	
	private static TrajectoryFactory instance = null;
	
	private EnhancedGeometryBuilder geomBuilder;
	
	private LocalDateTimeFactory timeFact;
	
	public TrajectoryFactory() {
		this(
			EnhancedGeometryBuilder.getInstance(),
			LocalDateTimeFactory.getInstance());
	}
	
	public TrajectoryFactory(
		EnhancedGeometryBuilder geomBuilder,
		LocalDateTimeFactory timeFact)
	{
		this.geomBuilder = geomBuilder;
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

	public Trajectory trajectory(double[] x, double[] y, double[] t) {
		// TODO check sizes
		
		int n = x.length;
		
		return trajectory(x, y, t, n);
	}

	public Trajectory trajectory(double[] x, double[] y, double[] t, int n) {
		// TODO check sizes
		
		List<Point> path = new ArrayList<>(n);
		
		for (int i = 0; i < n; ++i)
			path.add(geomBuilder.point(x[i], y[i]));
		
		List<LocalDateTime> times = Arrays.stream(t, 0, n)
			.mapToObj(timeFact::seconds)
			.collect(Collectors.toList());
		
		return new Trajectory(path, times);
	}

}
