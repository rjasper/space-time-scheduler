package tasks;

import java.time.Period;
import java.util.Calendar;

import com.vividsolutions.jts.geom.Point;

public class Task {
	
	private Point location;
	
	private Calendar startTime;
	
	private Calendar finishTime;
	
	private transient Period duration;

}
