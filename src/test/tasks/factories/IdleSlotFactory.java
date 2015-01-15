package tasks.factories;

import static jts.geom.immutable.StaticGeometryBuilder.point;

import java.time.LocalDateTime;

import tasks.IdleSlot;
import world.LocalDateTimeFactory;

import com.vividsolutions.jts.geom.Point;

public class IdleSlotFactory {
	
	private static IdleSlotFactory instance = null;
	
	private LocalDateTimeFactory timeFact;
	
	public IdleSlotFactory() {
		this(LocalDateTimeFactory.getInstance());
	}
	
	public IdleSlotFactory(LocalDateTimeFactory timeFact) {
		this.timeFact = timeFact;
	}

	public static IdleSlotFactory getInstance() {
		if (instance == null)
			instance = new IdleSlotFactory();
		
		return instance;
	}
	
	public IdleSlot idleSlot(double x1, double y1, double t1, double x2, double y2, double t2) {
		Point p1 = point(x1, y1);
		Point p2 = point(x2, y2);
		
		LocalDateTime time1 = timeFact.seconds(t1);
		LocalDateTime time2 = timeFact.seconds(t2);
		
		return new IdleSlot(p1, p2, time1, time2);
	}

}
