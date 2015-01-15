package tasks.factories;

import static jts.geom.immutable.StaticGeometryBuilder.box;

import java.time.Duration;
import java.time.LocalDateTime;

import tasks.Specification;
import world.LocalDateTimeFactory;

import com.vividsolutions.jts.geom.Polygon;

public class SpecificationFactory {
	
	private static SpecificationFactory instance = null;
	
	private LocalDateTimeFactory timeFact;
	
	public SpecificationFactory() {
		this(LocalDateTimeFactory.getInstance());
	}
	
	public SpecificationFactory(LocalDateTimeFactory timeFact) {
		this.timeFact = timeFact;
	}
	
	public static SpecificationFactory getInstance() {
		if (instance == null)
			instance = new SpecificationFactory();
		
		return instance;
	}

	public Specification specification(double x, double y, double width, double height, long tMin, long tMax, long d) {
		if (!Double.isFinite(width) || width <= 0)
			throw new IllegalArgumentException("width is not a positive finite");
		if (!Double.isFinite(height) || height <= 0)
			throw new IllegalArgumentException("height is not a positive finite");
		
		Polygon area = box(x, y, x + width, y + height);
		LocalDateTime earliestStartTime = timeFact.second(tMin);
		LocalDateTime latestStartTime = timeFact.second(tMax);
		Duration duration = Duration.ofSeconds(d);
		
		return new Specification(area, earliestStartTime, latestStartTime, duration);
	}
	
}
