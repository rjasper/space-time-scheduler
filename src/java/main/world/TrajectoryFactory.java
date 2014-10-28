package world;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.vividsolutions.jts.geom.LineString;

import jts.geom.factories.EnhancedGeometryBuilder;

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
		
		LineString path = geomBuilder.lineString(x, y, n);
		
		List<LocalDateTime> times = Arrays.stream(t, 0, n)
			.mapToObj(timeFact::seconds)
			.collect(Collectors.toList());
		
		return new Trajectory(path, times);
	}

}
