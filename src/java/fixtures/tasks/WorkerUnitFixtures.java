package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

import world.LocalDateTimeFactory;
import world.Trajectory;
import jts.geom.factories.EnhancedGeometryBuilder;
import jts.geom.factories.StaticJtsFactories;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import static java.time.Month.*;
import static jts.geom.factories.StaticJtsFactories.*;

public final class WorkerUnitFixtures {
	
	private static WorkerUnitFactory wFact = WorkerUnitFactory.getInstance();
	
//	private static TaskPlanner tp = new TaskPlanner();
//	
//	static {
//		tp.setStaticObstacles(Collections.emptyList());
//		tp.setWorkerPool(Collections.emptyList());
//	}

	public static WKTReader wkt() {
		return StaticJtsFactories.wktReader();
	}
	
//	public static Polygon defaultShape() {
//		try {
//			return (Polygon) wkt().read("POLYGON ((-5 5, 5 5, 5 -5, -5 -5, -5 5))");
//		} catch (ParseException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
	
//	public static WorkerUnit basicWorker(Point location) {
//		return new WorkerUnit(defaultShape(), 1., location,
//			LocalDateTime.of(2000, JANUARY, 1, 0, 0)
//		);
//	}
	
//	private static void addTask(WorkerUnit worker, double x, double y, long t, long d) {
//		EnhancedGeometryBuilder geomFact = EnhancedGeometryBuilder.getInstance();
//		LocalDateTimeFactory timeFact = LocalDateTimeFactory.getInstance();
//		
//		Point location = geomFact.point(x, y);
//		LocalDateTime time = timeFact.second(t);
//		Duration duration = Duration.ofSeconds(d);
//
//		tp.setWorker(worker);
//		tp.setLocation(location);
//		tp.setEarliestStartTime(time);
//		tp.setLatestStartTime(time);
//		tp.setDuration(duration);
//		
//		tp.plan();
//		
//		Task task = tp.getTask();
//		Trajectory toTask = tp.getToTask();
//		Trajectory fromTask = tp.getFromTask();
//		
//		worker.addTask(task, toTask, fromTask);
//	}
	
	public static WorkerUnit withTwoTasks1() {
//		WorkerUnit worker = basicWorker(geomFactory().createPoint(new Coordinate(-3600., 0.)));
//		
//		worker.addTask(new Task(
//			geomFactory().createPoint(new Coordinate(-3600., 0.)),
//			LocalDateTime.of(2000, JANUARY, 1, 0, 0),
//			Duration.ofHours(3L)
//		));
//		
//		worker.addTask(new Task(
//			geomFactory().createPoint(new Coordinate(0., 3600.)),
//			LocalDateTime.of(2000, JANUARY, 1, 7, 0),
//			Duration.ofHours(3L)
//		));
		
		WorkerUnit worker = wFact.createWorkerUnit(-3600., 0.);
		
		wFact.addTaskWithDuration(worker, -3600.,    0., 0L * 3600L, 3L * 3600L);
		wFact.addTaskWithDuration(worker,     0., 3600., 7L * 3600L, 3L * 3600L);
		
		return worker;
	}
	
	public static WorkerUnit withTwoTasks2() {
//		WorkerUnit worker = basicWorker(geomFactory().createPoint(new Coordinate(0., -3600.)));
//		
//		worker.addTask(new Task(
//			geomFactory().createPoint(new Coordinate(0., -3600.)),
//			LocalDateTime.of(2000, JANUARY, 1, 0, 0),
//			Duration.ofHours(2L)
//		));
//		
//		worker.addTask(new Task(
//			geomFactory().createPoint(new Coordinate(3600., 0.)),
//			LocalDateTime.of(2000, JANUARY, 1, 8, 0),
//			Duration.ofHours(2L)
//		));
		
		WorkerUnit worker = wFact.createWorkerUnit(0., -3600.);
		
		wFact.addTaskWithDuration(worker,    0., -3600., 0L * 3600L, 2L * 3600L);
		wFact.addTaskWithDuration(worker, 3600.,     0., 7L * 3600L, 2L * 3600L);
		
		return worker;
	}
	
	public static WorkerUnit withThreeTasks() {
//		WorkerUnit worker = basicWorker(geomFactory().createPoint(new Coordinate(0., 0.)));
		
//		worker.addTask(new Task(
//			geomFactory().createPoint(new Coordinate(10., 10.)),
//			LocalDateTime.of(2000, JANUARY, 1, 6, 0),
//			Duration.ofHours(1L)
//		));
//		
//		worker.addTask(new Task(
//			geomFactory().createPoint(new Coordinate(20., 10.)),
//			LocalDateTime.of(2000, JANUARY, 1, 12, 0),
//			Duration.ofHours(3L)
//		));
//		
//		worker.addTask(new Task(
//			geomFactory().createPoint(new Coordinate(20., 20.)),
//			LocalDateTime.of(2000, JANUARY, 1, 18, 0),
//			Duration.ofHours(2L)
//		));
		
		WorkerUnit worker = wFact.createWorkerUnit(0., 0.);
		
		wFact.addTaskWithDuration(worker, 10., 10.,  6L * 3600L, 1L * 3600L);
		wFact.addTaskWithDuration(worker, 20., 10., 12L * 3600L, 3L * 3600L);
		wFact.addTaskWithDuration(worker, 20., 20., 18L * 3600L, 2L * 3600L);
		
		return worker;
	}

}
