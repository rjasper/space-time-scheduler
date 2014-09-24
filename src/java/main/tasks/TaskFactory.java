package tasks;

import java.time.LocalDateTime;

import world.LocalDateTimeFactory;

import com.vividsolutions.jts.geom.Point;

import jts.geom.factories.EnhancedGeometryBuilder;

public class TaskFactory {
	
	private static TaskFactory instance = null;

	private EnhancedGeometryBuilder builder;

	private LocalDateTimeFactory timeFactory;
	
	public TaskFactory() {
		this(EnhancedGeometryBuilder.getInstance(), LocalDateTimeFactory.getInstance());
	}

	public TaskFactory(EnhancedGeometryBuilder builder, LocalDateTimeFactory timeFactory) {
		this.builder = builder;
		this.timeFactory = timeFactory;
	}
	
	public static TaskFactory getInstance() {
		if (instance == null)
			instance = new TaskFactory();
		
		return instance;
	}

	private EnhancedGeometryBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(EnhancedGeometryBuilder builder) {
		this.builder = builder;
	}

	private LocalDateTimeFactory getTimeFactory() {
		return timeFactory;
	}

	public void setTimeFactory(LocalDateTimeFactory timeFactory) {
		this.timeFactory = timeFactory;
	}

	public Task createTask(double x, double y, long tStart, long tFinish) {
		EnhancedGeometryBuilder builder = getBuilder();
		LocalDateTimeFactory timeFact = getTimeFactory();
		
		Point location = builder.point(x, y);
		LocalDateTime startTime = timeFact.second(tStart);
		LocalDateTime finishTime = timeFact.second(tFinish);
		
		return new Task(location, startTime, finishTime);
	}
	
	public Task createTaskWithDuration(double x, double y, long tStart, long duration) {
		return createTask(x, y, tStart, tStart + duration);
	}

}
