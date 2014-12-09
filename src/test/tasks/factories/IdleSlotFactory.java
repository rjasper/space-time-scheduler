package tasks.factories;

import java.time.LocalDateTime;

import jts.geom.factories.EnhancedGeometryBuilder;
import tasks.IdleSlot;
import world.LocalDateTimeFactory;

import com.vividsolutions.jts.geom.Point;

public class IdleSlotFactory {
	
	private static IdleSlotFactory instance = null;
	
	private EnhancedGeometryBuilder geomBuilder;
	
	private LocalDateTimeFactory timeFact;
	
	public IdleSlotFactory() {
		this(EnhancedGeometryBuilder.getInstance(), LocalDateTimeFactory.getInstance());
	}
	
	public IdleSlotFactory(EnhancedGeometryBuilder geomBuilder, LocalDateTimeFactory timeFact) {
		this.geomBuilder = geomBuilder;
		this.timeFact = timeFact;
	}

	public static IdleSlotFactory getInstance() {
		if (instance == null)
			instance = new IdleSlotFactory();
		
		return instance;
	}
	
	public IdleSlot idleSlot(double x1, double y1, double t1, double x2, double y2, double t2) {
		Point p1 = geomBuilder.point(x1, y1);
		Point p2 = geomBuilder.point(x2, y2);
		
		LocalDateTime time1 = timeFact.seconds(t1);
		LocalDateTime time2 = timeFact.seconds(t2);
		
		return new IdleSlot(p1, p2, time1, time2);
	}

}
