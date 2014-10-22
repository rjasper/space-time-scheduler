package world;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

	public Trajectory trajectory(double[] x, double[] y, double[] t) {
		// TODO check sizes (same size, minimum size)
		
		int n = x.length;
		
		double[] xy = new double[2*n];
		int k = 0;
		for (int i = 0; i < n; ++i) {
			xy[k++] = x[i];
			xy[k++] = y[i];
		}
		
		LineString path = geomBuilder.lineString(xy);
		
		List<LocalDateTime> times = Arrays.stream(t)
			.mapToObj((sec) -> timeFact.seconds(sec))
			.collect(Collectors.toCollection(ArrayList<LocalDateTime>::new));
		
		return new Trajectory(path, times);
	}

}
